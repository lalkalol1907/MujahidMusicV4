package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.commands.CommandTestFixtures;
import com.lalkalol.mujahid.db.PlaylistSummary;
import com.lalkalol.mujahid.db.StoredTrack;
import dev.arbjerg.lavalink.client.player.Track;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlaylistCommandTest {

    @Test
    void save_rejectsEmptyName() {
        var h = CommandTestFixtures.harness()
                .withStringOption("name", "   ");
        when(h.event.getSubcommandName()).thenReturn("save");

        new PlaylistCommand().execute(h.context());

        assertTrue(h.lastEphemeralDescription().contains("cannot be empty"));
    }

    @Test
    void save_rejectsWhenNothingToSave() {
        var h = CommandTestFixtures.harness()
                .withStringOption("name", "favorites");
        when(h.event.getSubcommandName()).thenReturn("save");
        when(h.scheduler.getCurrent()).thenReturn(null);
        when(h.scheduler.queueSnapshot()).thenReturn(List.of());

        new PlaylistCommand().execute(h.context());

        assertTrue(h.lastEphemeralDescription().contains("nothing playing or queued"));
    }

    @Test
    void save_persistsCurrentTrackAndQueue() {
        var h = CommandTestFixtures.harness()
                .withStringOption("name", "favorites");
        when(h.event.getSubcommandName()).thenReturn("save");

        Track current = h.mockTrack("Now");
        Track queued = h.mockTrack("Later");
        when(h.scheduler.getCurrent()).thenReturn(current);
        when(h.scheduler.queueSnapshot()).thenReturn(List.of(queued));
        when(h.playlists.save(eq(CommandTestFixtures.USER_ID), eq("favorites"), anyList())).thenReturn(2);

        new PlaylistCommand().execute(h.context());

        verify(h.playlists).save(eq(CommandTestFixtures.USER_ID), eq("favorites"), anyList());
        assertTrue(h.lastReplyDescription().contains("favorites"));
        assertTrue(h.lastReplyDescription().contains("2"));
    }

    @Test
    void load_rejectsMissingPlaylist() {
        var h = CommandTestFixtures.harness()
                .withStringOption("name", "missing");
        when(h.event.getSubcommandName()).thenReturn("load");
        when(h.playlists.load(CommandTestFixtures.USER_ID, "missing")).thenReturn(null);

        new PlaylistCommand().execute(h.context());

        assertTrue(h.lastEphemeralDescription().contains("missing"));
    }

    @Test
    void list_showsSavedPlaylists() {
        var h = CommandTestFixtures.harness();
        when(h.event.getSubcommandName()).thenReturn("list");
        when(h.playlists.list(CommandTestFixtures.USER_ID)).thenReturn(
                List.of(new PlaylistSummary("chill", 4), new PlaylistSummary("party", 10))
        );

        new PlaylistCommand().execute(h.context());

        assertTrue(h.lastEphemeralDescription().contains("chill"));
        assertTrue(h.lastEphemeralDescription().contains("party"));
    }

    @Test
    void list_rejectsWhenNoPlaylists() {
        var h = CommandTestFixtures.harness();
        when(h.event.getSubcommandName()).thenReturn("list");
        when(h.playlists.list(CommandTestFixtures.USER_ID)).thenReturn(List.of());

        new PlaylistCommand().execute(h.context());

        assertTrue(h.lastEphemeralDescription().contains("no saved playlists"));
    }

    @Test
    void delete_removesExistingPlaylist() {
        var h = CommandTestFixtures.harness()
                .withStringOption("name", "old");
        when(h.event.getSubcommandName()).thenReturn("delete");
        when(h.playlists.delete(CommandTestFixtures.USER_ID, "old")).thenReturn(true);

        new PlaylistCommand().execute(h.context());

        assertTrue(h.lastReplyDescription().contains("Deleted"));
    }

    @Test
    void delete_reportsMissingPlaylist() {
        var h = CommandTestFixtures.harness()
                .withStringOption("name", "ghost");
        when(h.event.getSubcommandName()).thenReturn("delete");
        when(h.playlists.delete(CommandTestFixtures.USER_ID, "ghost")).thenReturn(false);

        new PlaylistCommand().execute(h.context());

        assertTrue(h.lastEphemeralDescription().contains("ghost"));
    }

    @Test
    void load_rejectsEmptyStoredPlaylist() {
        var h = CommandTestFixtures.harness()
                .withStringOption("name", "empty");
        when(h.event.getSubcommandName()).thenReturn("load");
        when(h.playlists.load(CommandTestFixtures.USER_ID, "empty")).thenReturn(List.of());

        new PlaylistCommand().execute(h.context());

        assertTrue(h.lastEphemeralDescription().contains("empty"));
    }

    @Test
    void save_mapsTracksToStoredFormat() {
        var h = CommandTestFixtures.harness()
                .withStringOption("name", "mix");
        when(h.event.getSubcommandName()).thenReturn("save");
        var current = h.mockTrack("A");
        when(h.scheduler.getCurrent()).thenReturn(current);
        when(h.scheduler.queueSnapshot()).thenReturn(List.of());
        when(h.playlists.save(eq(CommandTestFixtures.USER_ID), eq("mix"), anyList())).thenAnswer(inv -> {
            @SuppressWarnings("unchecked")
            List<StoredTrack> tracks = inv.getArgument(2);
            return tracks.size();
        });

        new PlaylistCommand().execute(h.context());

        assertTrue(h.lastReplyDescription().contains("1"));
    }
}
