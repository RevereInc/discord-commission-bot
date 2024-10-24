package dev.revere.commission.discord.event;

import lombok.AllArgsConstructor;

import dev.revere.commission.discord.utility.CommissionData;
import dev.revere.commission.discord.utility.FluxEmbedBuilder;
import dev.revere.commission.entities.Commission;
import dev.revere.commission.repository.CommissionRepository;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
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

    @Override
    public void onStringSelectInteraction(@NotNull final StringSelectInteractionEvent p_stringSelectInteractionEvent) {
        final String stringInteraction = p_stringSelectInteractionEvent.getComponentId();
        final Member member = p_stringSelectInteractionEvent.getMember();

        switch (stringInteraction) {
            case "commission-menu" -> {
                String selectedCategory = p_stringSelectInteractionEvent.getValues().get(0);
                TextInput description = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
                        .setRequiredRange(1, 500)
                        .setRequired(true)
                        .build();

                TextInput quote = TextInput.create("quote", "Quote", TextInputStyle.SHORT)
                        .setRequiredRange(1, 5)
                        .setPlaceholder("Example: 50")
                        .setRequired(true)
                        .build();

                Modal modal = Modal.create("commission-model", selectedCategory).addComponents(ActionRow.of(description), ActionRow.of(quote)).build();

                // Store the selected category id for channel creation
                CommissionData.setSelectedCategory(member, selectedCategory);
                p_stringSelectInteractionEvent.replyModal(modal).queue();
            }

            case "approve-freelancer" -> {
                String selectedFreelancer = p_stringSelectInteractionEvent.getValues().get(0);
                Member freelancerMember = p_stringSelectInteractionEvent.getGuild().getMemberById(selectedFreelancer);

                final long interactionChannelId = p_stringSelectInteractionEvent.getChannel().getIdLong();

                final Commission commission = m_commissionRepository.findCommissionByChannelId(interactionChannelId);
                commission.setFreelancerId(Long.parseLong(selectedFreelancer));

                assert freelancerMember != null;
                commission.setFreelancer(freelancerMember.getUser().getName());

                m_commissionRepository.save(commission);

                final String commissionId = commission.getId();

                final String commissionDescription = commission.getDescription();
                final String commissionQuote = commission.getQuote();

                if (freelancerMember == null) {
                    return;
                }

                // Move the channel to different category
                String targetCategoryId = "1141427451457708112";
                Category targetCategory = p_stringSelectInteractionEvent.getGuild().getCategoryById(targetCategoryId);

                p_stringSelectInteractionEvent.getGuild().getTextChannelById(p_stringSelectInteractionEvent.getChannel().getId()).getManager().setParent(targetCategory).queue();

                Objects.requireNonNull(p_stringSelectInteractionEvent.getJDA().getGuildById("1139719606186020904")).createTextChannel("commission-" + commissionId)
                    .setParent(p_stringSelectInteractionEvent.getJDA().getCategoryById("1141428418328657930"))
                    .queue(textChannel -> {
                        textChannel.sendMessage(commissionEmbed(freelancerMember.getUser().getName(), commissionQuote, commissionDescription)).queue();
                        commission.setPublicChannelId(textChannel.getIdLong());
                        commission.setState(Commission.State.IN_PROGRESS);
                        m_commissionRepository.save(commission);
                        textChannel.upsertPermissionOverride(Objects.requireNonNull(textChannel.getGuild().getMemberById(commission.getUserId()))).setAllowed(Permission.VIEW_CHANNEL).queue();
                        textChannel.upsertPermissionOverride(Objects.requireNonNull(textChannel.getGuild().getMemberById(commission.getFreelancerId()))).setAllowed(Permission.VIEW_CHANNEL).queue();
                });

                p_stringSelectInteractionEvent.getChannel().sendMessage(approveEmbed(freelancerMember.getUser().getName())).queue();
            }
        }
    }

    public MessageCreateData commissionEmbed(String p_member, String p_quote, String p_description) {
        return new FluxEmbedBuilder()
                .setTitle("Commission Accepted | Flux Solutions")
                .setDescription(p_description)
                .addField("Freelancer", p_member, false)
                .addField("Quote", p_quote, false)
                .addButton(ButtonStyle.SUCCESS, "finish-commission", "Mark As Finished", Emoji.fromUnicode("U+1F3AB"))
                .addButton(ButtonStyle.PRIMARY, "payment-finished", "Mark Payment As Received", Emoji.fromUnicode("U+1F3AB"))
                .addButton(ButtonStyle.SECONDARY, "request-payment", "Request Payment", Emoji.fromUnicode("U+1F3AB"))
                .addButton(ButtonStyle.DANGER, "cancel-ongoing-commission", "Cancel Commission", Emoji.fromUnicode("U+1F3AB"))
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .build();
    }

    public MessageCreateData approveEmbed(String p_member) {
        return new FluxEmbedBuilder()
                .setTitle("Commission | Flux Solutions")
                .setDescription("An administrator has approved **" + p_member + "** for this commission.")
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .build();
    }
}