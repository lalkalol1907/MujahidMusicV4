import { getDb } from "../db/connection";

interface PlaylistRow {
  owner_id: number | string;
  name: string;
  track_count: number;
}

export async function listPlaylists(
  page: number,
  pageSize: number,
  ownerId?: string | null,
  name?: string | null,
): Promise<[PlaylistRow[], number]> {
  const db = getDb();
  const query: Record<string, unknown> = {};
  if (ownerId) {
    query.ownerId = Number(ownerId);
  }
  if (name) {
    query.name = { $regex: name, $options: "i" };
  }

  const total = await db.collection("playlists").countDocuments(query);
  const skip = Math.max(page - 1, 0) * pageSize;
  const docs = await db
    .collection("playlists")
    .find(query)
    .sort({ ownerId: 1, name: 1 })
    .skip(skip)
    .limit(pageSize)
    .toArray();

  const items = docs.map((doc) => ({
    owner_id: doc.ownerId as number,
    name: doc.name as string,
    track_count: ((doc.tracks as unknown[]) ?? []).length,
  }));
  return [items, total];
}

export async function getPlaylist(
  ownerId: string,
  name: string,
): Promise<{ owner_id: number; name: string; tracks: unknown[] } | null> {
  const doc = await getDb()
    .collection("playlists")
    .findOne({ ownerId: Number(ownerId), name });
  if (!doc) {
    return null;
  }
  return {
    owner_id: doc.ownerId as number,
    name: doc.name as string,
    tracks: (doc.tracks as unknown[]) ?? [],
  };
}

export async function deletePlaylist(ownerId: string, name: string): Promise<boolean> {
  const result = await getDb()
    .collection("playlists")
    .deleteOne({ ownerId: Number(ownerId), name });
  return result.deletedCount > 0;
}

export async function playlistStats(): Promise<{
  total_playlists: number;
  total_tracks: number;
  top_owners: { owner_id: number; playlist_count: number; track_count: number }[];
}> {
  const db = getDb();
  const topOwners = await db
    .collection("playlists")
    .aggregate([
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

  const totalPlaylists = await db.collection("playlists").countDocuments({});
  const trackRows = await db
    .collection("playlists")
    .aggregate([
      { $project: { track_count: { $size: { $ifNull: ["$tracks", []] } } } },
      { $group: { _id: null, total_tracks: { $sum: "$track_count" } } },
    ])
    .toArray();

  return {
    total_playlists: totalPlaylists,
    total_tracks: (trackRows[0]?.total_tracks as number) ?? 0,
    top_owners: topOwners.map((row) => ({
      owner_id: row._id as number,
      playlist_count: row.playlist_count as number,
      track_count: row.track_count as number,
    })),
  };
}
