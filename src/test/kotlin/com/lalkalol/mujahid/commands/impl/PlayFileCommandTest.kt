package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.CommandTestFixtures
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PlayFileCommandTest {
    @Test
    fun playFile_rejectsNonAudioAttachment() = runBlocking {
        val h = CommandTestFixtures.harness()
        val option = mock<OptionMapping>()
        val attachment = mock<Message.Attachment>()
        whenever(h.event.getOption("file")).thenReturn(option)
        whenever(option.asAttachment).thenReturn(attachment)
        whenever(attachment.contentType).thenReturn("image/png")
        whenever(attachment.fileName).thenReturn("cover.png")

        PlayFileCommand().execute(h.context())

        assertTrue(h.lastEphemeralDescription().contains("not an audio file"))
        assertTrue(h.lastEphemeralDescription().contains("cover.png"))
    }

    @Test
    fun playFile_rejectsUnknownContentType() = runBlocking {
        val h = CommandTestFixtures.harness()
        val option = mock<OptionMapping>()
        val attachment = mock<Message.Attachment>()
        whenever(h.event.getOption("file")).thenReturn(option)
        whenever(option.asAttachment).thenReturn(attachment)
        whenever(attachment.contentType).thenReturn(null)
        whenever(attachment.fileName).thenReturn("mystery.bin")

        PlayFileCommand().execute(h.context())

        assertTrue(h.lastEphemeralDescription().contains("unknown"))
    }

    @Test
    fun playFile_requiresVoiceWhenAudioIsValid() = runBlocking {
        val h = CommandTestFixtures.harness().memberNotInVoice()
        val option = mock<OptionMapping>()
        val attachment = mock<Message.Attachment>()
        whenever(h.event.getOption("file")).thenReturn(option)
        whenever(option.asAttachment).thenReturn(attachment)
        whenever(attachment.contentType).thenReturn("audio/mpeg")
        whenever(attachment.fileName).thenReturn("song.mp3")
        whenever(attachment.url).thenReturn("https://cdn.example/song.mp3")

        PlayFileCommand().execute(h.context())

        assertTrue(h.lastEphemeralDescription().contains("voice channel"))
    }
}
