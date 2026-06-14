package com.lalkalol.mujahid.commands

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify

class CommandContextTest {
    @Test
    fun ensureConnected_rejectsWhenMemberNotInVoice() {
        val h = CommandTestFixtures.harness().memberNotInVoice()

        assertFalse(h.context().ensureConnected())
        assertTrue(h.lastEphemeralDescription().contains("voice channel"))
    }

    @Test
    fun ensureConnected_connectsWhenBotIsNotInVoice() {
        val h = CommandTestFixtures.harness()
            .memberInVoice(1L, "General")
            .botNotInVoice()

        assertTrue(h.context().ensureConnected())
        verify(h.audioController).connect(
            argThat { channel -> channel != null && channel.idLong == 1L },
        )
        verify(h.lavalink).getOrCreate(CommandTestFixtures.GUILD_ID)
    }

    @Test
    fun ensureConnected_rejectsWhenInDifferentChannels() {
        val h = CommandTestFixtures.harness()
            .memberInVoice(1L, "General")
            .botInVoice(2L, "Music")

        assertFalse(h.context().ensureConnected())
        assertTrue(h.lastEphemeralDescription().contains("Music"))
    }

    @Test
    fun ensureConnected_allowsWhenInSameChannel() {
        val h = CommandTestFixtures.harness().bothInVoice(1L, "General")

        assertTrue(h.context().ensureConnected())
    }

    @Test
    fun requireActivePlayer_rejectsWhenNothingPlaying() {
        val h = CommandTestFixtures.harness().withNoActivePlayer()

        assertFalse(h.context().requireActivePlayer())
        assertTrue(h.lastEphemeralDescription().contains("Nothing is playing"))
    }

    @Test
    fun requireActivePlayer_allowsWhenTrackIsActive() {
        val h = CommandTestFixtures.harness().withActivePlayer()

        assertTrue(h.context().requireActivePlayer())
    }
}
