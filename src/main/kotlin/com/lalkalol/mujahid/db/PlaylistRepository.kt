package com.lalkalol.mujahid.db

import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.MongoDatabase

data class StoredTrack(val encoded: String, val title: String, val uri: String?)

data class PlaylistDocument(
    val ownerId: Long,
    val name: String,
    val tracks: List<StoredTrack>,
)

data class PlaylistSummary(val name: String, val trackCount: Int)

class PlaylistRepository(database: MongoDatabase) {
    private val collection = database.getCollection<PlaylistDocument>("playlists")

    init {
        collection.createIndex(
            Indexes.ascending("ownerId", "name"),
            IndexOptions().unique(true),
        )
    }

    fun save(ownerId: Long, name: String, tracks: List<StoredTrack>): Int {
        collection.replaceOne(
            ownerAndName(ownerId, name),
            PlaylistDocument(ownerId, name, tracks),
            ReplaceOptions().upsert(true),
        )
        return tracks.size
    }

    fun load(ownerId: Long, name: String): List<StoredTrack>? =
        collection.find(ownerAndName(ownerId, name)).firstOrNull()?.tracks

    fun list(ownerId: Long): List<PlaylistSummary> =
        collection.find(Filters.eq("ownerId", ownerId))
            .sort(Indexes.ascending("name"))
            .map { PlaylistSummary(it.name, it.tracks.size) }
            .toList()

    fun delete(ownerId: Long, name: String): Boolean =
        collection.deleteOne(ownerAndName(ownerId, name)).deletedCount > 0

    private fun ownerAndName(ownerId: Long, name: String) =
        Filters.and(Filters.eq("ownerId", ownerId), Filters.eq("name", name))
}
