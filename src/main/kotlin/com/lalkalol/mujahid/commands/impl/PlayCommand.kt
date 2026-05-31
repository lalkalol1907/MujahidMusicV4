package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.audio.TrackUserData
import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import com.lalkalol.mujahid.util.Embeds
import com.lalkalol.mujahid.util.Identifiers
import dev.arbjerg.lavalink.client.player.LoadFailed
import dev.arbjerg.lavalink.client.player.NoMatches
import dev.arbjerg.lavalink.client.player.PlaylistLoaded
import dev.arbjerg.lavalink.client.player.SearchResult
import dev.arbjerg.lavalink.client.player.Track
import dev.arbjerg.lavalink.client.player.TrackLoaded
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

class PlayCommand : Command {
    override val name = "play"
    override val data = Commands.slash("play", "Play a track from a URL/search, or an uploaded audio file.")
        .addOption(OptionType.STRING, "query", "A search term or a YouTube/SoundCloud/direct URL", false)
        .addOption(OptionType.ATTACHMENT, "file", "An audio file (mp3, ogg, wav, flac, m4a) to play", false)

    override fun execute(ctx: CommandContext) {
        val event = ctx.event
        val attachment = event.getOption("file")?.asAttachment
        val query = event.getOption("query")?.asString

        if (attachment == null && query.isNullOrBlank()) {
            ctx.replyEphemeral("Provide a `query` or attach a `file`.")
            return
        }

        val identifier = if (attachment != null) {
            val contentType = attachment.contentType ?: ""
            if (!contentType.startsWith("audio")) {
                ctx.replyEphemeral("`${attachment.fileName}` is not an audio file (detected `${contentType.ifBlank { "unknown" }}`).")
                return
            }
            attachment.url
        } else {
            Identifiers.fromQuery(query!!)
        }

        if (!ctx.ensureConnected()) return

        event.deferReply().queue()

        val manager = ctx.musicManager
        val userData = TrackUserData(event.user.idLong, event.channelIdLong)

        manager.link.loadItem(identifier).subscribe { result ->
            when (result) {
                is TrackLoaded -> {
                    val track = result.track.applyData(userData)
                    val started = manager.scheduler.enqueue(track)
                    sendPlayed(event, track, started, manager.scheduler.queueSize())
                }

                is SearchResult -> {
                    val first = result.tracks.firstOrNull()
                    if (first == null) {
                        event.hook.sendMessageEmbeds(Embeds.warning("No matches found.")).queue()
                    } else {
                        val track = first.applyData(userData)
                        val started = manager.scheduler.enqueue(track)
                        sendPlayed(event, track, started, manager.scheduler.queueSize())
                    }
                }

                is PlaylistLoaded -> {
                    val tracks = result.tracks.map { it.applyData(userData) }
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

    private fun Track.applyData(data: TrackUserData): Track = apply { setUserData(data) }

    private fun sendPlayed(
        event: net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent,
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
}
