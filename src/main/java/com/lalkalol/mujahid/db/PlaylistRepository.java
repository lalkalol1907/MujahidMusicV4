package com.lalkalol.mujahid.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReplaceOptions;

import java.util.ArrayList;
import java.util.List;

public class PlaylistRepository {
    private final MongoCollection<PlaylistDocument> collection;

    public PlaylistRepository(MongoDatabase database) {
        collection = database.getCollection("playlists", PlaylistDocument.class);
        collection.createIndex(
                Indexes.ascending("ownerId", "name"),
                new IndexOptions().unique(true)
        );
    }

    public int save(long ownerId, String name, List<StoredTrack> tracks) {
        collection.replaceOne(
                ownerAndName(ownerId, name),
                new PlaylistDocument(ownerId, name, tracks),
                new ReplaceOptions().upsert(true)
        );
        return tracks.size();
    }

    public List<StoredTrack> load(long ownerId, String name) {
        PlaylistDocument document = collection.find(ownerAndName(ownerId, name)).first();
        return document != null ? document.tracks() : null;
    }

    public List<PlaylistSummary> list(long ownerId) {
        List<PlaylistSummary> summaries = new ArrayList<>();
        collection.find(Filters.eq("ownerId", ownerId))
                .sort(Indexes.ascending("name"))
                .forEach(document -> summaries.add(
                        new PlaylistSummary(document.name(), document.tracks().size())
                ));
        return summaries;
    }

    public boolean delete(long ownerId, String name) {
        return collection.deleteOne(ownerAndName(ownerId, name)).getDeletedCount() > 0;
    }

    private static org.bson.conversions.Bson ownerAndName(long ownerId, String name) {
        return Filters.and(Filters.eq("ownerId", ownerId), Filters.eq("name", name));
    }
}
