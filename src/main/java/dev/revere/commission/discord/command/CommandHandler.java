package dev.revere.commission.discord.command;

import dev.revere.commission.discord.command.impl.*;
import dev.revere.commission.discord.command.impl.admin.*;
import dev.revere.commission.discord.command.impl.freelancer.CardCommand;
import dev.revere.commission.discord.command.impl.freelancer.SetBioCommand;
import dev.revere.commission.discord.command.impl.freelancer.SyncCommand;
import dev.revere.commission.discord.command.impl.moderation.BanCommand;
import dev.revere.commission.discord.command.impl.moderation.KickCommand;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.springframework.stereotype.Service;

/**
 * @author Revere Development
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 */
@AllArgsConstructor
@Service
@Slf4j
public class CommandHandler {
    private final SupportCommand m_supportCommand;
    private final AddFreelancerCommand m_Add_freelancerCommand;
    private final ProfileCommand m_profileCommand;
    private final AddDepartmentCommand m_addDepartmentCommand;
    private final BanCommand m_banCommand;
    private final KickCommand m_kickCommand;
    private final RemoveDepartmentCommand m_removeServiceCommand;
    private final RemoveFreelancerCommand m_removeFreelancerCommand;
    private final DeleteDepartmentCommand m_deleteDepartmentCommand;
    private final CardCommand m_cardCommand;
    private final SyncCommand m_syncCommand;
    private final SetBioCommand m_setBioCommand;
    private final CreateDepartmentCommand m_createDepartmentCommand;
    private final TonicInfoCommand m_tonicInfoCommand;

    /**
     * Get the configured CommandClient for the bot.
     *
     * @return Configured CommandClient instance.
     */
    public CommandClient getCommandClient() {
        return new CommandClientBuilder()
                .setOwnerId(1034531618951528488L)
                .setCoOwnerIds(811580599068262421L)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.watching("booting up..."))
                .addSlashCommands(
                        m_supportCommand, m_removeFreelancerCommand, m_createDepartmentCommand, m_tonicInfoCommand,
                        m_Add_freelancerCommand, m_profileCommand, m_addDepartmentCommand,
                        m_banCommand, m_kickCommand, m_removeServiceCommand,
                        m_cardCommand, m_syncCommand, m_setBioCommand, m_deleteDepartmentCommand
                )
                .build();
    }
}
