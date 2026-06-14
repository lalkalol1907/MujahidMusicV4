package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import com.lalkalol.mujahid.metrics.MetricsHolder
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class JoinCommand : Command {
    override fun name(): String = "join"

    override fun data(): SlashCommandData =
        Commands.slash("join", "Join your current voice channel.")

    override suspend fun execute(ctx: CommandContext) {
        val channel = ctx.getMemberVoiceChannel()
        if (channel == null) {
            ctx.replyEphemeral("You need to be in a voice channel first.")
            return
        }
        ctx.event.jda.directAudioController.connect(channel)
        ctx.getLavalink().getOrCreate(ctx.getGuild().idLong)
        MetricsHolder.get().recordVoiceSession("join")
        MetricsHolder.get().setActivePlayers(ctx.getLavalink().activePlayerCount())
        ctx.reply("Joined **${channel.name}**.")
    }
}
