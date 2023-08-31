package com.flux.discordbot.discord.command.impl;

import com.flux.discordbot.discord.utility.FluxEmbedBuilder;
import com.flux.discordbot.repository.CommissionRepository;
import com.flux.discordbot.repository.FreelancerRepository;
import com.flux.discordbot.repository.ReviewRepository;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class FluxInfoCommand extends SlashCommand {

    private final CommissionRepository m_commissionRepository;
    private final FreelancerRepository m_freelancerRepository;
    private final ReviewRepository m_reviewRepository;

    @Autowired
    public FluxInfoCommand(final CommissionRepository p_commissionRepository, final FreelancerRepository p_freelancerRepository, final ReviewRepository p_reviewRepository) {
        this.m_commissionRepository = p_commissionRepository;
        this.m_freelancerRepository = p_freelancerRepository;
        this.m_reviewRepository = p_reviewRepository;

        this.name = "flux";
        this.help = "Retrieve all information about flux";
        this.guildOnly = true;

        this.userPermissions = new Permission[] { Permission.ADMINISTRATOR };
        this.userMissingPermMessage = "You are missing the `ADMINISTRATOR` permission required to execute this command.";
    }

    @Override
    protected void execute(final SlashCommandEvent p_slashCommandEvent) {
        p_slashCommandEvent.reply(informationEmbed()).queue();
    }

    public MessageCreateData informationEmbed() {
        return new FluxEmbedBuilder()
                .setTitle("Information | Flux Solutions")
                .addField("Freelancers", String.valueOf(m_freelancerRepository.count()), false)
                .addField("Commissions", String.valueOf(m_commissionRepository.count()), false)
                .addField("Reviews", String.valueOf(m_reviewRepository.count()), false)
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .build();
    }
}
