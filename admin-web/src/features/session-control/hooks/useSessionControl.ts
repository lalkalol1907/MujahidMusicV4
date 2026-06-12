import { useMutation, useQueryClient } from "@tanstack/react-query";
import { leaveSession, sessionKeys, skipSession, stopSession } from "@/entities/session";

type SessionAction = "skip" | "stop" | "leave";

export function useSessionControl(onSuccess?: () => void) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ guildId, type }: { guildId: string; type: SessionAction }) => {
      if (type === "skip") return skipSession(guildId);
      if (type === "stop") return stopSession(guildId);
      return leaveSession(guildId);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: sessionKeys.all });
      onSuccess?.();
    },
  });
}
