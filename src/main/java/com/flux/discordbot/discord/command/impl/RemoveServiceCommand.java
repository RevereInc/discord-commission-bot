package com.flux.discordbot.discord.command.impl;

import com.flux.discordbot.entities.Freelancer;
import com.flux.discordbot.repository.FreelancerRepository;
import com.flux.discordbot.services.FreelancerService;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RemoveServiceCommand extends SlashCommand {
    private final FreelancerRepository m_freelancerRepository;
    private final FreelancerService m_freelancerService;
    @Autowired
    public RemoveServiceCommand(final FreelancerRepository p_freelancerRepository, final FreelancerService p_freelancerService) {
        m_freelancerRepository = p_freelancerRepository;
        m_freelancerService = p_freelancerService;

        this.name = "removeservice";
        this.help = "Remove Service to freelancer";
        this.guildOnly = true;

        this.userPermissions = new Permission[] { Permission.ADMINISTRATOR };
        this.userMissingPermMessage = "You are missing the `ADMINISTRATOR` permission required to execute this command.";

        // Define command options
        this.options = List.of(
                new OptionData(OptionType.USER, "user", "the freelancer").setRequired(true),
                new OptionData(OptionType.ROLE, "service", "Service to remove").setRequired(true)
        );
    }

    @Override
    protected void execute(final SlashCommandEvent p_slashCommandEvent) {
        final User user = p_slashCommandEvent.getOption("user").getAsUser();
        final Role role = p_slashCommandEvent.getOption("service").getAsRole();

        if (!m_freelancerRepository.existsFreelancerByUserId(user.getIdLong())) {
            p_slashCommandEvent.reply("Could not find freelancer with name " + user.getName()).queue();
            return;
        }

        final Freelancer freelancer = m_freelancerRepository.findFreelancerByUserId(user.getIdLong());

        if (!freelancer.getServiceRoleIds().contains(role.getIdLong())) {
            p_slashCommandEvent.reply("You do not have this service.").queue();
            return;
        }

        m_freelancerService.removeService(freelancer, role);

        p_slashCommandEvent.reply("Removed service `" + role.getName() + "` from `" + user.getName() + "`").queue();
    }
}
