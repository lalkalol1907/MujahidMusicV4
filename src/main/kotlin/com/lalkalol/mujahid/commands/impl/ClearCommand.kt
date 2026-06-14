package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class ClearCommand : Command {
    override fun name(): String = "clear"

    override fun data(): SlashCommandData =
        Commands.slash("clear", "Clear the queue without stopping the current track.")

    override suspend fun execute(ctx: CommandContext) {
        val size = ctx.getMusicManager().scheduler.queueSize()
        if (size == 0) {
            ctx.replyEphemeral("The queue is already empty.")
            return
        }
        ctx.getMusicManager().scheduler.clearQueue()
        ctx.reply("Cleared **$size** track(s) from the queue.")
    }
}
