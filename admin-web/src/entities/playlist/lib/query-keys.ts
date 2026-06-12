export const playlistKeys = {
  all: (page: number, ownerId: string, name: string) =>
    ["playlists", page, ownerId, name] as const,
  stats: ["playlist-stats"] as const,
  detail: (ownerId: string, name: string) => ["playlist", ownerId, name] as const,
};
