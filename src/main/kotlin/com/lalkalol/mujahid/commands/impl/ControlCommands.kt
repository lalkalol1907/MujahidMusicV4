package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.audio.LoopMode
import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import com.lalkalol.mujahid.util.Format
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class LoopCommand : Command {
    override val name = "loop"
    override val data = Commands.slash("loop", "Set the loop mode.")
        .addOptions(
            OptionData(OptionType.STRING, "mode", "What to loop", true)
                .addChoice("Off", "off")
                .addChoice("Track", "track")
                .addChoice("Queue", "queue"),
        )

    override fun execute(ctx: CommandContext) {
        val mode = LoopMode.from(ctx.event.getOption("mode")!!.asString) ?: LoopMode.OFF
        ctx.musicManager.scheduler.loopMode = mode
        ctx.reply("Loop mode set to **${mode.name.lowercase()}**.")
    }
}

class ShuffleCommand : Command {
    override val name = "shuffle"
    override val data = Commands.slash("shuffle", "Shuffle the queue.")

    override fun execute(ctx: CommandContext) {
        if (ctx.musicManager.scheduler.shuffle()) {
            ctx.reply("Shuffled the queue.")
        } else {
            ctx.replyEphemeral("Need at least 2 tracks in the queue to shuffle.")
        }
    }
}

class SeekCommand : Command {
    override val name = "seek"
    override val data = Commands.slash("seek", "Seek to a position in the current track.")
        .addOption(OptionType.STRING, "position", "Timestamp like 90, 1:30 or 1:02:03", true)

    override fun execute(ctx: CommandContext) {
        if (!ctx.requireActivePlayer()) return
        val track = ctx.musicManager.player?.track ?: ctx.musicManager.scheduler.current
        if (track == null) {
            ctx.replyEphemeral("Nothing is playing right now.")
            return
        }
        if (track.info.isStream || !track.info.isSeekable) {
            ctx.replyEphemeral("This track cannot be seeked.")
            return
        }

        val raw = ctx.event.getOption("position")!!.asString
        val ms = Format.parseTimestamp(raw)
        if (ms == null) {
            ctx.replyEphemeral("Invalid timestamp. Use `90`, `1:30` or `1:02:03`.")
            return
        }
        if (ms > track.info.length) {
            ctx.replyEphemeral("That position is past the end of the track (${Format.duration(track.info.length)}).")
            return
        }

        ctx.musicManager.cachedLink?.createOrUpdatePlayer()?.setPosition(ms)?.subscribe()
        ctx.reply("Seeked to **${Format.duration(ms)}**.")
    }
}

class RemoveCommand : Command {
    override val name = "remove"
    override val data = Commands.slash("remove", "Remove a track from the queue by position.")
        .addOptions(
            OptionData(OptionType.INTEGER, "position", "Queue position (see /queue)", true)
                .setMinValue(1),
        )

    override fun execute(ctx: CommandContext) {
        val position = ctx.event.getOption("position")!!.asInt
        val removed = ctx.musicManager.scheduler.removeAt(position - 1)
        if (removed == null) {
            ctx.replyEphemeral("There is no track at position $position.")
        } else {
            ctx.reply("Removed **${removed.info.title.ifBlank { "Unknown" }}** from the queue.")
        }
    }
}

class ClearCommand : Command {
    override val name = "clear"
    override val data = Commands.slash("clear", "Clear the queue without stopping the current track.")

    override fun execute(ctx: CommandContext) {
        val size = ctx.musicManager.scheduler.queueSize()
        if (size == 0) {
            ctx.replyEphemeral("The queue is already empty.")
            return
        }
        ctx.musicManager.scheduler.clearQueue()
        ctx.reply("Cleared **$size** track(s) from the queue.")
    }
}
