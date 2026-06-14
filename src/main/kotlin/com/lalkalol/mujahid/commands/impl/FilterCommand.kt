package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.audio.AudioFilter
import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class FilterCommand : Command {
    override fun name(): String = "filter"

    override fun data(): SlashCommandData =
        Commands.slash("filter", "Apply an audio filter preset.")
            .addOptions(
                OptionData(OptionType.STRING, "preset", "Filter preset to apply", true)
                    .addChoice("Off", "off")
                    .addChoice("Bass Boost", "bassboost")
                    .addChoice("Nightcore", "nightcore")
                    .addChoice("8D", "8d")
                    .addChoice("Karaoke", "karaoke"),
            )

    override suspend fun execute(ctx: CommandContext) {
        if (!ctx.requireActivePlayer()) {
            return
        }
        var preset = AudioFilter.from(ctx.event.getOption("preset")!!.asString)
        if (preset == null) {
            preset = AudioFilter.OFF
        }
        ctx.getMusicManager().scheduler.applyFilter(preset)
        if (preset == AudioFilter.OFF) {
            ctx.reply("Filters cleared.")
        } else {
            ctx.reply("Filter set to **${preset.displayName}**.")
        }
    }
}
