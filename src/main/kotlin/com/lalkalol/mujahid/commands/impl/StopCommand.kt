package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class StopCommand : Command {
    override fun name(): String = "stop"

    override fun data(): SlashCommandData =
        Commands.slash("stop", "Stop playback and clear the queue.")

    override suspend fun execute(ctx: CommandContext) {
        if (!ctx.requireActivePlayer()) {
            return
        }
        ctx.getMusicManager().scheduler.stop()
        ctx.reply("Stopped playback and cleared the queue.")
    }
}
