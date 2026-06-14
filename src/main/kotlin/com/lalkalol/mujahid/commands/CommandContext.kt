package com.lalkalol.mujahid.commands

import com.lalkalol.mujahid.audio.GuildMusicManager
import com.lalkalol.mujahid.audio.LavalinkManager
import com.lalkalol.mujahid.db.PlaylistRepository
import com.lalkalol.mujahid.util.Embeds
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class CommandContext(
    val event: SlashCommandInteractionEvent,
    private val lavalink: LavalinkManager,
    private val playlists: PlaylistRepository,
) {
    fun getLavalink(): LavalinkManager = lavalink

    fun getPlaylists(): PlaylistRepository = playlists

    fun getGuild(): Guild = event.guild!!

    fun getMember(): Member = event.member!!

    fun getMusicManager(): GuildMusicManager = lavalink.getOrCreate(getGuild().idLong)

    fun getMemberVoiceChannel(): AudioChannel? = getMember().voiceState?.channel

    fun getSelfVoiceChannel(): AudioChannel? = getGuild().selfMember.voiceState?.channel

    fun reply(message: String) {
        event.replyEmbeds(Embeds.success(message)).queue()
    }

    fun replyInfo(message: String) {
        event.replyEmbeds(Embeds.info(message)).queue()
    }

    fun replyEphemeral(message: String) {
        event.replyEmbeds(Embeds.warning(message)).setEphemeral(true).queue()
    }

    fun replyError(message: String) {
        event.replyEmbeds(Embeds.error(message)).setEphemeral(true).queue()
    }

    fun ensureConnected(): Boolean {
        val memberChannel = getMemberVoiceChannel()
        if (memberChannel == null) {
            replyEphemeral("You need to be in a voice channel first.")
            return false
        }
        val self = getSelfVoiceChannel()
        if (self == null) {
            event.jda.directAudioController.connect(memberChannel)
            lavalink.getOrCreate(getGuild().idLong)
            return true
        }
        if (self.idLong != memberChannel.idLong) {
            replyEphemeral("You must be in my voice channel (${self.name}) to do that.")
            return false
        }
        return true
    }

    fun requireActivePlayer(): Boolean {
        val player = getMusicManager().getPlayer()
        if (player == null || (player.track == null && getMusicManager().scheduler.getCurrent() == null)) {
            replyEphemeral("Nothing is playing right now.")
            return false
        }
        return true
    }
}
