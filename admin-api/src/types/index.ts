export interface StatusResponse {
  status: string;
  uptimeSeconds: number;
  botReachable: boolean;
  mongoReachable: boolean;
  environment: string;
}

export interface MetricsSummary {
  guilds_total: number;
  active_players: number;
  commands: { command: string; count?: number; errors?: number }[];
  track_load_failures: { reason: string; count: number }[];
  playlist_ops: { op: string; count: number }[];
}

export interface SessionsResponse {
  nodes: { name: string; connected: boolean; players: number; playing: number }[];
  sessions: Session[];
}

export interface Session {
  guildId: string;
  guildName: string;
  trackTitle: string | null;
  trackAuthor: string | null;
  positionMs: number;
  lengthMs: number;
  paused: boolean;
  volume: number;
  queueSize: number;
  loopMode: string;
}

export interface QueueTrack {
  position: number;
  title: string;
  author: string;
  uri: string;
  durationMs: number;
}

export interface ActionResponse {
  status: string;
}

export interface Guild {
  id: string;
  name: string;
  memberCount: number;
}

export interface PlaylistSummary {
  ownerId: string;
  name: string;
  trackCount: number;
}

export interface PlaylistTrack {
  encoded: string;
  title: string;
  uri: string;
}

export interface PlaylistDetail {
  ownerId: string;
  name: string;
  tracks: PlaylistTrack[];
}

export interface TopOwner {
  ownerId: string;
  playlistCount: number;
  trackCount: number;
}

export interface PlaylistStats {
  totalPlaylists: number;
  totalTracks: number;
  topOwners: TopOwner[];
}

export interface PaginatedPlaylists {
  items: PlaylistSummary[];
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
}

export interface AuditEntry {
  action: string;
  target: string;
  at: string | null;
  ip: string | null;
}

export interface PaginatedAudit {
  items: AuditEntry[];
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
}
