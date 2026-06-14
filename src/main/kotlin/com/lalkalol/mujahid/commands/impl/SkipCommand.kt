package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class SkipCommand : Command {
    override fun name(): String = "skip"

    override fun data(): SlashCommandData =
        Commands.slash("skip", "Skip the current track.")

    override suspend fun execute(ctx: CommandContext) {
        if (!ctx.ensureConnected()) {
            return
        }
        if (!ctx.requireActivePlayer()) {
            return
        }
        val next = ctx.getMusicManager().scheduler.skip()
        if (next == null) {
            ctx.reply("Skipped. The queue is now empty.")
        } else {
            val title = if (next.info.title.isBlank()) "Unknown" else next.info.title
            ctx.reply("Skipped. Now playing **$title**.")
        }
    }
}
