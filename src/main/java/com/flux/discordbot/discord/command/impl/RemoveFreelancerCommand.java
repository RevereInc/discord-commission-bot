package com.flux.discordbot.discord.command.impl;

import com.flux.discordbot.discord.utility.FluxEmbedBuilder;
import com.flux.discordbot.entities.Freelancer;
import com.flux.discordbot.repository.FreelancerRepository;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Flux
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 */
@Service
public class RemoveFreelancerCommand extends SlashCommand {
    private final FreelancerRepository m_freelancerRepository;

    @Autowired
    public RemoveFreelancerCommand(final FreelancerRepository p_freelancerRepository) {
        m_freelancerRepository = p_freelancerRepository;

        this.name = "removefreelancer";
        this.help = "Remove a freelancer";
        this.guildOnly = true;

        this.userPermissions = new Permission[] { Permission.ADMINISTRATOR };
        this.userMissingPermMessage = "You are missing the `ADMINISTRATOR` permission required to execute this command.";

        // Define command options
        final List<OptionData> optionData = new ArrayList<>();
        optionData.add(new OptionData(OptionType.USER, "userid", "userid of the freelancer").setRequired(true));

        this.options = optionData;
    }

    @Override
    protected void execute(final SlashCommandEvent p_slashCommandEvent) {
        // Retrieve command options
        final User userId = p_slashCommandEvent.getOption("userid").getAsUser();

        Freelancer freelancer = m_freelancerRepository.findFreelancerByUserId(userId.getIdLong());

        if (freelancer == null) {
            p_slashCommandEvent.reply("This freelancer does not exist").queue();
            return;
        }
        m_freelancerRepository.delete(freelancer);
        p_slashCommandEvent.reply(deletedFreelancer(name)).setEphemeral(false).queue();
    }

    /**
     * Create and configure the deleted freelancer message.
     *
     * @return MessageCreateData containing the support embed.
     */
    public MessageCreateData deletedFreelancer(String name) {
        return new FluxEmbedBuilder()
                .setTitle("Freelancer | Flux Solutions")
                .setDescription("Successfully deleted freelancer, ``" + name + "``.")
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .build();
    }
}
