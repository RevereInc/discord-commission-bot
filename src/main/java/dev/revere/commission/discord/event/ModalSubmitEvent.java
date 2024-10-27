package dev.revere.commission.discord.event;

import dev.revere.commission.Constants;
import dev.revere.commission.discord.utility.CommissionData;
import dev.revere.commission.discord.utility.TonicEmbedBuilder;
import dev.revere.commission.entities.Commission;
import dev.revere.commission.entities.Department;
import dev.revere.commission.repository.CommissionRepository;
import dev.revere.commission.services.CommissionService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ModalSubmitEvent extends ListenerAdapter {
    private final CommissionRepository m_commissionRepository;
    private final CommissionService m_commissionService;

    private final CommissionMessageEvent m_commissionMessageEvent;

    @Override
    public void onModalInteraction(@NotNull final ModalInteractionEvent p_modalInteractionEvent) {
        String modalId = p_modalInteractionEvent.getModalId();
        Member member = p_modalInteractionEvent.getMember();

        switch (modalId) {
            case "commission-model" -> {
                assert member != null;

                final Department commissionData = CommissionData.getSelectedCategory(member);
                if (commissionData == null) {
                    p_modalInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Please select a category")).setEphemeral(true).queue();
                    return;
                }

                if (p_modalInteractionEvent.getJDA().getCategoryById(commissionData.getCommissionGuildCategoryID()) == null) {
                    p_modalInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("The selected category does not exist")).setEphemeral(true).queue();
                    return;
                }

                String description = p_modalInteractionEvent.getValue("description").getAsString();
                String quote = p_modalInteractionEvent.getValue("quote").getAsString();

                if (description.isEmpty() || quote.isEmpty()) {
                    p_modalInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Please fill out all fields")).setEphemeral(true).queue();
                    return;
                }

                try {
                    Double.parseDouble(quote);
                } catch (NumberFormatException e) {
                    p_modalInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Please enter a valid number for the quote")).setEphemeral(true).queue();
                    return;
                }

                final Commission commission = m_commissionService.createCommission(member, commissionData.getName(), description, quote);

                Objects.requireNonNull(p_modalInteractionEvent.getJDA().getGuildById(Constants.COMMISSION_GUILD_ID))
                        .createTextChannel(member.getUser().getName())
                        .setParent(p_modalInteractionEvent.getJDA().getCategoryById(commissionData.getCommissionGuildCategoryID()))
                        .queue(textChannel -> {
                            textChannel.sendMessage(getCommissionEmbed(member.getUser().getName(), commission.getDescription(), commission.getFormattedQuote())).queue();
                            commission.setChannelId(textChannel.getIdLong());

                            Objects.requireNonNull(p_modalInteractionEvent.getJDA().getGuildById(Constants.MAIN_GUILD_ID))
                                    .createTextChannel(member.getUser().getName())
                                    .setParent(p_modalInteractionEvent.getJDA().getCategoryById(commissionData.getMainGuildCategoryID()))
                                    .queue(publicTextChannel -> {
                                        publicTextChannel.upsertPermissionOverride(member)
                                                .setAllowed(Permission.VIEW_CHANNEL)
                                                .queue();
                                        publicTextChannel.sendMessage(getCustomerCommissionEmbed(commission.getDescription(), commission.getFormattedQuote())).queue();
                                        commission.setPublicChannelId(publicTextChannel.getIdLong());
                                        m_commissionRepository.save(commission);
                                        m_commissionMessageEvent.setupInitialMessages(commission, p_modalInteractionEvent.getJDA());

                                        p_modalInteractionEvent.reply(getSucceedCommissionCreation(commission.getPublicChannelId()))
                                                .setEphemeral(true)
                                                .queue();
                                    });
                        });

                CommissionData.removeSelectedCategory(member);
            }
            default -> {
                if (modalId.startsWith("quote-model-")) {
                    final String commissionId = modalId.substring("quote-model-".length());
                    final Commission commission = m_commissionRepository.findCommissionById(commissionId);
                    final User user = p_modalInteractionEvent.getUser();
                    final String quote = p_modalInteractionEvent.getValue("quote").getAsString();

                    try {
                        Double.parseDouble(quote);
                    } catch (NumberFormatException e) {
                        p_modalInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Please enter a valid number for the quote")).setEphemeral(true).queue();
                        return;
                    }

                    String formattedQuote = "$" + String.format("%.2f", Double.parseDouble(quote));

                    p_modalInteractionEvent.getJDA().getGuildById(Constants.MAIN_GUILD_ID)
                            .getTextChannelById(commission.getPublicChannelId())
                            .sendMessage(sendQuoteOffer(user, quote, formattedQuote))
                            .queue();

                    p_modalInteractionEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("The new quote has been delivered to the client")).setEphemeral(true).queue();
                }
            }
        }
    }

    public MessageCreateData sendQuoteOffer(User p_user, String p_quote, String p_formattedQuote) {
        String description = String.format(
                """
                        **%s** has sent a new quote for the commission.
                        ### <:1270673327098167347:1299806215915700315> New Quoted Price
                        ```
                        %s
                        ```""",
                p_user.getName(),
                p_formattedQuote
        );

        return new TonicEmbedBuilder()
                .setTitle(" ")
                .setDescription(description)
                .setTimeStamp(Instant.now())
                .setColor(Color.decode("#2b2d31"))
                .addButton(ButtonStyle.SUCCESS, "accept-quote-" + p_user.getId() + "-" + p_quote, "Accept Quote", Emoji.fromUnicode("U+1F3AB"))
                .addButton(ButtonStyle.DANGER, "decline-quote-" + p_user.getId(), "Decline Quote", Emoji.fromUnicode("U+1F3AB"))
                .build();
    }

    public MessageCreateData getCustomerCommissionEmbed(final String p_description, final String p_value) {
        String description = String.format(
                """
                        Your commission request to **%s** has been received. This is an automated message. Please wait for a response from one of our freelancers.
                        ### <:1270455353620041829:1299806081140133898> Commission Details
                        %s
                        ### <:1270673327098167347:1299806215915700315> Quoted Price
                        ```
                        %s
                        ```""",
                Constants.PROJECT_NAME,
                p_description,
                p_value
        );

        return new TonicEmbedBuilder()
                .setTitle(" ")
                .setDescription(description)
                .addButton(ButtonStyle.SECONDARY, "revoke-commission", "Revoke Commission", Emoji.fromUnicode("U+1F3AB"))
                .setTimeStamp(Instant.now())
                .setColor(Color.decode("#2b2d31"))
                .build();
    }

    public MessageCreateData getCommissionEmbed(final String p_user, final String p_description, final String p_value) {
        String description = String.format(
                """
                        A new commission request has been received from **%s**.
                        ### <:1270455353620041829:1299806081140133898> Commission Details
                        %s
                        ### <:1270673327098167347:1299806215915700315> Quoted Price
                        ```
                        %s
                        ```""",
                p_user,
                p_description,
                p_value
        );

        return new TonicEmbedBuilder()
                .setTitle(" ")
                .setDescription(description)
                .setTimeStamp(Instant.now())
                .setColor(Color.decode("#2b2d31"))
                .addButton(ButtonStyle.SUCCESS, "accept-commission", "Accept Commission", Emoji.fromUnicode("U+1F3AB"))
                .addButton(ButtonStyle.SECONDARY, "quote-commission", "Offer a new quote", Emoji.fromUnicode("U+1F4B0"))
                .addButton(ButtonStyle.DANGER, "deny-commission", "Decline Commission", Emoji.fromUnicode("U+1F6AB"))
                .addButton(ButtonStyle.DANGER, "delete-commission", "Delete Commission", Emoji.fromUnicode("U+1F6AB"))
                .build();
    }

    public MessageCreateData getSucceedCommissionCreation(long channelId) {
        return new TonicEmbedBuilder()
                .setTitle(" ")
                .setDescription("Your commission has been successfully created and sent to our freelancers. Please find your commission request in <#" + channelId + ">.")
                .setTimeStamp(Instant.now())
                .setColor(Color.decode("#2b2d31"))
                .build();
    }
}