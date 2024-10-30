package dev.revere.commission.data;

import dev.revere.commission.services.PaymentService;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/30/2024
 */
public record PaymentSummary(
        String paymentService,
        String invoiceId,
        double amountPaid,
        double totalAmount,
        boolean isFullyPaid
) {}