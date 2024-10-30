package dev.revere.commission.data;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/30/2024
 */
public record WebhookPayload(
        String eventType,
        String transmissionId,
        String transmissionTime,
        String transmissionSig,
        String authAlgo,
        String certUrl,
        String webhookEvent
) {}