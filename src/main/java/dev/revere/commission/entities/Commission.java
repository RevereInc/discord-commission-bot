package dev.revere.commission.entities;

import dev.revere.commission.services.PaymentService;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Represents a commission entity stored in a MongoDB collection.
 * This class defines the structure of a commission, including category, quote, description, freelancer, and other details.
 * It also includes methods like constructors and toString for convenience.
 *
 * @author Revere Development
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "commissions")
public class Commission implements Serializable {
    @Id
    private String id;

    // The ID of the user associated with the commission
    private long userId;

    // The username of the user associated with the commission
    private String client;

    // The category of the commission
    private String category;

    // The quote or price for the commission
    private String quote;

    // The description of the commission
    private String description;

    // The ID of the invoice associated with the commission
    private String invoiceId;

    // The ID of the payment link associated with the commission (for Stripe)
    private String stripePaymentLinkId;

    // The payment service used for the commission
    private String paymentService;

    // The freelancer associated with the commission
    private String freelancer;

    // The ID of the freelancer for the commission
    private long freelancerId;

    // The channel ID where the commission was requested
    private long channelId;

    // The channel ID where the commission is being handled
    private long publicChannelId;

    // The list of freelancers interested in the commission
    private HashMap<Long, String> interestedFreelancers;

    // The list of freelancers who have been declined for the commission
    private HashMap<Long, String> declinedFreelancers;

    // Mark the commission as finished
    private State state;

    // Check if the payment is still pending
    private boolean paymentPending;

    private HashMap<Long, Long> clientMessages = new HashMap<>();
    private HashMap<Long, Long> freelancerMessages = new HashMap<>();
    private long initialClientMessageId;
    private long initialFreelancerMessageId;

    public String getFormattedQuote() {
        return "$" + String.format("%.2f", Double.parseDouble(quote));
    }

    @Override
    public String toString() {
        return "Commission{" +
                "id='" + id +
                ", userId=" + userId +
                ", username='" + client +
                ", category='" + category +
                ", quote='" + quote +
                ", invoiceId='" + invoiceId +
                ", paymentService='" + paymentService +
                ", description='" + description +
                ", freelancer='" + freelancer +
                ", freelancerId=" + freelancerId +
                ", channelId=" + channelId +
                ", publicChannelId=" + publicChannelId +
                ", interestedFreelancers=" + interestedFreelancers +
                ", declinedFreelancers=" + declinedFreelancers +
                ", state=" + state +
                ", paymentPending=" + paymentPending +
                '}';
    }

    public enum State implements Serializable {
        COMPLETED, IN_PROGRESS, PENDING, CANCELLED;
    }
}
