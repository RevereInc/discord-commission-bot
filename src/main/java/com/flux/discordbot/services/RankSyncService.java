package com.flux.discordbot.services;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.apache.commons.collections4.BidiMap;

public interface RankSyncService {
    /**
     * Get the role relation for a given role.
     *
     * @param p_role The Role object for which the role relation is retrieved.
     * @return The corresponding Role object representing the role relation, or null if not found.
     */
    Role getRoleRelation(Role p_role);

    /**
     * Synchronize a member to a specific role.
     *
     * @param p_member The Member object to synchronize.
     * @param p_role   The Role object to which the member should be synchronized.
     */
    void syncMemberToRole(Member p_member, Role p_role);

    /**
     * Synchronize a member to their associated role based on the role relation.
     *
     * @param p_member The Member object to synchronize.
     */
    void syncMember(Member p_member);
}
