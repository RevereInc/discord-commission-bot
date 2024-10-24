package dev.revere.commission.discord.event;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;

/**
 * @author Revere Development
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 */
@Service
@Slf4j
public class ReadyEvent extends ListenerAdapter {
    @Override
    public void onReady(final net.dv8tion.jda.api.events.session.ReadyEvent event) {
        log.info("Successfully logged into " + event.getJDA().getSelfUser().getName());
    }
}

