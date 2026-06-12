from __future__ import annotations

from typing import Any

import httpx

from mujahid_admin.config import settings


class BotClient:
    def __init__(self) -> None:
        self._headers = {"Authorization": f"Bearer {settings.internal_api_key}"}

    async def get(self, path: str) -> Any:
        async with httpx.AsyncClient(timeout=10.0) as client:
            response = await client.get(
                f"{settings.bot_internal_url.rstrip('/')}{path}",
                headers=self._headers,
            )
            response.raise_for_status()
            return response.json()

    async def post(self, path: str) -> Any:
        async with httpx.AsyncClient(timeout=10.0) as client:
            response = await client.post(
                f"{settings.bot_internal_url.rstrip('/')}{path}",
                headers=self._headers,
            )
            response.raise_for_status()
            if response.content:
                return response.json()
            return {"status": "ok"}

    async def fetch_metrics_text(self) -> str:
        async with httpx.AsyncClient(timeout=10.0) as client:
            response = await client.get(f"{settings.bot_metrics_url.rstrip('/')}/metrics")
            response.raise_for_status()
            return response.text

    async def health_check(self) -> bool:
        try:
            async with httpx.AsyncClient(timeout=5.0) as client:
                response = await client.get(
                    f"{settings.bot_internal_url.rstrip('/')}/internal/health"
                )
                return response.status_code == 200
        except httpx.HTTPError:
            return False


bot_client = BotClient()
