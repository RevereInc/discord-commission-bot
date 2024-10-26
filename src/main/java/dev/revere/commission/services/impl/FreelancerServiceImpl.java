package dev.revere.commission.services.impl;

import dev.revere.commission.entities.Department;
import dev.revere.commission.entities.Freelancer;
import dev.revere.commission.entities.TitleDescription;
import dev.revere.commission.repository.FreelancerRepository;
import dev.revere.commission.services.FreelancerService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Service
public class FreelancerServiceImpl implements FreelancerService {
    private final FreelancerRepository m_freelancerRepository;

    /**
     * Add a department to a freelancer.
     *
     * @param p_freelancer The Freelancer object to which the department is added.
     * @param p_department The Department object representing the department to add.
     */
    @Override
    public void addDepartment(final Freelancer p_freelancer, final Department p_department) {
        if (p_freelancer == null) {
            throw new IllegalArgumentException("Freelancer cannot be null");
        }

        List<Department> departments = p_freelancer.getDepartments();
        if (departments == null) {
            departments = new ArrayList<>();
        }
        departments.add(p_department);
        p_freelancer.setDepartments(departments);
        m_freelancerRepository.save(p_freelancer);
    }

    /**
     * Remove a department from a freelancer.
     *
     * @param p_freelancer The Freelancer object from which the department is removed.
     * @param p_department The Department object representing the department to remove.
     */
    @Override
    public void removeDepartment(final Freelancer p_freelancer, final Department p_department) {
        if (p_freelancer == null) {
            throw new IllegalArgumentException("Freelancer cannot be null");
        }

        List<Department> departments = p_freelancer.getDepartments();
        if (departments != null) {
            departments.remove(p_department);
            p_freelancer.setDepartments(departments);
            m_freelancerRepository.save(p_freelancer);
        }
    }

    /**
     * Set the bio of a freelancer.
     *
     * @param p_freelancer The Freelancer object to which the bio is set.
     * @param p_bio        The bio to set.
     */
    @Override
    public void setBio(Freelancer p_freelancer, String p_bio) {
        if (p_freelancer == null) {
            throw new IllegalArgumentException("Freelancer cannot be null");
        }

        p_freelancer.setBio(p_bio);
        m_freelancerRepository.save(p_freelancer);
    }

    /**
     * Check if a freelancer has reached the maximum number of cards (TitleDescriptions).
     *
     * @param p_freelancer The Freelancer object to check.
     * @return `true` if the freelancer has reached the maximum number of cards, `false` otherwise.
     */
    @Override
    public boolean maxCards(final Freelancer p_freelancer) {
        if (p_freelancer == null) {
            throw new IllegalArgumentException("Freelancer cannot be null");
        }

        if (p_freelancer.getTitleDescriptions() == null) {
            return false;
        }
        return p_freelancer.getTitleDescriptions().size() >= 3;
    }

    /**
     * Add a card (TitleDescription) to a freelancer and update the database.
     *
     * @param p_freelancer       The Freelancer object to which the card is added.
     * @param p_titleDescription The TitleDescription object representing the card's details.
     */
    @Override
    public void addCard(final Freelancer p_freelancer, final TitleDescription p_titleDescription) {
        if (p_freelancer == null) {
            throw new IllegalArgumentException("Freelancer cannot be null");
        }

        final List<TitleDescription> titleDescriptions = Objects.requireNonNullElse(p_freelancer.getTitleDescriptions(), new ArrayList<>());
        titleDescriptions.add(p_titleDescription);

        p_freelancer.setTitleDescriptions(titleDescriptions);

        m_freelancerRepository.save(p_freelancer);
    }

    /**
     * Remove a card (TitleDescription) from a freelancer based on its index and update the database.
     *
     * @param p_freelancer The Freelancer object from which the card is removed.
     * @param p_index      The index of the card to remove.
     * @return The TitleDescription object representing the removed card, or null if the index is out of bounds.
     */
    @Override
    public TitleDescription removeCard(final Freelancer p_freelancer, final int p_index) {
        if (p_freelancer == null) {
            throw new IllegalArgumentException("Freelancer cannot be null");
        }

        if (p_freelancer.getTitleDescriptions().size() < p_index) {
            return null;
        }

        final List<TitleDescription> titleDescriptions = p_freelancer.getTitleDescriptions();
        final TitleDescription titleDescription = titleDescriptions.remove(p_index - 1);

        p_freelancer.setTitleDescriptions(titleDescriptions);

        m_freelancerRepository.save(p_freelancer);

        return titleDescription;
    }
}
