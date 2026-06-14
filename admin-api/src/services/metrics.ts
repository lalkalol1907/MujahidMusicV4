import { botClient } from "@/clients/bot";
import { buildMetricsSummary, parsePrometheus } from "@/services/metrics-parser";
import type { MetricsSummary } from "@/types";

export async function getMetricsSummary(): Promise<MetricsSummary> {
  const text = await botClient.fetchMetricsText();
  const parsed = parsePrometheus(text);
  return buildMetricsSummary(parsed);
}
