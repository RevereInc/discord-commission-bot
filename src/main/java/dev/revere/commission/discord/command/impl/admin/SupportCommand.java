package dev.revere.commission.discord.command.impl.admin;

import dev.revere.commission.Constants;
import dev.revere.commission.discord.utility.TonicEmbedBuilder;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.Instant;

/**
 * @author Revere Development
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 */
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
    protected void execute(SlashCommandEvent p_slashCommandEvent) {
        p_slashCommandEvent.getChannel().sendMessage(getSupportEmbed()).queue();
    }

    /**
     * Create and configure the support embed message.
     *
     * @return MessageCreateData containing the support embed.
     */
    public MessageCreateData getSupportEmbed() {
        String description = String.format(
                """
                        **Welcome to %s's Commission Channel!**
                        If you need assistance or have questions regarding your commission requests, feel free to ask here. Our team is here to help you!
                        ### <:1270446417206312980:1299806078044868719> How to Create a Commission:
                        - Click the button below to get started on your commission request.
                        - Our freelancers will assist you as soon as possible.
                        """,
                Constants.PROJECT_NAME
        );

        return new TonicEmbedBuilder()
                .setTitle(" ")
                .setDescription(description)
                .setTimeStamp(Instant.now())
                .setColor(Color.decode("#2b2d31"))
                .addButton(ButtonStyle.SECONDARY, "create-commission", "Create A Commission", Emoji.fromFormatted("<:1270446417206312980:1299806078044868719>"))
                .build();
    }

}