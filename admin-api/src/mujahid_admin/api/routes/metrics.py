from fastapi import APIRouter

from mujahid_admin.schemas.metrics import MetricsSummary
from mujahid_admin.services import metrics as metrics_service

router = APIRouter()


@router.get("/metrics/summary", response_model=MetricsSummary)
async def metrics_summary() -> MetricsSummary:
    return await metrics_service.get_metrics_summary()
