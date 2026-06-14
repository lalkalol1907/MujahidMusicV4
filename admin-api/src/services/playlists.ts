import * as auditRepo from "@/repositories/audit";
import * as playlistRepo from "@/repositories/playlists";
import type { PaginatedPlaylists, PlaylistDetail, PlaylistStats } from "@/types";

function paginate(page: number, pageSize: number, total: number): number {
  return Math.max(Math.ceil(total / pageSize), 1);
}

export async function listPlaylists(
  page: number,
  pageSize: number,
  ownerId?: string | null,
  name?: string | null,
): Promise<PaginatedPlaylists> {
  const [items, total] = await playlistRepo.listPlaylists(page, pageSize, ownerId, name);
  return {
    items: items.map((item) => ({
      ownerId: String(item.owner_id),
      name: item.name,
      trackCount: item.track_count,
    })),
    page,
    pageSize,
    total,
    totalPages: paginate(page, pageSize, total),
  };
}

export async function getPlaylist(ownerId: string, name: string): Promise<PlaylistDetail | null> {
  const doc = await playlistRepo.getPlaylist(ownerId, name);
  if (!doc) {
    return null;
  }
  return {
    ownerId: String(doc.owner_id),
    name: doc.name,
    tracks: doc.tracks,
  };
}

export async function deletePlaylist(
  ownerId: string,
  name: string,
  ip: string | null,
): Promise<boolean> {
  const deleted = await playlistRepo.deletePlaylist(ownerId, name);
  if (deleted) {
    await auditRepo.writeAudit("DELETE_PLAYLIST", `${ownerId}/${name}`, ip);
  }
  return deleted;
}

export async function getPlaylistStats(): Promise<PlaylistStats> {
  const stats = await playlistRepo.playlistStats();
  return {
    totalPlaylists: stats.total_playlists,
    totalTracks: stats.total_tracks,
    topOwners: stats.top_owners.map((owner) => ({
      ownerId: String(owner.owner_id),
      playlistCount: owner.playlist_count,
      trackCount: owner.track_count,
    })),
  };
}
