package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class ShuffleCommand : Command {
    override fun name(): String = "shuffle"

    override fun data(): SlashCommandData =
        Commands.slash("shuffle", "Shuffle the queue.")

    override suspend fun execute(ctx: CommandContext) {
        if (ctx.getMusicManager().scheduler.shuffle()) {
            ctx.reply("Shuffled the queue.")
        } else {
            ctx.replyEphemeral("Need at least 2 tracks in the queue to shuffle.")
        }
    }
}
