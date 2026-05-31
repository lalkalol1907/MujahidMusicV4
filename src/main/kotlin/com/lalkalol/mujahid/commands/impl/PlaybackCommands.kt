package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

class PauseCommand : Command {
    override val name = "pause"
    override val data = Commands.slash("pause", "Pause the current track.")

    override fun execute(ctx: CommandContext) {
        if (!ctx.requireActivePlayer()) return
        val player = ctx.musicManager.player
        if (player != null && player.paused) {
            ctx.replyEphemeral("Already paused.")
            return
        }
        ctx.musicManager.cachedLink?.createOrUpdatePlayer()?.setPaused(true)?.subscribe()
        ctx.reply("Paused.")
    }
}

class ResumeCommand : Command {
    override val name = "resume"
    override val data = Commands.slash("resume", "Resume playback.")

    override fun execute(ctx: CommandContext) {
        if (!ctx.requireActivePlayer()) return
        val player = ctx.musicManager.player
        if (player != null && !player.paused) {
            ctx.replyEphemeral("Already playing.")
            return
        }
        ctx.musicManager.cachedLink?.createOrUpdatePlayer()?.setPaused(false)?.subscribe()
        ctx.reply("Resumed.")
    }
}

class SkipCommand : Command {
    override val name = "skip"
    override val data = Commands.slash("skip", "Skip the current track.")

    override fun execute(ctx: CommandContext) {
        if (!ctx.ensureConnected()) return
        if (!ctx.requireActivePlayer()) return
        val next = ctx.musicManager.scheduler.skip()
        if (next == null) {
            ctx.reply("Skipped. The queue is now empty.")
        } else {
            ctx.reply("Skipped. Now playing **${next.info.title.ifBlank { "Unknown" }}**.")
        }
    }
}

class StopCommand : Command {
    override val name = "stop"
    override val data = Commands.slash("stop", "Stop playback and clear the queue.")

    override fun execute(ctx: CommandContext) {
        if (!ctx.requireActivePlayer()) return
        ctx.musicManager.scheduler.stop()
        ctx.reply("Stopped playback and cleared the queue.")
    }
}

class VolumeCommand : Command {
    override val name = "volume"
    override val data = Commands.slash("volume", "Set the playback volume (0-200).")
        .addOptions(
            net.dv8tion.jda.api.interactions.commands.build.OptionData(
                OptionType.INTEGER,
                "level",
                "Volume percentage from 0 to 200",
                true,
            ).setRequiredRange(0, 200),
        )

    override fun execute(ctx: CommandContext) {
        if (!ctx.requireActivePlayer()) return
        val level = ctx.event.getOption("level")!!.asInt.coerceIn(0, 200)
        ctx.musicManager.scheduler.applyVolume(level)
        ctx.reply("Volume set to **$level%**.")
    }
}
