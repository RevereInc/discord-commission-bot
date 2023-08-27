package com.flux.discordbot.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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
public class Freelancer {
    // Unique identifier for the Freelancer
    @Id
    @Field(name = "id")
    private String m_id;

    // User ID associated with this Freelancer
    @Field(name = "user_id")
    private long m_userId;

    // Name of the Freelancer
    @Field(name = "name")
    private String m_name;

    // List of service role IDs associated with the Freelancer
    @Field(name = "services")
    private List<Long> m_serviceRoleIds;

    // Biography or description of the Freelancer
    @Field(name = "bio")
    private String m_bio;

    // List of title descriptions associated with the Freelancer
    @Field(name = "title_descriptions")
    private List<TitleDescription> m_titleDescriptions;
}
