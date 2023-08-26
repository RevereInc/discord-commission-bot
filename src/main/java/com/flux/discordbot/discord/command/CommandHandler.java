package com.flux.discordbot.discord.command;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.SlashCommand;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import org.reflections.Reflections;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class CommandHandler {
    @Getter
    public CommandClient m_commandClient;

    public CommandHandler() throws InstantiationException, IllegalAccessException {
        final List<SlashCommand> commands = scanCommands("com.flux.discordbot.discord.command.impl");

        m_commandClient = new CommandClientBuilder()
                .setOwnerId(1034531618951528488L)
                .setCoOwnerIds(811580599068262421L)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.watching("x customers"))
                .addSlashCommands(commands.toArray(new SlashCommand[0]))
                .build();
    }

    private List<SlashCommand> scanCommands(final String p_packageName) throws InstantiationException, IllegalAccessException {
        final List<SlashCommand> commands = new ArrayList<>();

        final Reflections reflections = new Reflections(p_packageName);
        final Set<Class<? extends SlashCommand>> commandClasses = reflections.getSubTypesOf(SlashCommand.class);

        for (final Class<? extends SlashCommand> commandClass : commandClasses) {
            try {
                commands.add(commandClass.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return commands;
    }
}
