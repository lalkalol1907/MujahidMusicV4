package com.lalkalol.mujahid.listeners

import com.lalkalol.mujahid.audio.LavalinkManager
import com.lalkalol.mujahid.util.Embeds
import com.lalkalol.mujahid.util.MusicControls
import com.lalkalol.mujahid.util.QueuePages
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import org.slf4j.LoggerFactory

object MusicButtonHandler {
    private val log = LoggerFactory.getLogger(MusicButtonHandler::class.java)

    fun handle(event: ButtonInteractionEvent, lavalink: LavalinkManager) {
        val id = event.componentId
        if (!id.startsWith(MusicControls.PREFIX)) {
            return
        }

        val guild = event.guild
        if (guild == null) {
            event.replyEmbeds(Embeds.warning("This button only works in a server."))
                .setEphemeral(true)
                .queue()
            return
        }

        if (!canControl(event, guild)) {
            event.replyEmbeds(Embeds.warning("Join my voice channel to use these controls."))
                .setEphemeral(true)
                .queue()
            return
        }

        val guildId = guild.idLong
        val manager = lavalink.getOrCreate(guildId)
        val scheduler = manager.scheduler

        val parts = id.substring(MusicControls.PREFIX.length).split(":")
        val action = parts[0]

        when (action) {
            "skip" -> {
                if (scheduler.getCurrent() == null && scheduler.queueSize() == 0) {
                    event.replyEmbeds(Embeds.warning("Nothing is playing.")).setEphemeral(true).queue()
                    return
                }
                val next = scheduler.skip()
                log.info("Button skip in guild {} by {}", guildId, event.user.id)
                if (next == null) {
                    event.replyEmbeds(Embeds.success("Skipped. Queue is empty.")).setEphemeral(true).queue()
                } else {
                    event.replyEmbeds(Embeds.success("Skipped. Now playing **${next.info.title}**."))
                        .setEphemeral(true)
                        .queue()
                }
            }
            "pause" -> {
                manager.getCachedLink()?.createOrUpdatePlayer()?.setPaused(true)?.subscribe()
                log.debug("Button pause in guild {}", guildId)
                event.replyEmbeds(Embeds.success("Paused.")).setEphemeral(true).queue()
            }
            "resume" -> {
                manager.getCachedLink()?.createOrUpdatePlayer()?.setPaused(false)?.subscribe()
                log.debug("Button resume in guild {}", guildId)
                event.replyEmbeds(Embeds.success("Resumed.")).setEphemeral(true).queue()
            }
            "stop" -> {
                scheduler.stop()
                log.info("Button stop in guild {} by {}", guildId, event.user.id)
                event.replyEmbeds(Embeds.success("Stopped and cleared the queue.")).setEphemeral(true).queue()
            }
            "qprev", "qnext" -> {
                if (parts.size < 3) {
                    return
                }
                val currentPage = parts[2].toInt()
                val targetPage = if (action == "qprev") currentPage - 1 else currentPage + 1
                val view = QueuePages.build(guildId, scheduler, targetPage)
                event.editMessageEmbeds(view.embed)
                    .setComponents(view.components)
                    .queue()
            }
        }
    }

    private fun canControl(event: ButtonInteractionEvent, guild: Guild): Boolean {
        val self = guild.selfMember.voiceState?.channel ?: return false
        val member = event.member?.voiceState?.channel ?: return false
        return member.idLong == self.idLong
    }
}
