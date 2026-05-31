package com.lalkalol.mujahid.listeners

import com.lalkalol.mujahid.audio.LavalinkManager
import com.lalkalol.mujahid.commands.CommandRegistry
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

class InteractionListener(
    private val registry: CommandRegistry,
    private val lavalink: LavalinkManager,
    private val devGuildId: Long?,
) : ListenerAdapter() {
    private val log = LoggerFactory.getLogger(InteractionListener::class.java)

    override fun onReady(event: ReadyEvent) {
        log.info("Logged in as {}", event.jda.selfUser.asTag)
        val commands = registry.commandData()

        if (devGuildId == null) {
            event.jda.updateCommands().addCommands(commands).queue {
                log.info("Registered {} commands globally", commands.size)
            }
            return
        }

        val guild = event.jda.getGuildById(devGuildId)
        if (guild != null) {
            guild.updateCommands().addCommands(commands).queue {
                log.info("Registered {} commands to dev guild {}", commands.size, devGuildId)
            }
            return
        }
        log.warn("DEV_GUILD_ID {} not found; registering commands globally instead", devGuildId)
        event.jda.updateCommands().addCommands(commands).queue()
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        registry.handle(event)
    }

    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        val guild = event.guild
        val selfId = guild.selfMember.idLong

        // The bot itself was moved/disconnected: drop our local state.
        if (event.member.idLong == selfId) {
            if (event.channelJoined == null) {
                lavalink.destroy(guild.idLong)
            }
            return
        }

        // A user left; if the bot is now alone in its channel, leave automatically.
        val selfChannel = guild.selfMember.voiceState?.channel ?: return
        if (event.channelLeft?.idLong != selfChannel.idLong) return

        val remainingHumans = selfChannel.members.count { !it.user.isBot }
        if (remainingHumans == 0) {
            log.info("Bot alone in '{}' (guild {}); leaving", selfChannel.name, guild.idLong)
            lavalink.getExisting(guild.idLong)?.scheduler?.stop()
            guild.jda.directAudioController.disconnect(guild)
            lavalink.destroy(guild.idLong)
        }
    }
}
