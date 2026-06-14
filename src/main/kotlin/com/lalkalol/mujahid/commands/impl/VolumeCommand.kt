package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class VolumeCommand : Command {
    override fun name(): String = "volume"

    override fun data(): SlashCommandData =
        Commands.slash("volume", "Set the playback volume (0-200).")
            .addOptions(
                OptionData(OptionType.INTEGER, "level", "Volume percentage from 0 to 200", true)
                    .setRequiredRange(0, 200),
            )

    override suspend fun execute(ctx: CommandContext) {
        if (!ctx.requireActivePlayer()) {
            return
        }
        val level = ctx.event.getOption("level")!!.asInt.coerceIn(0, 200)
        ctx.getMusicManager().scheduler.applyVolume(level)
        ctx.reply("Volume set to **$level%**.")
    }
}
