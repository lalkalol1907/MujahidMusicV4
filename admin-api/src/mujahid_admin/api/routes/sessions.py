from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, Request

from mujahid_admin.core.dependencies import check_rate_limit
from mujahid_admin.core.exceptions import BotNotFoundError
from mujahid_admin.schemas.session import ActionResponse, QueueTrack, SessionsResponse
from mujahid_admin.services import sessions as sessions_service

router = APIRouter()


@router.get("/sessions", response_model=SessionsResponse)
async def list_sessions() -> SessionsResponse:
    return await sessions_service.list_sessions()


@router.get("/sessions/{guild_id}", response_model=list[QueueTrack])
async def session_detail(guild_id: str) -> list[QueueTrack]:
    try:
        return await sessions_service.get_session_queue(guild_id)
    except BotNotFoundError as exc:
        raise HTTPException(status_code=404, detail="No active session") from exc


@router.post("/sessions/{guild_id}/skip", response_model=ActionResponse)
async def skip_session(
    guild_id: str,
    request: Request,
    _: None = Depends(check_rate_limit),
) -> ActionResponse:
    return await sessions_service.moderate_session(
        guild_id,
        "SKIP",
        f"/internal/sessions/{guild_id}/skip",
        request.client.host if request.client else None,
    )


@router.post("/sessions/{guild_id}/stop", response_model=ActionResponse)
async def stop_session(
    guild_id: str,
    request: Request,
    _: None = Depends(check_rate_limit),
) -> ActionResponse:
    return await sessions_service.moderate_session(
        guild_id,
        "STOP",
        f"/internal/sessions/{guild_id}/stop",
        request.client.host if request.client else None,
    )


@router.post("/sessions/{guild_id}/leave", response_model=ActionResponse)
async def leave_session(
    guild_id: str,
    request: Request,
    _: None = Depends(check_rate_limit),
) -> ActionResponse:
    return await sessions_service.moderate_session(
        guild_id,
        "LEAVE",
        f"/internal/sessions/{guild_id}/leave",
        request.client.host if request.client else None,
    )
