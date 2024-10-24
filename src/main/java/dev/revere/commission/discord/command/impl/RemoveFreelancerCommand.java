package dev.revere.commission.discord.command.impl;

import dev.revere.commission.discord.JDAInitializer;
import dev.revere.commission.discord.utility.FluxEmbedBuilder;
import dev.revere.commission.entities.Department;
import dev.revere.commission.entities.Freelancer;
import dev.revere.commission.repository.FreelancerRepository;
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
import java.util.Objects;

/**
 * @author Revere Development
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
        optionData.add(new OptionData(OptionType.USER, "user", "the freelancer").setRequired(true));

        this.options = optionData;
    }

    @Override
    protected void execute(final SlashCommandEvent p_slashCommandEvent) {
        final User user = p_slashCommandEvent.getOption("user").getAsUser();

        Freelancer freelancer = m_freelancerRepository.findFreelancerByUserId(user.getIdLong());
        if (freelancer == null) {
            p_slashCommandEvent.reply("This freelancer does not exist").queue();
            return;
        }

        for (Department department : freelancer.getDepartments()) {
            Role mainRole = Objects.requireNonNull(p_slashCommandEvent.getJDA().getRoleById(department.getMainGuildRoleId()));
            Role commissionRole = Objects.requireNonNull(p_slashCommandEvent.getJDA().getRoleById(department.getCommissionGuildRoleId()));

            Objects.requireNonNull(p_slashCommandEvent.getJDA().getGuildById(JDAInitializer.mainGuildID)).removeRoleFromMember(user, mainRole).queue();
            Objects.requireNonNull(p_slashCommandEvent.getJDA().getGuildById(JDAInitializer.commissionGuildID)).removeRoleFromMember(user, commissionRole).queue();
        }

        m_freelancerRepository.delete(freelancer);
        p_slashCommandEvent.reply(deletedFreelancer(user.getName())).setEphemeral(false).queue();
    }

    /**
     * Create and configure the deleted freelancer message.
     *
     * @return MessageCreateData containing the support embed.
     */
    public MessageCreateData deletedFreelancer(String name) {
        return new FluxEmbedBuilder()
                .setTitle("Freelancer | Flux Solutions")
                .setDescription(name + " has successfully been removed from the team")
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .build();
    }
}
