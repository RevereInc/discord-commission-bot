package com.flux.discordbot.services;

import com.flux.discordbot.entities.Freelancer;
import com.flux.discordbot.entities.Review;
import com.flux.discordbot.repository.ReviewRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository m_reviewRepository;

    /**
     * Calculate the average rating for a freelancer based on their reviews.
     *
     * @param p_freelancer The Freelancer object for which the average rating is calculated.
     * @return The average rating as a floating-point number. Returns -1 if no reviews are available.
     */
    @Override
    public float averageRating(final Freelancer p_freelancer) {
        // Retrieve all reviews for the given freelancer
        final List<Review> reviews = m_reviewRepository.findAllByFreelancer(p_freelancer);

        // If no reviews are available, return -1
        if (reviews.size() < 1) {
            return -1;
        }

        final int amountOfReviews = reviews.size();

        // Calculate the total sum of review ratings
        float reviewRatingSum = 0;

        for (final Review review : reviews) {
            reviewRatingSum += review.getRating();
        }

        // Calculate and return the average rating
        return reviewRatingSum / amountOfReviews;
    }

    @Override
    public String parseRating(final float p_rating) {
        if (Float.compare(p_rating, -1.0F) == 0) {
            return "No ratings";
        }
        return String.valueOf(p_rating); // TODO: Make emoji parser
    }
}
