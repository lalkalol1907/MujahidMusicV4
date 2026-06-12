export const sessionKeys = {
  all: ["sessions"] as const,
  queue: (guildId: string) => ["session-queue", guildId] as const,
};
