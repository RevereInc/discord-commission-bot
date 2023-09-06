package com.flux.discordbot.repository;

import com.flux.discordbot.entities.Freelancer;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository interface for managing Freelancer entities in MongoDB.
 * This interface provides methods for CRUD operations on Freelancer objects.
 *
 * @author Flux Development
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 */
public interface FreelancerRepository extends MongoRepository<Freelancer, String> {

    /**
     * Find a freelancer by the user ID.
     *
     * @param userId The ID of the user associated with the freelancer.
     * @return The Freelancer object associated with the given user ID.
     */
    Freelancer findFreelancerByUserId(long userId);

    /**
     * Check if a freelancer with the specified user ID exists in the repository.
     *
     * @param userId The ID of the user associated with the freelancer.
     * @return `true` if a freelancer with the given user ID exists, `false` otherwise.
     */
    boolean existsFreelancerByUserId(long userId);

    /**
     * Retrieve the total amount of all freelancers
     *
     * @return The count of all freelancers
     */
    long count();

}