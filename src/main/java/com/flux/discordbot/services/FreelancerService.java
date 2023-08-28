package com.flux.discordbot.services;

import com.flux.discordbot.entities.Freelancer;
import com.flux.discordbot.entities.TitleDescription;
import net.dv8tion.jda.api.entities.Role;

public interface FreelancerService {
    void addService(Freelancer p_freelancer, Role p_role);
    boolean maxCards(Freelancer p_freelancer);
    void addCard(Freelancer p_freelancer, TitleDescription p_titleDescription);
    TitleDescription removeCard(Freelancer p_freelancer, int p_index);
}
