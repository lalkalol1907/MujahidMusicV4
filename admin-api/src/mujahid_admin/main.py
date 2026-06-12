from __future__ import annotations

from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from mujahid_admin.config import settings
from mujahid_admin.db.mongo import close_mongo
from mujahid_admin.routers import audit, guilds, metrics, playlists, sessions, status


@asynccontextmanager
async def lifespan(_: FastAPI):
    yield
    await close_mongo()


def create_app() -> FastAPI:
    app = FastAPI(
        title="MujahidMusic Admin API",
        version="1.0.0",
        lifespan=lifespan,
        docs_url="/docs" if settings.environment != "production" else None,
        redoc_url=None,
    )

    app.add_middleware(
        CORSMiddleware,
        allow_origins=[settings.admin_cors_origin],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    app.include_router(status.router)
    app.include_router(metrics.router)
    app.include_router(sessions.router)
    app.include_router(guilds.router)
    app.include_router(playlists.router)
    app.include_router(audit.router)

    return app


app = create_app()
