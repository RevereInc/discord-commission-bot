package dev.revere.commission.repository;

import dev.revere.commission.entities.Freelancer;
import dev.revere.commission.entities.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository interface for managing Review entities in MongoDB.
 *
 * @author Revere Development
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
     * @param freelancer the freelancer.
     * @return All the reviews associated with said freelancer.
     */
    List<Review> findAllByFreelancer(Freelancer freelancer);

    /**
     * Retrieve the total amount of all reviews
     *
     * @return The count of all reviews
     */
    long count();
}