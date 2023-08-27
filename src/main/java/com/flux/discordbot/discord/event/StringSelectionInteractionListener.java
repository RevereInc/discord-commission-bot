package com.flux.discordbot.discord.event;

import com.flux.discordbot.discord.utility.CommissionData;
import com.flux.discordbot.discord.utility.FluxEmbedBuilder;
import com.flux.discordbot.entities.Commission;
import com.flux.discordbot.repository.CommissionRepository;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * @author Flux
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
                        .setRequiredRange(1, 2)
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

                System.out.println(interactionChannelId);
                final Commission commission = m_commissionRepository.findCommissionByChannelId(interactionChannelId);
                commission.setApprovedFreelancerId(Long.parseLong(selectedFreelancer));

                m_commissionRepository.insert(commission);

                final String commissionId = commission.getId();
                final long commissionCreatorId = commission.getUserId();

                final String commissionDescription = commission.getDescription();
                final String commissionQuote = commission.getQuote();

                if (freelancerMember == null) {
                    return;
                }

                // Move the channel to different category
                String targetCategoryId = "1141427451457708112";
                Category targetCategory = p_stringSelectInteractionEvent.getGuild().getCategoryById(targetCategoryId);

                p_stringSelectInteractionEvent.getGuild().getTextChannelById(p_stringSelectInteractionEvent.getChannel().getId()).getManager().setParent(targetCategory).queue();

                p_stringSelectInteractionEvent.getJDA().getGuildById("1139719606186020904").createTextChannel("commission-" + commissionId)
                    .setParent(p_stringSelectInteractionEvent.getJDA().getCategoryById("1141428418328657930"))
                    .addMemberPermissionOverride(commission.getUserId(), List.of(Permission.VIEW_CHANNEL), Collections.emptyList())
                    .addMemberPermissionOverride(commission.getApprovedFreelancerId(), List.of(Permission.VIEW_CHANNEL), Collections.emptyList())
                    .queue(textChannel -> {
                        textChannel.sendMessage(commissionEmbed(freelancerMember.getUser().getName(), commissionQuote, commissionDescription)).queue();
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