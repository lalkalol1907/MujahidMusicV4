package com.lalkalol.mujahid;

import com.lalkalol.mujahid.audio.LavalinkManager;
import com.lalkalol.mujahid.commands.CommandRegistry;
import com.lalkalol.mujahid.commands.impl.ClearCommand;
import com.lalkalol.mujahid.commands.impl.FilterCommand;
import com.lalkalol.mujahid.commands.impl.JoinCommand;
import com.lalkalol.mujahid.commands.impl.LeaveCommand;
import com.lalkalol.mujahid.commands.impl.LoopCommand;
import com.lalkalol.mujahid.commands.impl.NowPlayingCommand;
import com.lalkalol.mujahid.commands.impl.PauseCommand;
import com.lalkalol.mujahid.commands.impl.PlayCommand;
import com.lalkalol.mujahid.commands.impl.PlayFileCommand;
import com.lalkalol.mujahid.commands.impl.PlaylistCommand;
import com.lalkalol.mujahid.commands.impl.QueueCommand;
import com.lalkalol.mujahid.commands.impl.RemoveCommand;
import com.lalkalol.mujahid.commands.impl.ResumeCommand;
import com.lalkalol.mujahid.commands.impl.SeekCommand;
import com.lalkalol.mujahid.commands.impl.ShuffleCommand;
import com.lalkalol.mujahid.commands.impl.SkipCommand;
import com.lalkalol.mujahid.commands.impl.StopCommand;
import com.lalkalol.mujahid.commands.impl.VolumeCommand;
import com.lalkalol.mujahid.config.Config;
import com.lalkalol.mujahid.db.MongoStorage;
import com.lalkalol.mujahid.db.PlaylistRepository;
import com.lalkalol.mujahid.health.HealthMonitor;
import com.lalkalol.mujahid.listeners.InteractionListener;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private Main() {
    }

    public static void main(String[] args) throws InterruptedException {
        Config config = Config.load();

        MongoStorage mongo = new MongoStorage(config.mongoUri(), config.mongoDatabase());
        PlaylistRepository playlists = new PlaylistRepository(mongo.getDatabase());

        LavalinkManager lavalink = new LavalinkManager(config);
        lavalink.start();

        CommandRegistry registry = new CommandRegistry(lavalink, playlists);
        registry.register(
                new PlayCommand(),
                new PlayFileCommand(),
                new JoinCommand(),
                new LeaveCommand(),
                new PauseCommand(),
                new ResumeCommand(),
                new SkipCommand(),
                new StopCommand(),
                new VolumeCommand(),
                new QueueCommand(),
                new NowPlayingCommand(),
                new LoopCommand(),
                new ShuffleCommand(),
                new SeekCommand(),
                new RemoveCommand(),
                new ClearCommand(),
                new FilterCommand(),
                new PlaylistCommand()
        );

        var jda = JDABuilder.createDefault(config.discordToken())
                .setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(lavalink.getClient()))
                .enableIntents(GatewayIntent.GUILD_VOICE_STATES)
                .enableCache(CacheFlag.VOICE_STATE)
                .setActivity(Activity.listening("/play"))
                .addEventListeners(new InteractionListener(registry, lavalink, config.devGuildId()))
                .build();

        lavalink.setJda(jda);

        HealthMonitor health = new HealthMonitor(jda, Path.of(config.healthFile()));
        health.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down...");
            health.stop();
            jda.shutdown();
            mongo.close();
        }));

        jda.awaitReady();
        log.info("MujahidMusicV4 is up and running.");
    }
}
