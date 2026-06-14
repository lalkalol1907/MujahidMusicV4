package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.audio.AudioFilter
import com.lalkalol.mujahid.audio.LoopMode
import com.lalkalol.mujahid.commands.CommandTestFixtures
import dev.arbjerg.lavalink.client.Link
import dev.arbjerg.lavalink.client.player.PlayerUpdateBuilder
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PlaybackCommandsTest {
    @Test
    fun loopCommand_setsModeAndReplies() = runBlocking {
        val h = CommandTestFixtures.harness().withStringOption("mode", "queue")

        LoopCommand().execute(h.context())

        verify(h.scheduler).setLoopMode(LoopMode.QUEUE)
        assertTrue(h.lastReplyDescription().contains("queue"))
    }

    @Test
    fun clearCommand_rejectsEmptyQueue() = runBlocking {
        val h = CommandTestFixtures.harness()
        whenever(h.scheduler.queueSize()).thenReturn(0)

        ClearCommand().execute(h.context())

        assertTrue(h.lastEphemeralDescription().contains("already empty"))
    }

    @Test
    fun clearCommand_clearsQueueAndReportsCount() = runBlocking {
        val h = CommandTestFixtures.harness()
        whenever(h.scheduler.queueSize()).thenReturn(3)

        ClearCommand().execute(h.context())

        verify(h.scheduler).clearQueue()
        assertTrue(h.lastReplyDescription().contains("3"))
    }

    @Test
    fun removeCommand_reportsMissingPosition() = runBlocking {
        val h = CommandTestFixtures.harness().withIntOption("position", 5)
        whenever(h.scheduler.removeAt(4)).thenReturn(null)

        RemoveCommand().execute(h.context())

        assertTrue(h.lastEphemeralDescription().contains("position 5"))
    }

    @Test
    fun removeCommand_removesTrackByTitle() = runBlocking {
        val h = CommandTestFixtures.harness().withIntOption("position", 1)
        val removed = h.mockTrack("Cool Song")
        whenever(h.scheduler.removeAt(0)).thenReturn(removed)

        RemoveCommand().execute(h.context())

        assertTrue(h.lastReplyDescription().contains("Cool Song"))
    }

    @Test
    fun volumeCommand_requiresActivePlayer() = runBlocking {
        val h = CommandTestFixtures.harness()
            .withIntOption("level", 80)
            .withNoActivePlayer()

        VolumeCommand().execute(h.context())

        assertTrue(h.lastEphemeralDescription().contains("Nothing is playing"))
    }

    @Test
    fun volumeCommand_clampsAndAppliesLevel() = runBlocking {
        val h = CommandTestFixtures.harness()
            .withIntOption("level", 250)
            .withActivePlayer()

        VolumeCommand().execute(h.context())

        verify(h.scheduler).applyVolume(200)
        assertTrue(h.lastReplyDescription().contains("200%"))
    }

    @Test
    fun pauseCommand_rejectsWhenAlreadyPaused() = runBlocking {
        val h = CommandTestFixtures.harness().withPausedPlayer()

        PauseCommand().execute(h.context())

        assertTrue(h.lastEphemeralDescription().contains("Already paused"))
    }

    @Test
    fun pauseCommand_pausesActivePlayback() = runBlocking {
        val h = CommandTestFixtures.harness().withPlayingPlayer()
        val link = mock<Link>()
        val builder = mock<PlayerUpdateBuilder>()
        whenever(h.musicManager.getCachedLink()).thenReturn(link)
        whenever(link.createOrUpdatePlayer()).thenReturn(builder)
        whenever(builder.setPaused(true)).thenReturn(builder)

        PauseCommand().execute(h.context())

        verify(builder).setPaused(true)
        assertEquals("Paused.", h.lastReplyDescription())
    }

    @Test
    fun filterCommand_clearsWhenOffSelected() = runBlocking {
        val h = CommandTestFixtures.harness()
            .withStringOption("preset", "off")
            .withActivePlayer()

        FilterCommand().execute(h.context())

        verify(h.scheduler).applyFilter(AudioFilter.OFF)
        assertEquals("Filters cleared.", h.lastReplyDescription())
    }

    @Test
    fun filterCommand_appliesNamedPreset() = runBlocking {
        val h = CommandTestFixtures.harness()
            .withStringOption("preset", "nightcore")
            .withActivePlayer()

        FilterCommand().execute(h.context())

        verify(h.scheduler).applyFilter(AudioFilter.NIGHTCORE)
        assertTrue(h.lastReplyDescription().contains("Nightcore"))
    }

    @Test
    fun skipCommand_requiresConnectionAndActivePlayer() = runBlocking {
        val h = CommandTestFixtures.harness()
            .memberNotInVoice()
            .withActivePlayer()

        SkipCommand().execute(h.context())

        assertTrue(h.lastEphemeralDescription().contains("voice channel"))
    }

    @Test
    fun skipCommand_reportsEmptyQueueAfterSkip() = runBlocking {
        val h = CommandTestFixtures.harness()
            .bothInVoice(1L, "General")
            .withActivePlayer()
        whenever(h.scheduler.skip()).thenReturn(null)

        SkipCommand().execute(h.context())

        assertTrue(h.lastReplyDescription().contains("queue is now empty"))
    }

    @Test
    fun skipCommand_reportsNextTrack() = runBlocking {
        val h = CommandTestFixtures.harness()
            .bothInVoice(1L, "General")
            .withActivePlayer()
        val next = h.mockTrack("Next Up")
        whenever(h.scheduler.skip()).thenReturn(next)

        SkipCommand().execute(h.context())

        assertTrue(h.lastReplyDescription().contains("Next Up"))
    }
}
