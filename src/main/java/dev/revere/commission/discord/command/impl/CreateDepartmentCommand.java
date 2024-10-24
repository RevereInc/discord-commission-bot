package dev.revere.commission.discord.command.impl;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.revere.commission.discord.JDAInitializer;
import dev.revere.commission.entities.Department;
import dev.revere.commission.repository.DepartmentRepository;
import dev.revere.commission.services.DepartmentService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.EnumSet;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/24/2024
 */
@Service
public class CreateDepartmentCommand extends SlashCommand {
    private final DepartmentService m_departmentService;
    private final DepartmentRepository m_departmentRepository;

    @Autowired
    public CreateDepartmentCommand(DepartmentService p_departmentService, DepartmentRepository p_departmentRepository) {
        m_departmentService = p_departmentService;
        m_departmentRepository = p_departmentRepository;

        this.name = "createdepartment";
        this.help = "Creates a new department";
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
            p_slashCommandEvent.reply("This command must be run in a server.").setEphemeral(true).queue();
            return;
        }

        if (m_departmentRepository.existsDepartmentByName(departmentName)) {
            p_slashCommandEvent.reply("A department with the name `" + departmentName + "` already exists.").queue();
            return;
        }

        createRolesAndPermissions(guild.getJDA(), departmentName, p_slashCommandEvent);
    }

    private void createRolesAndPermissions(JDA jda, String departmentName, SlashCommandInteractionEvent event) {
        Guild mainGuild = jda.getGuildById(JDAInitializer.mainGuildID);

        if (mainGuild == null) {
            event.reply("Failed to get main guild.").queue();
            return;
        }

        mainGuild.createRole()
                .setName(departmentName)
                .setPermissions(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)
                .queue(mainRole -> {
                    Guild commissionGuild = mainGuild.getJDA().getGuildById(JDAInitializer.commissionGuildID);
                    if (commissionGuild == null) {
                        event.reply("Failed to get commission guild.").queue();
                        return;
                    }

                    commissionGuild.createRole()
                            .setName(departmentName)
                            .setPermissions(Permission.VIEW_CHANNEL, Permission.MANAGE_CHANNEL)
                            .queue(commissionRole -> {
                                mainGuild.createCategory(departmentName).queue(categoryInMainGuild -> {
                                    categoryInMainGuild.getManager()
                                            .putPermissionOverride(mainRole, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
                                            .queue(mainSuccess -> {
                                                categoryInMainGuild.getManager()
                                                        .putPermissionOverride(mainGuild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                                                        .queue();

                                                commissionGuild.createCategory(departmentName).queue(categoryInCommissionGuild -> {
                                                    categoryInCommissionGuild.getManager()
                                                            .putPermissionOverride(commissionRole, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
                                                            .queue(success -> {
                                                                categoryInCommissionGuild.getManager()
                                                                        .putPermissionOverride(commissionGuild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                                                                        .queue();

                                                                Department department = new Department();
                                                                department.setName(departmentName);
                                                                department.setMainGuildRoleId(mainRole.getIdLong());
                                                                department.setCommissionGuildRoleId(commissionRole.getIdLong());
                                                                department.setMainGuildCategoryID(categoryInMainGuild.getIdLong());
                                                                department.setCommissionGuildCategoryID(categoryInCommissionGuild.getIdLong());

                                                                m_departmentService.createDepartment(department);
                                                                event.reply("Department `" + departmentName + "` has been created successfully.").queue();
                                                            }, failure -> {
                                                                event.reply("Failed to set permissions in commission guild category: " + failure.getMessage()).queue();
                                                            });
                                                }, failure -> {
                                                    event.reply("Failed to create category in commission guild: " + failure.getMessage()).queue();
                                                });
                                            }, mainFailure -> {
                                                event.reply("Failed to set permissions in main guild category: " + mainFailure.getMessage()).queue();
                                            });
                                }, failure -> {
                                    event.reply("Failed to create category in main guild: " + failure.getMessage()).queue();
                                });
                            });
                });
    }
}
