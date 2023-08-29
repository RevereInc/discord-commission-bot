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

    @Override
    public float averageRating(final Freelancer p_freelancer) {
        final List<Review> reviews = m_reviewRepository.findAllByFreelancer(p_freelancer);

        if (reviews.size() < 1) {
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
        if (Float.compare(p_rating, -1.0F) == 0) {
            return "No ratings";
        }
        return String.valueOf(p_rating); // TODO: Make emoji parser
    }
}
