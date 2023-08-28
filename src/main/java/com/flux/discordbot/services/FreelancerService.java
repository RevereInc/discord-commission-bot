package com.flux.discordbot.services;

import com.flux.discordbot.entities.Freelancer;
import net.dv8tion.jda.api.entities.Role;

public interface FreelancerService {
    void addService(Freelancer p_freelancer, Role p_role);
}
