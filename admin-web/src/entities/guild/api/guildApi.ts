import { request } from "@/shared/api";
import type { Guild } from "../model/types";

export function fetchGuilds() {
  return request<Guild[]>("/api/admin/guilds");
}
