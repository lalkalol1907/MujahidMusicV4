package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.audio.LoopMode
import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class LoopCommand : Command {
    override fun name(): String = "loop"

    override fun data(): SlashCommandData =
        Commands.slash("loop", "Set the loop mode.")
            .addOptions(
                OptionData(OptionType.STRING, "mode", "What to loop", true)
                    .addChoice("Off", "off")
                    .addChoice("Track", "track")
                    .addChoice("Queue", "queue"),
            )

    override suspend fun execute(ctx: CommandContext) {
        var mode = LoopMode.from(ctx.event.getOption("mode")!!.asString)
        if (mode == null) {
            mode = LoopMode.OFF
        }
        ctx.getMusicManager().scheduler.setLoopMode(mode)
        ctx.reply("Loop mode set to **${mode.name.lowercase()}**.")
    }
}
