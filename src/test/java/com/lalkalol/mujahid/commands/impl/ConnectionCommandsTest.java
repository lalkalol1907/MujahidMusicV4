package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.commands.CommandTestFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConnectionCommandsTest {

    @Test
    void joinCommand_requiresMemberVoiceChannel() {
        var h = CommandTestFixtures.harness().memberNotInVoice();

        new JoinCommand().execute(h.context());

        assertTrue(h.lastEphemeralDescription().contains("voice channel"));
    }

    @Test
    void joinCommand_connectsAndRegistersManager() {
        var h = CommandTestFixtures.harness().memberInVoice(5L, "Lounge");

        new JoinCommand().execute(h.context());

        verify(h.audioController).connect(org.mockito.ArgumentMatchers.any());
        verify(h.lavalink).getOrCreate(CommandTestFixtures.GUILD_ID);
        assertTrue(h.lastReplyDescription().contains("Lounge"));
    }

    @Test
    void leaveCommand_rejectsWhenBotNotConnected() {
        var h = CommandTestFixtures.harness().botNotInVoice();

        new LeaveCommand().execute(h.context());

        assertTrue(h.lastEphemeralDescription().contains("not connected"));
    }

    @Test
    void leaveCommand_stopsPlaybackAndDisconnects() {
        var h = CommandTestFixtures.harness().botInVoice(1L, "General");
        when(h.lavalink.getExisting(CommandTestFixtures.GUILD_ID)).thenReturn(h.musicManager);

        new LeaveCommand().execute(h.context());

        verify(h.scheduler).stop();
        verify(h.audioController).disconnect(h.guild);
        verify(h.lavalink).destroy(CommandTestFixtures.GUILD_ID);
        assertTrue(h.lastReplyDescription().contains("Bye"));
    }
}
