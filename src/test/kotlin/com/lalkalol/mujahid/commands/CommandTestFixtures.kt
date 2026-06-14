package com.lalkalol.mujahid.commands

import com.lalkalol.mujahid.audio.GuildMusicManager
import com.lalkalol.mujahid.audio.LavalinkManager
import com.lalkalol.mujahid.audio.TrackScheduler
import dev.arbjerg.lavalink.client.player.LavalinkPlayer
import dev.arbjerg.lavalink.client.player.Track
import dev.arbjerg.lavalink.protocol.v4.TrackInfo
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.SelfMember
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.managers.DirectAudioController
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

object CommandTestFixtures {
    const val GUILD_ID: Long = 42L
    const val USER_ID: Long = 100L
    const val CHANNEL_ID: Long = 200L

    class Harness {
        val event: SlashCommandInteractionEvent = mock()
        val lavalink: LavalinkManager = mock()
        val playlists: com.lalkalol.mujahid.db.PlaylistRepository = mock()
        val musicManager: GuildMusicManager = mock()
        val scheduler: TrackScheduler = mock()
        val guild: Guild = mock()
        val member: Member = mock()
        val selfMember: SelfMember = mock()
        val user: User = mock()
        val jda: JDA = mock()
        val audioController: DirectAudioController = mock()
        val replyAction: ReplyCallbackAction = mock()

        private val replies = mutableListOf<MessageEmbed>()
        private val ephemeralReplies = mutableListOf<MessageEmbed>()

        init {
            whenever(event.guild).thenReturn(guild)
            whenever(event.member).thenReturn(member)
            whenever(event.user).thenReturn(user)
            whenever(event.jda).thenReturn(jda)
            whenever(event.channelIdLong).thenReturn(CHANNEL_ID)
            whenever(user.idLong).thenReturn(USER_ID)
            whenever(jda.directAudioController).thenReturn(audioController)
            whenever(guild.idLong).thenReturn(GUILD_ID)
            whenever(guild.selfMember).thenReturn(selfMember)
            whenever(lavalink.getOrCreate(GUILD_ID)).thenReturn(musicManager)
            whenever(musicManager.scheduler).thenReturn(scheduler)

            whenever(event.replyEmbeds(any<MessageEmbed>())).doAnswer { invocation ->
                replies.add(invocation.getArgument(0))
                replyAction
            }
            whenever(event.replyEmbeds(any(), any())).doAnswer { invocation ->
                replies.add(invocation.getArgument(0))
                replyAction
            }
            whenever(replyAction.setEphemeral(true)).doAnswer {
                if (replies.isNotEmpty()) {
                    ephemeralReplies.add(replies.last())
                }
                replyAction
            }
            doNothing().whenever(replyAction).queue()
            doNothing().whenever(replyAction).queue(any())
        }

        fun context(): CommandContext = CommandContext(event, lavalink, playlists)

        fun lastReplyDescription(): String = replies.last().description ?: ""

        fun lastEphemeralDescription(): String = ephemeralReplies.last().description ?: ""

        fun replyCount(): Int = replies.size

        fun memberNotInVoice(): Harness {
            whenever(member.voiceState).thenReturn(null)
            return this
        }

        fun memberInVoice(channelId: Long, name: String): Harness {
            val voiceState = mock<net.dv8tion.jda.api.entities.GuildVoiceState>()
            val channel = voiceChannel(channelId, name)
            whenever(member.voiceState).thenReturn(voiceState)
            whenever(voiceState.channel).thenReturn(channel)
            return this
        }

        fun botNotInVoice(): Harness {
            whenever(selfMember.voiceState).thenReturn(null)
            return this
        }

        fun botInVoice(channelId: Long, name: String): Harness {
            val voiceState = mock<net.dv8tion.jda.api.entities.GuildVoiceState>()
            val channel = voiceChannel(channelId, name)
            whenever(selfMember.voiceState).thenReturn(voiceState)
            whenever(voiceState.channel).thenReturn(channel)
            return this
        }

        fun bothInVoice(channelId: Long, name: String): Harness =
            memberInVoice(channelId, name).botInVoice(channelId, name)

        fun withStringOption(name: String, value: String): Harness {
            val option = mock<OptionMapping>()
            whenever(event.getOption(name)).thenReturn(option)
            whenever(option.asString).thenReturn(value)
            return this
        }

        fun withIntOption(name: String, value: Int): Harness {
            val option = mock<OptionMapping>()
            whenever(event.getOption(name)).thenReturn(option)
            whenever(option.asInt).thenReturn(value)
            return this
        }

        fun withActivePlayer(): Harness {
            val player = mock<LavalinkPlayer>()
            val track = mock<Track>()
            whenever(musicManager.getPlayer()).thenReturn(player)
            whenever(player.track).thenReturn(track)
            whenever(scheduler.getCurrent()).thenReturn(null)
            return this
        }

        fun withNoActivePlayer(): Harness {
            whenever(musicManager.getPlayer()).thenReturn(null)
            whenever(scheduler.getCurrent()).thenReturn(null)
            return this
        }

        fun withPausedPlayer(): Harness {
            val player = mock<LavalinkPlayer>()
            val track = mock<Track>()
            whenever(musicManager.getPlayer()).thenReturn(player)
            whenever(player.track).thenReturn(track)
            whenever(player.paused).thenReturn(true)
            return this
        }

        fun withPlayingPlayer(): Harness {
            val player = mock<LavalinkPlayer>()
            val track = mock<Track>()
            whenever(musicManager.getPlayer()).thenReturn(player)
            whenever(player.track).thenReturn(track)
            whenever(player.paused).thenReturn(false)
            return this
        }

        fun mockTrack(title: String): Track {
            val track = mock<Track>()
            val info = mock<TrackInfo>()
            whenever(track.info).thenReturn(info)
            whenever(info.title).thenReturn(title)
            whenever(info.uri).thenReturn("https://example.com/$title")
            whenever(track.encoded).thenReturn("encoded-$title")
            return track
        }

        private fun voiceChannel(channelId: Long, name: String): AudioChannelUnion {
            val channel = mock<AudioChannelUnion>()
            whenever(channel.idLong).thenReturn(channelId)
            whenever(channel.name).thenReturn(name)
            return channel
        }
    }

    fun harness(): Harness = Harness()

    fun mockHook(): InteractionHook {
        val hook = mock<InteractionHook>()
        val hookAction = mock<WebhookMessageCreateAction<net.dv8tion.jda.api.entities.Message>>()
        whenever(hook.sendMessageEmbeds(any(), any())).thenReturn(hookAction)
        whenever(hookAction.setEphemeral(true)).thenReturn(hookAction)
        doNothing().whenever(hookAction).queue()
        return hook
    }
}
