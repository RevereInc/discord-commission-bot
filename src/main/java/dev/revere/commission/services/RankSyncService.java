package dev.revere.commission.services;

import dev.revere.commission.entities.Department;
import dev.revere.commission.entities.Freelancer;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.apache.commons.collections4.BidiMap;

import java.util.List;

public interface RankSyncService {
    /**
     * Synchronize a member to a specific department role in both the main and commission guilds.
     *
     * @param p_member   The Member object to synchronize.
     * @param p_department The Department object containing role information.
     */
    void syncMemberToDepartment(final Member p_member, final Department p_department);

    /**
     * Synchronize a freelancer to all departments they are associated with.
     *
     * @param p_member The Member object to synchronize.
     * @param freelancer The Freelancer object containing department information.
     */
    void syncMember(Member p_member, Freelancer freelancer);
}
