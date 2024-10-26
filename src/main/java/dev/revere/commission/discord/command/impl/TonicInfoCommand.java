package dev.revere.commission.discord.command.impl;

import dev.revere.commission.discord.utility.TonicEmbedBuilder;
import dev.revere.commission.repository.CommissionRepository;
import dev.revere.commission.repository.FreelancerRepository;
import dev.revere.commission.repository.ReviewRepository;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.Instant;

@Service
public class TonicInfoCommand extends SlashCommand {

    private final CommissionRepository m_commissionRepository;
    private final FreelancerRepository m_freelancerRepository;
    private final ReviewRepository m_reviewRepository;

    @Autowired
    public TonicInfoCommand(final CommissionRepository p_commissionRepository, final FreelancerRepository p_freelancerRepository, final ReviewRepository p_reviewRepository) {
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
        return new TonicEmbedBuilder()
                .setTitle(" ")
                .addField("Freelancers", String.valueOf(m_freelancerRepository.count()), false)
                .addField("Commissions", String.valueOf(m_commissionRepository.count()), false)
                .addField("Reviews", String.valueOf(m_reviewRepository.count()), false)
                .setTimeStamp(Instant.now())
                .setColor(Color.decode("#2b2d31"))
                .build();
    }
}
