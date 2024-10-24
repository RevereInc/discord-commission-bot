package dev.revere.commission.discord.event;

import lombok.AllArgsConstructor;

import dev.revere.commission.discord.utility.FluxEmbedBuilder;
import dev.revere.commission.entities.Commission;
import dev.revere.commission.repository.CommissionRepository;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.entities.Member;
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

import java.time.Instant;
import java.util.*;
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

    // Maps for tracking deletion tasks and accepted commissions
    private final Map<Long, ScheduledFuture<?>> deletionTasks = new HashMap<>();
    private final Map<Long, List<Long>> acceptedCommissions = new HashMap<>();

    @Override
    public void onButtonInteraction(@NotNull final ButtonInteractionEvent p_buttonInteractionEvent) {
        final String button = p_buttonInteractionEvent.getComponentId();
        final Member member = p_buttonInteractionEvent.getMember();

        switch (button) {
            case "create-commission" -> {
                // Handle the "create-commission" button click
                StringSelectMenu selectionMenu = StringSelectMenu.create("commission-menu").setPlaceholder("Chose the valid category for your commission")
                        .addOption("Plugin Developer", "1141050234353496088")
                        .addOption("Client Developer", "1141050256675577916")
                        .addOption("Game Developer", "1141050275948404919").build();

                p_buttonInteractionEvent.replyEmbeds(selectCategoryEmbed().getEmbeds()).addActionRow(selectionMenu).setEphemeral(true).queue();
            }
            case "delete-commission" -> {
                // Handle the "delete-commission" button click
                Message message = p_buttonInteractionEvent.getChannel().sendMessage(deleteCommissionEmbed()).complete();

                ScheduledFuture<?> deletionTask = Objects.requireNonNull(Objects.requireNonNull(p_buttonInteractionEvent.getGuild()).getTextChannelById(message.getChannel().getId())).delete().queueAfter(10, TimeUnit.SECONDS);
                deletionTasks.put(message.getIdLong(), deletionTask);

            }
            case "cancel-commission-deletion" -> {
                // Handle the "cancel-commission-deletion" button click
                p_buttonInteractionEvent.getChannel().sendMessage(cancelledCommissionDeletion()).queue();
                p_buttonInteractionEvent.getChannel().deleteMessageById(p_buttonInteractionEvent.getMessageId()).queue();

                ScheduledFuture<?> deletionTask = deletionTasks.remove(p_buttonInteractionEvent.getMessageIdLong());
                if (deletionTask != null) {
                    deletionTask.cancel(false);
                }
            }

            // ALL FREELANCERS
            case "accept-commission" -> {
                // Handle the "accept-commission" button click
                assert member != null;

                long commissionMessageId = p_buttonInteractionEvent.getMessageIdLong();
                long freelancerId = member.getIdLong();

                acceptedCommissions.computeIfAbsent(freelancerId, k -> new ArrayList<>()).add(commissionMessageId);

                p_buttonInteractionEvent.reply(acceptedCommissionEmbed(member)).queue();
            }

            case "quote-commission" -> {
                TextInput quote = TextInput.create("quote", "Quote", TextInputStyle.SHORT)
                        .setRequiredRange(1, 5)
                        .setPlaceholder("Example: 50")
                        .setRequired(true)
                        .build();


                Modal modal = Modal.create("quote-model", "").addComponents(ActionRow.of(quote)).build();
                p_buttonInteractionEvent.replyModal(modal).queue();
            }

            // ADMINISTRATOR ACTIONS
            case "approve-commission" -> {
                // Handle the "approve-commission" button click
                List<SelectOption> options = new ArrayList<>();

                for (Map.Entry<Long, List<Long>> entry : acceptedCommissions.entrySet()) {
                    long freelancerId = entry.getKey();
                    Member freelancer = p_buttonInteractionEvent.getGuild().getMemberById(freelancerId);

                    if (freelancer != null) {
                        String optionLabel = freelancer.getUser().getName() + "'s Commissions";
                        options.add(SelectOption.of(optionLabel, Long.toString(freelancerId)));
                    }
                }

                if (options.isEmpty()) {
                    p_buttonInteractionEvent.reply("No one has accepted this commission").queue();
                    return;
                }

                StringSelectMenu selectionMenu = StringSelectMenu.create("approve-freelancer").setPlaceholder("Choose a freelancer to approve").addOptions(options).build();

                p_buttonInteractionEvent.reply(choseToApprove()).addActionRow(selectionMenu).setEphemeral(true).queue();
            }

            case "disapprove-commission" -> {
                // Handle the "disapprove-commission" button click
                p_buttonInteractionEvent.reply(disapproveEmbed()).queue();
            }

            case "deny-commission" -> {
                p_buttonInteractionEvent.getChannel().sendMessage(declinedCommissionEmbed(p_buttonInteractionEvent.getUser().getName())).queue();
                p_buttonInteractionEvent.getGuild().getTextChannelById(p_buttonInteractionEvent.getChannel().getId()).upsertPermissionOverride(p_buttonInteractionEvent.getMember()).setDenied(Permission.VIEW_CHANNEL).queue();
            }

            case "request-payment" -> {
                final long channelId = Long.parseLong(p_buttonInteractionEvent.getMessage().getChannel().getId());
                final Commission commission = m_commissionRepository.findCommissionByPublicChannelId(channelId);

                String interactionUser = p_buttonInteractionEvent.getUser().getId();

                if(!interactionUser.equals(String.valueOf(commission.getFreelancerId()))) {
                    p_buttonInteractionEvent.reply(restrictedAccessEmbed()).queue();
                    return;
                }

                p_buttonInteractionEvent.reply(customMessageEmbed("Please send **$" + commission.getQuote() + "** to https://paypal.me/devuxious")).queue();
            }

            case "cancel-ongoing-commission" -> {
                final long channelId = Long.parseLong(p_buttonInteractionEvent.getMessage().getChannel().getId());
                final Commission commission = m_commissionRepository.findCommissionByPublicChannelId(channelId);

                String interactionUser = p_buttonInteractionEvent.getUser().getId();

                if(!interactionUser.equals(String.valueOf(commission.getFreelancerId()))) {
                    p_buttonInteractionEvent.reply(restrictedAccessEmbed()).queue();
                    return;
                }

                commission.setState(Commission.State.COMPLETED);
                m_commissionRepository.save(commission);

                p_buttonInteractionEvent.reply(customMessageEmbed("Cancelling commission, channel will be deleted after 10 seconds.")).queue();
                p_buttonInteractionEvent.getChannel().delete().queueAfter(10, TimeUnit.SECONDS);
            }

            case "payment-finished" -> {
                final long channelId = Long.parseLong(p_buttonInteractionEvent.getMessage().getChannel().getId());
                final Commission commission = m_commissionRepository.findCommissionByPublicChannelId(channelId);

                String interactionUser = p_buttonInteractionEvent.getUser().getId();

                if(!interactionUser.equals(String.valueOf(commission.getFreelancerId()))) {
                    p_buttonInteractionEvent.reply(restrictedAccessEmbed()).queue();
                    return;
                }

                commission.setPaymentPending(false);
                m_commissionRepository.save(commission);

                p_buttonInteractionEvent.reply(customMessageEmbed("Payment has been marked at received.")).queue();
            }

            case "finish-commission" -> {
                final long channelId = Long.parseLong(p_buttonInteractionEvent.getMessage().getChannel().getId());
                final Commission commission = m_commissionRepository.findCommissionByPublicChannelId(channelId);

                String interactionUser = p_buttonInteractionEvent.getUser().getId();

                if(!interactionUser.equals(String.valueOf(commission.getFreelancerId()))) {
                    p_buttonInteractionEvent.reply(restrictedAccessEmbed()).queue();
                    return;
                }

                if(commission.isPaymentPending()) {
                    p_buttonInteractionEvent.reply(customMessageEmbed("Waiting for payment before finishing commission")).queue();
                } else {
                    commission.setState(Commission.State.COMPLETED);
                    m_commissionRepository.save(commission);
                    p_buttonInteractionEvent.reply(customMessageEmbed("Commission has been marked as finished")).queue();
                }
            }
        }
    }

    // Methods to create embed messages
    public MessageCreateData selectCategoryEmbed() {
        // Create and configure the select category embed
        return new FluxEmbedBuilder()
                .setTitle("Commission | Flux Solutions")
                .setDescription("Select the category for your commission")
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .addButton(ButtonStyle.PRIMARY, "create-commission", "Create A Commission", Emoji.fromUnicode("U+1F3AB"))
                .build();
    }

    public MessageCreateData restrictedAccessEmbed() {
        return new FluxEmbedBuilder()
                .setTitle("Commission | Flux Solutions")
                .setDescription("Access is restricted to the freelancer")
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .build();
    }

    public MessageCreateData declinedCommissionEmbed(String name) {
        return new FluxEmbedBuilder()
                .setTitle("Commission | Flux Solutions")
                .setDescription(name + " has declined this commission")
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .build();
    }

    public MessageCreateData deleteCommissionEmbed() {
        // Create and configure the delete commission embed
        return new FluxEmbedBuilder()
                .setTitle("Commission | Flux Solutions")
                .setDescription("Deleting commission in 10 seconds...")
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .addButton(ButtonStyle.DANGER, "cancel-commission-deletion", "Cancel Deletion", Emoji.fromUnicode("U+1F3AB"))
                .build();
    }

    public MessageCreateData customMessageEmbed(String message) {
        // Create and configure the delete commission embed
        return new FluxEmbedBuilder()
                .setTitle("Commission | Flux Solutions")
                .setDescription(message)
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .build();
    }

    public MessageCreateData disapproveEmbed() {
        // Create and configure the disapprove embed
        return new FluxEmbedBuilder()
                .setTitle("Commission | Flux Solutions")
                .setDescription("An administrator has disapproved this commission.")
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .build();
    }

    public MessageCreateData choseToApprove() {
        // Create and configure the approve commission embed
        return new FluxEmbedBuilder()
                .setTitle("Commission | Flux Solutions")
                .setDescription("Select the freelancer to approve")
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .build();
    }

    public MessageCreateData acceptedCommissionEmbed(final Member p_member) {
        // Create and configure the accepted commission embed
        return new FluxEmbedBuilder()
                .setTitle("Commission | Flux Solutions")
                .setDescription(p_member.getUser().getName() + " has accepted this commission. Waiting for an administrator to approve.")
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .addButton(ButtonStyle.SUCCESS, "approve-commission", "Approve Commission", Emoji.fromUnicode("U+1F3AB"))
                .addButton(ButtonStyle.DANGER, "disapprove-commission", "Disapprove Commission", Emoji.fromUnicode("U+1F3AB"))
                .build();
    }

    public MessageCreateData cancelledCommissionDeletion() {
        // Create and configure the cancelled commission embed
        return new FluxEmbedBuilder()
                .setTitle("Commission | Flux Solutions")
                .setDescription("Cancelled the deletion of commission")
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .build();
    }
}