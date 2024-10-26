package dev.revere.commission.discord;

import dev.revere.commission.discord.command.CommandHandler;
import dev.revere.commission.discord.event.*;
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
 * @author Revere Development
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
    private final GuildJoinEvent m_guildJoinEvent;
    private final StringSelectionInteractionListener m_stringSelectionInteractionListener;
    private final CommandHandler m_commandHandler;

    // A static instance of ShardManager for global access within the application
    @Getter
    private static ShardManager m_shardManager;

    @Override
    public void run(final String... args) {
        // Create and configure the ShardManager
        String m_token = "ODAyODI2MzYzMjU4MTQyNzQx.GuNXeu.rKKpowJ0uym0vN-yc3l3iWQoI5KzX-W0_JoS2U";
        final DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(m_token);

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
                m_readyEvent, m_buttonClickEvent, m_modalSubmitEvent,
                m_stringSelectionInteractionListener, m_guildJoinEvent
        );

        log.info("Successfully initialized JDA");
        log.info("Logged into Discord as {}#{}", m_shardManager.getShards().get(0).getSelfUser().getName(), m_shardManager.getShards().get(0).getSelfUser().getDiscriminator());
    }
}
