package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import com.lalkalol.mujahid.util.Identifiers
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class PlayCommand : Command {
    override fun name(): String = "play"

    override fun data(): SlashCommandData =
        Commands.slash("play", "Play a track from a search query or URL.")
            .addOption(OptionType.STRING, "query", "A search term or a YouTube/SoundCloud/direct URL", true)

    override suspend fun execute(ctx: CommandContext) {
        val query = ctx.event.getOption("query")!!.asString
        PlaySupport.loadAndPlay(ctx, Identifiers.fromQuery(query))
    }
}
