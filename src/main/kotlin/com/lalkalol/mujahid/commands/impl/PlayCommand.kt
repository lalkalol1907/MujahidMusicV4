package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import com.lalkalol.mujahid.util.Identifiers
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

class PlayCommand : Command {
    override val name = "play"
    override val data = Commands.slash("play", "Play a track from a search query or URL.")
        .addOption(OptionType.STRING, "query", "A search term or a YouTube/SoundCloud/direct URL", true)

    override fun execute(ctx: CommandContext) {
        val query = ctx.event.getOption("query")!!.asString
        loadAndPlay(ctx, Identifiers.fromQuery(query))
    }
}
