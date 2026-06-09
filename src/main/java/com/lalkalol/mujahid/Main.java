package com.lalkalol.mujahid;

import com.lalkalol.mujahid.audio.LavalinkManager;
import com.lalkalol.mujahid.commands.CommandRegistry;
import com.lalkalol.mujahid.config.Config;
import com.lalkalol.mujahid.db.MongoStorage;
import com.lalkalol.mujahid.db.PlaylistRepository;
import com.lalkalol.mujahid.health.HealthMonitor;
import com.lalkalol.mujahid.listeners.InteractionListener;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public final class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private Main() {
    }

    public static void main(String[] args) throws InterruptedException {
        Config config = Config.load();
        Config.applyLogLevel(config.logLevel());

        MongoStorage mongo = new MongoStorage(config.mongoUri(), config.mongoDatabase());
        PlaylistRepository playlists = new PlaylistRepository(mongo.getDatabase());

        LavalinkManager lavalink = new LavalinkManager(config);
        lavalink.start();

        CommandRegistry registry = new CommandRegistry(lavalink, playlists);
        registry.registerDefaults();

        JDA jda = JDABuilder.createDefault(config.discordToken())
                .setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(lavalink.getClient()))
                .enableIntents(GatewayIntent.GUILD_VOICE_STATES)
                .enableCache(CacheFlag.VOICE_STATE)
                .setActivity(Activity.listening("/play"))
                .addEventListeners(new InteractionListener(registry, lavalink, config.devGuildId()))
                .build();

        lavalink.setJda(jda);

        HealthMonitor health = new HealthMonitor(jda, Path.of(config.healthFile()));
        health.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(health, lavalink, jda, mongo)));

        jda.awaitReady();
        log.info("MujahidMusicV4 is up and running.");
    }

    private static void shutdown(HealthMonitor health, LavalinkManager lavalink, JDA jda, MongoStorage mongo) {
        log.info("Shutting down...");
        health.stop();
        lavalink.shutdown();
        jda.shutdown();
        try {
            if (!jda.awaitShutdown(10, TimeUnit.SECONDS)) {
                log.warn("JDA shutdown timed out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for JDA shutdown");
        }
        mongo.close();
        log.info("Shutdown complete");
    }
}
