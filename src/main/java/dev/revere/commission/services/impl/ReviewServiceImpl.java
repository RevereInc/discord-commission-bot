package dev.revere.commission.services.impl;

import dev.revere.commission.entities.Freelancer;
import dev.revere.commission.entities.Review;
import dev.revere.commission.repository.ReviewRepository;
import dev.revere.commission.services.ReviewService;
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
        final List<Review> reviews = m_reviewRepository.findAllByFreelancer(p_freelancer);

        if (reviews.isEmpty()) {
            return -1;
        }

        final int amountOfReviews = reviews.size();

        float reviewRatingSum = 0;

        for (final Review review : reviews) {
            reviewRatingSum += review.getRating();
        }

        return reviewRatingSum / amountOfReviews;
    }

    @Override
    public String parseRating(final float p_rating) {
        final int MAX_RATING = 6;

        if (p_rating < 1.0F || p_rating > MAX_RATING) {
            return "No ratings";
        }

        return "⭐".repeat((int) p_rating);
    }
}