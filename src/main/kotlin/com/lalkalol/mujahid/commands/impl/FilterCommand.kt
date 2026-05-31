package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.audio.AudioFilter
import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class FilterCommand : Command {
    override val name = "filter"
    override val data = Commands.slash("filter", "Apply an audio filter preset.")
        .addOptions(
            OptionData(OptionType.STRING, "preset", "Filter preset to apply", true)
                .addChoice("Off", "off")
                .addChoice("Bass Boost", "bassboost")
                .addChoice("Nightcore", "nightcore")
                .addChoice("8D", "8d")
                .addChoice("Karaoke", "karaoke"),
        )

    override fun execute(ctx: CommandContext) {
        if (!ctx.requireActivePlayer()) return
        val preset = AudioFilter.from(ctx.event.getOption("preset")!!.asString) ?: AudioFilter.OFF
        ctx.musicManager.scheduler.applyFilter(preset)
        if (preset == AudioFilter.OFF) {
            ctx.reply("Filters cleared.")
        } else {
            ctx.reply("Filter set to **${preset.displayName}**.")
        }
    }
}
