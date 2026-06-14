package com.lalkalol.mujahid.commands

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

interface Command {
    fun name(): String
    fun data(): SlashCommandData
    suspend fun execute(ctx: CommandContext)
}
