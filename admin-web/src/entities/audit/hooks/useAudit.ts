import { useQuery } from "@tanstack/react-query";
import { fetchAudit } from "../api/auditApi";
import { auditKeys } from "../lib/query-keys";

export function useAudit(page: number) {
  return useQuery({
    queryKey: auditKeys.page(page),
    queryFn: () => fetchAudit(page),
  });
}
