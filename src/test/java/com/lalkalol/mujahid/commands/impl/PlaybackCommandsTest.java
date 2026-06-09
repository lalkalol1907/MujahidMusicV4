package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.audio.AudioFilter;
import com.lalkalol.mujahid.audio.LoopMode;
import com.lalkalol.mujahid.commands.CommandTestFixtures;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.PlayerUpdateBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlaybackCommandsTest {

    @Test
    void loopCommand_setsModeAndReplies() {
        var h = CommandTestFixtures.harness().withStringOption("mode", "queue");

        new LoopCommand().execute(h.context());

        verify(h.scheduler).setLoopMode(LoopMode.QUEUE);
        assertTrue(h.lastReplyDescription().contains("queue"));
    }

    @Test
    void clearCommand_rejectsEmptyQueue() {
        var h = CommandTestFixtures.harness();
        when(h.scheduler.queueSize()).thenReturn(0);

        new ClearCommand().execute(h.context());

        assertTrue(h.lastEphemeralDescription().contains("already empty"));
    }

    @Test
    void clearCommand_clearsQueueAndReportsCount() {
        var h = CommandTestFixtures.harness();
        when(h.scheduler.queueSize()).thenReturn(3);

        new ClearCommand().execute(h.context());

        verify(h.scheduler).clearQueue();
        assertTrue(h.lastReplyDescription().contains("3"));
    }

    @Test
    void removeCommand_reportsMissingPosition() {
        var h = CommandTestFixtures.harness().withIntOption("position", 5);
        when(h.scheduler.removeAt(4)).thenReturn(null);

        new RemoveCommand().execute(h.context());

        assertTrue(h.lastEphemeralDescription().contains("position 5"));
    }

    @Test
    void removeCommand_removesTrackByTitle() {
        var h = CommandTestFixtures.harness().withIntOption("position", 1);
        var removed = h.mockTrack("Cool Song");
        when(h.scheduler.removeAt(0)).thenReturn(removed);

        new RemoveCommand().execute(h.context());

        assertTrue(h.lastReplyDescription().contains("Cool Song"));
    }

    @Test
    void volumeCommand_requiresActivePlayer() {
        var h = CommandTestFixtures.harness()
                .withIntOption("level", 80)
                .withNoActivePlayer();

        new VolumeCommand().execute(h.context());

        assertTrue(h.lastEphemeralDescription().contains("Nothing is playing"));
    }

    @Test
    void volumeCommand_clampsAndAppliesLevel() {
        var h = CommandTestFixtures.harness()
                .withIntOption("level", 250)
                .withActivePlayer();

        new VolumeCommand().execute(h.context());

        verify(h.scheduler).applyVolume(200);
        assertTrue(h.lastReplyDescription().contains("200%"));
    }

    @Test
    void pauseCommand_rejectsWhenAlreadyPaused() {
        var h = CommandTestFixtures.harness().withPausedPlayer();

        new PauseCommand().execute(h.context());

        assertTrue(h.lastEphemeralDescription().contains("Already paused"));
    }

    @Test
    void pauseCommand_pausesActivePlayback() {
        var h = CommandTestFixtures.harness().withPlayingPlayer();
        Link link = mock(Link.class);
        PlayerUpdateBuilder builder = mock(PlayerUpdateBuilder.class);
        when(h.musicManager.getCachedLink()).thenReturn(link);
        when(link.createOrUpdatePlayer()).thenReturn(builder);
        when(builder.setPaused(true)).thenReturn(builder);

        new PauseCommand().execute(h.context());

        verify(builder).setPaused(true);
        assertEquals("Paused.", h.lastReplyDescription());
    }

    @Test
    void filterCommand_clearsWhenOffSelected() {
        var h = CommandTestFixtures.harness()
                .withStringOption("preset", "off")
                .withActivePlayer();

        new FilterCommand().execute(h.context());

        verify(h.scheduler).applyFilter(AudioFilter.OFF);
        assertEquals("Filters cleared.", h.lastReplyDescription());
    }

    @Test
    void filterCommand_appliesNamedPreset() {
        var h = CommandTestFixtures.harness()
                .withStringOption("preset", "nightcore")
                .withActivePlayer();

        new FilterCommand().execute(h.context());

        verify(h.scheduler).applyFilter(AudioFilter.NIGHTCORE);
        assertTrue(h.lastReplyDescription().contains("Nightcore"));
    }

    @Test
    void skipCommand_requiresConnectionAndActivePlayer() {
        var h = CommandTestFixtures.harness()
                .memberNotInVoice()
                .withActivePlayer();

        new SkipCommand().execute(h.context());

        assertTrue(h.lastEphemeralDescription().contains("voice channel"));
    }

    @Test
    void skipCommand_reportsEmptyQueueAfterSkip() {
        var h = CommandTestFixtures.harness()
                .bothInVoice(1L, "General")
                .withActivePlayer();
        when(h.scheduler.skip()).thenReturn(null);

        new SkipCommand().execute(h.context());

        assertTrue(h.lastReplyDescription().contains("queue is now empty"));
    }

    @Test
    void skipCommand_reportsNextTrack() {
        var h = CommandTestFixtures.harness()
                .bothInVoice(1L, "General")
                .withActivePlayer();
        var next = h.mockTrack("Next Up");
        when(h.scheduler.skip()).thenReturn(next);

        new SkipCommand().execute(h.context());

        assertTrue(h.lastReplyDescription().contains("Next Up"));
    }
}
