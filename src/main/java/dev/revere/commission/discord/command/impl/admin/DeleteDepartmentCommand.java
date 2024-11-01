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

        m_departmentService.deleteDepartment(departmentName);
        m_departmentService.deleteDepartmentRoles(departmentName, guild.getJDA().getShardManager());
        p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Department `" + departmentName + "` has been deleted.")).queue();
    }
}
