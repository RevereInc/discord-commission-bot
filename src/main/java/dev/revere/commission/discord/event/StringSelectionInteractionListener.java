package dev.revere.commission.discord.event;

import dev.revere.commission.Constants;
import dev.revere.commission.discord.utility.CommissionData;
import dev.revere.commission.discord.utility.TonicEmbedBuilder;
import dev.revere.commission.entities.Commission;
import dev.revere.commission.repository.CommissionRepository;
import dev.revere.commission.repository.DepartmentRepository;
import dev.revere.commission.repository.FreelancerRepository;
import dev.revere.commission.services.CommissionService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.Instant;
import java.util.Objects;

/**
 * @author Revere Development
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 */
@AllArgsConstructor
@Service
public class StringSelectionInteractionListener extends ListenerAdapter {
    private final CommissionRepository m_commissionRepository;
    private final DepartmentRepository m_departmentRepository;
    private final FreelancerRepository m_freelancerRepository;
    private final CommissionService m_commissionService;

    @Override
    public void onStringSelectInteraction(@NotNull final StringSelectInteractionEvent p_stringSelectInteractionEvent) {
        final String stringInteraction = p_stringSelectInteractionEvent.getComponentId();
        final Member member = p_stringSelectInteractionEvent.getMember();

        switch (stringInteraction) {
            case "commission-menu" -> {
                String selectedCategory = p_stringSelectInteractionEvent.getValues().get(0);

                TextInput description = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
                        .setRequiredRange(1, 500)
                        .setPlaceholder("Example: I need a logo for my business with a modern design.. PS: the more detail the better!")
                        .setRequired(true)
                        .build();

                TextInput quote = TextInput.create("quote", "Quote", TextInputStyle.SHORT)
                        .setRequiredRange(1, 5)
                        .setPlaceholder("Example: $50")
                        .setRequired(true)
                        .build();

                Modal modal = Modal.create("commission-modal", selectedCategory)
                        .addComponents(ActionRow.of(description), ActionRow.of(quote)).build();

                CommissionData.setSelectedCategory(member, m_departmentRepository.findDepartmentByName(selectedCategory));
                p_stringSelectInteractionEvent.replyModal(modal).queue();
            }

            case "approve-freelancer" -> {
                String selectedFreelancerId = p_stringSelectInteractionEvent.getValues().get(0);
                Member freelancerMember = p_stringSelectInteractionEvent.getGuild().getMemberById(selectedFreelancerId);

                final long interactionChannelId = p_stringSelectInteractionEvent.getChannel().getIdLong();
                final Commission commission = m_commissionRepository.findCommissionByPublicChannelId(interactionChannelId);

                if (freelancerMember == null) {
                    commission.getInterestedFreelancers().remove(Long.parseLong(selectedFreelancerId));
                    p_stringSelectInteractionEvent.getChannel().sendMessage("The selected freelancer does not exist").queue();
                    return;
                }

                m_commissionService.approveFreelancer(commission, freelancerMember);

                String targetCategoryId = "1299299754089385994";
                Category targetCategory = p_stringSelectInteractionEvent.getJDA().getGuildById(Constants.COMMISSION_GUILD_ID).getCategoryById(targetCategoryId);

                p_stringSelectInteractionEvent.getJDA().getGuildById(Constants.COMMISSION_GUILD_ID).getTextChannelById(commission.getChannelId()).getManager().setParent(targetCategory).queue();
                p_stringSelectInteractionEvent.reply(approveEmbed(freelancerMember.getUser().getName())).setEphemeral(true)
                        .queue(reply -> {
                            TextChannel channel = Objects.requireNonNull(p_stringSelectInteractionEvent.getJDA().getGuildById(Constants.MAIN_GUILD_ID))
                                    .getTextChannelById(commission.getPublicChannelId());

                            assert channel != null;
                            channel.upsertPermissionOverride(freelancerMember)
                                    .setAllowed(Permission.VIEW_CHANNEL)
                                    .queue();

                            String message = String.format("Congratulations %s! You have been approved for this commission.", freelancerMember.getUser().getAsMention());
                            channel.sendMessage(message).queue();
                            channel.sendMessage(commissionEmbed(freelancerMember.getUser().getName(), commission.getDescription(), commission.getFormattedQuote())).queue();

                        });
            }

            case "disapprove-freelancer" -> {
                String selectedFreelancerId = p_stringSelectInteractionEvent.getValues().get(0);
                User freelancerUser = p_stringSelectInteractionEvent.getJDA().getUserById(selectedFreelancerId);
                Member freelancerMember = p_stringSelectInteractionEvent.getGuild().getMember(freelancerUser);
                if (freelancerMember == null) {
                    p_stringSelectInteractionEvent.getChannel().sendMessage(TonicEmbedBuilder.sharedMessageEmbed("The selected freelancer does not exist")).queue();
                    return;
                }

                final long interactionChannelId = p_stringSelectInteractionEvent.getChannel().getIdLong();
                final Commission commission = m_commissionRepository.findCommissionByPublicChannelId(interactionChannelId);

                m_commissionService.declineFreelancer(commission, freelancerMember);

                p_stringSelectInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("You have declined " + freelancerUser.getName() + " for the commission")).queue();

                Guild targetGuild = p_stringSelectInteractionEvent.getJDA().getGuildById(Constants.COMMISSION_GUILD_ID);
                if (targetGuild == null) {
                    p_stringSelectInteractionEvent.getChannel().sendMessage(TonicEmbedBuilder.sharedMessageEmbed("Target guild not found.")).queue();
                    return;
                }

                TextChannel channel = targetGuild.getTextChannelById(commission.getChannelId());
                if (channel == null) {
                    p_stringSelectInteractionEvent.getChannel().sendMessage(TonicEmbedBuilder.sharedMessageEmbed("Channel not found in the target guild.")).queue();
                    return;
                }

                Member freelancerCommissionMember = targetGuild.getMember(freelancerUser);
                if (freelancerCommissionMember != null) {
                    channel.upsertPermissionOverride(freelancerCommissionMember)
                            .setDenied(Permission.VIEW_CHANNEL)
                            .queue();
                } else {
                    p_stringSelectInteractionEvent.getChannel().sendMessage("The selected freelancer is not a member of the target guild.").queue();
                }

                freelancerUser.openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessage(TonicEmbedBuilder.sharedMessageEmbed(p_stringSelectInteractionEvent.getUser().getName() + " has declined your commission request.")).queue();
                });
            }
        }
    }

    public MessageCreateData commissionEmbed(String p_member, String p_description, String p_quote) {
        String description = String.format(
                """
                        This commission will now be handled by **%s**. Here are the details:
                        ### <:1270455353620041829:1299806081140133898> Commission Details
                        %s
                        ### <:1270673327098167347:1299806215915700315> Quoted Price
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

    public MessageCreateData approveEmbed(String p_member) {
        return new TonicEmbedBuilder()
                .setTitle(" ")
                .setDescription("You have approved " + p_member + " for the commission")
                .setTimeStamp(Instant.now())
                .setColor(Color.decode("#2b2d31"))
                .build();
    }
}