package com.flux.discordbot.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Represents a commission entity stored in a MongoDB collection.
 * This class defines the structure of a commission, including category, quote, description, freelancer, and other details.
 * It also includes methods like constructors and toString for convenience.
 *
 * @author Flux Development
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "commissions")
public class Commission {
    @Id
    private String id;

    // The ID of the user associated with the commission
    private long userId;

    // The category of the commission
    private String category;

    // The quote or price for the commission
    private String quote;

    // The description of the commission
    private String description;

    // The freelancer associated with the commission
    private String freelancer;

    // The ID of the approved freelancer for the commission
    private long approvedFreelancerId;

    // The channel ID where the commission was requested
    private long channelId;

    @Override
    public String toString() {
        return "Commission{" +
                "id='" + id + '\'' +
                ", userId=" + userId +
                ", category='" + category + '\'' +
                ", quote='" + quote + '\'' +
                ", description='" + description + '\'' +
                ", freelancer='" + freelancer + '\'' +
                ", approvedFreelancerId=" + approvedFreelancerId +
                ", channelId=" + channelId +
                '}';
    }
}
