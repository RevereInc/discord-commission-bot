package com.flux.discordbot.discord;

import com.flux.discordbot.discord.command.CommandHandler;
import com.flux.discordbot.discord.event.ButtonClickEvent;
import com.flux.discordbot.discord.event.ModalSubmitEvent;
import com.flux.discordbot.discord.event.ReadyEvent;
import com.flux.discordbot.discord.event.StringSelectionInteractionListener;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initializes and configures the JDA (Java Discord API) for the bot.
 * This class implements CommandLineRunner, allowing it to run after Spring Boot starts.
 * It sets up the JDA with the necessary event listeners and configurations.
 * The ShardManager instance is made available for other parts of the application to use.
 *
 * @author Flux Development
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 */
@AllArgsConstructor
@Component
@Slf4j
public class JDAInitializer implements CommandLineRunner {
    private final ReadyEvent m_readyEvent;
    private final ButtonClickEvent m_buttonClickEvent;
    private final ModalSubmitEvent m_modalSubmitEvent;
    private final StringSelectionInteractionListener m_stringSelectionInteractionListener;
    private final CommandHandler m_commandHandler;

    // A static instance of ShardManager for global access within the application
    @Getter private static ShardManager m_shardManager;

    @Override
    public void run(final String... args) {
        // Create and configure the ShardManager
        final DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault("MTE0MDQxODIwNjA2NzA4MTI0Ng.G4l2tm.it5LGO-qtotBCV_wzmkwPEFEqsa0Nu9Z_eqaSg");

        // Enable required intents
        builder.enableIntents(
                GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_PRESENCES
        );

        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.setChunkingFilter(ChunkingFilter.ALL);
        builder.enableCache(CacheFlag.ONLINE_STATUS);
        m_shardManager = builder.build();

        // Register event listeners
        m_shardManager.addEventListener(
                m_commandHandler.getCommandClient(),
                m_readyEvent, m_buttonClickEvent, m_modalSubmitEvent, m_stringSelectionInteractionListener
        );

        log.info("Successfully loaded bot");
    }
}
