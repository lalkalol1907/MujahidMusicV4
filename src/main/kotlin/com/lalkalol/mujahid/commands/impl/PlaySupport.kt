package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.audio.AddResult
import com.lalkalol.mujahid.audio.TrackScheduler
import com.lalkalol.mujahid.audio.TrackUserData
import com.lalkalol.mujahid.commands.CommandContext
import com.lalkalol.mujahid.metrics.MetricsHolder
import com.lalkalol.mujahid.util.Embeds
import com.lalkalol.mujahid.util.MusicControls
import com.lalkalol.mujahid.util.await
import com.lalkalol.mujahid.util.loadItemSuspend
import dev.arbjerg.lavalink.client.player.LoadFailed
import dev.arbjerg.lavalink.client.player.LavalinkLoadResult
import dev.arbjerg.lavalink.client.player.NoMatches
import dev.arbjerg.lavalink.client.player.PlaylistLoaded
import dev.arbjerg.lavalink.client.player.SearchResult
import dev.arbjerg.lavalink.client.player.Track
import dev.arbjerg.lavalink.client.player.TrackLoaded
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.slf4j.LoggerFactory

object PlaySupport {
    private val log = LoggerFactory.getLogger(PlaySupport::class.java)

    suspend fun loadAndPlay(ctx: CommandContext, identifier: String) {
        loadTrack(ctx, identifier, false)
    }

    suspend fun loadAndEnqueueNext(ctx: CommandContext, identifier: String) {
        loadTrack(ctx, identifier, true)
    }

    private suspend fun loadTrack(ctx: CommandContext, identifier: String, playNext: Boolean) {
        if (!ctx.ensureConnected()) {
            return
        }

        val event = ctx.event
        event.deferReply().await()

        val manager = ctx.getMusicManager()
        val userData = TrackUserData(event.user.idLong, event.channelIdLong)
        val guildId = ctx.getGuild().idLong

        log.info("Loading '{}' for guild {} (playNext={})", identifier, guildId, playNext)

        try {
            val result = manager.getLink().loadItemSuspend(identifier)
            handleLoadResult(event, manager.scheduler, userData, guildId, playNext, result)
        } catch (error: Exception) {
            log.error("Load failed for '{}' in guild {}", identifier, guildId, error)
            event.hook.sendMessageEmbeds(
                Embeds.error("Failed to load: ${error.message}"),
            ).queue()
        }
    }

    private fun handleLoadResult(
        event: SlashCommandInteractionEvent,
        scheduler: TrackScheduler,
        userData: TrackUserData,
        guildId: Long,
        playNext: Boolean,
        result: LavalinkLoadResult,
    ) {
        when (result) {
            is TrackLoaded -> {
                val track = withData(result.track, userData)
                val addResult = if (playNext) scheduler.enqueueNext(track) else scheduler.enqueue(track)
                MetricsHolder.get().recordTrackPlayed("url")
                sendAdded(event, track, addResult, scheduler.queueSize(), guildId, playNext)
            }
            is SearchResult -> {
                val tracks = result.tracks
                if (tracks.isEmpty()) {
                    event.hook.sendMessageEmbeds(Embeds.warning("No matches found.")).queue()
                } else {
                    val track = withData(tracks.first(), userData)
                    val addResult = if (playNext) scheduler.enqueueNext(track) else scheduler.enqueue(track)
                    MetricsHolder.get().recordTrackPlayed("search")
                    sendAdded(event, track, addResult, scheduler.queueSize(), guildId, playNext)
                }
            }
            is PlaylistLoaded -> {
                val tracks = result.tracks.map { withData(it, userData) }
                scheduler.enqueueAll(tracks)
                MetricsHolder.get().recordTrackPlayed("playlist")
                event.hook.sendMessageEmbeds(
                    Embeds.success(
                        "Added **${tracks.size}** tracks from playlist **${result.info.name}**.",
                    ),
                ).queue()
            }
            is NoMatches -> {
                MetricsHolder.get().recordTrackLoadFailure("no_matches")
                event.hook.sendMessageEmbeds(Embeds.warning("No matches found for your input.")).queue()
            }
            is LoadFailed -> {
                log.warn("Lavalink load failed in guild {}: {}", guildId, result.exception.message)
                MetricsHolder.get().recordTrackLoadFailure("lavalink_error")
                event.hook.sendMessageEmbeds(
                    Embeds.error("Failed to load: ${result.exception.message}"),
                ).queue()
            }
        }
    }

    private fun withData(track: Track, data: TrackUserData): Track {
        track.setUserData(data)
        return track
    }

    private fun sendAdded(
        event: SlashCommandInteractionEvent,
        track: Track,
        addResult: AddResult,
        queueSize: Int,
        guildId: Long,
        playNext: Boolean,
    ) {
        if (addResult == AddResult.REJECTED_FULL) {
            event.hook.sendMessageEmbeds(
                Embeds.warning("Queue is full (${TrackScheduler.MAX_QUEUE_SIZE} tracks)."),
            ).queue()
            return
        }

        val info = track.info
        val author = when (addResult) {
            AddResult.STARTED_NOW -> "Now playing"
            AddResult.QUEUED -> if (playNext) "Playing next" else "Added to queue"
            AddResult.REJECTED_FULL -> throw IllegalStateException()
        }

        val builder = Embeds.music()
            .setAuthor(author)
            .setTitle(if (info.title.isBlank()) "Unknown" else info.title, info.uri)
            .setDescription("by ${info.author}")

        if (addResult == AddResult.QUEUED) {
            val position = if (playNext) 1 else queueSize
            builder.setFooter("Position in queue: $position")
        }

        val hook = event.hook.sendMessageEmbeds(builder.build())
        if (addResult == AddResult.STARTED_NOW) {
            hook.setComponents(MusicControls.playbackRow(guildId, false))
        }
        hook.queue()
    }
}
