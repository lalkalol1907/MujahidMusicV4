const TOKEN_KEY = "mujahid_admin_token";

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

export class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers = new Headers(init.headers);
  headers.set("Content-Type", "application/json");
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(path, { ...init, headers });
  if (!response.ok) {
    const text = await response.text();
    throw new ApiError(response.status, text || response.statusText);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return response.json() as Promise<T>;
}

export const api = {
  status: () => request<StatusResponse>("/api/admin/status"),
  metricsSummary: () => request<MetricsSummary>("/api/admin/metrics/summary"),
  sessions: () => request<SessionsResponse>("/api/admin/sessions"),
  sessionQueue: (guildId: string) => request<QueueTrack[]>(`/api/admin/sessions/${guildId}`),
  skip: (guildId: string) => request(`/api/admin/sessions/${guildId}/skip`, { method: "POST" }),
  stop: (guildId: string) => request(`/api/admin/sessions/${guildId}/stop`, { method: "POST" }),
  leave: (guildId: string) => request(`/api/admin/sessions/${guildId}/leave`, { method: "POST" }),
  guilds: () => request<Guild[]>("/api/admin/guilds"),
  playlists: (params: URLSearchParams) =>
    request<PlaylistPage>(`/api/admin/playlists?${params.toString()}`),
  playlistStats: () => request<PlaylistStats>("/api/admin/playlists/stats"),
  playlist: (ownerId: string, name: string) =>
    request<PlaylistDetail>(`/api/admin/playlists/${ownerId}/${encodeURIComponent(name)}`),
  deletePlaylist: (ownerId: string, name: string) =>
    request<void>(`/api/admin/playlists/${ownerId}/${encodeURIComponent(name)}`, {
      method: "DELETE",
    }),
  audit: (page = 1) => request<AuditPage>(`/api/admin/audit?page=${page}`),
};

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

export interface Guild {
  id: string;
  name: string;
  memberCount: number;
}

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

export interface AuditPage {
  items: { action: string; target: string; at: string | null; ip: string | null }[];
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
}
