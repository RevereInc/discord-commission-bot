package com.flux.discordbot.repository;

import com.flux.discordbot.entities.Commission;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository interface for managing Commission entities in MongoDB.
 * This interface provides methods for CRUD operations on Commission objects.
 *
 * @author Flux Development
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 */
public interface CommissionRepository extends MongoRepository<Commission, String> {

    /**
     * Find a commission by the channel ID.
     *
     * @param channelId The ID of the channel associated with the commission.
     * @return The Commission object associated with the given channel ID.
     */
    Commission findCommissionByChannelId(long channelId);

    /**
     * Find a commission by the user ID.
     *
     * @param userId The ID of the user associated with the commission.
     * @return The Commission object associated with the given user ID.
     */
    Commission findCommissionByUserId(long userId);
}