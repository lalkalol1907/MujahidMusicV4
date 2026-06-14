package com.lalkalol.mujahid.db

data class PlaylistDocument(
    val ownerId: Long,
    val name: String,
    val tracks: List<StoredTrack>,
)
