package com.flux.discordbot.discord.command.impl;

import com.flux.discordbot.discord.utility.FluxEmbedBuilder;
import com.flux.discordbot.entities.Freelancer;
import com.flux.discordbot.repository.FreelancerRepository;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
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
public class FreelancerCommand extends SlashCommand {
    private final FreelancerRepository m_freelancerRepository;

    @Autowired
    public FreelancerCommand(final FreelancerRepository p_freelancerRepository) {
        m_freelancerRepository = p_freelancerRepository;

        this.name = "freelancer";
        this.help = "Freelancer edits";
        this.guildOnly = true;

        this.userPermissions = new Permission[] { Permission.ADMINISTRATOR };
        this.userMissingPermMessage = "You are missing the `ADMINISTRATOR` permission required to execute this command.";

        // Define command options
        final List<OptionData> optionData = new ArrayList<>();
        optionData.add(new OptionData(OptionType.USER, "userid", "userid of the freelancer").setRequired(true));
        optionData.add(new OptionData(OptionType.STRING, "name", "name of the freelancer").setRequired(true));
        optionData.add(new OptionData(OptionType.ROLE, "services", "services freelancer offers").setRequired(true));
        optionData.add(new OptionData(OptionType.STRING, "bio", "bio of the freelancer").setRequired(true));

        this.options = optionData;
    }

    @Override
    protected void execute(final SlashCommandEvent p_slashCommandEvent) {
        // Retrieve command options
        final User userId = p_slashCommandEvent.getOption("userid").getAsUser();
        final String name = p_slashCommandEvent.getOption("name").getAsString();
        final Role services = p_slashCommandEvent.getOption("services").getAsRole();
        final String bio = p_slashCommandEvent.getOption("bio").getAsString();

        Freelancer existingFreelancer = m_freelancerRepository.findFreelancerByUserId(userId.getIdLong());

        if (existingFreelancer != null) {
            p_slashCommandEvent.reply(alreadyExists(name)).setEphemeral(false).queue();
        } else {
            // Create a new Freelancer entity
            final Freelancer freelancer = new Freelancer();
            freelancer.setName(name);
            freelancer.setUserId(userId.getIdLong());
            freelancer.setBio(bio);
            freelancer.setServiceRoleIds(List.of(Long.valueOf(services.getId())));

            // Save the Freelancer entity to the repository
            m_freelancerRepository.save(freelancer);

            // Send a response message
            p_slashCommandEvent.reply(createdFreelancer(name)).setEphemeral(false).queue();
        }
    }

    /**
     * Create and configure the created freelancer message.
     *
     * @return MessageCreateData containing the support embed.
     */
    public MessageCreateData createdFreelancer(String name) {
        return new FluxEmbedBuilder()
                .setTitle("Freelancer | Flux Solutions")
                .setDescription("Successfully created freelancer, ``" + name + "``.")
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .build();
    }

    /**
     * Create and configure the created freelancer message.
     *
     * @return MessageCreateData containing the support embed.
     */
    public MessageCreateData alreadyExists(String name) {
        return new FluxEmbedBuilder()
                .setTitle("Freelancer | Flux Solutions")
                .setDescription("A freelancer with the name of, ``" + name + "`` already exists.")
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .build();
    }
}
