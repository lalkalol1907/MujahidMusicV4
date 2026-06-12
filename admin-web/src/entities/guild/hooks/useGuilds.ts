import { useQuery } from "@tanstack/react-query";
import { fetchGuilds } from "../api/guildApi";
import { guildKeys } from "../lib/query-keys";

export function useGuilds() {
  return useQuery({
    queryKey: guildKeys.all,
    queryFn: fetchGuilds,
  });
}
