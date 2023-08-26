package com.flux.discordbot.discord;

import com.flux.discordbot.discord.event.ReadyEvent;
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

@AllArgsConstructor
@Component
@Slf4j
public class JDAInitializer implements CommandLineRunner {
    private final ReadyEvent m_readyEvent;
    @Getter
    private static ShardManager shardManager;

    @Override
    public void run(final String... args) {
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault("MTE0MDQxODIwNjA2NzA4MTI0Ng.G4l2tm.it5LGO-qtotBCV_wzmkwPEFEqsa0Nu9Z_eqaSg");
        builder.enableIntents(
                GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_PRESENCES
        );

        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.setChunkingFilter(ChunkingFilter.ALL);
        builder.enableCache(CacheFlag.ONLINE_STATUS);
        shardManager = builder.build();

//        new CommandHandler();

        // Register event listeners
        shardManager.addEventListener(
                m_readyEvent
//                new CommandHandler().getClient(), new ModalSubmitEvent(), new StringSelectionInteractionListener(), new ButtonClickEvent(),
        );

        // Log the bots version upon successful startup
        log.info("Successfully loaded bot");
    }
}
