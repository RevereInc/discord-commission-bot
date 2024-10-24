package dev.revere.commission.discord.command.impl;

import dev.revere.commission.discord.JDAInitializer;
import dev.revere.commission.discord.utility.FluxEmbedBuilder;
import dev.revere.commission.entities.Department;
import dev.revere.commission.entities.Freelancer;
import dev.revere.commission.repository.FreelancerRepository;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.revere.commission.services.DepartmentService;
import dev.revere.commission.services.FreelancerService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
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
public class AddFreelancerCommand extends SlashCommand {
    private final FreelancerRepository m_freelancerRepository;
    private final FreelancerService m_freelancerService;
    private final DepartmentService m_departmentService;
    @Autowired
    public AddFreelancerCommand(final FreelancerRepository p_freelancerRepository, final FreelancerService p_freelancerService, final DepartmentService p_departmentService) {
        m_freelancerRepository = p_freelancerRepository;
        m_freelancerService = p_freelancerService;
        m_departmentService = p_departmentService;

        this.name = "addfreelancer";
        this.help = "Add a freelancer to the database";
        this.guildOnly = true;

        this.userPermissions = new Permission[] { Permission.ADMINISTRATOR };
        this.userMissingPermMessage = "You are missing the `ADMINISTRATOR` permission required to execute this command.";

        final List<OptionData> optionData = new ArrayList<>();
        optionData.add(new OptionData(OptionType.USER, "user", "the person to become a freelancer").setRequired(true));
        optionData.add(new OptionData(OptionType.ROLE, "department", "the department to put the person in").setRequired(true));

        this.options = optionData;
    }

    @Override
    protected void execute(final SlashCommandEvent p_slashCommandEvent) {
        final Member member = Objects.requireNonNull(p_slashCommandEvent.getOption("user")).getAsMember();
        final Role departmentRole = Objects.requireNonNull(p_slashCommandEvent.getOption("department")).getAsRole();

        if (member == null) {
            p_slashCommandEvent.reply("User not found").setEphemeral(true).queue();
            return;
        }

        final Freelancer existingFreelancer = m_freelancerRepository.findFreelancerByUserId(member.getUser().getIdLong());
        final Department department = m_departmentService.getDepartmentFromRole(departmentRole);

        if (department == null) {
            p_slashCommandEvent.reply("Department not found").setEphemeral(true).queue();
            return;
        }

        if (existingFreelancer != null) {
            p_slashCommandEvent.reply(alreadyExists(member.getUser().getName())).setEphemeral(false).queue();
            return;
        }

        final Freelancer freelancer = new Freelancer();
        freelancer.setName(member.getUser().getName());
        freelancer.setUserId(member.getUser().getIdLong());
        freelancer.setBio("");

        m_freelancerService.addDepartment(freelancer, department);
        m_freelancerRepository.save(freelancer);

        Role mainRole = Objects.requireNonNull(p_slashCommandEvent.getJDA().getRoleById(department.getMainGuildRoleId()));
        Role commissionRole = Objects.requireNonNull(p_slashCommandEvent.getJDA().getRoleById(department.getCommissionGuildRoleId()));

        Objects.requireNonNull(p_slashCommandEvent.getJDA().getGuildById(JDAInitializer.mainGuildID)).addRoleToMember(member, mainRole).queue();
        Objects.requireNonNull(p_slashCommandEvent.getJDA().getGuildById(JDAInitializer.commissionGuildID)).addRoleToMember(member, commissionRole).queue();

        p_slashCommandEvent.reply(createdFreelancer(freelancer.getName())).setEphemeral(false).queue();
    }

    /**
     * Create and configure the created freelancer message.
     *
     * @return MessageCreateData containing the support embed.
     */
    public MessageCreateData createdFreelancer(String name) {
        return new FluxEmbedBuilder()
                .setTitle("Freelancer | Flux Solutions")
                .setDescription(name + " has successfully been added as a freelancer")
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
