package com.lalkalol.mujahid.db

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.ReplaceOptions

class PlaylistRepository(database: MongoDatabase) {
    private val collection: MongoCollection<PlaylistDocument> =
        database.getCollection("playlists", PlaylistDocument::class.java)

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

    fun load(ownerId: Long, name: String): List<StoredTrack>? {
        val document = collection.find(ownerAndName(ownerId, name)).first()
        return document?.tracks
    }

    fun list(ownerId: Long): List<PlaylistSummary> {
        val summaries = mutableListOf<PlaylistSummary>()
        collection.find(Filters.eq("ownerId", ownerId))
            .sort(Indexes.ascending("name"))
            .forEach { document ->
                summaries.add(PlaylistSummary(document.name, document.tracks.size))
            }
        return summaries
    }

    fun delete(ownerId: Long, name: String): Boolean =
        collection.deleteOne(ownerAndName(ownerId, name)).deletedCount > 0

    private fun ownerAndName(ownerId: Long, name: String) =
        Filters.and(Filters.eq("ownerId", ownerId), Filters.eq("name", name))
}
