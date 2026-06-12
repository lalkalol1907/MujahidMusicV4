from __future__ import annotations

from mujahid_admin.repositories import audit as audit_repo
from mujahid_admin.schemas.audit import AuditEntry, PaginatedAudit


def _paginate(page: int, page_size: int, total: int) -> int:
    return max((total + page_size - 1) // page_size, 1)


async def list_audit(page: int, page_size: int) -> PaginatedAudit:
    items, total = await audit_repo.list_audit(page, page_size)
    return PaginatedAudit(
        items=[
            AuditEntry(
                action=item["action"],
                target=item["target"],
                at=item["at"].isoformat() if item["at"] else None,
                ip=item["ip"],
            )
            for item in items
        ],
        page=page,
        page_size=page_size,
        total=total,
        total_pages=_paginate(page, page_size, total),
    )
