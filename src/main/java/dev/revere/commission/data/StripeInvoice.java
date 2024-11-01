package dev.revere.commission.data;

import dev.revere.commission.entities.Commission;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a Stripe invoice with relevant details.
 *
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 11/1/2024
 */
@Builder
public class StripeInvoice implements Serializable {
    private String id;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime dueAt;
    private double amountPaid;
    private double amountRemaining;
    private InvoiceStatus status;
    private String paymentLink;
    private String clientId;
    private String clientName;
    private String clientEmail;
    private String title;
    private String memo;
    private List<Payment> payments;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(LocalDateTime dueAt) {
        this.dueAt = dueAt;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public double getAmountRemaining() {
        return amountRemaining;
    }

    public void setAmountRemaining(double amountRemaining) {
        this.amountRemaining = amountRemaining;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public String getPaymentLink() {
        return paymentLink;
    }

    public void setPaymentLink(String paymentLink) {
        this.paymentLink = paymentLink;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String p_clientEmail) {
        clientEmail = p_clientEmail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public boolean isPaid() {
        return status == InvoiceStatus.PAID;
    }

    public boolean isPartiallyPaid() {
        return status == InvoiceStatus.PARTIALLY_PAID;
    }

    public boolean isPastDue() {
        return status == InvoiceStatus.PAST_DUE;
    }

    public double getProgressPercentage(Commission commission) {
        return (Double.parseDouble(commission.getQuote()) > 0) ? (amountPaid / Double.parseDouble(commission.getQuote())) * 100 : 0;
    }

    public String getFormattedAmountPaid() {
        return String.format("$%.2f", amountPaid);
    }

    public String getFormattedAmountRemaining() {
        return String.format("$%.2f", amountRemaining);
    }

    @AllArgsConstructor
    public static class Payment implements Serializable {
        private String id;
        private LocalDateTime paidAt;
        private double amount;
        private String paymentMethod;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public LocalDateTime getPaidAt() {
            return paidAt;
        }

        public void setPaidAt(LocalDateTime paidAt) {
            this.paidAt = paidAt;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }
    }

    public enum InvoiceStatus {
        DRAFT("draft", "Invoice is a draft"),
        OPEN("open", "Invoice has been finalized and awaiting payment"),
        PAID("paid", "Invoice has been paid in full"),
        UNCOLLECTIBLE("uncollectible", "Invoice has been marked as uncollectible"),
        VOID("void", "Invoice has been voided"),
        PARTIALLY_PAID("partially_paid", "Invoice has been partially paid"),
        PAYMENT_PENDING("payment_pending", "Payment is being processed"),
        PAST_DUE("past_due", "Invoice is past due");

        private final String stripeStatus;
        private final String description;

        InvoiceStatus(String stripeStatus, String description) {
            this.stripeStatus = stripeStatus;
            this.description = description;
        }

        public String getStripeStatus() {
            return stripeStatus;
        }

        public String getDescription() {
            return description;
        }

        public static InvoiceStatus fromStripeStatus(String stripeStatus) {
            return Arrays.stream(values())
                    .filter(status -> status.stripeStatus.equals(stripeStatus))
                    .findFirst()
                    .orElse(DRAFT);
        }
    }
}