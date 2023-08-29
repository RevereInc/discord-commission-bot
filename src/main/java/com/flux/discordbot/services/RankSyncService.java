package com.flux.discordbot.services;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.apache.commons.collections4.BidiMap;

public interface RankSyncService {
    Role getRoleRelation(Role p_role);
    void syncMemberToRole(Member p_member, Role p_role);
    void syncMember(Member p_member);
}
