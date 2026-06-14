import { type Filter } from "mongodb";
import { playlistsCollection } from "@/db/collections";
import type {
  PlaylistDocument,
  PlaylistOwnerStats,
  TotalTracksAggregation,
} from "@/db/documents";

export interface PlaylistListRow {
  owner_id: number;
  name: string;
  track_count: number;
}

export interface PlaylistDetailRow {
  owner_id: number;
  name: string;
  tracks: PlaylistDocument["tracks"];
}

export interface PlaylistOwnerSummary {
  owner_id: number;
  playlist_count: number;
  track_count: number;
}

export interface PlaylistStatsRow {
  total_playlists: number;
  total_tracks: number;
  top_owners: PlaylistOwnerSummary[];
}

export async function listPlaylists(
  page: number,
  pageSize: number,
  ownerId?: string | null,
  name?: string | null,
): Promise<[PlaylistListRow[], number]> {
  const collection = playlistsCollection();
  const query: Filter<PlaylistDocument> = {};
  if (ownerId) {
    query.ownerId = Number(ownerId);
  }
  if (name) {
    query.name = { $regex: name, $options: "i" };
  }

  const total = await collection.countDocuments(query);
  const skip = Math.max(page - 1, 0) * pageSize;
  const docs = await collection
    .find(query)
    .sort({ ownerId: 1, name: 1 })
    .skip(skip)
    .limit(pageSize)
    .toArray();

  const items: PlaylistListRow[] = docs.map((doc) => ({
    owner_id: doc.ownerId,
    name: doc.name,
    track_count: doc.tracks.length,
  }));
  return [items, total];
}

export async function getPlaylist(
  ownerId: string,
  name: string,
): Promise<PlaylistDetailRow | null> {
  const doc = await playlistsCollection().findOne({
    ownerId: Number(ownerId),
    name,
  });
  if (!doc) {
    return null;
  }
  return {
    owner_id: doc.ownerId,
    name: doc.name,
    tracks: doc.tracks,
  };
}

export async function deletePlaylist(ownerId: string, name: string): Promise<boolean> {
  const result = await playlistsCollection().deleteOne({
    ownerId: Number(ownerId),
    name,
  });
  return result.deletedCount > 0;
}

export async function playlistStats(): Promise<PlaylistStatsRow> {
  const collection = playlistsCollection();
  const topOwners = await collection
    .aggregate<PlaylistOwnerStats>([
      {
        $group: {
          _id: "$ownerId",
          playlist_count: { $sum: 1 },
          track_count: { $sum: { $size: { $ifNull: ["$tracks", []] } } },
        },
      },
      { $sort: { playlist_count: -1 } },
      { $limit: 10 },
    ])
    .toArray();

  const totalPlaylists = await collection.countDocuments({});
  const trackRows = await collection
    .aggregate<TotalTracksAggregation>([
      { $project: { track_count: { $size: { $ifNull: ["$tracks", []] } } } },
      { $group: { _id: null, total_tracks: { $sum: "$track_count" } } },
    ])
    .toArray();

  return {
    total_playlists: totalPlaylists,
    total_tracks: trackRows[0]?.total_tracks ?? 0,
    top_owners: topOwners.map(({ _id, playlist_count, track_count }) => ({
      owner_id: _id,
      playlist_count,
      track_count,
    })),
  };
}
