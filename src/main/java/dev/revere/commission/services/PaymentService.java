package dev.revere.commission.services;

import dev.revere.commission.data.InvoiceDetails;
import dev.revere.commission.data.PaymentSummary;
import dev.revere.commission.data.WebhookPayload;
import dev.revere.commission.entities.Commission;
import dev.revere.commission.exception.PaymentException;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/30/2024
 */
public interface PaymentService {
    String createInvoice(Commission p_commission, double p_amount, String p_description) throws PaymentException;

    InvoiceDetails getInvoiceDetails(String p_invoiceId) throws PaymentException;

    PaymentSummary handlePaymentWebhook(WebhookPayload p_webhookPayload) throws PaymentException;

    void updateCommissionWithInvoiceDetails(Commission p_commission, String p_invoiceId);

    String getPaymentLink(String p_invoiceId) throws PaymentException;

    String getServiceName();
}