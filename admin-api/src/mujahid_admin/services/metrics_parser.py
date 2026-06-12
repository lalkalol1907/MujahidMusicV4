from __future__ import annotations

import re
from typing import Any


_COUNTER_LINE = re.compile(
    r"^(?P<name>[a-zA-Z_:][a-zA-Z0-9_:]*)(?:\{(?P<labels>[^}]*)\})?\s+(?P<value>-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?)$"
)
_LABEL_PAIR = re.compile(r'(\w+)="((?:\\.|[^"\\])*)"')


def _parse_labels(raw: str) -> dict[str, str]:
    return {match.group(1): match.group(2) for match in _LABEL_PAIR.finditer(raw)}


def parse_prometheus(text: str) -> dict[str, Any]:
    counters: dict[str, float] = {}
    labeled_counters: list[dict[str, Any]] = []
    gauges: dict[str, float] = {}

    for line in text.splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        match = _COUNTER_LINE.match(line)
        if not match:
            continue
        name = match.group("name")
        labels_raw = match.group("labels")
        value = float(match.group("value"))
        if labels_raw:
            labels = _parse_labels(labels_raw)
            labeled_counters.append({"name": name, "labels": labels, "value": value})
        elif name.endswith("_total"):
            counters[name] = value
        else:
            gauges[name] = value

    return {
        "counters": counters,
        "labeled_counters": labeled_counters,
        "gauges": gauges,
    }


def build_metrics_summary(parsed: dict[str, Any]) -> dict[str, Any]:
    commands: list[dict[str, Any]] = []
    track_failures: list[dict[str, Any]] = []
    playlist_ops: list[dict[str, Any]] = []

    for entry in parsed["labeled_counters"]:
        name = entry["name"]
        labels = entry["labels"]
        value = entry["value"]
        if name == "mujahid_commands_total" and "command" in labels:
            commands.append({"command": labels["command"], "count": int(value)})
        elif name == "mujahid_command_errors_total" and "command" in labels:
            commands.append({"command": labels["command"], "errors": int(value)})
        elif name == "mujahid_track_load_failures_total" and "reason" in labels:
            track_failures.append({"reason": labels["reason"], "count": int(value)})
        elif name == "mujahid_playlist_ops_total" and "op" in labels:
            playlist_ops.append({"op": labels["op"], "count": int(value)})

    commands.sort(key=lambda item: item.get("count", item.get("errors", 0)), reverse=True)

    gauges = parsed["gauges"]
    counters = parsed["counters"]
    guilds_total = gauges.get("mujahid_guilds_total", counters.get("mujahid_guilds_total", 0))
    return {
        "guilds_total": int(guilds_total),
        "active_players": int(gauges.get("mujahid_active_players", 0)),
        "commands": commands[:10],
        "track_load_failures": track_failures,
        "playlist_ops": playlist_ops,
    }
