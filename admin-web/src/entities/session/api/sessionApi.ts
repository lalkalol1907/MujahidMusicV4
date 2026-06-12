import { request } from "@/shared/api";
import type { QueueTrack, SessionsResponse } from "../model/types";

export function fetchSessions() {
  return request<SessionsResponse>("/api/admin/sessions");
}

export function fetchSessionQueue(guildId: string) {
  return request<QueueTrack[]>(`/api/admin/sessions/${guildId}`);
}

export function skipSession(guildId: string) {
  return request(`/api/admin/sessions/${guildId}/skip`, { method: "POST" });
}

export function stopSession(guildId: string) {
  return request(`/api/admin/sessions/${guildId}/stop`, { method: "POST" });
}

export function leaveSession(guildId: string) {
  return request(`/api/admin/sessions/${guildId}/leave`, { method: "POST" });
}
