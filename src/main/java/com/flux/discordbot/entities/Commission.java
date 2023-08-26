package com.flux.discordbot.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "commissions")
public class Commission {
    @Id
    @Field(name = "id")
    private String m_id;
    @Field(name = "user_id")
    private long m_userId;
    @Field(name = "category")
    private String m_category;
    @Field(name = "quote")
    private String m_quote;
    @Field(name = "description")
    private String m_description;
    @Field(name = "freelancer")
    private String m_freelancer;
    @Field(name = "approved_freelancer_id")
    private long m_approvedFreelancerId;
    @Field(name = "channel_id")
    private long m_channelId;

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
