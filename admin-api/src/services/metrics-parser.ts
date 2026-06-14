const COUNTER_LINE =
  /^(?<name>[a-zA-Z_:][a-zA-Z0-9_:]*)(?:\{(?<labels>[^}]*)\})?\s+(?<value>-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?)$/;

const LABEL_PAIR = /(\w+)="((?:\\.|[^"\\])*)"/g;

function parseLabels(raw: string): Record<string, string> {
  const labels: Record<string, string> = {};
  for (const match of raw.matchAll(LABEL_PAIR)) {
    labels[match[1]] = match[2];
  }
  return labels;
}

export function parsePrometheus(text: string) {
  const counters: Record<string, number> = {};
  const labeledCounters: { name: string; labels: Record<string, string>; value: number }[] = [];
  const gauges: Record<string, number> = {};

  for (const line of text.split("\n")) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith("#")) {
      continue;
    }
    const match = COUNTER_LINE.exec(trimmed);
    if (!match?.groups) {
      continue;
    }
    const name = match.groups.name;
    const labelsRaw = match.groups.labels;
    const value = Number(match.groups.value);
    if (labelsRaw) {
      labeledCounters.push({ name, labels: parseLabels(labelsRaw), value });
    } else if (name.endsWith("_total")) {
      counters[name] = value;
    } else {
      gauges[name] = value;
    }
  }

  return { counters, labeled_counters: labeledCounters, gauges };
}

export function buildMetricsSummary(parsed: ReturnType<typeof parsePrometheus>) {
  const commands: { command: string; count?: number; errors?: number }[] = [];
  const trackFailures: { reason: string; count: number }[] = [];
  const playlistOps: { op: string; count: number }[] = [];

  for (const entry of parsed.labeled_counters) {
    const { name, labels, value } = entry;
    if (name === "mujahid_commands_total" && labels.command) {
      commands.push({ command: labels.command, count: Math.trunc(value) });
    } else if (name === "mujahid_command_errors_total" && labels.command) {
      commands.push({ command: labels.command, errors: Math.trunc(value) });
    } else if (name === "mujahid_track_load_failures_total" && labels.reason) {
      trackFailures.push({ reason: labels.reason, count: Math.trunc(value) });
    } else if (name === "mujahid_playlist_ops_total" && labels.op) {
      playlistOps.push({ op: labels.op, count: Math.trunc(value) });
    }
  }

  commands.sort(
    (a, b) => (b.count ?? b.errors ?? 0) - (a.count ?? a.errors ?? 0),
  );

  const guildsTotal =
    parsed.gauges.mujahid_guilds_total ?? parsed.counters.mujahid_guilds_total ?? 0;

  return {
    guilds_total: Math.trunc(guildsTotal),
    active_players: Math.trunc(parsed.gauges.mujahid_active_players ?? 0),
    commands: commands.slice(0, 10),
    track_load_failures: trackFailures,
    playlist_ops: playlistOps,
  };
}
