package com.lalkalol.mujahid.commands

import com.lalkalol.mujahid.audio.LavalinkManager
import com.lalkalol.mujahid.commands.impl.LoopCommand
import com.lalkalol.mujahid.db.PlaylistRepository
import com.lalkalol.mujahid.metrics.BotMetrics
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class CommandRegistryTest {
    private val lavalink: LavalinkManager = mock()
    private val playlists: PlaylistRepository = mock()
    private val metrics: BotMetrics = mock()
    private val event: SlashCommandInteractionEvent = mock()
    private lateinit var registry: CommandRegistry

    @BeforeEach
    fun setUp() {
        registry = CommandRegistry(lavalink, playlists, metrics)
        registry.register(LoopCommand())
    }

    @Test
    fun handle_unknownCommandRepliesWithError() {
        whenever(event.name).thenReturn("nope")
        val reply = mock<ReplyCallbackAction>()
        whenever(event.replyEmbeds(any<MessageEmbed>())).thenReturn(reply)
        whenever(reply.setEphemeral(true)).thenReturn(reply)

        registry.handle(event)

        verify(event).replyEmbeds(any<MessageEmbed>())
        verify(reply).setEphemeral(true)
    }

    @Test
    fun handle_dmUsageIsRejected() {
        whenever(event.name).thenReturn("loop")
        whenever(event.guild).thenReturn(null)
        val reply = mock<ReplyCallbackAction>()
        whenever(event.replyEmbeds(any<MessageEmbed>())).thenReturn(reply)
        whenever(reply.setEphemeral(true)).thenReturn(reply)

        registry.handle(event)

        verify(event).replyEmbeds(any<MessageEmbed>())
    }

    @Test
    fun handle_commandExceptionSendsErrorEmbed() = runBlocking {
        whenever(event.name).thenReturn("loop")
        whenever(event.guild).thenReturn(mock<Guild>())

        val reply = mock<ReplyCallbackAction>()
        whenever(event.replyEmbeds(any<MessageEmbed>())).thenReturn(reply)
        whenever(reply.setEphemeral(true)).thenReturn(reply)

        registry.handle(event)
        delay(200)

        verify(event).replyEmbeds(any<MessageEmbed>())
    }

    @Test
    fun handle_acknowledgedExceptionUsesHook() = runBlocking {
        whenever(event.name).thenReturn("loop")
        whenever(event.guild).thenReturn(mock<Guild>())
        whenever(event.isAcknowledged).thenReturn(true)

        val hook = CommandTestFixtures.mockHook()
        whenever(event.hook).thenReturn(hook)

        registry.handle(event)
        delay(200)

        verify(hook).sendMessageEmbeds(any(), any())
        verify(event, never()).replyEmbeds(any<MessageEmbed>())
    }
}
