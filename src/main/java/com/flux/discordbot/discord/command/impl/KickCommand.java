package com.flux.discordbot.discord.command.impl;

import com.flux.discordbot.discord.utility.FluxEmbedBuilder;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class KickCommand extends SlashCommand {

    @Autowired
    public KickCommand() {

        this.name = "kick";
        this.help = "Kick a user";
        this.guildOnly = true;

        this.userPermissions = new Permission[] { Permission.ADMINISTRATOR };
        this.userMissingPermMessage = "You are missing the `ADMINISTRATOR` permission required to execute this command.";

        // Define command options
        this.options = List.of(
                new OptionData(OptionType.USER, "user", "The user to kick").setRequired(true)
        );
    }

    @Override
    protected void execute(final SlashCommandEvent p_slashCommandEvent) {
        final User user = Objects.requireNonNull(p_slashCommandEvent.getOption("user")).getAsUser();

        if(p_slashCommandEvent.getGuild() == null) { // this is never gonna happen but intelliJ is annoying me
            return;
        }
        p_slashCommandEvent.getGuild().kick(user).queue();
        p_slashCommandEvent.reply(successfulKickEmbed(user.getName())).queue();
    }

    public MessageCreateData successfulKickEmbed(String name) {
        return new FluxEmbedBuilder()
                .setTitle("Punishment | Flux Solutions")
                .setDescription(name + " has successfully been kicked")
                .setTimeStamp(Instant.now())
                .setColor(-1)
                .build();
    }
}
