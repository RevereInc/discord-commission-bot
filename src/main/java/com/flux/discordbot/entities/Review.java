package com.flux.discordbot.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Represents a review entity stored in a MongoDB collection.
 * This class defines the structure of a review, including the reviewer, rating, title, and content.
 * It also includes a reference to the associated Freelancer.
 *
 * @author Flux Development
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document("reviews")
public class Review {

    // Unique identifier for the review
    @Id
    private String id;

    // The ID of the reviewer
    private long reviewerId;

    // The rating given in the review
    private float rating;

    // The title of the review
    private String title;

    // The content of the review
    private String review;

    // Reference to the associated Freelancer
    @DBRef
    private Freelancer freelancer;
}