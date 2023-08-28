package com.flux.discordbot.services;

import com.flux.discordbot.entities.Freelancer;
import com.flux.discordbot.repository.FreelancerRepository;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class FreelancerServiceImpl implements FreelancerService {
    private final FreelancerRepository m_freelancerRepository;

    @Override
    public void addService(final Freelancer p_freelancer, final Role p_role) {
        final Freelancer freelancer = p_freelancer.clone();

        final List<Long> serviceRoles = freelancer.getServiceRoleIds();
        serviceRoles.add(p_role.getIdLong());
        freelancer.setServiceRoleIds(serviceRoles);

        m_freelancerRepository.save(freelancer);
    }
}
