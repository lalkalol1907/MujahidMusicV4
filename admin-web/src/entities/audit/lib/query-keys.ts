export const auditKeys = {
  page: (page: number) => ["audit", page] as const,
};
