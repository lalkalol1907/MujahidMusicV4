from __future__ import annotations

from mujahid_admin.clients.bot import bot_client
from mujahid_admin.schemas.metrics import MetricsSummary
from mujahid_admin.services.metrics_parser import build_metrics_summary, parse_prometheus


async def get_metrics_summary() -> MetricsSummary:
    text = await bot_client.fetch_metrics_text()
    parsed = parse_prometheus(text)
    summary = build_metrics_summary(parsed)
    return MetricsSummary.model_validate(summary)
