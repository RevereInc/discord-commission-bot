package dev.revere.commission.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.net.Webhook;
import dev.revere.commission.data.StripeInvoice;
import dev.revere.commission.discord.JDAInitializer;
import dev.revere.commission.entities.Commission;
import dev.revere.commission.repository.CommissionRepository;
import dev.revere.commission.services.impl.StripeServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.time.Instant;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/30/2024
 */
@Slf4j
@RestController
@RequestMapping("/api/webhooks")
public class StripeWebhookController {
    private final StripeServiceImpl stripeService;
    private final CommissionRepository commissionRepository;
    private final String webhookSecret;

    public StripeWebhookController(
            StripeServiceImpl stripeService,
            CommissionRepository commissionRepository,
            @Value("${stripe.webhook-secret}") String webhookSecret
    ) {
        this.stripeService = stripeService;
        this.commissionRepository = commissionRepository;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            log.info("Received Stripe webhook event: {}", event.getType());

            switch (event.getType()) {
                case "invoice.payment_succeeded" -> handleInvoicePaymentSucceeded(event);
                case "invoice.payment_failed" -> handleInvoicePaymentFailed(event);
                case "invoice.paid" -> handleInvoicePaid(event);
                case "payment_intent.succeeded" -> {
                    log.info("Payment intent succeeded: {}", event.getData().getObject().toJson());
                }
                default -> log.info("Unhandled event type: {}", event.getType());
            }

            return ResponseEntity.ok().body("Webhook processed successfully");
        } catch (SignatureVerificationException e) {
            log.error("Invalid signature", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook");
        }
    }

    private void handleInvoicePaymentSucceeded(Event event) throws StripeException {
        Invoice stripeInvoice = (Invoice) event.getDataObjectDeserializer().getObject().get();
        StripeInvoice updatedInvoice = stripeService.handleInvoicePayment(stripeInvoice);

        // Only send payment success notification if it's not fully paid
        if (!updatedInvoice.isPaid()) {
            sendPaymentNotification(updatedInvoice, PaymentNotificationType.PAYMENT_SUCCESS);
        }
    }

    private void handleInvoicePaymentFailed(Event event) throws StripeException {
        Invoice stripeInvoice = (Invoice) event.getDataObjectDeserializer().getObject().get();
        StripeInvoice updatedInvoice = stripeService.handleInvoicePayment(stripeInvoice);
        sendPaymentNotification(updatedInvoice, PaymentNotificationType.PAYMENT_FAILED);
    }

    private void handleInvoicePaid(Event event) throws StripeException {
        Invoice stripeInvoice = (Invoice) event.getDataObjectDeserializer().getObject().get();
        StripeInvoice updatedInvoice = stripeService.handleInvoicePayment(stripeInvoice);
        sendPaymentNotification(updatedInvoice, PaymentNotificationType.INVOICE_PAID);
    }

    private String getNotificationMessage(PaymentNotificationType type, double amount) {
        return switch (type) {
            case PAYMENT_SUCCESS -> String.format("A payment of $%.2f has been successfully received for your commission.", amount);
            case PAYMENT_FAILED -> "A payment attempt for your commission has failed.";
            case INVOICE_PAID -> "Your commission invoice has been fully paid!";
        };
    }

    private void sendPaymentNotification(StripeInvoice invoice, PaymentNotificationType type) {
        try {
            Commission commission = commissionRepository.findCommissionByInvoiceId(invoice.getId())
                    .orElseThrow(() -> new RuntimeException("Commission not found for invoice: " + invoice.getId()));

            TextChannel channel = JDAInitializer.getShardManager().getTextChannelById(commission.getPublicChannelId());
            if (channel != null) {
                String status = invoice.isPaid() ? "PAID" : "PARTIALLY PAID";
                String description;

                if (type == PaymentNotificationType.PAYMENT_SUCCESS) {
                    description = String.format(
                            """
                            %s
                            ### <:1270455353620041829:1299806081140133898> Payment Details
                            - **Status:** %s
                            - **Amount Paid:** $%.2f
                            - **Total Amount:** $%.2f
                            - **Remaining Balance:** $%.2f
                            ### <:1270673327098167347:1299806215915700315> Payment Service
                            ```
                            Stripe
                            ```""",
                            getNotificationMessage(type, invoice.getAmountPaid()),
                            status,
                            invoice.getAmountPaid(),
                            Double.parseDouble(commission.getQuote()),
                            invoice.getAmountRemaining()
                    );
                } else {
                    description = String.format(
                            """
                            %s
                            ### <:1270455353620041829:1299806081140133898> Payment Details
                            - **Status:** %s
                            - **Amount:** $%.2f
                            ### <:1270673327098167347:1299806215915700315> Payment Service
                            ```
                            Stripe
                            ```""",
                            getNotificationMessage(type, invoice.getAmountPaid()),
                            status,
                            invoice.getAmountPaid()
                    );
                }

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle(" ")
                        .setDescription(description)
                        .setColor(Color.decode("#2b2d31"))
                        .setTimestamp(Instant.now());

                channel.sendMessageEmbeds(embed.build()).queue();
            }

            commissionRepository.save(commission);
        } catch (Exception e) {
            log.error("Failed to send payment notification", e);
        }
    }

    public enum PaymentNotificationType {
        PAYMENT_SUCCESS,
        PAYMENT_FAILED,
        INVOICE_PAID
    }
}