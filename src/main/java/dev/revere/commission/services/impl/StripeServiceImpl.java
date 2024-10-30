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
import org.springframework.web.client.HttpClientErrorException;
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
@Qualifier("stripe")
public class StripeServiceImpl implements PaymentService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    private static final String STRIPE_PAYMENT_LINKS_API = "https://api.stripe.com/v1/payment_links";
    private final CommissionRepository m_commissionRepository;

    public StripeServiceImpl(@Value("${stripe.api-key}") String apiKey, CommissionRepository commissionRepository) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
        this.m_commissionRepository = commissionRepository;
    }

    @Override
    public String createInvoice(Commission commission, double amount, String description) throws PaymentException {
        try {
            MultiValueMap<String, String> paymentData = new LinkedMultiValueMap<>();

            paymentData.add("line_items[0][price]", createPrice(amount, description));
            paymentData.add("line_items[0][quantity]", "1");

            paymentData.add("metadata[commission_id]", commission.getId());
            paymentData.add("metadata[client_id]", String.valueOf(commission.getUserId()));

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(paymentData, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(STRIPE_PAYMENT_LINKS_API, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                String paymentLinkId = (String) body.get("id");
                String fullUrl = (String) body.get("url");
                String publicId = extractIdFromUrl(fullUrl);
                return publicId + "|" + paymentLinkId;
            } else {
                log.error("Failed to create payment link. Status code: {} Response: {}",
                        response.getStatusCode(), response.getBody());
                throw new PaymentException("Failed to create payment link: " +
                        response.getBody().get("error"));
            }
        } catch (Exception e) {
            log.error("Exception while creating payment link", e);
            throw new PaymentException("Failed to create payment link", e);
        }
    }

    private String extractIdFromUrl(String fullUrl) {
        String[] parts = fullUrl.split("/");
        return parts[parts.length - 1];
    }

    private String createPrice(double amount, String description) throws PaymentException {
        try {
            String createPriceUrl = "https://api.stripe.com/v1/prices";
            MultiValueMap<String, String> priceData = new LinkedMultiValueMap<>();
            priceData.add("unit_amount", String.valueOf((int)(amount * 100)));
            priceData.add("currency", "usd");
            priceData.add("product_data[name]", description);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(priceData, headers);
            ResponseEntity<Map> priceResponse = restTemplate.postForEntity(createPriceUrl, request, Map.class);

            if (priceResponse.getStatusCode().is2xxSuccessful()) {
                return (String) priceResponse.getBody().get("id");
            } else {
                throw new PaymentException("Failed to create price: " + priceResponse.getBody().toString());
            }
        } catch (Exception e) {
            log.error("Exception while creating price", e);
            throw new PaymentException("Failed to create price", e);
        }
    }

    @Override
    public InvoiceDetails getInvoiceDetails(String p_paymentLinkId) throws PaymentException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);

            String fullUrl = STRIPE_PAYMENT_LINKS_API + "/" + p_paymentLinkId;
            log.debug("Attempting to fetch payment link details from URL: {}", fullUrl);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(fullUrl, HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                log.debug("Received successful response: {}", body);

                List<Map<String, Object>> lineItems;
                if (body.containsKey("line_items")) {
                    lineItems = (List<Map<String, Object>>) body.get("line_items");
                } else {
                    String lineItemsUrl = fullUrl + "/line_items";
                    log.debug("Fetching line items from URL: {}", lineItemsUrl);
                    ResponseEntity<Map> lineItemsResponse = restTemplate.exchange(lineItemsUrl, HttpMethod.GET, request, Map.class);
                    Map<String, Object> lineItemsBody = lineItemsResponse.getBody();
                    lineItems = (List<Map<String, Object>>) lineItemsBody.get("data");
                }

                double totalAmount = 0;
                for (Map<String, Object> item : lineItems) {
                    Map<String, Object> price = (Map<String, Object>) item.get("price");
                    int unitAmount = (int) price.get("unit_amount");
                    int quantity = (int) item.get("quantity");
                    totalAmount += (unitAmount * quantity) / 100.0;
                }

                boolean isActive = (boolean) body.get("active");
                PaymentStatus paymentStatus = isActive ? PaymentStatus.PENDING : PaymentStatus.FAILED;

                return new InvoiceDetails(p_paymentLinkId, totalAmount, paymentStatus);
            } else {
                log.error("Failed to get payment link details for ID: {}. Status code: {}", p_paymentLinkId, response.getStatusCode());
                throw new PaymentException("Failed to get payment link details: " + response.getBody().get("error"));
            }
        } catch (HttpClientErrorException.NotFound e) {
            log.error("Payment link not found for ID: {}. Full response: {}", p_paymentLinkId, e.getResponseBodyAsString());
            throw new PaymentException("No such payment link: " + p_paymentLinkId);
        } catch (Exception e) {
            log.error("Failed to get payment link details", e);
            throw new PaymentException("Failed to get payment link details", e);
        }
    }

    @Override
    public PaymentSummary handlePaymentWebhook(WebhookPayload p_webhookPayload) throws PaymentException {
        try {
            JsonNode webhookEvent = objectMapper.readTree(p_webhookPayload.webhookEvent());
            String eventType = webhookEvent.path("type").asText();

            log.info("Received Stripe webhook event type: {}", eventType);

            return switch (eventType) {
                case "checkout.session.completed" -> handleCheckoutSessionCompleted(webhookEvent);
                default -> {
                    log.warn("Unhandled Stripe webhook event type: {}", eventType);
                    yield null;
                }
            };
        } catch (Exception e) {
            log.error("Failed to process Stripe webhook", e);
            throw new PaymentException("Failed to process Stripe webhook", e);
        }
    }

    private PaymentSummary handleCheckoutSessionCompleted(JsonNode webhookEvent) {
        JsonNode data = webhookEvent.path("data").path("object");
        String paymentLinkId = data.path("payment_link").asText();
        double amountTotal = data.path("amount_total").asDouble() / 100.0;

        return new PaymentSummary(this.getServiceName(), paymentLinkId, amountTotal, amountTotal, true);
    }

    @Override
    public void updateCommissionWithInvoiceDetails(Commission p_commission, String p_invoiceId) {
        p_commission.setPaymentService(getServiceName());
        p_commission.setPaymentPending(true);

        String[] idParts = p_invoiceId.split("\\|");
        if (idParts.length == 2) {
            p_commission.setInvoiceId(idParts[0]);
            p_commission.setStripePaymentLinkId(idParts[1]);
        } else {
            p_commission.setInvoiceId(p_invoiceId);
        }
        m_commissionRepository.save(p_commission);
    }

    @Override
    public String getPaymentLink(String p_invoiceId) {
        return "https://buy.stripe.com/" + p_invoiceId;
    }

    @Override
    public String getServiceName() {
        return "Stripe";
    }
}
