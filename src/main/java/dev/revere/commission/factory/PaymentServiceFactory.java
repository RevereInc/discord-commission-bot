package dev.revere.commission.factory;

import dev.revere.commission.services.PaymentService;
import dev.revere.commission.services.impl.PayPalServiceImpl;
import dev.revere.commission.services.impl.StripeServiceImpl;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/30/2024
 */
@Service
public class PaymentServiceFactory {
    private final Map<String, PaymentService> paymentServices;

    public PaymentServiceFactory(List<PaymentService> services) {
        paymentServices = new HashMap<>();
        for (PaymentService service : services) {
            paymentServices.put(service.getServiceName().toLowerCase(), service);
        }
    }

    public PaymentService getPaymentService(String type) {
        PaymentService service = paymentServices.get(type.toLowerCase());
        if (service == null) {
            throw new IllegalArgumentException("Unknown payment service type: " + type);
        }
        return service;
    }

    public Map<String, PaymentService> getPaymentServices() {
        return paymentServices;
    }
}