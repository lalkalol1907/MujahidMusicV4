export interface PlaylistPage {
  items: { ownerId: string; name: string; trackCount: number }[];
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
}

export interface PlaylistStats {
  totalPlaylists: number;
  totalTracks: number;
  topOwners: { ownerId: string; playlistCount: number; trackCount: number }[];
}

export interface PlaylistDetail {
  ownerId: string;
  name: string;
  tracks: { encoded: string; title: string; uri: string }[];
}
