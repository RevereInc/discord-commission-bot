package com.flux.discordbot.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;
/**
 * @author Flux Development
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 *
 * Represents a Freelancer entity stored in a MongoDB collection.
 */
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "freelancers")
public class Freelancer implements Serializable, Cloneable {
    // Unique identifier for the Freelancer
    @Id
    private String id;

    // User ID associated with this Freelancer
    private long userId;

    // Name of the Freelancer
    private String name;

    // List of service role IDs associated with the Freelancer
    private List<Long> serviceRoleIds;

    // Biography or description of the Freelancer
    private String bio;

    // List of title descriptions associated with the Freelancer
    private List<TitleDescription> titleDescriptions;

    @Override
    public Freelancer clone() {
        try {
            final Freelancer clone = (Freelancer) super.clone();

            clone.id = id;
            clone.userId = userId;
            clone.name = name;
            clone.serviceRoleIds = serviceRoleIds;
            clone.bio = bio;
            clone.titleDescriptions = titleDescriptions;

            return clone;
        } catch (final CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
