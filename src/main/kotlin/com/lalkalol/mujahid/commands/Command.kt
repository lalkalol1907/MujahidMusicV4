package com.lalkalol.mujahid.commands

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

interface Command {
    val name: String
    val data: SlashCommandData

    fun execute(ctx: CommandContext)
}
