package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import net.dv8tion.jda.api.interactions.commands.build.Commands

class JoinCommand : Command {
    override val name = "join"
    override val data = Commands.slash("join", "Join your current voice channel.")

    override fun execute(ctx: CommandContext) {
        val channel = ctx.memberVoiceChannel
        if (channel == null) {
            ctx.replyEphemeral("You need to be in a voice channel first.")
            return
        }
        ctx.event.jda.directAudioController.connect(channel)
        ctx.lavalink.getOrCreate(ctx.guild.idLong)
        ctx.reply("Joined **${channel.name}**.")
    }
}

class LeaveCommand : Command {
    override val name = "leave"
    override val data = Commands.slash("leave", "Leave the voice channel and clear the queue.")

    override fun execute(ctx: CommandContext) {
        if (ctx.selfVoiceChannel == null) {
            ctx.replyEphemeral("I'm not connected to a voice channel.")
            return
        }
        ctx.lavalink.getExisting(ctx.guild.idLong)?.scheduler?.stop()
        ctx.event.jda.directAudioController.disconnect(ctx.guild)
        ctx.lavalink.destroy(ctx.guild.idLong)
        ctx.reply("Left the voice channel. Bye!")
    }
}
