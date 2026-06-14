import { botClient } from "@/clients/bot";
import { settings } from "@/core/config";
import { pingMongo } from "@/db/connection";
import type { StatusResponse } from "@/types";

const startedAt = Date.now();

export async function getStatus(): Promise<StatusResponse> {
  const [botOk, mongoOk] = await Promise.all([botClient.healthCheck(), pingMongo()]);
  return {
    status: botOk && mongoOk ? "UP" : "DEGRADED",
    uptimeSeconds: Math.floor((Date.now() - startedAt) / 1000),
    botReachable: botOk,
    mongoReachable: mongoOk,
    environment: settings.environment,
  };
}
