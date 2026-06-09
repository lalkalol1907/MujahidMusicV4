package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.commands.CommandTestFixtures;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayFileCommandTest {

    @Test
    void playFile_rejectsNonAudioAttachment() {
        var h = CommandTestFixtures.harness();
        OptionMapping option = mock(OptionMapping.class);
        Message.Attachment attachment = mock(Message.Attachment.class);
        when(h.event.getOption("file")).thenReturn(option);
        when(option.getAsAttachment()).thenReturn(attachment);
        when(attachment.getContentType()).thenReturn("image/png");
        when(attachment.getFileName()).thenReturn("cover.png");

        new PlayFileCommand().execute(h.context());

        assertTrue(h.lastEphemeralDescription().contains("not an audio file"));
        assertTrue(h.lastEphemeralDescription().contains("cover.png"));
    }

    @Test
    void playFile_rejectsUnknownContentType() {
        var h = CommandTestFixtures.harness();
        OptionMapping option = mock(OptionMapping.class);
        Message.Attachment attachment = mock(Message.Attachment.class);
        when(h.event.getOption("file")).thenReturn(option);
        when(option.getAsAttachment()).thenReturn(attachment);
        when(attachment.getContentType()).thenReturn(null);
        when(attachment.getFileName()).thenReturn("mystery.bin");

        new PlayFileCommand().execute(h.context());

        assertTrue(h.lastEphemeralDescription().contains("unknown"));
    }

    @Test
    void playFile_requiresVoiceWhenAudioIsValid() {
        var h = CommandTestFixtures.harness().memberNotInVoice();
        OptionMapping option = mock(OptionMapping.class);
        Message.Attachment attachment = mock(Message.Attachment.class);
        when(h.event.getOption("file")).thenReturn(option);
        when(option.getAsAttachment()).thenReturn(attachment);
        when(attachment.getContentType()).thenReturn("audio/mpeg");
        when(attachment.getFileName()).thenReturn("song.mp3");
        when(attachment.getUrl()).thenReturn("https://cdn.example/song.mp3");

        new PlayFileCommand().execute(h.context());

        assertTrue(h.lastEphemeralDescription().contains("voice channel"));
    }
}
