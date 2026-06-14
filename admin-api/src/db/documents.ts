export interface StoredTrackDocument {
  encoded: string;
  title: string;
  uri: string;
}

export interface PlaylistDocument {
  ownerId: number;
  name: string;
  tracks: StoredTrackDocument[];
}

export interface AuditDocument {
  action: string;
  target: string;
  at: Date;
  ip: string | null;
}

export interface PlaylistOwnerStats {
  _id: number;
  playlist_count: number;
  track_count: number;
}

export interface TotalTracksAggregation {
  _id: null;
  total_tracks: number;
}
