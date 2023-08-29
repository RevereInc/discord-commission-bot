package com.flux.discordbot.services;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RankSyncServiceImpl implements RankSyncService {
    private static final BidiMap<Long, Long> roleRelationBidiMap = new DualHashBidiMap<>();

    private static final long mainGuildId = 1139719606186020904L;
    private static final long commissionGuildId = 1141049396453187690L;

    static {
        // MAIN SERVER ROLE ID - COMMISSION SERVER ROLE ID
        roleRelationBidiMap.put(1140378574726123620L, 1141049396901986463L); // Data pack Development
    }

    @Override
    public Role getRoleRelation(final Role p_role) {
        final long roleId = p_role.getIdLong();

        if (roleRelationBidiMap.containsKey(roleId)) {
            return p_role.getJDA().getRoleById(
                    roleRelationBidiMap.get(roleId)
            );
        }
        if (roleRelationBidiMap.containsValue(roleId)) {
            return p_role.getJDA().getRoleById(
                    roleRelationBidiMap.getKey(roleId)
            );
        }
        return null;
    }

    @Override
    public void syncMemberToRole(final Member p_member, final Role p_role) {
        final Role roleRelation = getRoleRelation(p_role);

        if (roleRelation == null) {
            return;
        }

        final Guild mainGuild = p_member.getJDA().getGuildById(mainGuildId);
        final List<Role> mainGuildRoles = mainGuild.getRoles();
        final Guild commissionGuild = p_member.getJDA().getGuildById(commissionGuildId);
        final List<Role> commissionGuildRoles = commissionGuild.getRoles();

        if (mainGuildRoles.contains(p_role)) {
            mainGuild.addRoleToMember(p_member, p_role).queue();
        }

        if (commissionGuildRoles.contains(p_role)) {
            commissionGuild.addRoleToMember(p_member, p_role).queue();
        }

        if (mainGuildRoles.contains(roleRelation)) {
            mainGuild.addRoleToMember(p_member, roleRelation).queue();
        }

        if (commissionGuildRoles.contains(roleRelation)) {
            commissionGuild.addRoleToMember(p_member, roleRelation).queue();
        }
    }

    @Override
    public void syncMember(final Member p_member) {
        for (final Role role : p_member.getRoles()) {
            this.syncMemberToRole(p_member, role);
        }
    }
}
