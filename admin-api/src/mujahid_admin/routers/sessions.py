from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, Request

from mujahid_admin.client.bot import bot_client
from mujahid_admin.db.mongo import write_audit
from mujahid_admin.dependencies import check_rate_limit, verify_admin

router = APIRouter(prefix="/api/admin", tags=["sessions"])


@router.get("/sessions")
async def list_sessions(_: None = Depends(verify_admin)) -> dict:
    return await bot_client.get("/internal/sessions")


@router.get("/sessions/{guild_id}")
async def session_detail(guild_id: str, _: None = Depends(verify_admin)) -> list:
    try:
        return await bot_client.get(f"/internal/sessions/{guild_id}/queue")
    except Exception as exc:
        raise HTTPException(status_code=404, detail="No active session") from exc


@router.post("/sessions/{guild_id}/skip")
async def skip_session(
    guild_id: str,
    request: Request,
    _: None = Depends(verify_admin),
) -> dict:
    check_rate_limit(request)
    result = await bot_client.post(f"/internal/sessions/{guild_id}/skip")
    await write_audit("SKIP", guild_id, request.client.host if request.client else None)
    return result


@router.post("/sessions/{guild_id}/stop")
async def stop_session(
    guild_id: str,
    request: Request,
    _: None = Depends(verify_admin),
) -> dict:
    check_rate_limit(request)
    result = await bot_client.post(f"/internal/sessions/{guild_id}/stop")
    await write_audit("STOP", guild_id, request.client.host if request.client else None)
    return result


@router.post("/sessions/{guild_id}/leave")
async def leave_session(
    guild_id: str,
    request: Request,
    _: None = Depends(verify_admin),
) -> dict:
    check_rate_limit(request)
    result = await bot_client.post(f"/internal/sessions/{guild_id}/leave")
    await write_audit("LEAVE", guild_id, request.client.host if request.client else None)
    return result
