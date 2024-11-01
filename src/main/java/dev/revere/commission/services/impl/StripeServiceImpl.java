package dev.revere.commission.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import dev.revere.commission.data.StripeInvoice;
import dev.revere.commission.entities.Commission;
import dev.revere.commission.repository.CommissionRepository;
import dev.revere.commission.services.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/30/2024
 */
@Slf4j
@Service
public class StripeServiceImpl implements PaymentService {
    private final CommissionRepository commissionRepository;

    public StripeServiceImpl(
            @Value("${stripe.api-key}") String apiKey,
            CommissionRepository commissionRepository
    ) {
        this.commissionRepository = commissionRepository;
        Stripe.apiKey = apiKey;
    }

    @Override
    public StripeInvoice createInvoice(Commission commission, String email, String quote) throws StripeException {
        try {
            String customerId = getOrCreateCustomer(commission, email);

            InvoiceCreateParams invoiceParams = InvoiceCreateParams.builder()
                    .setCustomer(customerId)
                    .setAutoAdvance(false)
                    .setCollectionMethod(InvoiceCreateParams.CollectionMethod.SEND_INVOICE)
                    .setDaysUntilDue(30L)
                    .setDescription(String.format("Commission Invoice for %s - %s",
                            commission.getClient(), commission.getCategory()))
                    .putMetadata("commission_id", commission.getId())
                    .putMetadata("discord_user_id", String.valueOf(commission.getUserId()))
                    .build();

            Invoice stripeInvoice = Invoice.create(invoiceParams);

            InvoiceItemCreateParams itemParams = InvoiceItemCreateParams.builder()
                    .setCustomer(customerId)
                    .setInvoice(stripeInvoice.getId())
                    .setAmount((long) (Double.parseDouble(quote) * 100))
                    .setCurrency("usd")
                    .setDescription(commission.getCategory() + " Commission Service")
                    .build();

            InvoiceItem.create(itemParams);

            Invoice finalizedInvoice = stripeInvoice.finalizeInvoice();

            StripeInvoice invoice = StripeInvoice.builder()
                    .id(finalizedInvoice.getId())
                    .createdAt(LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(finalizedInvoice.getCreated()),
                            ZoneId.systemDefault()))
                    .dueAt(LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(finalizedInvoice.getDueDate()),
                            ZoneId.systemDefault()))
                    .amountPaid(finalizedInvoice.getAmountPaid().doubleValue() / 100)
                    .amountRemaining(finalizedInvoice.getAmountRemaining().doubleValue() / 100)
                    .status(StripeInvoice.InvoiceStatus.fromStripeStatus(finalizedInvoice.getStatus()))
                    .paymentLink(finalizedInvoice.getHostedInvoiceUrl())
                    .clientId(String.valueOf(commission.getUserId()))
                    .clientName(commission.getClient())
                    .clientEmail(email)
                    .title(commission.getCategory() + " Commission Service")
                    .memo(commission.getDescription())
                    .payments(new ArrayList<>())
                    .build();

            commission.setInvoice(invoice);
            commissionRepository.save(commission);

            return invoice;
        } catch (StripeException e) {
            log.error("Failed to create Stripe invoice", e);
            throw e;
        }
    }

    private String getOrCreateCustomer(Commission commission, String email) throws StripeException {
        CustomerSearchParams params = CustomerSearchParams.builder()
                .setQuery("metadata['discord_user_id']:'" + commission.getUserId() + "'")
                .build();
        CustomerSearchResult searchResult = Customer.search(params);

        if (!searchResult.getData().isEmpty()) {
            Customer existingCustomer = searchResult.getData().get(0);
            if (existingCustomer.getEmail() == null || existingCustomer.getEmail().isEmpty()) {
                CustomerUpdateParams updateParams = CustomerUpdateParams.builder()
                        .setEmail(email)
                        .build();
                existingCustomer.update(updateParams);
            }
            return existingCustomer.getId();
        }

        CustomerCreateParams customerParams = CustomerCreateParams.builder()
                .setName(commission.getClient())
                .setEmail(email)
                .setDescription("Discord User: " + commission.getClient())
                .putMetadata("discord_user_id", String.valueOf(commission.getUserId()))
                .build();

        Customer customer = Customer.create(customerParams);
        return customer.getId();
    }

    @Override
    public StripeInvoice getInvoiceDetails(String invoiceId) throws StripeException {
        try {
            Invoice stripeInvoice = Invoice.retrieve(invoiceId);
            return handleInvoicePayment(stripeInvoice);
        } catch (StripeException e) {
            log.error("Failed to get Stripe invoice details", e);
            throw e;
        }
    }

    @Override
    public StripeInvoice handleInvoicePayment(Invoice stripeInvoice) throws StripeException {
        try {
            Commission commission = commissionRepository.findCommissionByInvoiceId(stripeInvoice.getId())
                    .orElseThrow(() -> new RuntimeException("Commission not found for invoice: " + stripeInvoice.getId()));

            StripeInvoice invoice = commission.getInvoice();
            invoice.setAmountPaid(stripeInvoice.getAmountPaid().doubleValue() / 100);
            invoice.setAmountRemaining(stripeInvoice.getAmountRemaining().doubleValue() / 100);
            invoice.setStatus(StripeInvoice.InvoiceStatus.fromStripeStatus(stripeInvoice.getStatus()));

            if (stripeInvoice.getPaid()) {
                invoice.setPaidAt(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(stripeInvoice.getStatusTransitions().getPaidAt()),
                        ZoneId.systemDefault()
                ));
            }

            if (stripeInvoice.getPaymentIntent() != null) {
                PaymentIntent paymentIntent = PaymentIntent.retrieve(stripeInvoice.getPaymentIntent());
                invoice.getPayments().add(new StripeInvoice.Payment(
                        paymentIntent.getId(),
                        LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(paymentIntent.getCreated()),
                                ZoneId.systemDefault()),
                        paymentIntent.getAmount().doubleValue() / 100,
                        paymentIntent.getPaymentMethod()
                ));
            }

            commission.setInvoice(invoice);
            commissionRepository.save(commission);

            return invoice;
        } catch (StripeException e) {
            log.error("Failed to handle Stripe invoice payment", e);
            throw e;
        }
    }

    @Override
    public String[] createStripeAccount(String countryCode) {
        try {
            Account account = Account.create(
                    AccountCreateParams.builder()
                            .setType(AccountCreateParams.Type.EXPRESS)
                            .setCountry(countryCode)
                            .setCapabilities(
                                    AccountCreateParams.Capabilities.builder()
                                            .setTransfers(AccountCreateParams.Capabilities.Transfers.builder()
                                                    .setRequested(true)
                                                    .build())
                                            .build())
                            .build()
            );

            AccountLinkCreateParams accountLinkParams = AccountLinkCreateParams.builder()
                    .setAccount(account.getId())
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .setRefreshUrl("https://discord.gg/tonicconsulting")
                    .setReturnUrl("https://discord.gg/tonicconsulting")
                    .build();

            AccountLink accountLink = AccountLink.create(accountLinkParams);
            return new String[]{account.getId(), accountLink.getUrl()};
        } catch (Exception e) {
            log.error("Failed to create Stripe account", e);
            return null;
        }
    }
}