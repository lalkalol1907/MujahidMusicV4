package com.lalkalol.mujahid.commands

import com.lalkalol.mujahid.audio.LavalinkManager
import com.lalkalol.mujahid.db.PlaylistRepository
import com.lalkalol.mujahid.util.Embeds
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.slf4j.LoggerFactory

class CommandRegistry(
    private val lavalink: LavalinkManager,
    private val playlists: PlaylistRepository,
) {
    private val log = LoggerFactory.getLogger(CommandRegistry::class.java)
    private val commands = LinkedHashMap<String, Command>()

    fun register(vararg toAdd: Command) {
        toAdd.forEach { commands[it.name] = it }
    }

    fun commandData(): List<SlashCommandData> = commands.values.map { it.data }

    fun handle(event: SlashCommandInteractionEvent) {
        val command = commands[event.name] ?: run {
            event.replyEmbeds(Embeds.error("Unknown command.")).setEphemeral(true).queue()
            return
        }

        if (event.guild == null) {
            event.replyEmbeds(Embeds.warning("This command can only be used in a server."))
                .setEphemeral(true).queue()
            return
        }

        try {
            command.execute(CommandContext(event, lavalink, playlists))
        } catch (e: Exception) {
            log.error("Error while executing /{}", event.name, e)
            val embed = Embeds.error("Something went wrong: ${e.message}")
            if (event.isAcknowledged) {
                event.hook.sendMessageEmbeds(embed).setEphemeral(true).queue()
                return
            }
            event.replyEmbeds(embed).setEphemeral(true).queue()
        }
    }
}
