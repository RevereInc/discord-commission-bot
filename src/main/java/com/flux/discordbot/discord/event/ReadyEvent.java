package com.flux.discordbot.discord.event;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ReadyEvent extends ListenerAdapter {
    @Override
    public void onReady(final net.dv8tion.jda.api.events.session.ReadyEvent event) {
        log.info("Successfully connected to " + event.getJDA().getSelfUser().getName());
    }
}
