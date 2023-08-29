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
        roleRelationBidiMap.put(1140377996889424034L, 1141049396901986462L); // Plugin Development
        roleRelationBidiMap.put(1140377990262427649L, 1141049396901986461L); // Client Development
        roleRelationBidiMap.put(1140377992833544333L, 1141049396901986460L); // Game Development
        roleRelationBidiMap.put(1140377996386115764L, 1141049396901986459L); // Web & App Design
        roleRelationBidiMap.put(1140378309813862441L, 1141049396901986458L); // App Development
        roleRelationBidiMap.put(1140378632586543114L, 1141049396901986457L); // Full-Stack Development
        roleRelationBidiMap.put(1140378630673936566L, 1141049396901986456L); // Front-End Development
        roleRelationBidiMap.put(1140378631600873683L, 1141049396872630361L); // Backend Development
        roleRelationBidiMap.put(1140377994951663726L, 1141049396872630360L); // Model Designer
        roleRelationBidiMap.put(1140376933222322186L, 1141049396872630359L); // GFX Designer
        roleRelationBidiMap.put(1140377991621382245L, 1141049396872630358L); // Video Editor
        roleRelationBidiMap.put(1140378633769324585L, 1141049396872630357L); // 3D Modeller
        roleRelationBidiMap.put(1140378918520623164L, 1141049396872630356L); // 3D Design
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
