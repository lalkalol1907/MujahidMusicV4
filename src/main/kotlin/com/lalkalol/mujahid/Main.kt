package com.lalkalol.mujahid

import com.lalkalol.mujahid.audio.LavalinkManager
import com.lalkalol.mujahid.commands.CommandRegistry
import com.lalkalol.mujahid.config.Config
import com.lalkalol.mujahid.db.MongoStorage
import com.lalkalol.mujahid.db.PlaylistRepository
import com.lalkalol.mujahid.health.HealthMonitor
import com.lalkalol.mujahid.internal.ControlHttpServer
import com.lalkalol.mujahid.listeners.InteractionListener
import com.lalkalol.mujahid.metrics.BotMetrics
import com.lalkalol.mujahid.metrics.MetricsHolder
import com.lalkalol.mujahid.metrics.MetricsHttpServer
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.concurrent.TimeUnit

fun main() {
    val log = LoggerFactory.getLogger("com.lalkalol.mujahid.Main")
    val config = Config.load()
    Config.applyLogLevel(config.logLevel)

    val metrics = BotMetrics()
    MetricsHolder.set(metrics)

    val mongo = MongoStorage(config.mongoUri, config.mongoDatabase)
    val playlists = PlaylistRepository(mongo.database)

    val lavalink = LavalinkManager(config)
    lavalink.start()

    val registry = CommandRegistry(lavalink, playlists, metrics)
    registry.registerDefaults()

    val jda = JDABuilder.createDefault(config.discordToken)
        .setVoiceDispatchInterceptor(JDAVoiceUpdateListener(lavalink.client))
        .enableIntents(GatewayIntent.GUILD_VOICE_STATES)
        .enableCache(CacheFlag.VOICE_STATE)
        .setActivity(Activity.listening("/play"))
        .addEventListeners(InteractionListener(registry, lavalink, config.devGuildId))
        .build()

    lavalink.setJda(jda)

    val health = HealthMonitor(jda, Path.of(config.healthFile))
    health.start()

    val metricsPort = config.metricsPort
    val metricsServer = MetricsHttpServer(metrics, lavalink)
    metricsServer.start(metricsPort)

    val controlServer = ControlHttpServer(config.internalApiKey, lavalink)
    controlServer.start(config.internalPort)

    Runtime.getRuntime().addShutdownHook(
        Thread {
            shutdown(log, health, lavalink, jda, mongo, metricsServer, controlServer)
        },
    )

    jda.awaitReady()

    metrics.bindJdaGuildGauge(jda)

    log.info(
        "MujahidMusicV4 is up and running. Metrics on :{}, Control Plane on :{}",
        metricsPort,
        config.internalPort,
    )
}

private fun shutdown(
    log: org.slf4j.Logger,
    health: HealthMonitor,
    lavalink: LavalinkManager,
    jda: JDA,
    mongo: MongoStorage,
    metricsServer: MetricsHttpServer,
    controlServer: ControlHttpServer,
) {
    log.info("Shutting down...")
    controlServer.stop()
    metricsServer.stop()
    health.stop()
    lavalink.shutdown()
    jda.shutdown()
    try {
        if (!jda.awaitShutdown(10, TimeUnit.SECONDS)) {
            log.warn("JDA shutdown timed out")
        }
    } catch (_: InterruptedException) {
        Thread.currentThread().interrupt()
        log.warn("Interrupted while waiting for JDA shutdown")
    }
    mongo.close()
    log.info("Shutdown complete")
}
