package dev.revere.commission.discord.event;

import dev.revere.commission.Constants;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class GuildJoinEvent extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        final Member member = event.getMember();

        if (event.getGuild().getIdLong() == Constants.MAIN_GUILD_ID) {
            System.out.println("Member joined: " + member.getUser().getName());

            final Guild guild = event.getJDA().getGuildById(Constants.MAIN_GUILD_ID);
            if (guild == null) {
                System.out.println("Guild not found");
                return;
            }

            TextChannel channel = guild.getTextChannelById(Constants.WELCOME_CHANNEL_ID);
            if (channel != null) {
                System.out.println("Welcome message sent to " + member.getUser().getName() + " in " + guild.getName() + "!");
                channel.sendMessage("Welcome to **@Tonic Consulting**, <@" + member.getUser().getId() + "> - you are the **" + guild.getMembers().size() + "th member **!").queue();
            }
        }
    }
}
