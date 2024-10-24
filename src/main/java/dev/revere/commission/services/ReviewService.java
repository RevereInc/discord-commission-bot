package dev.revere.commission.services;

import dev.revere.commission.entities.Freelancer;

public interface ReviewService {
    /**
     * Calculate the average rating for a freelancer.
     *
     * @param p_freelancer The Freelancer object for which the average rating is calculated.
     * @return The average rating as a floating-point number.
     */
    float averageRating(final Freelancer p_freelancer);
    String parseRating(final float p_rating);
}
