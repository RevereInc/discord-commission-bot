package dev.revere.commission.discord.command.impl.admin;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.revere.commission.data.StripeInvoice;
import dev.revere.commission.discord.utility.TonicEmbedBuilder;
import dev.revere.commission.entities.Commission;
import dev.revere.commission.repository.CommissionRepository;
import dev.revere.commission.services.PaymentService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/30/2024
 */
@Service
public class RegenerateInvoiceCommand extends SlashCommand {

    private final CommissionRepository m_commissionRepository;
    private final PaymentService m_paymentService;

    @Autowired
    public RegenerateInvoiceCommand(CommissionRepository p_commissionRepository, PaymentService p_paymentService) {
        this.m_commissionRepository = p_commissionRepository;
        this.m_paymentService = p_paymentService;

        this.name = "regenerate-invoice";
        this.help = "Regenerate an invoice for a commission";
        this.guildOnly = true;
        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.userMissingPermMessage = "You are missing the `ADMINISTRATOR` permission required to execute this command.";

        final List<OptionData> optionData = new ArrayList<>();

        optionData.add(new OptionData(OptionType.STRING, "quote", "The cost amount of the commission", false));
        optionData.add(new OptionData(OptionType.STRING, "email", "The email of the client", false));
        optionData.add(new OptionData(OptionType.STRING, "commission_id", "The ID of the commission", false));
        this.options = optionData;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        OptionMapping commissionIdOption = event.getOption("commission_id");
        OptionMapping emailOption = event.getOption("email");
        OptionMapping quoteOption = event.getOption("quote");

        Commission commission;
        String email;
        String quote;

        if (commissionIdOption != null) {
            String commissionId = commissionIdOption.getAsString();
            commission = m_commissionRepository.findById(commissionId).orElse(null);
        } else {
            long channelId = event.getChannel().getIdLong();
            commission = m_commissionRepository.findCommissionByPublicChannelId(channelId);
        }

        if (commission == null) {
            event.reply(TonicEmbedBuilder.sharedMessageEmbed("Commission not found.")).setEphemeral(true).queue();
            return;
        }

        if (emailOption != null) {
            email = emailOption.getAsString();
        } else {
            if (commission.hasInvoice()) {
                email = commission.getInvoice().getClientEmail();
            } else {
                event.reply(TonicEmbedBuilder.sharedMessageEmbed("The email of the client is required to regenerate the invoice."))
                        .setEphemeral(true)
                        .queue();
                return;
            }
        }

        if (quoteOption != null) {
            quote = quoteOption.getAsString();
        } else {
            quote = commission.getQuote();
        }

        try {
            StripeInvoice invoice = m_paymentService.createInvoice(commission, email, quote);
            commission.setInvoice(invoice);
            m_commissionRepository.save(commission);

            String description = String.format(
                    """
                            A new payment invoice has been generated for your commission.
                            ### <:1270455353620041829:1299806081140133898> Commission Details
                            - **Amount:** %s
                            - **Description:** %s
                            ### <:1270673327098167347:1299806215915700315> Payment Information
                            - **Payment Service:** Stripe
                            - **Payment Link:** %s
                            Click the payment link above to complete your payment.
                            """,
                    commission.getFormattedQuote(),
                    commission.getDescription(),
                    invoice.getPaymentLink()
            );

            TonicEmbedBuilder embed = new TonicEmbedBuilder()
                    .setTitle("Invoice Regenerated")
                    .setDescription(description)
                    .setColor(Color.decode("#2b2d31"))
                    .setTimeStamp(Instant.now());

            event.reply(embed.build()).queue();

        } catch (Exception e) {
            event.reply(TonicEmbedBuilder.sharedMessageEmbed("An error occurred while regenerating the invoice. Please try again later."))
                    .setEphemeral(true)
                    .queue();
            e.printStackTrace();
        }
    }
}