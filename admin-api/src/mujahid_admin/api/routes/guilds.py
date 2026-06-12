from fastapi import APIRouter

from mujahid_admin.schemas.guild import Guild
from mujahid_admin.services import guilds as guilds_service

router = APIRouter()


@router.get("/guilds", response_model=list[Guild])
async def list_guilds() -> list[Guild]:
    return await guilds_service.list_guilds()
