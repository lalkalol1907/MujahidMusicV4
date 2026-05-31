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
    val lavalink: LavalinkManager,
    val playlists: PlaylistRepository,
) {
    val guild: Guild get() = event.guild!!
    val member: Member get() = event.member!!

    val musicManager: GuildMusicManager get() = lavalink.getOrCreate(guild.idLong)

    val memberVoiceChannel: AudioChannel? get() = member.voiceState?.channel
    val selfVoiceChannel: AudioChannel? get() = guild.selfMember.voiceState?.channel

    fun reply(message: String) = event.replyEmbeds(Embeds.success(message)).queue()

    fun replyInfo(message: String) = event.replyEmbeds(Embeds.info(message)).queue()

    fun replyEphemeral(message: String) =
        event.replyEmbeds(Embeds.warning(message)).setEphemeral(true).queue()

    fun replyError(message: String) =
        event.replyEmbeds(Embeds.error(message)).setEphemeral(true).queue()

    fun ensureConnected(): Boolean {
        val memberChannel = memberVoiceChannel
        if (memberChannel == null) {
            replyEphemeral("You need to be in a voice channel first.")
            return false
        }
        val self = selfVoiceChannel
        if (self == null) {
            event.jda.directAudioController.connect(memberChannel)
            lavalink.getOrCreate(guild.idLong)
            return true
        }
        if (self.idLong != memberChannel.idLong) {
            replyEphemeral("You must be in my voice channel (${self.name}) to do that.")
            return false
        }
        return true
    }

    fun requireActivePlayer(): Boolean {
        val player = musicManager.player
        if (player?.track == null && musicManager.scheduler.current == null) {
            replyEphemeral("Nothing is playing right now.")
            return false
        }
        return true
    }
}
