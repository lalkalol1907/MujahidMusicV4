from __future__ import annotations

from typing import Any

import httpx

from mujahid_admin.core.config import settings
from mujahid_admin.core.exceptions import BotError, BotNotFoundError


class BotClient:
    def __init__(self) -> None:
        self._client: httpx.AsyncClient | None = None
        self._headers = {"Authorization": f"Bearer {settings.internal_api_key}"}

    async def start(self) -> None:
        if self._client is None:
            self._client = httpx.AsyncClient(timeout=10.0)

    async def close(self) -> None:
        if self._client is not None:
            await self._client.aclose()
            self._client = None

    def _http(self) -> httpx.AsyncClient:
        if self._client is None:
            raise RuntimeError("BotClient is not started")
        return self._client

    async def get(self, path: str) -> Any:
        try:
            response = await self._http().get(
                f"{settings.bot_internal_url.rstrip('/')}{path}",
                headers=self._headers,
            )
            response.raise_for_status()
            return response.json()
        except httpx.HTTPStatusError as exc:
            if exc.response.status_code == 404:
                raise BotNotFoundError(str(exc)) from exc
            raise BotError(str(exc)) from exc
        except httpx.HTTPError as exc:
            raise BotError(str(exc)) from exc

    async def post(self, path: str) -> Any:
        try:
            response = await self._http().post(
                f"{settings.bot_internal_url.rstrip('/')}{path}",
                headers=self._headers,
            )
            response.raise_for_status()
            if response.content:
                return response.json()
            return {"status": "ok"}
        except httpx.HTTPStatusError as exc:
            if exc.response.status_code == 404:
                raise BotNotFoundError(str(exc)) from exc
            raise BotError(str(exc)) from exc
        except httpx.HTTPError as exc:
            raise BotError(str(exc)) from exc

    async def fetch_metrics_text(self) -> str:
        try:
            response = await self._http().get(f"{settings.bot_metrics_url.rstrip('/')}/metrics")
            response.raise_for_status()
            return response.text
        except httpx.HTTPError as exc:
            raise BotError(str(exc)) from exc

    async def health_check(self) -> bool:
        try:
            response = await self._http().get(
                f"{settings.bot_internal_url.rstrip('/')}/internal/health",
                timeout=5.0,
            )
            return response.status_code == 200
        except httpx.HTTPError:
            return False


bot_client = BotClient()
