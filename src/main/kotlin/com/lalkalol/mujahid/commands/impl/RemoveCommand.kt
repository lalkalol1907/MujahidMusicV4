package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class RemoveCommand : Command {
    override fun name(): String = "remove"

    override fun data(): SlashCommandData =
        Commands.slash("remove", "Remove a track from the queue by position.")
            .addOptions(
                OptionData(OptionType.INTEGER, "position", "Queue position (see /queue)", true)
                    .setMinValue(1),
            )

    override suspend fun execute(ctx: CommandContext) {
        val position = ctx.event.getOption("position")!!.asInt
        val removed = ctx.getMusicManager().scheduler.removeAt(position - 1)
        if (removed == null) {
            ctx.replyEphemeral("There is no track at position $position.")
        } else {
            val title = if (removed.info.title.isBlank()) "Unknown" else removed.info.title
            ctx.reply("Removed **$title** from the queue.")
        }
    }
}
