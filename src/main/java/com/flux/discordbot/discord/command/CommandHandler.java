package com.flux.discordbot.discord.command;

import com.flux.discordbot.discord.command.impl.FreelancerCommand;
import com.flux.discordbot.discord.command.impl.SupportCommand;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class CommandHandler {
    private final SupportCommand m_supportCommand;
    private final FreelancerCommand m_freelancerCommand;


    public CommandClient getCommandClient() {
        return new CommandClientBuilder()
                .setOwnerId(1034531618951528488L)
                .setCoOwnerIds(811580599068262421L)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.watching("x customers"))
                .addSlashCommands(m_supportCommand, m_freelancerCommand)
                .build();
    }
}
