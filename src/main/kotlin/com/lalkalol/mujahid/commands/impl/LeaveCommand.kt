package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import com.lalkalol.mujahid.metrics.MetricsHolder
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class LeaveCommand : Command {
    override fun name(): String = "leave"

    override fun data(): SlashCommandData =
        Commands.slash("leave", "Leave the voice channel and clear the queue.")

    override suspend fun execute(ctx: CommandContext) {
        if (ctx.getSelfVoiceChannel() == null) {
            ctx.replyEphemeral("I'm not connected to a voice channel.")
            return
        }
        ctx.getLavalink().getExisting(ctx.getGuild().idLong)?.scheduler?.stop()
        ctx.event.jda.directAudioController.disconnect(ctx.getGuild())
        ctx.getLavalink().destroy(ctx.getGuild().idLong)
        MetricsHolder.get().recordVoiceSession("leave")
        MetricsHolder.get().setActivePlayers(ctx.getLavalink().activePlayerCount())
        ctx.reply("Left the voice channel. Bye!")
    }
}
