package com.flux.discordbot.discord.event;

import com.flux.discordbot.discord.utility.FluxEmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.awt.*;

@Service
public class GuildJoinEvent extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        // Get the newly joined member
        final Member member = event.getMember();

        // Get the guild the member joined
        final Guild guild = event.getGuild();

        Role role = event.getGuild().getRoleById("1140379900713369702");
        if (role != null) {
            event.getGuild().addRoleToMember(event.getMember(), role).queue();
        } else {
            System.out.println("Role not found");
        }

        // Retrieve the channel by its configuration value
        TextChannel channel = guild.getJDA().getTextChannelById("1140420352451809421");

        if (channel != null) {
            channel.sendMessage(welcomeEmbed(member, guild)).queue();
        }
    }

    public MessageCreateData welcomeEmbed(Member p_member, Guild p_guild) {
        return new FluxEmbedBuilder()
                .setTitle("Welcome " + p_member.getEffectiveName() + " (@" + p_member.getUser().getName() + ") to " + p_guild.getName())
                .setDescription("Welcome to the official Hysteria Network discord server!" +
                        "\n\nThis is a enlarging freelancing business which provides you with your needs!" +
                        "\n\nWe are now at **" + p_guild.getMembers().size() + "** members!")
                .setImage("https://cdn.discordapp.com/attachments/1140382016295149730/1141048428353294436/wCsYPzMWHRipAAAAABJRU5ErkJggg.png")
                .setColor(-1)
                .build();
    }
}
