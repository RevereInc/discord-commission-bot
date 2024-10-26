package dev.revere.commission.services.impl;

import dev.revere.commission.Constants;
import dev.revere.commission.discord.JDAInitializer;
import dev.revere.commission.discord.utility.TonicEmbedBuilder;
import dev.revere.commission.entities.Commission;
import dev.revere.commission.repository.CommissionRepository;
import dev.revere.commission.services.CommissionService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/25/2024
 */
@AllArgsConstructor
@Service
public class CommissionServiceImpl implements CommissionService {
    private final CommissionRepository commissionRepository;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final HashMap<Long, ScheduledFuture<?>> deletionTasks = new HashMap<>();

    @Override
    public Commission createCommission(Member p_member, String p_string, String p_description, String p_quote) {
        Commission commission = new Commission();
        commission.setUserId(p_member.getIdLong());
        commission.setClient(p_member.getUser().getName());
        commission.setCategory(p_string);
        commission.setDescription(p_description);
        commission.setQuote(p_quote);
        commission.setPaymentPending(true);
        commission.setState(Commission.State.PENDING);
        commission.setInterestedFreelancers(new HashMap<>());
        commission.setDeclinedFreelancers(new HashMap<>());

        return commissionRepository.save(commission);
    }

    @Override
    public void approveFreelancer(Commission p_commission, Member p_freelancerMember) {
        if (p_commission == null) {
            throw new IllegalArgumentException("Commission does not exist.");
        }

        p_commission.setFreelancerId(p_freelancerMember.getIdLong());
        p_commission.setFreelancer(p_freelancerMember.getUser().getName());
        p_commission.setState(Commission.State.IN_PROGRESS);
        commissionRepository.save(p_commission);
    }

    @Override
    public void declineFreelancer(Commission p_commission, Member p_freelancerMember) {
        if (p_commission == null) {
            throw new IllegalArgumentException("Commission does not exist.");
        }

        p_commission.getInterestedFreelancers().remove(p_freelancerMember.getIdLong());
        p_commission.getDeclinedFreelancers().put(p_freelancerMember.getIdLong(), p_freelancerMember.getUser().getName());
        commissionRepository.save(p_commission);
    }

    @Override
    public void closeAndDeleteCommission(Commission p_commission, Member p_member, long p_privateChannelId, long p_publicChannelId) {
        if (p_commission == null) {
            throw new IllegalArgumentException("Commission does not exist.");
        }

        if (hasPermission(p_member, Permission.ADMINISTRATOR)) {
            deleteChannelIfExists(p_member.getJDA().getGuildById(Constants.COMMISSION_GUILD_ID).getTextChannelById(p_privateChannelId));
            deleteChannelIfExists(p_member.getJDA().getGuildById(Constants.MAIN_GUILD_ID).getTextChannelById(p_publicChannelId));
            commissionRepository.delete(p_commission);
        } else {
            throw new SecurityException("User does not have permission to delete this commission.");
        }
    }

    @Override
    public void deleteCommission(Commission p_commission, Member p_member) {
        if (p_commission == null) {
            throw new IllegalArgumentException("Commission does not exist.");
        }

        if (hasPermission(p_member, Permission.ADMINISTRATOR)) {
            commissionRepository.delete(p_commission);
        } else {
            throw new SecurityException("User does not have permission to delete this commission.");
        }
    }

    @Override
    public void scheduleDeletion(Commission p_commission, Member p_member, long p_privateChannelId, long p_publicChannelId) {
        if (p_commission == null) {
            throw new IllegalArgumentException("Commission does not exist.");
        }

        ScheduledFuture<?> commissionDeletionTask = scheduler.schedule(() -> {
            closeAndDeleteCommission(p_commission, p_member, p_privateChannelId, p_publicChannelId);
        }, 10, TimeUnit.SECONDS);

        deletionTasks.put(p_publicChannelId, commissionDeletionTask);
    }

    @Override
    public void cancelDeletion(long p_privateChannelId, long p_publicChannelId, String p_commissionId) {
        Commission p_commission = commissionRepository.findCommissionById(p_commissionId);
        if (p_commission == null) {
            throw new IllegalArgumentException("Commission does not exist.");
        }

        ScheduledFuture<?> commissionDeletionTask = deletionTasks.remove(p_publicChannelId);

        if (commissionDeletionTask != null) {
            commissionDeletionTask.cancel(false);
        }
    }

    @Override
    public void transcriptCommission(Commission p_commission, Member p_member) {
        if (p_commission == null || p_member == null) {
            throw new IllegalArgumentException("Commission or member does not exist.");
        }

        Guild guild = p_member.getJDA().getGuildById(Constants.MAIN_GUILD_ID);
        if (guild == null) {
            throw new IllegalArgumentException("Guild does not exist.");
        }

        TextChannel publicChannel = guild.getTextChannelById(p_commission.getPublicChannelId());
        if (publicChannel == null) {
            throw new IllegalArgumentException("Channel does not exist.");
        }

        Category targetCategory = guild.getCategoryById(Constants.TRANSCRIPT_CHANNEL_ID);
        if (targetCategory == null) {
            throw new IllegalArgumentException("Category does not exist.");
        }

        publicChannel.getManager().putPermissionOverride(p_member, null, List.of(Permission.VIEW_CHANNEL)).queue();
        publicChannel.getManager().setParent(targetCategory).queue();
        publicChannel.getManager().sync(targetCategory).queue();

        String description = String.format(
                """
                        This commission has been transcribed by %s.
                        ### <:RVC_Log:1299484630101262387> Commission Details
                        %s
                        ### <:RVC_Discount:1299484670098145341> Quoted Price
                        ```
                        $%s
                        ```""",
                p_member,
                p_commission.getDescription(),
                p_commission.getFormattedQuote()
        );

        publicChannel.sendMessage(TonicEmbedBuilder.sharedMessageEmbed(description)).queue();
    }

    @Override
    public void finishCommission(Commission p_commission, Member p_member) {
        if (p_commission == null) {
            throw new IllegalArgumentException("Commission does not exist.");
        }

        p_commission.setState(Commission.State.COMPLETED);
        commissionRepository.save(p_commission);
    }


    @Override
    public void receivePayment(Commission p_commission, Member p_member) {
        if (p_commission == null) {
            throw new IllegalArgumentException("Commission does not exist.");
        }

        p_commission.setPaymentPending(false);
        commissionRepository.save(p_commission);
    }


    @Override
    public void cancelCommission(Commission p_commission) {
        if (p_commission == null) {
            throw new IllegalArgumentException("Commission does not exist.");
        }

        p_commission.setState(Commission.State.CANCELLED);
        commissionRepository.save(p_commission);
    }

    @Override
    public void setQuote(Commission p_commission, String p_quote) {
        if (p_commission == null) {
            throw new IllegalArgumentException("Commission does not exist.");
        }

        p_commission.setQuote(p_quote);
        commissionRepository.save(p_commission);
    }

    @Override
    public boolean hasPermission(Member p_member, Permission p_permission) {
        return p_member.hasPermission(p_permission);
    }

    @Override
    public List<Commission> getAllCommissions() {
        return commissionRepository.findAll();
    }

    @Override
    public void deleteChannelIfExists(TextChannel p_channel) {
        if (p_channel != null) {
            p_channel.delete().queue(
                    null,
                    throwable -> {
                        throw new RuntimeException("Failed to delete channel: " + p_channel.getId(), throwable);
                    }
            );
        } else {
            throw new IllegalArgumentException("Channel does not exist.");
        }
    }
}
