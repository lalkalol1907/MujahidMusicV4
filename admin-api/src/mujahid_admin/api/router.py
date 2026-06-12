from fastapi import APIRouter, Depends

from mujahid_admin.api.routes import audit, guilds, metrics, playlists, sessions, status
from mujahid_admin.core.dependencies import verify_admin

router = APIRouter(prefix="/api/admin", dependencies=[Depends(verify_admin)])

router.include_router(status.router, tags=["status"])
router.include_router(metrics.router, tags=["metrics"])
router.include_router(sessions.router, tags=["sessions"])
router.include_router(guilds.router, tags=["guilds"])
router.include_router(playlists.router, tags=["playlists"])
router.include_router(audit.router, tags=["audit"])
