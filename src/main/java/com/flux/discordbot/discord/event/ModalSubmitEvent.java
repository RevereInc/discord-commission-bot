package com.flux.discordbot.discord.event;

import com.flux.discordbot.discord.utility.CommissionData;
import com.flux.discordbot.discord.utility.FluxEmbedBuilder;
import com.flux.discordbot.entities.Commission;
import com.flux.discordbot.repository.CommissionRepository;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * @author Flux
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 */
@AllArgsConstructor
@Service
public class ModalSubmitEvent extends ListenerAdapter {
    private final CommissionRepository m_commissionRepository;

    @Override
    public void onModalInteraction(@NotNull final ModalInteractionEvent p_modalInteractionEvent) {
        String modalId = p_modalInteractionEvent.getModalId();
        Member member = p_modalInteractionEvent.getMember();

        switch (modalId) {
            case "commission-model" -> {
                assert member != null;

                // Category ID
                final String commissionData = CommissionData.getSelectedCategory(member);

                if(p_modalInteractionEvent.getJDA().getCategoryById(commissionData) == null) {
                    p_modalInteractionEvent.reply("The selected category does not exist").setEphemeral(true).queue();
                    return;
                }

                // Create a Commission instance
                final Commission commission = new Commission();
                commission.setUserId(member.getIdLong());
                commission.setCategory(commissionData);
                commission.setDescription(p_modalInteractionEvent.getValue("description").getAsString());
                commission.setQuote(p_modalInteractionEvent.getValue("quote").getAsString());
                commission.setPaymentPending(true);
                commission.setFinished(false);

                p_modalInteractionEvent.getJDA().getGuildById("1141049396453187690").createTextChannel(member.getUser().getName()).setParent(p_modalInteractionEvent.getJDA().getCategoryById(commissionData)).queue(textChannel -> {
                    textChannel.sendMessage(getCommissionEmbed(member.getUser().getName(), p_modalInteractionEvent.getValue("description").getAsString(), p_modalInteractionEvent.getValue("quote").getAsString())).queue();
                    commission.setChannelId(textChannel.getIdLong());
                    m_commissionRepository.save(commission);
                });

                // Remove selected category
                CommissionData.removeSelectedCategory(member);

                p_modalInteractionEvent.reply(getSucceedCommissionCreation()).setEphemeral(true).queue();
            }
            case "quote-model" -> {
                p_modalInteractionEvent.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage(sendQuoteOffer("A freelancer has offered you a quote of $" + p_modalInteractionEvent.getValue("quote").getAsString()))).queue();
            }
        }
    }

    public MessageCreateData sendQuoteOffer(String message) {
        return new FluxEmbedBuilder()
                .setTitle("New Quote | Flux Solutions")
                .setDescription(message)
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .addButton(ButtonStyle.SUCCESS, "accept-quote", "Accept Quote", Emoji.fromUnicode("U+1F3AB"))
                .addButton(ButtonStyle.DANGER, "decline-quote", "Decline Quote", Emoji.fromUnicode("U+1F3AB"))
                .build();
    }

    public MessageCreateData getCommissionEmbed(final String p_user, final String p_description, final String p_value) {
        return new FluxEmbedBuilder()
                .setTitle("New commission from " + p_user + " | Flux Solutions")
                .setDescription(p_description)
                .addField("Quote", p_value, false)
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .addButton(ButtonStyle.SUCCESS, "accept-commission", "Accept Commission", Emoji.fromUnicode("U+1F3AB"))
                .addButton(ButtonStyle.PRIMARY, "deny-commission", "Deny Commission", Emoji.fromUnicode("U+1F3AB"))
                .addButton(ButtonStyle.SECONDARY, "quote-commission", "Send a quote", Emoji.fromUnicode("U+1F3AB"))
                .addButton(ButtonStyle.DANGER, "delete-commission", "Delete Commission", Emoji.fromUnicode("U+1F3AB"))
                .build();
    }

    public MessageCreateData getSucceedCommissionCreation() {
        return new FluxEmbedBuilder()
                .setTitle("Commission Creation | Flux Solutions")
                .setDescription("Your request has been successfully submitted to our freelancers. Once your request is accepted, a dedicated channel will be created with the assigned freelancer.")
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .build();
    }
}