from pydantic import BaseModel, ConfigDict, Field


class AuditEntry(BaseModel):
    action: str
    target: str
    at: str | None
    ip: str | None


class PaginatedAudit(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    items: list[AuditEntry]
    page: int
    page_size: int = Field(alias="pageSize")
    total: int
    total_pages: int = Field(alias="totalPages")
