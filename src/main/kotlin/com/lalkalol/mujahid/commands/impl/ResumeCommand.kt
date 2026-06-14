package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class ResumeCommand : Command {
    override fun name(): String = "resume"

    override fun data(): SlashCommandData =
        Commands.slash("resume", "Resume playback.")

    override suspend fun execute(ctx: CommandContext) {
        if (!ctx.requireActivePlayer()) {
            return
        }
        val player = ctx.getMusicManager().getPlayer()
        if (player != null && !player.paused) {
            ctx.replyEphemeral("Already playing.")
            return
        }
        ctx.getMusicManager().getCachedLink()?.createOrUpdatePlayer()?.setPaused(false)?.subscribe()
        ctx.reply("Resumed.")
    }
}
