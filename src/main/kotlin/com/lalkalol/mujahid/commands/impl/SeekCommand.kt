package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import com.lalkalol.mujahid.util.Format
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class SeekCommand : Command {
    override fun name(): String = "seek"

    override fun data(): SlashCommandData =
        Commands.slash("seek", "Seek to a position in the current track.")
            .addOption(OptionType.STRING, "position", "Timestamp like 90, 1:30 or 1:02:03", true)

    override suspend fun execute(ctx: CommandContext) {
        if (!ctx.requireActivePlayer()) {
            return
        }
        val player = ctx.getMusicManager().getPlayer()
        val track = player?.track ?: ctx.getMusicManager().scheduler.getCurrent()
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
            ctx.replyEphemeral(
                "That position is past the end of the track (${Format.duration(track.info.length)}).",
            )
            return
        }

        ctx.getMusicManager().getCachedLink()?.createOrUpdatePlayer()?.setPosition(ms)?.subscribe()
        ctx.reply("Seeked to **${Format.duration(ms)}**.")
    }
}
