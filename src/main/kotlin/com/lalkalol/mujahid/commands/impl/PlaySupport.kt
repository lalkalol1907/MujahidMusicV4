package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.audio.TrackUserData
import com.lalkalol.mujahid.commands.CommandContext
import com.lalkalol.mujahid.util.Embeds
import dev.arbjerg.lavalink.client.player.LoadFailed
import dev.arbjerg.lavalink.client.player.NoMatches
import dev.arbjerg.lavalink.client.player.PlaylistLoaded
import dev.arbjerg.lavalink.client.player.SearchResult
import dev.arbjerg.lavalink.client.player.Track
import dev.arbjerg.lavalink.client.player.TrackLoaded
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

/**
 * Shared loading logic for `/play` and `/playfile`: connects to voice, resolves the
 * identifier through Lavalink and reports the result. Both commands only differ in how
 * they obtain the identifier.
 */
internal fun loadAndPlay(ctx: CommandContext, identifier: String) {
    if (!ctx.ensureConnected()) return

    val event = ctx.event
    event.deferReply().queue()

    val manager = ctx.musicManager
    val userData = TrackUserData(event.user.idLong, event.channelIdLong)

    manager.link.loadItem(identifier).subscribe { result ->
        when (result) {
            is TrackLoaded -> {
                val track = result.track.withData(userData)
                val started = manager.scheduler.enqueue(track)
                sendPlayed(event, track, started, manager.scheduler.queueSize())
            }

            is SearchResult -> {
                val first = result.tracks.firstOrNull()
                if (first == null) {
                    event.hook.sendMessageEmbeds(Embeds.warning("No matches found.")).queue()
                } else {
                    val track = first.withData(userData)
                    val started = manager.scheduler.enqueue(track)
                    sendPlayed(event, track, started, manager.scheduler.queueSize())
                }
            }

            is PlaylistLoaded -> {
                val tracks = result.tracks.map { it.withData(userData) }
                manager.scheduler.enqueueAll(tracks)
                event.hook.sendMessageEmbeds(
                    Embeds.success("Added **${tracks.size}** tracks from playlist **${result.info.name}**."),
                ).queue()
            }

            is NoMatches ->
                event.hook.sendMessageEmbeds(Embeds.warning("No matches found for your input.")).queue()

            is LoadFailed ->
                event.hook.sendMessageEmbeds(Embeds.error("Failed to load: ${result.exception.message}")).queue()
        }
    }
}

private fun Track.withData(data: TrackUserData): Track = apply { setUserData(data) }

private fun sendPlayed(
    event: SlashCommandInteractionEvent,
    track: Track,
    startedNow: Boolean,
    queueSize: Int,
) {
    val info = track.info
    val embed = Embeds.music()
        .setAuthor(if (startedNow) "Now playing" else "Added to queue")
        .setTitle(info.title.ifBlank { "Unknown" }, info.uri)
        .setDescription("by ${info.author}")
        .apply {
            if (!startedNow) setFooter("Position in queue: $queueSize")
        }
        .build()
    event.hook.sendMessageEmbeds(embed).queue()
}
