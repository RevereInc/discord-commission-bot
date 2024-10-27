package dev.revere.commission.services;

import dev.revere.commission.entities.Department;
import dev.revere.commission.entities.Freelancer;
import dev.revere.commission.entities.TitleDescription;

public interface FreelancerService {
    /**
     * Add a department to a freelancer.
     *
     * @param p_freelancer The Freelancer object to which the department is added.
     * @param p_department The Department object representing the department to add.
     */
    void addDepartment(Freelancer p_freelancer, Department p_department);

    /**
     * Remove a department from a freelancer.
     *
     * @param p_freelancer The Freelancer object from which the department is removed.
     * @param p_department The Department object representing the department to remove.
     */
    void removeDepartment(Freelancer p_freelancer, Department p_department);

    /**
     * Set the portfolio of a freelancer.
     *
     * @param p_freelancer The Freelancer object to which the portfolio is set.
     * @param p_portfolio  The portfolio to set.
     */
    void setPortfolio(Freelancer p_freelancer, String p_portfolio);

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
