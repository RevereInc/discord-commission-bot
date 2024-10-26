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
        // Get the newly joined member
        final Member member = event.getMember();

        System.out.println("Member joined: " + member.getUser().getName());

        // Get the guild the member joined
        final Guild guild = event.getJDA().getGuildById(Constants.MAIN_GUILD_ID);
        if (guild == null) {
            System.out.println("Guild not found");
            return;
        }

        // Retrieve the channel by its configuration value
        TextChannel channel = guild.getTextChannelById(Constants.WELCOME_CHANNEL_ID);
        if (channel != null) {
            System.out.println("Welcome message sent to " + member.getUser().getName() + " in " + guild.getName() + "!");
            channel.sendMessage("<:RVC_W1:1299490606179029106><:RVC_W2:1299490648415666269><:RVC_W3:1299490670356070431><:RVC_W4:1299490696138592370><:RVC_W5:1299490715411284039> to **@Tonic Consulting**, <@" + member.getUser().getId() + "> - you are the **" + guild.getMembers().size() + "th member **!").queue();
        }
    }
}
