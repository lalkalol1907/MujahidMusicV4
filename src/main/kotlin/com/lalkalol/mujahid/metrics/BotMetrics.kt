package com.lalkalol.mujahid.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import net.dv8tion.jda.api.JDA
import java.util.concurrent.atomic.AtomicInteger

open class BotMetrics {
    val registry: PrometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    private val activePlayers = AtomicInteger(0)

    init {
        Gauge.builder("mujahid_active_players", activePlayers) { it.get().toDouble() }
            .description("Number of guilds with an active Lavalink player")
            .register(registry)
    }

    fun bindJdaGuildGauge(jda: JDA) {
        Gauge.builder("mujahid_guilds_total", jda) { it.guilds.size.toDouble() }
            .description("Number of guilds the bot is in")
            .register(registry)
    }

    open fun setActivePlayers(count: Int) {
        activePlayers.set(count)
    }

    open fun recordCommandExecution(commandName: String) {
        Counter.builder("mujahid_commands_total")
            .description("Total slash commands executed")
            .tag("command", commandName)
            .register(registry)
            .increment()
    }

    open fun recordCommandError(commandName: String) {
        Counter.builder("mujahid_command_errors_total")
            .description("Total slash command errors")
            .tag("command", commandName)
            .register(registry)
            .increment()
    }

    open fun recordTrackPlayed(source: String) {
        Counter.builder("mujahid_tracks_played_total")
            .description("Total tracks started")
            .tag("source", source)
            .register(registry)
            .increment()
    }

    open fun recordTrackLoadFailure(reason: String) {
        Counter.builder("mujahid_track_load_failures_total")
            .description("Total track load failures")
            .tag("reason", reason)
            .register(registry)
            .increment()
    }

    open fun recordVoiceSession(action: String) {
        Counter.builder("mujahid_voice_sessions_total")
            .description("Voice session events")
            .tag("action", action)
            .register(registry)
            .increment()
    }

    open fun recordPlaylistOp(op: String) {
        Counter.builder("mujahid_playlist_ops_total")
            .description("Playlist operations")
            .tag("op", op)
            .register(registry)
            .increment()
    }
}
