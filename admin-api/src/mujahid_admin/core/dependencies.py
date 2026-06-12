from __future__ import annotations

import time

from fastapi import Header, HTTPException, Request

from mujahid_admin.core.config import settings

_rate_buckets: dict[str, list[float]] = {}
_RATE_LIMIT = 10
_RATE_WINDOW_SECONDS = 60.0


async def verify_admin(authorization: str | None = Header(default=None)) -> None:
    if authorization is None or authorization != f"Bearer {settings.admin_api_key}":
        raise HTTPException(status_code=401, detail="Unauthorized")


def check_rate_limit(request: Request) -> None:
    client_ip = request.client.host if request.client else "unknown"
    now = time.time()
    bucket = _rate_buckets.setdefault(client_ip, [])
    bucket[:] = [stamp for stamp in bucket if now - stamp < _RATE_WINDOW_SECONDS]
    if len(bucket) >= _RATE_LIMIT:
        raise HTTPException(status_code=429, detail="Rate limit exceeded")
    bucket.append(now)
