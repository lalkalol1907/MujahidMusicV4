import { botClient } from "@/clients/bot";
import type { Guild } from "@/types";

export async function listGuilds(): Promise<Guild[]> {
  return botClient.get<Guild[]>("/internal/guilds");
}
