import { describe, expect, test } from "bun:test";
import { buildMetricsSummary, parsePrometheus } from "@/services/metrics-parser";

describe("metrics parser", () => {
  test("parses prometheus text", () => {
    const text = `
# HELP mujahid_guilds_total guilds
mujahid_guilds_total 3
mujahid_active_players 1
mujahid_commands_total{command="play"} 10
`;
    const parsed = parsePrometheus(text);
    const summary = buildMetricsSummary(parsed);
    expect(summary.guilds_total).toBe(3);
    expect(summary.active_players).toBe(1);
    expect(summary.commands[0]?.command).toBe("play");
  });
});
