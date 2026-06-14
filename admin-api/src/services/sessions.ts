import { botClient } from "../clients/bot";
import { BotNotFoundError } from "../core/errors";
import * as auditRepo from "../repositories/audit";
import type { ActionResponse, QueueTrack, SessionsResponse } from "../types";

export async function listSessions(): Promise<SessionsResponse> {
  return botClient.get<SessionsResponse>("/internal/sessions");
}

export async function getSessionQueue(guildId: string): Promise<QueueTrack[]> {
  try {
    return await botClient.get<QueueTrack[]>(`/internal/sessions/${guildId}/queue`);
  } catch (error) {
    if (error instanceof BotNotFoundError) {
      throw new BotNotFoundError("No active session");
    }
    throw error;
  }
}

export async function moderateSession(
  guildId: string,
  action: string,
  path: string,
  ip: string | null,
): Promise<ActionResponse> {
  const result = await botClient.post<ActionResponse>(path);
  await auditRepo.writeAudit(action, guildId, ip);
  return result;
}
