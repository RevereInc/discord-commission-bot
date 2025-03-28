package dev.revere.commission.discord.command.impl.moderation;

import dev.revere.commission.discord.utility.TonicEmbedBuilder;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class KickCommand extends SlashCommand {

    public KickCommand() {

        this.name = "kick";
        this.help = "Kick a user";
        this.guildOnly = true;

        this.userPermissions = new Permission[] { Permission.ADMINISTRATOR };
        this.userMissingPermMessage = "You are missing the `ADMINISTRATOR` permission required to execute this command.";

        this.options = List.of(
                new OptionData(OptionType.USER, "user", "The user to kick").setRequired(true)
        );
    }

    @Override
    protected void execute(final SlashCommandEvent p_slashCommandEvent) {
        final User user = Objects.requireNonNull(p_slashCommandEvent.getOption("user")).getAsUser();

        Objects.requireNonNull(p_slashCommandEvent.getGuild()).kick(user).queue();
        p_slashCommandEvent.reply(successfulKickEmbed(user.getName())).queue();
    }

    public MessageCreateData successfulKickEmbed(String name) {
        return new TonicEmbedBuilder()
                .setTitle(" ")
                .setDescription(name + " has successfully been kicked")
                .setTimeStamp(Instant.now())
                .setColor(Color.decode("#2b2d31"))
                .build();
    }
}
