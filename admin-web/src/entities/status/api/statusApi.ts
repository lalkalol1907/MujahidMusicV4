import { request } from "@/shared/api";
import type { MetricsSummary, StatusResponse } from "../model/types";

export function fetchStatus() {
  return request<StatusResponse>("/api/admin/status");
}

export function fetchMetricsSummary() {
  return request<MetricsSummary>("/api/admin/metrics/summary");
}
