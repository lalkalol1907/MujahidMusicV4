import { useQuery } from "@tanstack/react-query";
import { fetchSessionQueue, fetchSessions } from "../api/sessionApi";
import { sessionKeys } from "../lib/query-keys";

export function useSessions(refetchInterval = 3_000) {
  return useQuery({
    queryKey: sessionKeys.all,
    queryFn: fetchSessions,
    refetchInterval,
  });
}

export function useSessionQueue(guildId: string | undefined) {
  return useQuery({
    queryKey: sessionKeys.queue(guildId ?? ""),
    queryFn: () => fetchSessionQueue(guildId!),
    enabled: !!guildId,
  });
}
