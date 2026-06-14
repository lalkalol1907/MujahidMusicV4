import * as auditRepo from "@/repositories/audit";
import type { PaginatedAudit } from "@/types";

function paginate(page: number, pageSize: number, total: number): number {
  return Math.max(Math.ceil(total / pageSize), 1);
}

export async function listAudit(page: number, pageSize: number): Promise<PaginatedAudit> {
  const [items, total] = await auditRepo.listAudit(page, pageSize);
  return {
    items: items.map((item) => ({
      action: item.action,
      target: item.target,
      at: item.at ? item.at.toISOString() : null,
      ip: item.ip,
    })),
    page,
    pageSize,
    total,
    totalPages: paginate(page, pageSize, total),
  };
}
