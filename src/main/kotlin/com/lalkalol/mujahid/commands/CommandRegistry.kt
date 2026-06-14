package com.lalkalol.mujahid.commands

import com.lalkalol.mujahid.audio.LavalinkManager
import com.lalkalol.mujahid.commands.impl.ClearCommand
import com.lalkalol.mujahid.commands.impl.FilterCommand
import com.lalkalol.mujahid.commands.impl.JoinCommand
import com.lalkalol.mujahid.commands.impl.LeaveCommand
import com.lalkalol.mujahid.commands.impl.LoopCommand
import com.lalkalol.mujahid.commands.impl.NowPlayingCommand
import com.lalkalol.mujahid.commands.impl.PauseCommand
import com.lalkalol.mujahid.commands.impl.PlayCommand
import com.lalkalol.mujahid.commands.impl.PlayFileCommand
import com.lalkalol.mujahid.commands.impl.PlayNextCommand
import com.lalkalol.mujahid.commands.impl.PlaylistCommand
import com.lalkalol.mujahid.commands.impl.QueueCommand
import com.lalkalol.mujahid.commands.impl.RemoveCommand
import com.lalkalol.mujahid.commands.impl.ResumeCommand
import com.lalkalol.mujahid.commands.impl.SeekCommand
import com.lalkalol.mujahid.commands.impl.ShuffleCommand
import com.lalkalol.mujahid.commands.impl.SkipCommand
import com.lalkalol.mujahid.commands.impl.StopCommand
import com.lalkalol.mujahid.commands.impl.VolumeCommand
import com.lalkalol.mujahid.db.PlaylistRepository
import com.lalkalol.mujahid.metrics.BotMetrics
import com.lalkalol.mujahid.util.Embeds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.slf4j.LoggerFactory

class CommandRegistry(
    private val lavalink: LavalinkManager,
    private val playlists: PlaylistRepository,
    private val metrics: BotMetrics,
) {
    private val log = LoggerFactory.getLogger(CommandRegistry::class.java)
    private val commands = linkedMapOf<String, Command>()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun registerDefaults() {
        register(
            PlayCommand(),
            PlayNextCommand(),
            PlayFileCommand(),
            JoinCommand(),
            LeaveCommand(),
            PauseCommand(),
            ResumeCommand(),
            SkipCommand(),
            StopCommand(),
            VolumeCommand(),
            QueueCommand(),
            NowPlayingCommand(),
            LoopCommand(),
            ShuffleCommand(),
            SeekCommand(),
            RemoveCommand(),
            ClearCommand(),
            FilterCommand(),
            PlaylistCommand(),
        )
        log.info("Registered {} slash commands", commands.size)
    }

    fun register(vararg toAdd: Command) {
        for (command in toAdd) {
            commands[command.name()] = command
        }
    }

    fun commandData(): List<SlashCommandData> = commands.values.map { it.data() }

    fun handle(event: SlashCommandInteractionEvent) {
        val command = commands[event.name]
        if (command == null) {
            event.replyEmbeds(Embeds.error("Unknown command.")).setEphemeral(true).queue()
            return
        }

        if (event.guild == null) {
            event.replyEmbeds(Embeds.warning("This command can only be used in a server."))
                .setEphemeral(true)
                .queue()
            return
        }

        val userId = event.user?.id ?: "unknown"
        log.info("Executing /{} in guild {} (user {})", event.name, event.guild!!.id, userId)

        scope.launch {
            try {
                command.execute(CommandContext(event, lavalink, playlists))
                metrics.recordCommandExecution(event.name)
            } catch (e: Exception) {
                log.error("Error while executing /{} in guild {}", event.name, event.guild!!.id, e)
                metrics.recordCommandError(event.name)
                val embed = Embeds.error("Something went wrong: ${e.message}")
                if (event.isAcknowledged) {
                    event.hook.sendMessageEmbeds(embed).setEphemeral(true).queue()
                } else {
                    event.replyEmbeds(embed).setEphemeral(true).queue()
                }
            }
        }
    }
}
