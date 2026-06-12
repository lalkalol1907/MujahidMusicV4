import { useQuery } from "@tanstack/react-query";
import { fetchMetricsSummary, fetchStatus } from "../api/statusApi";
import { statusKeys } from "../lib/query-keys";

export function useStatus(refetchInterval = 10_000) {
  return useQuery({
    queryKey: statusKeys.all,
    queryFn: fetchStatus,
    refetchInterval,
  });
}

export function useMetricsSummary(refetchInterval = 10_000) {
  return useQuery({
    queryKey: statusKeys.metrics,
    queryFn: fetchMetricsSummary,
    refetchInterval,
  });
}
