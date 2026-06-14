package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.CommandTestFixtures
import com.lalkalol.mujahid.db.PlaylistSummary
import com.lalkalol.mujahid.db.StoredTrack
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PlaylistCommandTest {
    @Test
    fun save_rejectsEmptyName() = runBlocking {
        val h = CommandTestFixtures.harness()
            .withStringOption("name", "   ")
        whenever(h.event.subcommandName).thenReturn("save")

        PlaylistCommand().execute(h.context())

        assertTrue(h.lastEphemeralDescription().contains("cannot be empty"))
    }

    @Test
    fun save_rejectsWhenNothingToSave() = runBlocking {
        val h = CommandTestFixtures.harness()
            .withStringOption("name", "favorites")
        whenever(h.event.subcommandName).thenReturn("save")
        whenever(h.scheduler.getCurrent()).thenReturn(null)
        whenever(h.scheduler.queueSnapshot()).thenReturn(emptyList())

        PlaylistCommand().execute(h.context())

        assertTrue(h.lastEphemeralDescription().contains("nothing playing or queued"))
    }

    @Test
    fun save_persistsCurrentTrackAndQueue() = runBlocking {
        val h = CommandTestFixtures.harness()
            .withStringOption("name", "favorites")
        whenever(h.event.subcommandName).thenReturn("save")

        val current = h.mockTrack("Now")
        val queued = h.mockTrack("Later")
        whenever(h.scheduler.getCurrent()).thenReturn(current)
        whenever(h.scheduler.queueSnapshot()).thenReturn(listOf(queued))
        whenever(h.playlists.save(eq(CommandTestFixtures.USER_ID), eq("favorites"), any())).thenReturn(2)

        PlaylistCommand().execute(h.context())

        verify(h.playlists).save(eq(CommandTestFixtures.USER_ID), eq("favorites"), any())
        assertTrue(h.lastReplyDescription().contains("favorites"))
        assertTrue(h.lastReplyDescription().contains("2"))
    }

    @Test
    fun load_rejectsMissingPlaylist() = runBlocking {
        val h = CommandTestFixtures.harness()
            .withStringOption("name", "missing")
        whenever(h.event.subcommandName).thenReturn("load")
        whenever(h.playlists.load(CommandTestFixtures.USER_ID, "missing")).thenReturn(null)

        PlaylistCommand().execute(h.context())

        assertTrue(h.lastEphemeralDescription().contains("missing"))
    }

    @Test
    fun list_showsSavedPlaylists() = runBlocking {
        val h = CommandTestFixtures.harness()
        whenever(h.event.subcommandName).thenReturn("list")
        whenever(h.playlists.list(CommandTestFixtures.USER_ID)).thenReturn(
            listOf(PlaylistSummary("chill", 4), PlaylistSummary("party", 10)),
        )

        PlaylistCommand().execute(h.context())

        assertTrue(h.lastEphemeralDescription().contains("chill"))
        assertTrue(h.lastEphemeralDescription().contains("party"))
    }

    @Test
    fun list_rejectsWhenNoPlaylists() = runBlocking {
        val h = CommandTestFixtures.harness()
        whenever(h.event.subcommandName).thenReturn("list")
        whenever(h.playlists.list(CommandTestFixtures.USER_ID)).thenReturn(emptyList())

        PlaylistCommand().execute(h.context())

        assertTrue(h.lastEphemeralDescription().contains("no saved playlists"))
    }

    @Test
    fun delete_removesExistingPlaylist() = runBlocking {
        val h = CommandTestFixtures.harness()
            .withStringOption("name", "old")
        whenever(h.event.subcommandName).thenReturn("delete")
        whenever(h.playlists.delete(CommandTestFixtures.USER_ID, "old")).thenReturn(true)

        PlaylistCommand().execute(h.context())

        assertTrue(h.lastReplyDescription().contains("Deleted"))
    }

    @Test
    fun delete_reportsMissingPlaylist() = runBlocking {
        val h = CommandTestFixtures.harness()
            .withStringOption("name", "ghost")
        whenever(h.event.subcommandName).thenReturn("delete")
        whenever(h.playlists.delete(CommandTestFixtures.USER_ID, "ghost")).thenReturn(false)

        PlaylistCommand().execute(h.context())

        assertTrue(h.lastEphemeralDescription().contains("ghost"))
    }

    @Test
    fun load_rejectsEmptyStoredPlaylist() = runBlocking {
        val h = CommandTestFixtures.harness()
            .withStringOption("name", "empty")
        whenever(h.event.subcommandName).thenReturn("load")
        whenever(h.playlists.load(CommandTestFixtures.USER_ID, "empty")).thenReturn(emptyList())

        PlaylistCommand().execute(h.context())

        assertTrue(h.lastEphemeralDescription().contains("empty"))
    }

    @Test
    fun save_mapsTracksToStoredFormat() = runBlocking {
        val h = CommandTestFixtures.harness()
            .withStringOption("name", "mix")
        whenever(h.event.subcommandName).thenReturn("save")
        val current = h.mockTrack("A")
        whenever(h.scheduler.getCurrent()).thenReturn(current)
        whenever(h.scheduler.queueSnapshot()).thenReturn(emptyList())
        whenever(h.playlists.save(eq(CommandTestFixtures.USER_ID), eq("mix"), any())).thenAnswer { invocation ->
            @Suppress("UNCHECKED_CAST")
            val tracks = invocation.getArgument<List<StoredTrack>>(2)
            tracks.size
        }

        PlaylistCommand().execute(h.context())

        assertTrue(h.lastReplyDescription().contains("1"))
    }
}
