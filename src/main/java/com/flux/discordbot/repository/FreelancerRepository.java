package com.flux.discordbot.repository;

import com.flux.discordbot.entities.Freelancer;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FreelancerRepository extends MongoRepository<Freelancer, String> {
    Freelancer findFreelancerByUserId(long userId);
}
