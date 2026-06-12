import { useQuery } from "@tanstack/react-query";
import { fetchPlaylist, fetchPlaylists, fetchPlaylistStats } from "../api/playlistApi";
import { playlistKeys } from "../lib/query-keys";

export function usePlaylists(page: number, ownerId: string, name: string) {
  const params = new URLSearchParams({ page: String(page), pageSize: "50" });
  if (ownerId) params.set("ownerId", ownerId);
  if (name) params.set("name", name);

  return useQuery({
    queryKey: playlistKeys.all(page, ownerId, name),
    queryFn: () => fetchPlaylists(params),
  });
}

export function usePlaylistStats(refetchInterval = 30_000) {
  return useQuery({
    queryKey: playlistKeys.stats,
    queryFn: fetchPlaylistStats,
    refetchInterval,
  });
}

export function usePlaylistDetail(ownerId: string | undefined, name: string | undefined) {
  return useQuery({
    queryKey: playlistKeys.detail(ownerId ?? "", name ?? ""),
    queryFn: () => fetchPlaylist(ownerId!, name!),
    enabled: !!ownerId && !!name,
  });
}
