package dev.revere.commission.discord.command.impl.admin;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.revere.commission.Constants;
import dev.revere.commission.discord.utility.TonicEmbedBuilder;
import dev.revere.commission.entities.Department;
import dev.revere.commission.repository.DepartmentRepository;
import dev.revere.commission.services.DepartmentService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/24/2024
 */
@Service
public class DeleteDepartmentCommand extends SlashCommand {
    private final DepartmentService m_departmentService;
    private final DepartmentRepository m_departmentRepository;

    @Autowired
    public DeleteDepartmentCommand(DepartmentService p_departmentService, DepartmentRepository p_departmentRepository) {
        m_departmentService = p_departmentService;
        m_departmentRepository = p_departmentRepository;

        this.name = "deletedepartment";
        this.help = "Deletes a department";
        this.guildOnly = true;
        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.userMissingPermMessage = "You are missing the `ADMINISTRATOR` permission required to execute this command.";

        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "name", "The name of the department").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent p_slashCommandEvent) {
        String departmentName = p_slashCommandEvent.getOption("name").getAsString();
        Guild guild = p_slashCommandEvent.getGuild();

        if (guild == null) {
            p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("This command must be run in a server.")).setEphemeral(true).queue();
            return;
        }

        if (!m_departmentRepository.existsDepartmentByName(departmentName)) {
            p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("A department with the name `" + departmentName + "` does not exist.")).queue();
            return;
        }

        deleteDepartmentRole(guild.getJDA(), departmentName, p_slashCommandEvent);
    }

    private void deleteDepartmentRole(JDA jda, String departmentName, SlashCommandInteractionEvent event) {
        Department department = m_departmentRepository.findDepartmentByName(departmentName);
        if (department == null) {
            event.reply(TonicEmbedBuilder.sharedMessageEmbed("Failed to get department.")).queue();
            return;
        }
        m_departmentRepository.delete(department);

        Guild mainGuild = jda.getGuildById(Constants.MAIN_GUILD_ID);
        if (mainGuild != null) {
            Role mainRole = mainGuild.getRoleById(String.valueOf(department.getMainGuildRoleId()));
            if (mainRole != null) {
                mainRole.delete().queue(success -> {
                    event.getChannel().sendMessage(TonicEmbedBuilder.sharedMessageEmbed("Role `" + departmentName + "` has been deleted from the main guild.")).queue();
                }, failure -> event.getChannel().sendMessage(TonicEmbedBuilder.sharedMessageEmbed("Failed to delete role in main guild: " + failure.getMessage())).queue());
            } else {
                event.getChannel().sendMessage(TonicEmbedBuilder.sharedMessageEmbed("Role `" + departmentName + "` not found in the main guild.")).queue();
            }
        } else {
            event.getChannel().sendMessage(TonicEmbedBuilder.sharedMessageEmbed("Failed to get main guild.")).queue();
        }

        Guild commissionGuild = jda.getGuildById(Constants.COMMISSION_GUILD_ID);
        if (commissionGuild != null) {
            Role commissionRole = commissionGuild.getRoleById(String.valueOf(department.getCommissionGuildRoleId()));
            if (commissionRole != null) {
                commissionRole.delete().queue(success -> {
                    event.getChannel().sendMessage(TonicEmbedBuilder.sharedMessageEmbed("Role `" + departmentName + "` has been deleted from the commission guild.")).queue();
                }, failure -> event.getChannel().sendMessage(TonicEmbedBuilder.sharedMessageEmbed("Failed to delete role in commission guild: " + failure.getMessage())).queue());
            } else {
                event.getChannel().sendMessage(TonicEmbedBuilder.sharedMessageEmbed("Role `" + departmentName + "` not found in the commission guild.")).queue();
            }
        } else {
            event.getChannel().sendMessage(TonicEmbedBuilder.sharedMessageEmbed("Failed to get commission guild.")).queue();
        }

        event.getChannel().sendMessage(TonicEmbedBuilder.sharedMessageEmbed("Department `" + departmentName + "` has been deleted.")).queue();
    }
}
