package com.lalkalol.mujahid.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

class CommandContextTest {

    @Test
    void ensureConnected_rejectsWhenMemberNotInVoice() {
        var h = CommandTestFixtures.harness().memberNotInVoice();

        assertFalse(h.context().ensureConnected());
        assertTrue(h.lastEphemeralDescription().contains("voice channel"));
    }

    @Test
    void ensureConnected_connectsWhenBotIsNotInVoice() {
        var h = CommandTestFixtures.harness()
                .memberInVoice(1L, "General")
                .botNotInVoice();

        assertTrue(h.context().ensureConnected());
        verify(h.audioController).connect(anyChannel(1L));
        verify(h.lavalink).getOrCreate(CommandTestFixtures.GUILD_ID);
    }

    @Test
    void ensureConnected_rejectsWhenInDifferentChannels() {
        var h = CommandTestFixtures.harness()
                .memberInVoice(1L, "General")
                .botInVoice(2L, "Music");

        assertFalse(h.context().ensureConnected());
        assertTrue(h.lastEphemeralDescription().contains("Music"));
    }

    @Test
    void ensureConnected_allowsWhenInSameChannel() {
        var h = CommandTestFixtures.harness().bothInVoice(1L, "General");

        assertTrue(h.context().ensureConnected());
    }

    @Test
    void requireActivePlayer_rejectsWhenNothingPlaying() {
        var h = CommandTestFixtures.harness().withNoActivePlayer();

        assertFalse(h.context().requireActivePlayer());
        assertTrue(h.lastEphemeralDescription().contains("Nothing is playing"));
    }

    @Test
    void requireActivePlayer_allowsWhenTrackIsActive() {
        var h = CommandTestFixtures.harness().withActivePlayer();

        assertTrue(h.context().requireActivePlayer());
    }

    private static net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion anyChannel(long channelId) {
        return org.mockito.ArgumentMatchers.argThat(
                channel -> channel != null && channel.getIdLong() == channelId
        );
    }
}
