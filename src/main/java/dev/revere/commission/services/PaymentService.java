package dev.revere.commission.services;

import com.stripe.exception.StripeException;
import com.stripe.model.Invoice;
import dev.revere.commission.data.StripeInvoice;
import dev.revere.commission.entities.Commission;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/30/2024
 */
public interface PaymentService {
    /**
     * Creates a new invoice for the given commission.
     *
     * @param p_commission The commission to create an invoice for
     * @param email        The email of the client
     * @param quote        The cost amount of the commission
     * @return The created invoice
     * @throws StripeException If an error occurs while creating the invoice
     */
    StripeInvoice createInvoice(Commission p_commission, String email, String quote) throws StripeException;

    /**
     * Retrieves the details of the invoice with the given ID.
     *
     * @param p_invoiceId The ID of the invoice to retrieve
     * @return The details of the invoice
     * @throws StripeException If an error occurs while retrieving the invoice details
     */
    StripeInvoice getInvoiceDetails(String p_invoiceId) throws StripeException;

    /**
     * Handles the payment of the given invoice.
     *
     * @param stripeInvoice The invoice to handle the payment for
     * @return The updated invoice details
     * @throws StripeException If an error occurs while handling the payment
     */
    StripeInvoice handleInvoicePayment(Invoice stripeInvoice) throws StripeException;

    /**
     * Creates a new Stripe account with the given country code.
     *
     * @param p_countryCode The country code to create the account for
     * @return The created Stripe account onboarding URL
     */
    String[] createStripeAccount(String p_countryCode);
}