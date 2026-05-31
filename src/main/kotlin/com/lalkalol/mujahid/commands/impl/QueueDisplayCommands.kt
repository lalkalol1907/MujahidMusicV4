package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import com.lalkalol.mujahid.util.Embeds
import com.lalkalol.mujahid.util.Format
import net.dv8tion.jda.api.interactions.commands.build.Commands

private const val QUEUE_PAGE_SIZE = 10

class QueueCommand : Command {
    override val name = "queue"
    override val data = Commands.slash("queue", "Show the current queue.")

    override fun execute(ctx: CommandContext) {
        val scheduler = ctx.musicManager.scheduler
        val current = scheduler.current
        val queue = scheduler.queueSnapshot()

        if (current == null && queue.isEmpty()) {
            ctx.replyEphemeral("The queue is empty.")
            return
        }

        val description = buildString {
            current?.let {
                append("**Now playing:** [${it.info.title}](${it.info.uri}) ")
                append("`[${Format.duration(it.info.length, it.info.isStream)}]`\n\n")
            }
            if (queue.isNotEmpty()) {
                append("**Up next:**\n")
                queue.take(QUEUE_PAGE_SIZE).forEachIndexed { index, track ->
                    append("`${index + 1}.` ${track.info.title} ")
                    append("`[${Format.duration(track.info.length, track.info.isStream)}]`\n")
                }
                if (queue.size > QUEUE_PAGE_SIZE) {
                    append("\n…and **${queue.size - QUEUE_PAGE_SIZE}** more.")
                }
            }
        }

        val embed = Embeds.music()
            .setTitle("🎶 Queue")
            .setDescription(description)
            .setFooter(
                "Loop: ${scheduler.loopMode} • Filter: ${scheduler.filter.displayName} • " +
                    "Volume: ${scheduler.volume}% • ${queue.size} in queue",
            )
            .build()

        ctx.event.replyEmbeds(embed).queue()
    }
}

class NowPlayingCommand : Command {
    override val name = "nowplaying"
    override val data = Commands.slash("nowplaying", "Show what is currently playing.")

    override fun execute(ctx: CommandContext) {
        if (!ctx.requireActivePlayer()) return
        val player = ctx.musicManager.player
        val track = player?.track ?: ctx.musicManager.scheduler.current
        if (track == null) {
            ctx.replyEphemeral("Nothing is playing right now.")
            return
        }

        val info = track.info
        val position = player?.position ?: 0L
        val bar = Format.progressBar(position, info.length)
        val timeline = "${Format.duration(position)} / ${Format.duration(info.length, info.isStream)}"

        val scheduler = ctx.musicManager.scheduler
        val state = if (player?.paused == true) "Paused" else "Playing"

        val embed = Embeds.music()
            .setAuthor("Now playing")
            .setTitle(info.title.ifBlank { "Unknown" }, info.uri)
            .setDescription("by ${info.author}\n\n$bar\n`$timeline`")
            .setFooter("$state • Loop: ${scheduler.loopMode} • Filter: ${scheduler.filter.displayName}")
            .build()

        ctx.event.replyEmbeds(embed).queue()
    }
}
