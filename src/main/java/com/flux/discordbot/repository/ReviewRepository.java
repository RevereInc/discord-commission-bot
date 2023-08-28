package com.flux.discordbot.repository;

import com.flux.discordbot.entities.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository interface for managing Review entities in MongoDB.
 *
 * @author Flux Development
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 */
public interface ReviewRepository extends MongoRepository<Review, String> {

    /**
     * Find a review by the reviewer's ID.
     *
     * @param reviewerId The ID of the reviewer.
     * @return The Review object associated with the given reviewer ID.
     */
    List<Review> findAllByReviewerId(long reviewerId);

    /**
     * Find all Reviews by a freelancer's user id.
     *
     * @param userId the user id of the freelancer.
     * @return All the reviews associated with said freelancer.
     */
    List<Review> findAllByFreelancer_UserId(long userId);
}