package dev.revere.commission.discord.command.impl.admin;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.revere.commission.discord.utility.TonicEmbedBuilder;
import dev.revere.commission.entities.Commission;
import dev.revere.commission.factory.PaymentServiceFactory;
import dev.revere.commission.repository.CommissionRepository;
import dev.revere.commission.services.PaymentService;
import dev.revere.commission.services.impl.PayPalServiceImpl;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.Instant;
import java.util.Arrays;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/30/2024
 */
@Service
public class RegenerateInvoiceCommand extends SlashCommand {

    private final PaymentServiceFactory m_paymentServiceFactory;
    private final CommissionRepository m_commissionRepository;

    @Autowired
    public RegenerateInvoiceCommand(PaymentServiceFactory paymentServiceFactory, CommissionRepository commissionRepository) {
        this.m_paymentServiceFactory = paymentServiceFactory;
        this.m_commissionRepository = commissionRepository;

        this.name = "regenerate-invoice";
        this.help = "Regenerate an invoice for a commission";
        this.guildOnly = true;
        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.userMissingPermMessage = "You are missing the `ADMINISTRATOR` permission required to execute this command.";

        OptionData commissionIdOption = new OptionData(OptionType.STRING, "commission_id", "The ID of the commission", false);
        OptionData paymentServiceOption = new OptionData(OptionType.STRING, "payment_service", "The payment service to use", true);

        for (String serviceName : paymentServiceFactory.getPaymentServices().keySet()) {
            paymentServiceOption.addChoice(serviceName, serviceName);
        }

        this.options = Arrays.asList(paymentServiceOption, commissionIdOption);
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String selectedService = event.getOption("payment_service").getAsString();

        OptionMapping commissionIdOption = event.getOption("commission_id");
        Commission commission;

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

        try {
            PaymentService paymentService = m_paymentServiceFactory.getPaymentService(selectedService);
            String invoiceId = paymentService instanceof PayPalServiceImpl ? paymentService.createInvoice(commission, Double.parseDouble(commission.getQuote()),
                    """
                            Commission Payment
                            Commission ID: %s
                            Description: %s
                            Amount: %s
                            """
                            .formatted(commission.getId(), commission.getDescription(), commission.getFormattedQuote())
            ) : paymentService.createInvoice(commission, Double.parseDouble(commission.getQuote()), commission.getClient() + " | " + commission.getCategory() + " Service Payment");
            paymentService.updateCommissionWithInvoiceDetails(commission, invoiceId);

            String description = String.format(
                    """
                    A new payment invoice has been generated for your commission.
                    ### <:1270455353620041829:1299806081140133898> Commission Details
                    - **Amount:** %s
                    - **Description:** %s
                    ### <:1270673327098167347:1299806215915700315> Payment Information
                    - **Payment Service:** %s
                    - **Payment Link:** %s
                    Click the payment link above to complete your payment.
                    """,
                    commission.getFormattedQuote(),
                    commission.getDescription(),
                    selectedService,
                    paymentService.getPaymentLink(commission.getInvoiceId())
            );

            TonicEmbedBuilder embed = new TonicEmbedBuilder()
                    .setTitle(" ")
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