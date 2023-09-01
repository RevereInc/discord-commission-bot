package com.flux.discordbot.services;

import com.flux.discordbot.entities.Freelancer;
import com.flux.discordbot.entities.TitleDescription;
import com.flux.discordbot.repository.FreelancerRepository;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Service
public class FreelancerServiceImpl implements FreelancerService {
    private final FreelancerRepository m_freelancerRepository;

    /**
     * Add a service (Role) to a freelancer and update the database.
     *
     * @param p_freelancer The Freelancer object to which the service is added.
     * @param p_role       The Role object representing the service's role.
     */
    @Override
    public void addService(final Freelancer p_freelancer, final Role p_role) {
        final List<Long> serviceRoles = p_freelancer.getServiceRoleIds();
        serviceRoles.add(p_role.getIdLong());
        p_freelancer.setServiceRoleIds(serviceRoles);

        m_freelancerRepository.save(p_freelancer);
    }

    @Override
    public void removeService(final Freelancer p_freelancer, final Role p_role) {
        final List<Long> serviceRoles = p_freelancer.getServiceRoleIds();
        serviceRoles.remove(p_role.getIdLong());
        p_freelancer.setServiceRoleIds(serviceRoles);

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
        if (p_freelancer.getTitleDescriptions() == null) {
            return false;
        }
        return p_freelancer.getTitleDescriptions().size() >= 3;
    }

    /**
     * Add a card (TitleDescription) to a freelancer and update the database.
     *
     * @param p_freelancer      The Freelancer object to which the card is added.
     * @param p_titleDescription The TitleDescription object representing the card's details.
     */
    @Override
    public void addCard(final Freelancer p_freelancer, final TitleDescription p_titleDescription) {
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
