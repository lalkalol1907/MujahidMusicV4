package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import com.lalkalol.mujahid.util.Embeds
import com.lalkalol.mujahid.util.Format
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class NowPlayingCommand : Command {
    override fun name(): String = "nowplaying"

    override fun data(): SlashCommandData =
        Commands.slash("nowplaying", "Show what is currently playing.")

    override suspend fun execute(ctx: CommandContext) {
        if (!ctx.requireActivePlayer()) {
            return
        }
        val player = ctx.getMusicManager().getPlayer()
        val track = player?.track ?: ctx.getMusicManager().scheduler.getCurrent()
        if (track == null) {
            ctx.replyEphemeral("Nothing is playing right now.")
            return
        }

        val info = track.info
        val position = player?.position ?: 0L
        val bar = Format.progressBar(position, info.length)
        val timeline = "${Format.duration(position)} / ${Format.duration(info.length, info.isStream)}"

        val scheduler = ctx.getMusicManager().scheduler
        val state = if (player != null && player.paused) "Paused" else "Playing"

        val embed = Embeds.music()
            .setAuthor("Now playing")
            .setTitle(if (info.title.isBlank()) "Unknown" else info.title, info.uri)
            .setDescription("by ${info.author}\n\n$bar\n`$timeline`")
            .setFooter("$state • Loop: ${scheduler.getLoopMode()} • Filter: ${scheduler.getFilter().displayName}")
            .build()

        ctx.event.replyEmbeds(embed).queue()
    }
}
