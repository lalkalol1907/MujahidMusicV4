import { request } from "@/shared/api";
import type { AuditPage } from "../model/types";

export function fetchAudit(page = 1) {
  return request<AuditPage>(`/api/admin/audit?page=${page}`);
}
