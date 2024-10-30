package dev.revere.commission.services.impl;

import dev.revere.commission.Constants;
import dev.revere.commission.entities.Department;
import dev.revere.commission.entities.Freelancer;
import dev.revere.commission.services.RankSyncService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RankSyncServiceImpl implements RankSyncService {
    /**
     * Synchronize a member to a specific department role in both the main and commission guilds.
     *
     * @param p_member     The Member object to synchronize.
     * @param p_department The Department object containing role information.
     */
    @Override
    public void syncMemberToDepartment(final Member p_member, final Department p_department) {
        final Guild mainGuild = p_member.getJDA().getGuildById(Constants.MAIN_GUILD_ID);
        final Guild commissionGuild = p_member.getJDA().getGuildById(Constants.COMMISSION_GUILD_ID);

        if (mainGuild == null || commissionGuild == null) {
            return;
        }

        Role mainGuildRole = mainGuild.getRoleById(p_department.getMainGuildRoleId());
        Role globalRole = mainGuild.getRoleById(Constants.GLOBAL_FREELANCER_ROLE_ID);
        Role commissionGuildRole = commissionGuild.getRoleById(p_department.getCommissionGuildRoleId());

        if (mainGuildRole != null && !p_member.getRoles().contains(mainGuildRole)) {
            mainGuild.addRoleToMember(p_member, mainGuildRole).queue();
        }

        if (globalRole != null && !p_member.getRoles().contains(globalRole)) {
            mainGuild.addRoleToMember(p_member, globalRole).queue();
        }

        if (commissionGuildRole != null && !p_member.getRoles().contains(commissionGuildRole)) {
            commissionGuild.addRoleToMember(p_member, commissionGuildRole).queue();
        }
    }


    /**
     * Synchronize a freelancer to all departments they are associated with.
     *
     * @param p_member   The Member object to synchronize.
     * @param freelancer The Freelancer object containing department information.
     */
    @Override
    public void syncMember(final Member p_member, Freelancer freelancer) {
        List<Department> departments = freelancer.getDepartments();

        // Loop over only the departments the freelancer is associated with
        for (final Department department : departments) {
            syncMemberToDepartment(p_member, department);
        }
    }
}
