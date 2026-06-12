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
