package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.CommandTestFixtures
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ConnectionCommandsTest {
    @Test
    fun joinCommand_requiresMemberVoiceChannel() = runBlocking {
        val h = CommandTestFixtures.harness().memberNotInVoice()

        JoinCommand().execute(h.context())

        assertTrue(h.lastEphemeralDescription().contains("voice channel"))
    }

    @Test
    fun joinCommand_connectsAndRegistersManager() = runBlocking {
        val h = CommandTestFixtures.harness().memberInVoice(5L, "Lounge")

        JoinCommand().execute(h.context())

        verify(h.audioController).connect(any())
        verify(h.lavalink).getOrCreate(CommandTestFixtures.GUILD_ID)
        assertTrue(h.lastReplyDescription().contains("Lounge"))
    }

    @Test
    fun leaveCommand_rejectsWhenBotNotConnected() = runBlocking {
        val h = CommandTestFixtures.harness().botNotInVoice()

        LeaveCommand().execute(h.context())

        assertTrue(h.lastEphemeralDescription().contains("not connected"))
    }

    @Test
    fun leaveCommand_stopsPlaybackAndDisconnects() = runBlocking {
        val h = CommandTestFixtures.harness().botInVoice(1L, "General")
        whenever(h.lavalink.getExisting(CommandTestFixtures.GUILD_ID)).thenReturn(h.musicManager)

        LeaveCommand().execute(h.context())

        verify(h.scheduler).stop()
        verify(h.audioController).disconnect(h.guild)
        verify(h.lavalink).destroy(CommandTestFixtures.GUILD_ID)
        assertTrue(h.lastReplyDescription().contains("Bye"))
    }
}
