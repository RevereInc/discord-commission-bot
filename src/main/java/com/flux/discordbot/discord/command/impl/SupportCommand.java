package com.flux.discordbot.discord.command.impl;

import com.flux.discordbot.discord.utility.FluxEmbedBuilder;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SupportCommand extends SlashCommand {

    public SupportCommand() {
        this.name = "support";
        this.help = "Send the support embed";
        this.guildOnly = true;

        this.userPermissions = new Permission[] { Permission.ADMINISTRATOR };
        this.userMissingPermMessage = "You are missing the `ADMINISTRATOR` permission required to execute this command.";
    }

    @Override
    protected void execute(SlashCommandEvent slashCommandEvent) {
        slashCommandEvent.getChannel().sendMessage(getSupportEmbed()).queue();
    }

    public MessageCreateData getSupportEmbed() {
        return new FluxEmbedBuilder()
            .setTitle("Support | Flux Solutions")
            .setDescription("Welcome to our support channel! If you need assistance or have questions, feel free to ask here.")
            .setTimeStamp(Instant.now())
            .setColor(-1)
            .addButton(ButtonStyle.PRIMARY, "create-commission", "Create A Commission", Emoji.fromUnicode("U+1F3AB"))
            .build();
    }
}