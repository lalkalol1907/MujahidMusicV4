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
