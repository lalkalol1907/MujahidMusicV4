from __future__ import annotations

from typing import Any

from mujahid_admin.clients.bot import bot_client
from mujahid_admin.core.exceptions import BotNotFoundError
from mujahid_admin.repositories import audit as audit_repo
from mujahid_admin.schemas.session import ActionResponse, QueueTrack, SessionsResponse


async def list_sessions() -> SessionsResponse:
    data = await bot_client.get("/internal/sessions")
    return SessionsResponse.model_validate(data)


async def get_session_queue(guild_id: str) -> list[QueueTrack]:
    try:
        data = await bot_client.get(f"/internal/sessions/{guild_id}/queue")
    except BotNotFoundError as exc:
        raise BotNotFoundError("No active session") from exc
    return [QueueTrack.model_validate(item) for item in data]


async def moderate_session(
    guild_id: str,
    action: str,
    path: str,
    ip: str | None,
) -> ActionResponse:
    result: dict[str, Any] = await bot_client.post(path)
    await audit_repo.write_audit(action, guild_id, ip)
    return ActionResponse.model_validate(result)
