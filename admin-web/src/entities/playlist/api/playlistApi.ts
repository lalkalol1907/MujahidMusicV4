import { request } from "@/shared/api";
import type { PlaylistDetail, PlaylistPage, PlaylistStats } from "../model/types";

export function fetchPlaylists(params: URLSearchParams) {
  return request<PlaylistPage>(`/api/admin/playlists?${params.toString()}`);
}

export function fetchPlaylistStats() {
  return request<PlaylistStats>("/api/admin/playlists/stats");
}

export function fetchPlaylist(ownerId: string, name: string) {
  return request<PlaylistDetail>(
    `/api/admin/playlists/${ownerId}/${encodeURIComponent(name)}`,
  );
}

export function deletePlaylist(ownerId: string, name: string) {
  return request<void>(
    `/api/admin/playlists/${ownerId}/${encodeURIComponent(name)}`,
    { method: "DELETE" },
  );
}
