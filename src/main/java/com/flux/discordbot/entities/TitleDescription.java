package com.flux.discordbot.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a title and its description.
 * This class is often used within other entities, such as Freelancer.
 *
 * @author Flux Development
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 */
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class TitleDescription {

    // The title associated with this description
    private String m_title;

    // The description or details related to the title
    private String m_description;
}