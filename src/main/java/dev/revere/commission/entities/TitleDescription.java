package dev.revere.commission.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Represents a title and its description.
 * This class is often used within other entities, such as Freelancer.
 *
 * @author Revere Development
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TitleDescription implements Serializable {

    // The title associated with this description
    private String title;

    // The description or details related to the title
    private String description;
}