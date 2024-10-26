package dev.revere.commission.discord.event;

import dev.revere.commission.Constants;
import dev.revere.commission.discord.utility.TonicEmbedBuilder;
import dev.revere.commission.entities.Commission;
import dev.revere.commission.entities.Department;
import dev.revere.commission.repository.CommissionRepository;
import dev.revere.commission.repository.DepartmentRepository;
import dev.revere.commission.services.CommissionService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Revere Development
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 */
@Service
@AllArgsConstructor
public class ButtonClickEvent extends ListenerAdapter {
    private final CommissionRepository m_commissionRepository;
    private final DepartmentRepository m_departmentRepository;
    private final CommissionService m_commissionService;

    private final Map<Long, ScheduledFuture<?>> deletionTasks = new HashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void onButtonInteraction(@NotNull final ButtonInteractionEvent p_buttonInteractionEvent) {
        final String button = p_buttonInteractionEvent.getComponentId();
        final Member member = p_buttonInteractionEvent.getMember();

        switch (button) {
            case "create-commission" -> {
                List<Department> departments = m_departmentRepository.findAll();

                StringSelectMenu.Builder selectionMenuBuilder = StringSelectMenu.create("commission-menu")
                        .setPlaceholder("Choose the valid category for your commission");

                for (Department department : departments) {
                    selectionMenuBuilder.addOption(department.getName(), department.getName());
                }

                StringSelectMenu selectionMenu = selectionMenuBuilder.build();

                p_buttonInteractionEvent.replyEmbeds(selectCategoryEmbed().getEmbeds())
                        .addActionRow(selectionMenu)
                        .setEphemeral(true)
                        .queue();
            }

            case "accept-commission" -> {
                assert member != null;

                Commission commission = m_commissionRepository.findCommissionByChannelId(p_buttonInteractionEvent.getChannel().getIdLong());

                Long freelancerId = member.getIdLong();
                String freelancerName = member.getUser().getName();

                HashMap<Long, String> declinedFreelancers = commission.getDeclinedFreelancers();
                if (declinedFreelancers == null) {
                    declinedFreelancers = new HashMap<>();
                }

                if (declinedFreelancers.containsKey(freelancerId)) {
                    p_buttonInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("You have been declined by the client")).setEphemeral(true).queue();
                    return;
                }

                HashMap<Long, String> interestedFreelancers = commission.getInterestedFreelancers();

                if (interestedFreelancers == null) {
                    interestedFreelancers = new HashMap<>();
                }

                if (interestedFreelancers.containsKey(freelancerId)) {
                    p_buttonInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("You have already accepted this commission")).setEphemeral(true).queue();
                    return;
                }

                interestedFreelancers.put(freelancerId, freelancerName);
                commission.setInterestedFreelancers(interestedFreelancers);
                m_commissionRepository.save(commission);

                p_buttonInteractionEvent.getJDA().getGuildById(Constants.MAIN_GUILD_ID)
                        .getTextChannelById(commission.getPublicChannelId())
                        .sendMessage(acceptedCommissionEmbed(member)).queue();

                p_buttonInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("You have accepted this commission, awaiting approval by the client.")).setEphemeral(true).queue();
            }

            case "quote-commission" -> {
                long channelId = p_buttonInteractionEvent.getChannel().getIdLong();
                Commission commission = m_commissionRepository.findCommissionByChannelId(channelId);
                Long freelancerId = member.getIdLong();

                HashMap<Long, String> declinedFreelancers = commission.getDeclinedFreelancers();
                if (declinedFreelancers == null) {
                    declinedFreelancers = new HashMap<>();
                }

                if (declinedFreelancers.containsKey(freelancerId)) {
                    p_buttonInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("You have been declined by the client")).setEphemeral(true).queue();
                    return;
                }

                TextInput quote = TextInput.create("quote", "Quote", TextInputStyle.SHORT)
                        .setRequiredRange(1, 5)
                        .setPlaceholder("Example: $50")
                        .setRequired(true)
                        .build();

                String modalId = "quote-model-" + commission.getId();
                Modal modal = Modal.create(modalId, "Send a quote").addComponents(ActionRow.of(quote)).build();
                p_buttonInteractionEvent.replyModal(modal).queue();
            }

            case "approve-commission" -> {
                List<SelectOption> options = new ArrayList<>();
                Commission commission = m_commissionRepository.findCommissionByPublicChannelId(p_buttonInteractionEvent.getChannel().getIdLong());

                Map<Long, String> interestedFreelancers = commission.getInterestedFreelancers();

                if (interestedFreelancers != null && !interestedFreelancers.isEmpty()) {
                    for (Map.Entry<Long, String> entry : interestedFreelancers.entrySet()) {
                        Long freelancerId = entry.getKey();
                        String freelancerName = entry.getValue();
                        String optionLabel = freelancerName + "'s request";
                        options.add(SelectOption.of(optionLabel, String.valueOf(freelancerId)));
                    }
                }

                if (options.isEmpty()) {
                    p_buttonInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("No one has accepted this commission")).setEphemeral(true).queue();
                    return;
                }

                StringSelectMenu selectionMenu = StringSelectMenu.create("approve-freelancer")
                        .setPlaceholder("Choose a freelancer to approve")
                        .addOptions(options)
                        .build();

                p_buttonInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Select a freelancer to approve"))
                        .addActionRow(selectionMenu)
                        .setEphemeral(true)
                        .queue();
            }

            case "delete-commission" -> {
                p_buttonInteractionEvent.getChannel().sendMessage(deleteCommissionEmbed()).complete();

                long channelId = p_buttonInteractionEvent.getChannel().getIdLong();
                Commission commission = m_commissionRepository.findCommissionByChannelId(channelId);
                long publicChannelId = commission.getPublicChannelId();

                m_commissionService.scheduleDeletion(commission, member, channelId, publicChannelId);
            }

            case "cancel-commission" -> {
                p_buttonInteractionEvent.getChannel().sendMessage(cancelledCommissionDeletion()).queue();
                p_buttonInteractionEvent.getChannel().deleteMessageById(p_buttonInteractionEvent.getMessageId()).queue();

                long channelId = p_buttonInteractionEvent.getChannel().getIdLong();
                m_commissionService.cancelDeletion(channelId, channelId, String.valueOf(m_commissionRepository.findCommissionByChannelId(channelId).getId()));
            }

            case "disapprove-commission" -> {
                List<SelectOption> options = new ArrayList<>();
                Commission commission = m_commissionRepository.findCommissionByPublicChannelId(p_buttonInteractionEvent.getChannel().getIdLong());

                Map<Long, String> interestedFreelancers = commission.getInterestedFreelancers();

                if (interestedFreelancers != null && !interestedFreelancers.isEmpty()) {
                    for (Map.Entry<Long, String> entry : interestedFreelancers.entrySet()) {
                        Long freelancerId = entry.getKey();
                        String freelancerName = entry.getValue();
                        String optionLabel = freelancerName + "'s request";
                        options.add(SelectOption.of(optionLabel, String.valueOf(freelancerId)));
                    }
                }

                if (options.isEmpty()) {
                    p_buttonInteractionEvent.reply("No one has accepted this commission").setEphemeral(true).queue();
                    return;
                }

                StringSelectMenu selectionMenu = StringSelectMenu.create("disapprove-freelancer")
                        .setPlaceholder("Choose a freelancer to disapprove")
                        .addOptions(options)
                        .build();

                p_buttonInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Select a freelancer to disapprove"))
                        .addActionRow(selectionMenu)
                        .setEphemeral(true)
                        .queue();
            }

            case "deny-commission" -> {
                p_buttonInteractionEvent.getChannel().sendMessage(declinedCommissionEmbed(p_buttonInteractionEvent.getUser().getName())).queue();
                p_buttonInteractionEvent.getGuild().getTextChannelById(p_buttonInteractionEvent.getChannel().getId()).upsertPermissionOverride(p_buttonInteractionEvent.getMember()).setDenied(Permission.VIEW_CHANNEL).queue();
            }

            case "request-payment" -> {
                Commission commission = m_commissionRepository.findCommissionByPublicChannelId(p_buttonInteractionEvent.getChannel().getIdLong());
                // todo: paypal invoice integration
                p_buttonInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Pay the invoice: " + "generated invoice here")).queue();
            }

            case "cancel-ongoing-commission" -> {
                Commission commission = m_commissionRepository.findCommissionByPublicChannelId(p_buttonInteractionEvent.getChannel().getIdLong());
                m_commissionService.cancelCommission(commission);
                p_buttonInteractionEvent.reply("Cancelling commission, channel will be deleted after 10 seconds.").queue();
                p_buttonInteractionEvent.getChannel().delete().queueAfter(10, TimeUnit.SECONDS);
            }

            case "payment-finished" -> {
                final long channelId = Long.parseLong(p_buttonInteractionEvent.getMessage().getChannel().getId());
                final Commission commission = m_commissionRepository.findCommissionByPublicChannelId(channelId);

                String interactionUser = p_buttonInteractionEvent.getUser().getId();
                if (!interactionUser.equals(String.valueOf(commission.getFreelancerId()))) {
                    p_buttonInteractionEvent.reply(restrictedAccessEmbed()).setEphemeral(true).queue();
                    return;
                }

                commission.setPaymentPending(false);
                m_commissionRepository.save(commission);

                p_buttonInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Payment has been marked at received.")).queue();
            }

            case "finish-commission" -> {
                Commission commission = m_commissionRepository.findCommissionByPublicChannelId(p_buttonInteractionEvent.getChannel().getIdLong());
                String interactionUser = p_buttonInteractionEvent.getUser().getId();
                if (!interactionUser.equals(String.valueOf(commission.getFreelancerId()))) {
                    p_buttonInteractionEvent.reply(restrictedAccessEmbed()).setEphemeral(true).queue();
                    return;
                }

                if (commission.isPaymentPending()) {
                    p_buttonInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Waiting for payment before finishing commission")).queue();
                } else {
                    m_commissionService.finishCommission(commission, p_buttonInteractionEvent.getMember());
                    p_buttonInteractionEvent.reply(transcriptCommission("The commission has been marked as finished.", commission.getId())).queue();
                }
            }

            default -> {
                if (button.startsWith("accept-quote-")) {
                    String[] parts = button.split("-");
                    if (parts.length < 3) {
                        p_buttonInteractionEvent.reply("Invalid button interaction.").setEphemeral(true).queue();
                        return;
                    }

                    String freelancerId = parts[2];
                    String quote = parts[3];

                    Member freelancerMember = p_buttonInteractionEvent.getGuild().getMemberById(Long.parseLong(freelancerId));
                    Commission commission = m_commissionRepository.findCommissionByPublicChannelId(p_buttonInteractionEvent.getChannel().getIdLong());

                    if (commission.getState() == Commission.State.IN_PROGRESS) {
                        p_buttonInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Commission is already in progress.")).setEphemeral(true).queue();
                        return;
                    }

                    m_commissionService.setQuote(commission, quote);
                    m_commissionService.approveFreelancer(commission, freelancerMember);

                    String targetCategoryId = "1299299754089385994";
                    Category targetCategory = p_buttonInteractionEvent.getJDA().getGuildById(Constants.COMMISSION_GUILD_ID).getCategoryById(targetCategoryId);

                    p_buttonInteractionEvent.getJDA().getGuildById(Constants.COMMISSION_GUILD_ID).getTextChannelById(commission.getChannelId()).getManager().setParent(targetCategory).queue();
                    p_buttonInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("You have accepted " + freelancerMember.getUser().getName() + "'s quote!")).setEphemeral(true)
                            .queue(reply -> {
                                TextChannel channel = Objects.requireNonNull(p_buttonInteractionEvent.getJDA().getGuildById(Constants.MAIN_GUILD_ID))
                                        .getTextChannelById(commission.getPublicChannelId());

                                assert channel != null;
                                channel.upsertPermissionOverride(freelancerMember)
                                        .setAllowed(Permission.VIEW_CHANNEL)
                                        .queue();

                                String message = String.format("Congratulations %s! You have been approved for this commission.", freelancerMember.getUser().getAsMention());
                                channel.sendMessage(message).queue();
                                channel.sendMessage(commissionEmbed(freelancerMember.getUser().getName(), commission.getDescription(), commission.getFormattedQuote())).queue();
                            });
                } else if (button.startsWith("decline-quote-")) {
                    String[] parts = button.split("-");
                    if (parts.length < 3) {
                        p_buttonInteractionEvent.reply("Invalid button interaction.").setEphemeral(true).queue();
                        return;
                    }

                    String freelancerId = parts[2];
                    long messageId = p_buttonInteractionEvent.getMessageIdLong();

                    Member freelancerMember = p_buttonInteractionEvent.getGuild().getMemberById(Long.parseLong(freelancerId));
                    User freelancerUser = Objects.requireNonNull(freelancerMember).getUser();

                    freelancerUser.openPrivateChannel().queue(privateChannel -> {
                        privateChannel.sendMessage(TonicEmbedBuilder.sharedMessageEmbed(p_buttonInteractionEvent.getUser().getName() + " has declined your new quote.")).queue();
                    });

                    p_buttonInteractionEvent.getChannel().retrieveMessageById(messageId).queue(message -> {
                        message.delete().queue();
                    }, failure -> {
                        p_buttonInteractionEvent.reply("Failed to retrieve the message for deletion: " + failure.getMessage()).setEphemeral(true).queue();
                    });

                    p_buttonInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("You have declined " + freelancerMember.getUser().getName() + "'s quote.")).setEphemeral(true).queue();
                } else if (button.startsWith("transcript-commission-")) {
                    String commissionId = button.substring("transcript-commission-".length());
                    Commission commission = m_commissionRepository.findCommissionById(commissionId);

                    if (commission == null) {
                        p_buttonInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Failed to get commission.")).setEphemeral(true).queue();
                        return;
                    }

                    Member client = Objects.requireNonNull(p_buttonInteractionEvent.getGuild()).getMemberById(commission.getUserId());
                    if (client == null) {
                        p_buttonInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Failed to get client.")).setEphemeral(true).queue();
                        return;
                    }

                    p_buttonInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Transcripting commission...")).queue();

                    scheduler.schedule(() -> {
                        m_commissionService.transcriptCommission(commission, client);
                    }, 3, TimeUnit.SECONDS);
                }
            }
        }
    }

    public MessageCreateData commissionEmbed(String p_member, String p_description, String p_quote) {
        String description = String.format(
                """
                        This commission will now be handled by **%s**. Here are the details:
                        ### <:RVC_Log:1299484630101262387> Commission Details
                        %s
                        ### <:RVC_Discount:1299484670098145341> Quoted Price
                        ```
                        %s
                        ```""",
                p_member,
                p_description,
                p_quote
        );

        return new TonicEmbedBuilder()
                .setTitle(" ")
                .setDescription(description)
                .addButton(ButtonStyle.SUCCESS, "finish-commission", "Mark As Finished", Emoji.fromUnicode("U+1F3AB"))
                .addButton(ButtonStyle.PRIMARY, "payment-finished", "Mark Payment As Received", Emoji.fromUnicode("U+1F3AB"))
                .addButton(ButtonStyle.SECONDARY, "request-payment", "Request Payment", Emoji.fromUnicode("U+1F3AB"))
                .addButton(ButtonStyle.DANGER, "cancel-ongoing-commission", "Cancel Commission", Emoji.fromUnicode("U+1F3AB"))
                .setTimeStamp(Instant.now())
                .setColor(Color.decode("#2b2d31"))
                .build();
    }

    public MessageCreateData selectCategoryEmbed() {
        String description = String.format(
                """
                        **Welcome to %s's Commission Channel!**
                        Please select a category for your commission from the dropdown menu below. Our freelancers are ready to assist you with your request.  
                        ### <:RVC_Cart:1299484525348388995> How to Create a Commission:
                        - Choose the appropriate category for your request.  
                        - Click the button below to proceed with your commission.  
                        """,
                Constants.PROJECT_NAME
        );

        return new TonicEmbedBuilder()
                .setTitle("Select a Commission Category")
                .setDescription(description)
                .setTimeStamp(Instant.now())
                .setColor(Color.decode("#2b2d31"))
                .addButton(ButtonStyle.PRIMARY, "create-commission", "Select Category", Emoji.fromUnicode("U+1F3AB"))
                .build();
    }

    public static MessageCreateData transcriptCommission(String p_description, String commissionId) {
        return new TonicEmbedBuilder()
                .setTitle(" ")
                .setDescription(p_description)
                .setTimeStamp(Instant.now())
                .setColor(Color.decode("#2b2d31"))
                .addButton(ButtonStyle.PRIMARY, "transcript-commission-" + commissionId, "Transcript Commission", Emoji.fromUnicode("U+1F3AB"))
                .build();
    }

    public MessageCreateData restrictedAccessEmbed() {
        return new TonicEmbedBuilder()
                .setTitle(" ")
                .setDescription("Access is restricted to the freelancer")
                .setTimeStamp(Instant.now())
                .setColor(Color.decode("#2b2d31"))
                .build();
    }

    public MessageCreateData declinedCommissionEmbed(String name) {
        return new TonicEmbedBuilder()
                .setTitle(" ")
                .setDescription(name + " has declined this commission")
                .setTimeStamp(Instant.now())
                .setColor(Color.decode("#2b2d31"))
                .build();
    }

    public MessageCreateData deleteCommissionEmbed() {
        return new TonicEmbedBuilder()
                .setTitle(" ")
                .setDescription("Deleting commission in 10 seconds...")
                .setTimeStamp(Instant.now())
                .setColor(Color.decode("#2b2d31"))
                .addButton(ButtonStyle.DANGER, "cancel-commission", "Cancel Deletion", Emoji.fromUnicode("U+1F3AB"))
                .build();
    }

    public MessageCreateData disapproveEmbed(String freelancerName) {
        return new TonicEmbedBuilder()
                .setTitle(" ")
                .setDescription("You have disapproved " + freelancerName + " for this commission")
                .setTimeStamp(Instant.now())
                .setColor(Color.decode("#2b2d31"))
                .build();
    }

    public MessageCreateData acceptedCommissionEmbed(final Member p_member) {
        return new TonicEmbedBuilder()
                .setTitle(" ")
                .setDescription(p_member.getUser().getName() + " has accepted this commission. Would you like to approve them?")
                .setTimeStamp(Instant.now())
                .setColor(Color.decode("#2b2d31"))
                .addButton(ButtonStyle.SUCCESS, "approve-commission", "Approve Commission", Emoji.fromUnicode("U+1F3AB"))
                .addButton(ButtonStyle.DANGER, "disapprove-commission", "Disapprove Commission", Emoji.fromUnicode("U+1F3AB"))
                .build();
    }

    public MessageCreateData cancelledCommissionDeletion() {
        // Create and configure the cancelled commission embed
        return new TonicEmbedBuilder()
                .setTitle(" ")
                .setDescription("Cancelled the deletion of commission")
                .setTimeStamp(Instant.now())
                .setColor(Color.decode("#2b2d31"))
                .build();
    }
}