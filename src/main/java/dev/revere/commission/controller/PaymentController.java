package dev.revere.commission.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.net.Webhook;
import dev.revere.commission.data.PaymentSummary;
import dev.revere.commission.data.WebhookPayload;
import dev.revere.commission.discord.JDAInitializer;
import dev.revere.commission.entities.Commission;
import dev.revere.commission.repository.CommissionRepository;
import dev.revere.commission.services.PaymentService;
import dev.revere.commission.services.impl.PayPalServiceImpl;
import dev.revere.commission.services.impl.StripeServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/30/2024
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PayPalServiceImpl paypalService;
    private final StripeServiceImpl stripeService;
    private final CommissionRepository commissionRepository;

    private final String stripeWebhookSecret;
    private final String stripeApiKey;

    @Autowired
    public PaymentController(
            PayPalServiceImpl paypalService,
            StripeServiceImpl stripeService,
            CommissionRepository commissionRepository,
            @Value("${stripe.webhook-secret}") String stripeWebhookSecret,
            @Value("${stripe.api-key}") String stripeApiKey

    ) {
        this.paypalService = paypalService;
        this.stripeService = stripeService;
        this.commissionRepository = commissionRepository;
        this.stripeWebhookSecret = stripeWebhookSecret;
        this.stripeApiKey = stripeApiKey;

    }

    @PostMapping("/webhook/paypal")
    public ResponseEntity<Void> handlePayPalWebhook(
            @RequestBody String webhookEvent,
            @RequestHeader("PAYPAL-TRANSMISSION-ID") String transmissionId,
            @RequestHeader("PAYPAL-TRANSMISSION-TIME") String transmissionTime,
            @RequestHeader("PAYPAL-TRANSMISSION-SIG") String transmissionSig,
            @RequestHeader("PAYPAL-AUTH-ALGO") String authAlgo,
            @RequestHeader("PAYPAL-CERT-URL") String certUrl
    ) {
        try {
            JsonNode eventNode = new ObjectMapper().readTree(webhookEvent);
            String eventType = eventNode.path("event_type").asText();

            WebhookPayload payload = new WebhookPayload(
                    eventType,
                    transmissionId,
                    transmissionTime,
                    transmissionSig,
                    authAlgo,
                    certUrl,
                    webhookEvent
            );

            PaymentSummary payment = paypalService.handlePaymentWebhook(payload);
            if (payment != null) {
                handlePaymentUpdate(payment);
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to handle PayPal webhook", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/webhook/stripe")
    public ResponseEntity<Void> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) {
        try {
            Stripe.apiKey = stripeApiKey;
            com.stripe.model.Event stripeEvent = Webhook.constructEvent(
                    payload,
                    signature,
                    stripeWebhookSecret
            );

            WebhookPayload webhookPayload = new WebhookPayload(
                    stripeEvent.getType(),
                    null,
                    null,
                    signature,
                    null,
                    null,
                    payload
            );

            PaymentSummary payment = stripeService.handlePaymentWebhook(webhookPayload);
            if (payment != null) {
                handlePaymentUpdate(payment);
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to handle Stripe webhook", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private void handlePaymentUpdate(PaymentSummary payment) {
        try {
            Commission commission = payment.paymentService().equals("Stripe")
                    ? commissionRepository.findCommissionByStripePaymentLinkId(payment.invoiceId())
                    .orElseThrow(() -> new RuntimeException("Commission not found for invoice: " + payment.invoiceId()))
                    : commissionRepository.findCommissionByInvoiceId(payment.invoiceId())
                    .orElseThrow(() -> new RuntimeException("Commission not found for invoice: " + payment.invoiceId()));

            TextChannel channel = JDAInitializer.getShardManager().getTextChannelById(commission.getPublicChannelId());
            if (channel != null) {
                String status = payment.isFullyPaid() ? "PAID" : "PARTIALLY PAID";
                String amountDetails = Math.abs(payment.amountPaid() - payment.totalAmount()) > 0.01
                        ? String.format("$%.2f / $%.2f", payment.amountPaid(), payment.totalAmount())
                        : String.format("$%.2f", payment.amountPaid());

                String description = String.format(
                        """
                        A payment update has been received for your commission.
                        ### <:1270455353620041829:1299806081140133898> Payment Details
                        - **Status:** %s
                        - **Amount:** %s
                        ### <:1270673327098167347:1299806215915700315> Payment Service
                        ```
                        %s
                        ```""",
                        status,
                        amountDetails,
                        payment.paymentService()
                );

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle(" ")
                        .setDescription(description)
                        .setColor(Color.decode("#2b2d31"))
                        .setTimestamp(Instant.now());

                channel.sendMessageEmbeds(embed.build()).queue();
            }

            commission.setPaymentPending(false);
            commissionRepository.save(commission);
        } catch (Exception e) {
            log.error("Failed to handle payment update", e);
        }
    }
}