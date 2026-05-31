package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.audio.TrackUserData
import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import com.lalkalol.mujahid.db.StoredTrack
import com.lalkalol.mujahid.util.Embeds
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

class PlaylistCommand : Command {
    override val name = "playlist"
    override val data = Commands.slash("playlist", "Manage your personal saved playlists.")
        .addSubcommands(
            SubcommandData("save", "Save the current track and queue as a playlist")
                .addOption(OptionType.STRING, "name", "Playlist name", true),
            SubcommandData("load", "Load a saved playlist into the queue")
                .addOption(OptionType.STRING, "name", "Playlist name", true),
            SubcommandData("list", "List your saved playlists"),
            SubcommandData("delete", "Delete a saved playlist")
                .addOption(OptionType.STRING, "name", "Playlist name", true),
        )

    override fun execute(ctx: CommandContext) {
        when (ctx.event.subcommandName) {
            "save" -> save(ctx)
            "load" -> load(ctx)
            "list" -> list(ctx)
            "delete" -> delete(ctx)
            else -> ctx.replyEphemeral("Unknown subcommand.")
        }
    }

    private fun save(ctx: CommandContext) {
        val name = ctx.event.getOption("name")!!.asString.trim()
        if (name.isEmpty()) {
            ctx.replyEphemeral("Playlist name cannot be empty.")
            return
        }

        val scheduler = ctx.musicManager.scheduler
        val tracks = buildList {
            scheduler.current?.let { add(it) }
            addAll(scheduler.queueSnapshot())
        }
        if (tracks.isEmpty()) {
            ctx.replyEphemeral("There is nothing playing or queued to save.")
            return
        }

        val stored = tracks.map { StoredTrack(it.encoded, it.info.title, it.info.uri) }
        val count = ctx.playlists.save(ctx.event.user.idLong, name, stored)
        ctx.reply("Saved **$count** track(s) to playlist **$name**.")
    }

    private fun load(ctx: CommandContext) {
        val name = ctx.event.getOption("name")!!.asString.trim()
        val stored = ctx.playlists.load(ctx.event.user.idLong, name)
        if (stored == null) {
            ctx.replyEphemeral("You don't have a playlist named **$name**.")
            return
        }
        if (stored.isEmpty()) {
            ctx.replyEphemeral("Playlist **$name** is empty.")
            return
        }
        if (!ctx.ensureConnected()) return

        ctx.event.deferReply().queue()
        val userData = TrackUserData(ctx.event.user.idLong, ctx.event.channelIdLong)
        val node = ctx.musicManager.link.node

        node.decodeTracks(stored.map { it.encoded }).subscribe(
            { tracks ->
                tracks.forEach { it.setUserData(userData) }
                ctx.musicManager.scheduler.enqueueAll(tracks)
                ctx.event.hook.sendMessageEmbeds(
                    Embeds.success("Loaded **${tracks.size}** track(s) from **$name**."),
                ).queue()
            },
            { error ->
                ctx.event.hook.sendMessageEmbeds(
                    Embeds.error("Failed to load playlist: ${error.message}"),
                ).queue()
            },
        )
    }

    private fun list(ctx: CommandContext) {
        val playlists = ctx.playlists.list(ctx.event.user.idLong)
        if (playlists.isEmpty()) {
            ctx.replyEphemeral("You have no saved playlists. Use `/playlist save` to create one.")
            return
        }
        val description = playlists.joinToString("\n") { "🎵 **${it.name}** — ${it.trackCount} track(s)" }
        val embed = Embeds.music()
            .setTitle("Your playlists")
            .setDescription(description)
            .build()
        ctx.event.replyEmbeds(embed).setEphemeral(true).queue()
    }

    private fun delete(ctx: CommandContext) {
        val name = ctx.event.getOption("name")!!.asString.trim()
        if (ctx.playlists.delete(ctx.event.user.idLong, name)) {
            ctx.reply("Deleted playlist **$name**.")
            return
        }
        ctx.replyEphemeral("You don't have a playlist named **$name**.")
    }
}
