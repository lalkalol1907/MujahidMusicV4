from __future__ import annotations

from fastapi import APIRouter, Depends

from mujahid_admin.client.bot import bot_client
from mujahid_admin.dependencies import verify_admin
from mujahid_admin.services.metrics_parser import build_metrics_summary, parse_prometheus

router = APIRouter(prefix="/api/admin", tags=["metrics"])


@router.get("/metrics/summary")
async def metrics_summary(_: None = Depends(verify_admin)) -> dict:
    text = await bot_client.fetch_metrics_text()
    parsed = parse_prometheus(text)
    return build_metrics_summary(parsed)
