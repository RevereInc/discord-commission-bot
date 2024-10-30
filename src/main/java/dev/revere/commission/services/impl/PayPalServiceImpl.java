package dev.revere.commission.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.revere.commission.data.InvoiceDetails;
import dev.revere.commission.data.PaymentStatus;
import dev.revere.commission.data.PaymentSummary;
import dev.revere.commission.data.WebhookPayload;
import dev.revere.commission.entities.Commission;
import dev.revere.commission.exception.PaymentException;
import dev.revere.commission.repository.CommissionRepository;
import dev.revere.commission.services.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;


/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/30/2024
 */
@Slf4j
@Service
@Qualifier("paypal")
public class PayPalServiceImpl implements PaymentService {
    private final CommissionRepository m_commissionRepository;

    private final String clientId;
    private final String clientSecret;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String PAYPAL_INVOICING_API = "https://api-m.paypal.com/v2/invoicing/invoices";
    private static final String PAYMENT_CAPTURE_COMPLETED = "PAYMENT.CAPTURE.COMPLETED";

    public PayPalServiceImpl(
            CommissionRepository p_commissionRepository, @Value("${paypal.client-id}") String clientId,
            @Value("${paypal.client-secret}") String clientSecret
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.m_commissionRepository = p_commissionRepository;
    }

    @Override
    public String createInvoice(Commission p_commission, double p_amount, String p_description) throws PaymentException {
        Map<String, Object> merchantInfo = Map.of(
                "email", "remi@revere.dev",
                "first_name", "Remi",
                "last_name", "Gabrielsen",
                "business_name", "Revere Inc.",
                "phone", Map.of(
                        "country_code", "47",
                        "national_number", "96913664"
                )
        );

        List<Map<String, Object>> items = List.of(Map.of(
                "name", p_commission.getClient() + " | " + p_commission.getCategory() + " Service Payment",
                "quantity", "1",
                "unit_amount", Map.of(
                        "currency_code", "USD",
                        "value", String.format("%.2f", p_amount)
                )
        ));

        Map<String, Object> invoice = Map.of(
                "detail", Map.of(
                        "currency_code", "USD",
                        "note", p_description,
                        "payment_term", Map.of("term_type", "DUE_ON_RECEIPT"),
                        "invoice_number", "TONIC-" + System.currentTimeMillis()
                ),
                "merchant_info", merchantInfo,
                "items", items,
                "terms", "Refunds after terms are agreed upon"
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(invoice, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                PAYPAL_INVOICING_API,
                request,
                Map.class
        );

        if (response.getStatusCode() != HttpStatus.CREATED) {
            log.error("Failed to create PayPal invoice. Status code: {} Response: {}",
                    response.getStatusCode(), response.getBody());
            throw new PaymentException("Failed to create PayPal invoice: " +
                    response.getBody().get("message"));
        }

        return (String) response.getBody().get("id");

    }

    @Override
    public InvoiceDetails getInvoiceDetails(String p_invoiceId) throws PaymentException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getAccessToken());

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    PAYPAL_INVOICING_API + "/" + p_invoiceId,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            Map<String, Object> amount = (Map<String, Object>) body.get("amount");
            Map<String, Object> breakdown = (Map<String, Object>) amount.get("breakdown");

            double totalAmount = Double.parseDouble(
                    ((Map<String, Object>) breakdown.get("item_total")).get("value").toString()
            );

            String status = (String) body.get("status");
            PaymentStatus paymentStatus = switch (status) {
                case "PAID" -> PaymentStatus.PAID;
                case "PARTIALLY_PAID" -> PaymentStatus.PARTIALLY_PAID;
                case "PAYMENT_PENDING" -> PaymentStatus.PENDING;
                default -> PaymentStatus.FAILED;
            };

            return new InvoiceDetails(p_invoiceId, totalAmount, paymentStatus);
        } catch (Exception e) {
            log.error("Failed to get invoice details", e);
            throw new PaymentException("Failed to get invoice details", e);
        }
    }

    @Override
    public PaymentSummary handlePaymentWebhook(WebhookPayload p_webhookPayload) throws PaymentException {
        try {
            JsonNode webhookEvent = objectMapper.readTree(p_webhookPayload.webhookEvent());
            String eventType = p_webhookPayload.eventType();

            log.info("Received webhook event type: {}", eventType);

            if (!PAYMENT_CAPTURE_COMPLETED.equals(eventType)) {
                log.warn("Ignoring unsupported webhook event type: {}", eventType);
                return null;
            }

            JsonNode resource = webhookEvent.path("resource");
            String invoiceId = resource.path("supplementary_data")
                    .path("related_ids")
                    .path("invoice_id")
                    .asText();

            double amountPaid = resource.path("amount")
                    .path("value")
                    .asDouble();

            InvoiceDetails details = getInvoiceDetails(invoiceId);
            boolean isFullyPaid = Math.abs(amountPaid - details.totalAmount()) < 0.01;

            return new PaymentSummary(
                    getServiceName(),
                    invoiceId,
                    amountPaid,
                    details.totalAmount(),
                    isFullyPaid
            );
        } catch (Exception e) {
            log.error("Failed to process payment webhook", e);
            throw new PaymentException("Failed to process payment webhook", e);
        }
    }

    @Override
    public void updateCommissionWithInvoiceDetails(Commission p_commission, String p_invoiceId) {
        p_commission.setPaymentService(getServiceName());
        p_commission.setPaymentPending(true);
        p_commission.setInvoiceId(p_invoiceId);
        m_commissionRepository.save(p_commission);
    }

    @Override
    public String getPaymentLink(String p_invoiceId) {
        return "https://www.paypal.com/invoice/p/#" + p_invoiceId;
    }

    @Override
    public String getServiceName() {
        return "PayPal";
    }

    private String getAccessToken() throws PaymentException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(clientId, clientSecret);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api-m.paypal.com/v1/oauth2/token",
                    request,
                    Map.class
            );

            return (String) response.getBody().get("access_token");
        } catch (Exception e) {
            throw new PaymentException("Failed to get PayPal access token", e);
        }
    }
}