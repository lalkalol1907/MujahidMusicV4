package com.lalkalol.mujahid.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import net.dv8tion.jda.api.JDA;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Central Micrometer registry and metric helper for MujahidMusic.
 * Uses a PrometheusMeterRegistry so /metrics exposes Prometheus text format.
 */
public class BotMetrics {

    private final PrometheusMeterRegistry registry;

    // Gauge backing values
    private final AtomicInteger activePlayers = new AtomicInteger(0);

    public BotMetrics() {
        this.registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        Gauge.builder("mujahid_active_players", activePlayers, AtomicInteger::doubleValue)
                .description("Number of guilds with an active Lavalink player")
                .register(registry);
    }

    // ── Expose registry for HTTP server ─────────────────────────────────────

    public PrometheusMeterRegistry getRegistry() {
        return registry;
    }

    // ── Register JDA guild gauge (called after JDA is ready) ─────────────────

    public void bindJdaGuildGauge(JDA jda) {
        Gauge.builder("mujahid_guilds_total", jda, j -> j.getGuilds().size())
                .description("Number of guilds the bot is in")
                .register(registry);
    }

    // ── Gauge mutators ────────────────────────────────────────────────────────

    public void setActivePlayers(int count) {
        activePlayers.set(count);
    }

    // ── Command metrics ───────────────────────────────────────────────────────

    public void recordCommandExecution(String commandName) {
        Counter.builder("mujahid_commands_total")
                .description("Total slash commands executed")
                .tag("command", commandName)
                .register(registry)
                .increment();
    }

    public void recordCommandError(String commandName) {
        Counter.builder("mujahid_command_errors_total")
                .description("Total slash command errors")
                .tag("command", commandName)
                .register(registry)
                .increment();
    }

    // ── Track metrics ─────────────────────────────────────────────────────────

    public void recordTrackPlayed(String source) {
        Counter.builder("mujahid_tracks_played_total")
                .description("Total tracks started")
                .tag("source", source)
                .register(registry)
                .increment();
    }

    public void recordTrackLoadFailure(String reason) {
        Counter.builder("mujahid_track_load_failures_total")
                .description("Total track load failures")
                .tag("reason", reason)
                .register(registry)
                .increment();
    }

    // ── Voice session metrics ─────────────────────────────────────────────────

    public void recordVoiceSession(String action) {
        Counter.builder("mujahid_voice_sessions_total")
                .description("Voice session events")
                .tag("action", action)   // join | leave | auto_leave
                .register(registry)
                .increment();
    }

    // ── Playlist metrics ──────────────────────────────────────────────────────

    public void recordPlaylistOp(String op) {
        Counter.builder("mujahid_playlist_ops_total")
                .description("Playlist operations")
                .tag("op", op)   // save | load | delete
                .register(registry)
                .increment();
    }
}
