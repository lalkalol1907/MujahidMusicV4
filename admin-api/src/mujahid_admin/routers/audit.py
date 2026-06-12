from __future__ import annotations

from fastapi import APIRouter, Depends, Query

from mujahid_admin.db.mongo import list_audit
from mujahid_admin.dependencies import verify_admin

router = APIRouter(prefix="/api/admin", tags=["audit"])


@router.get("/audit")
async def audit_log(
    page: int = Query(default=1, ge=1),
    page_size: int = Query(default=50, ge=1, le=100, alias="pageSize"),
    _: None = Depends(verify_admin),
) -> dict:
    return await list_audit(page, page_size)
