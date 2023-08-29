package com.flux.discordbot.services;

import com.flux.discordbot.entities.Freelancer;
import com.flux.discordbot.entities.TitleDescription;
import net.dv8tion.jda.api.entities.Role;

public interface FreelancerService {
    /**
     * Add a service to a freelancer, associating it with a specific role.
     *
     * @param p_freelancer The Freelancer object to which the service is added.
     * @param p_role       The Role object representing the service's role.
     */
    void addService(Freelancer p_freelancer, Role p_role);

    /**
     * Remove a service to a freelancer, associating it with a specific role.
     *
     * @param p_freelancer The Freelancer object to which the service is removed.
     * @param p_role       The Role object representing the service's role.
     */
    void removeService(Freelancer p_freelancer, Role p_role);

    /**
     * Check if a freelancer has reached the maximum number of cards.
     *
     * @param p_freelancer The Freelancer object to check.
     * @return `true` if the freelancer has reached the maximum number of cards, `false` otherwise.
     */
    boolean maxCards(Freelancer p_freelancer);

    /**
     * Add a card (TitleDescription) to a freelancer.
     *
     * @param p_freelancer      The Freelancer object to which the card is added.
     * @param p_titleDescription The TitleDescription object representing the card's details.
     */
    void addCard(Freelancer p_freelancer, TitleDescription p_titleDescription);

    /**
     * Remove a card from a freelancer based on its index.
     *
     * @param p_freelancer The Freelancer object from which the card is removed.
     * @param p_index      The index of the card to remove.
     * @return The TitleDescription object representing the removed card.
     */
    TitleDescription removeCard(Freelancer p_freelancer, int p_index);
}
