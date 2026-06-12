from fastapi import APIRouter

from mujahid_admin.schemas.status import StatusResponse
from mujahid_admin.services import status as status_service

router = APIRouter()


@router.get("/status", response_model=StatusResponse)
async def status() -> StatusResponse:
    return await status_service.get_status()
