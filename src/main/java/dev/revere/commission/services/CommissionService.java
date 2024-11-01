package dev.revere.commission.services;

import dev.revere.commission.entities.Commission;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/25/2024
 */
import java.util.List;

public interface CommissionService {
    Commission createCommission(Member p_member, String p_string, String p_description, String p_quote);

    void approveFreelancer(Commission p_commission, Member p_freelancerMember);

    void declineFreelancer(Commission p_commission, Member p_freelancerMember);

    void deleteCommission(Commission p_commission, Member p_member);

    void closeAndDeleteCommission(Commission p_commission, Member p_member, long p_publicChannelId, long p_privateChannelId);

    void transcriptCommission(Commission p_commission, Member p_member);

    void setQuote(Commission p_commission, String p_quote);

    boolean hasPermission(Member p_member, Permission p_permission);

    void scheduleDeletion(Commission p_commission, Member p_member, long p_privateChannelId, long p_publicChannelId);

    void cancelDeletion(long p_privateChannelId, long p_publicChannelId, String p_commissionId);

    void finishCommission(Commission p_commission, Member p_member);

    void cancelCommission(Commission p_commission);

    void deleteChannelIfExists(TextChannel p_textChannel);

    List<Commission> getAllCommissions();
}