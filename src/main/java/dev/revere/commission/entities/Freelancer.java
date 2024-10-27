package dev.revere.commission.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

/**
 * @author Revere Development
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 *
 * Represents a Freelancer entity stored in a MongoDB collection.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "freelancers")
public class Freelancer implements Serializable {
    // Unique identifier for the Freelancer
    @Id
    private String id;

    // User ID associated with this Freelancer
    private long userId;

    // Name of the Freelancer
    private String name;

    // List of departments the Freelancer is associated with
    private List<Department> departments;

    // Portfolio of the Freelancer
    private String portfolio;

    // List of title descriptions associated with the Freelancer
    private List<TitleDescription> titleDescriptions;
}
