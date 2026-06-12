from fastapi import APIRouter, Query

from mujahid_admin.schemas.audit import PaginatedAudit
from mujahid_admin.services import audit as audit_service

router = APIRouter()


@router.get("/audit", response_model=PaginatedAudit)
async def audit_log(
    page: int = Query(default=1, ge=1),
    page_size: int = Query(default=50, ge=1, le=100, alias="pageSize"),
) -> PaginatedAudit:
    return await audit_service.list_audit(page, page_size)
