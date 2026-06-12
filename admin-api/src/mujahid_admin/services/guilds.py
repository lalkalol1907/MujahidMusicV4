from __future__ import annotations

from mujahid_admin.clients.bot import bot_client
from mujahid_admin.schemas.guild import Guild


async def list_guilds() -> list[Guild]:
    data = await bot_client.get("/internal/guilds")
    return [Guild.model_validate(item) for item in data]
