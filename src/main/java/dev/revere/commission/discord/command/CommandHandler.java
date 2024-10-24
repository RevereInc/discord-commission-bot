package dev.revere.commission.discord.command;

import dev.revere.commission.discord.command.impl.*;
import dev.revere.commission.repository.CommissionRepository;
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
    private final InfoCommand m_infoCommand;
    private final AddDepartmentCommand m_addDepartmentCommand;
    private final BanCommand m_banCommand;
    private final KickCommand m_kickCommand;
    private final RemoveDepartmentCommand m_removeServiceCommand;
    private final RemoveFreelancerCommand m_removeFreelancerCommand;
    private final CardCommand m_cardCommand;
    private final SyncCommand m_syncCommand;
    private final CreateDepartmentCommand m_createDepartmentCommand;
    private final FluxInfoCommand m_fluxInfoCommand;

    private final CommissionRepository m_commissionRepository;

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
                .setActivity(Activity.watching(m_commissionRepository.count() + " commissions"))
                .addSlashCommands(
                        m_supportCommand, m_removeFreelancerCommand, m_createDepartmentCommand, m_fluxInfoCommand,
                        m_Add_freelancerCommand, m_infoCommand, m_addDepartmentCommand,
                        m_banCommand, m_kickCommand, m_removeServiceCommand,
                        m_cardCommand, m_syncCommand
                )
                .build();
    }
}
