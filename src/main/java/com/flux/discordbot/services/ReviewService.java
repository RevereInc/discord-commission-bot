package com.flux.discordbot.services;

import com.flux.discordbot.entities.Freelancer;

public interface ReviewService {
    float averageRating(final Freelancer p_freelancer);
    String parseRating(final float p_rating);
}
