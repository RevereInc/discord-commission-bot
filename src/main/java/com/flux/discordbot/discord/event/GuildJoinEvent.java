package com.flux.discordbot.discord.event;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class GuildJoinEvent extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        try {
            Role role = event.getGuild().getRoleById("1140379900713369702");
            if (role != null) {
                event.getGuild().addRoleToMember(event.getMember(), role).queue();
            } else {
                System.out.println("Role not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
