package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class PauseCommand : Command {
    override fun name(): String = "pause"

    override fun data(): SlashCommandData =
        Commands.slash("pause", "Pause the current track.")

    override suspend fun execute(ctx: CommandContext) {
        if (!ctx.requireActivePlayer()) {
            return
        }
        val player = ctx.getMusicManager().getPlayer()
        if (player != null && player.paused) {
            ctx.replyEphemeral("Already paused.")
            return
        }
        ctx.getMusicManager().getCachedLink()?.createOrUpdatePlayer()?.setPaused(true)?.subscribe()
        ctx.reply("Paused.")
    }
}
