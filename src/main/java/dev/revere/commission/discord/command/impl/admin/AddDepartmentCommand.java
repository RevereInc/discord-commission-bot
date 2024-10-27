package dev.revere.commission.discord.command.impl.admin;

import dev.revere.commission.Constants;
import dev.revere.commission.discord.utility.TonicEmbedBuilder;
import dev.revere.commission.entities.Department;
import dev.revere.commission.entities.Freelancer;
import dev.revere.commission.repository.FreelancerRepository;
import dev.revere.commission.services.DepartmentService;
import dev.revere.commission.services.FreelancerService;
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
import java.util.Objects;

@Service
public class AddDepartmentCommand extends SlashCommand {
    private final FreelancerRepository m_freelancerRepository;
    private final FreelancerService m_freelancerService;
    private final DepartmentService m_departmentService;
    @Autowired
    public AddDepartmentCommand(final FreelancerRepository p_freelancerRepository, final FreelancerService p_freelancerService, final DepartmentService p_departmentService) {
        m_freelancerRepository = p_freelancerRepository;
        m_freelancerService = p_freelancerService;
        m_departmentService = p_departmentService;

        this.name = "adddepartment";
        this.help = "add department to freelancer";
        this.guildOnly = true;

        this.userPermissions = new Permission[] { Permission.ADMINISTRATOR };
        this.userMissingPermMessage = "You are missing the `ADMINISTRATOR` permission required to execute this command.";

        this.options = List.of(
                new OptionData(OptionType.USER, "user", "the freelancer").setRequired(true),
                new OptionData(OptionType.ROLE, "department", "department to add").setRequired(true)
        );
    }

    @Override
    protected void execute(final SlashCommandEvent p_slashCommandEvent) {
        final User user = p_slashCommandEvent.getOption("user").getAsUser();
        final Role role = p_slashCommandEvent.getOption("department").getAsRole();

        if (!m_freelancerRepository.existsFreelancerByUserId(user.getIdLong())) {
            p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Could not find freelancer with name " + user.getName())).queue();
            return;
        }

        final Freelancer freelancer = m_freelancerRepository.findFreelancerByUserId(user.getIdLong());
        final Department department = m_departmentService.getDepartmentFromRole(role);

        if (department == null) {
            p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Could not find department with name " + role.getName())).queue();
            return;
        }

        if (freelancer.getDepartments().contains(department)) {
            p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Freelancer already has department")).queue();
            return;
        }

        m_freelancerService.addDepartment(freelancer, department);

        Role mainRole = Objects.requireNonNull(p_slashCommandEvent.getJDA().getRoleById(department.getMainGuildRoleId()));
        Role commissionRole = Objects.requireNonNull(p_slashCommandEvent.getJDA().getRoleById(department.getCommissionGuildRoleId()));

        Objects.requireNonNull(p_slashCommandEvent.getJDA().getGuildById(Constants.MAIN_GUILD_ID)).addRoleToMember(user, mainRole).queue();
        Objects.requireNonNull(p_slashCommandEvent.getJDA().getGuildById(Constants.COMMISSION_GUILD_ID)).addRoleToMember(user, commissionRole).queue();

        p_slashCommandEvent.reply(TonicEmbedBuilder.sharedMessageEmbed("Added `" + user.getName() + "` to the `" + department.getName() + "` department")).queue();
    }
}
