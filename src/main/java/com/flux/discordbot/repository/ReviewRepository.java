package com.flux.discordbot.repository;

import com.flux.discordbot.entities.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReviewRepository extends MongoRepository<Review, String> {
    Review findReviewByReviewerId(long reviewerId);
}
