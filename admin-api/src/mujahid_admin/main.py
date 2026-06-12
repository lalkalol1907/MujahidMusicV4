from __future__ import annotations

from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from mujahid_admin.api.router import router
from mujahid_admin.clients.bot import bot_client
from mujahid_admin.core.config import settings
from mujahid_admin.db.connection import close_mongo


@asynccontextmanager
async def lifespan(_: FastAPI):
    await bot_client.start()
    yield
    await bot_client.close()
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

    app.include_router(router)

    return app


app = create_app()
