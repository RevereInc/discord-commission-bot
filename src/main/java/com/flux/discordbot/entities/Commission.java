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
    @Field(name = "id")
    private String m_id;

    // The ID of the user associated with the commission
    @Field(name = "user_id")
    private long m_userId;

    // The category of the commission
    @Field(name = "category")
    private String m_category;

    // The quote or price for the commission
    @Field(name = "quote")
    private String m_quote;

    // The description of the commission
    @Field(name = "description")
    private String m_description;

    // The freelancer associated with the commission
    @Field(name = "freelancer")
    private String m_freelancer;

    // The ID of the approved freelancer for the commission
    @Field(name = "approved_freelancer_id")
    private long m_approvedFreelancerId;

    // The channel ID where the commission was requested
    @Field(name = "channel_id")
    private long m_channelId;

    /**
     * Constructor with parameters to initialize a Commission object.
     *
     * @param p_category              The category of the commission.
     * @param p_quote                 The quote or price for the commission.
     * @param p_description           The description of the commission.
     * @param p_freelancer            The freelancer associated with the commission.
     * @param p_approvedFreelancerId  The ID of the approved freelancer for the commission.
     * @param p_channelId             The channel ID where the commission was requested.
     */
    public Commission(final String p_category, final String p_quote, final String p_description, final String p_freelancer, final long p_approvedFreelancerId, final long p_channelId) {
        m_category = p_category;
        m_quote = p_quote;
        m_description = p_description;
        m_freelancer = p_freelancer;
        m_approvedFreelancerId = p_approvedFreelancerId;
        m_channelId = p_channelId;
    }

    @Override
    public String toString() {
        return "Commission{" +
                "id='" + m_id + '\'' +
                ", m_category='" + m_category + '\'' +
                ", m_quote='" + m_quote + '\'' +
                ", m_description='" + m_description + '\'' +
                ", m_freelancer='" + m_freelancer + '\'' +
                ", m_approvedFreelancerId=" + m_approvedFreelancerId +
                ", m_channelId=" + m_channelId +
                '}';
    }
}
