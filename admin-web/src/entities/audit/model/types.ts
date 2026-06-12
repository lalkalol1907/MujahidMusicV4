export interface AuditPage {
  items: { action: string; target: string; at: string | null; ip: string | null }[];
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
}
