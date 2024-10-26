package dev.revere.commission.discord.command.impl.admin;

import dev.revere.commission.Constants;
import dev.revere.commission.discord.utility.TonicEmbedBuilder;
import dev.revere.commission.entities.Department;
import dev.revere.commission.entities.Freelancer;
import dev.revere.commission.repository.FreelancerRepository;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("This freelancer does not exist")).queue();
            return;
        }

        Guild mainGuild = p_slashCommandEvent.getJDA().getGuildById(Constants.MAIN_GUILD_ID);
        Guild commissionGuild = p_slashCommandEvent.getJDA().getGuildById(Constants.COMMISSION_GUILD_ID);
        if (mainGuild == null || commissionGuild == null) {
            p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Could not find guilds")).queue();
            return;
        }

        Role globalRole = Objects.requireNonNull(p_slashCommandEvent.getJDA().getRoleById(Constants.GLOBAL_FREELANCER_ROLE_ID));
        if (mainGuild.getMember(user) != null) {
            mainGuild.removeRoleFromMember(user, globalRole).queue();
        }

        for (Department department : freelancer.getDepartments()) {
            Role mainRole = Objects.requireNonNull(p_slashCommandEvent.getJDA().getRoleById(department.getMainGuildRoleId()));
            Role commissionRole = Objects.requireNonNull(p_slashCommandEvent.getJDA().getRoleById(department.getCommissionGuildRoleId()));

            if (commissionGuild.getMember(user) != null) {
                commissionGuild.removeRoleFromMember(user, commissionRole).queue();
            }

            if (mainGuild.getMember(user) != null) {
                mainGuild.removeRoleFromMember(user, mainRole).queue();
            }
        }

        m_freelancerRepository.delete(freelancer);
        p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Successfully removed " + user.getName() + " from the freelance team!")).setEphemeral(false).queue();
    }
}
