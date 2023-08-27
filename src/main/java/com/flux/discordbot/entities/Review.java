package com.flux.discordbot.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document("reviews")
public class Review {
    @Id
    @Field(name = "id")
    private String m_id;
    @Field(name = "reviewer")
    private long m_reviewerId;
    @Field(name = "rating")
    private float m_rating;
    @Field(name = "title")
    private String m_title;
    @Field(name = "review")
    private String m_review;
    @DBRef
    @Field(name = "freelancer")
    private Freelancer m_freelancer;
}
