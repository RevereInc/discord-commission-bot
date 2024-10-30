package dev.revere.commission.data;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/30/2024
 */
public record InvoiceDetails(
        String invoiceId,
        double totalAmount,
        PaymentStatus status
) {}