package com.lalkalol.mujahid

import com.lalkalol.mujahid.audio.LavalinkManager
import com.lalkalol.mujahid.commands.CommandRegistry
import com.lalkalol.mujahid.commands.impl.ClearCommand
import com.lalkalol.mujahid.commands.impl.FilterCommand
import com.lalkalol.mujahid.commands.impl.JoinCommand
import com.lalkalol.mujahid.commands.impl.LeaveCommand
import com.lalkalol.mujahid.commands.impl.LoopCommand
import com.lalkalol.mujahid.commands.impl.NowPlayingCommand
import com.lalkalol.mujahid.commands.impl.PauseCommand
import com.lalkalol.mujahid.commands.impl.PlayCommand
import com.lalkalol.mujahid.commands.impl.PlaylistCommand
import com.lalkalol.mujahid.commands.impl.QueueCommand
import com.lalkalol.mujahid.commands.impl.RemoveCommand
import com.lalkalol.mujahid.commands.impl.ResumeCommand
import com.lalkalol.mujahid.commands.impl.SeekCommand
import com.lalkalol.mujahid.commands.impl.ShuffleCommand
import com.lalkalol.mujahid.commands.impl.SkipCommand
import com.lalkalol.mujahid.commands.impl.StopCommand
import com.lalkalol.mujahid.commands.impl.VolumeCommand
import com.lalkalol.mujahid.config.Config
import com.lalkalol.mujahid.health.HealthMonitor
import com.lalkalol.mujahid.db.MongoStorage
import com.lalkalol.mujahid.db.PlaylistRepository
import com.lalkalol.mujahid.listeners.InteractionListener
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.slf4j.LoggerFactory
import java.nio.file.Path

private val log = LoggerFactory.getLogger("com.lalkalol.mujahid.Main")

fun main() {
    val config = Config.load()

    val mongo = MongoStorage(config.mongoUri, config.mongoDatabase)
    val playlists = PlaylistRepository(mongo.database)

    val lavalink = LavalinkManager(config)
    lavalink.start()

    val registry = CommandRegistry(lavalink, playlists)
    registry.register(
        PlayCommand(),
        JoinCommand(),
        LeaveCommand(),
        PauseCommand(),
        ResumeCommand(),
        SkipCommand(),
        StopCommand(),
        VolumeCommand(),
        QueueCommand(),
        NowPlayingCommand(),
        LoopCommand(),
        ShuffleCommand(),
        SeekCommand(),
        RemoveCommand(),
        ClearCommand(),
        FilterCommand(),
        PlaylistCommand(),
    )

    val jda = JDABuilder.createDefault(config.discordToken)
        .setVoiceDispatchInterceptor(JDAVoiceUpdateListener(lavalink.client))
        .enableIntents(GatewayIntent.GUILD_VOICE_STATES)
        .enableCache(CacheFlag.VOICE_STATE)
        .setActivity(Activity.listening("/play"))
        .addEventListeners(InteractionListener(registry, lavalink, config.devGuildId))
        .build()

    lavalink.jda = jda

    val health = HealthMonitor(jda, Path.of(config.healthFile))
    health.start()

    Runtime.getRuntime().addShutdownHook(
        Thread {
            log.info("Shutting down...")
            health.stop()
            jda.shutdown()
            mongo.close()
        },
    )

    jda.awaitReady()
    log.info("MujahidMusicV4 is up and running.")
}
