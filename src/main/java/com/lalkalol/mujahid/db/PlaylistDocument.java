package com.lalkalol.mujahid.db;

import java.util.List;

public record PlaylistDocument(long ownerId, String name, List<StoredTrack> tracks) {
}
