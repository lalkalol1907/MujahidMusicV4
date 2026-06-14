package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import com.lalkalol.mujahid.util.Identifiers
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class PlayNextCommand : Command {
    override fun name(): String = "playnext"

    override fun data(): SlashCommandData =
        Commands.slash("playnext", "Play a track next in the queue (after the current song).")
            .addOption(OptionType.STRING, "query", "A search term or a YouTube/SoundCloud/direct URL", true)

    override suspend fun execute(ctx: CommandContext) {
        val query = ctx.event.getOption("query")!!.asString
        PlaySupport.loadAndEnqueueNext(ctx, Identifiers.fromQuery(query))
    }
}
