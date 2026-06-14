import { useState } from "react";
import { useAudit } from "@/entities/audit";
import { PageHeader, Spinner } from "@/shared/ui";

export function AuditLog() {
  const [page, setPage] = useState(1);
  const audit = useAudit(page);
  const items = audit.data?.items ?? [];

  return (
    <div>
      <PageHeader title="Audit Log" subtitle="History of admin moderation actions" />

      {audit.isLoading ? (
        <div className="page-loading">
          <Spinner />
          Loading audit log…
        </div>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Action</th>
                <th>Target</th>
                <th>At</th>
                <th>IP</th>
              </tr>
            </thead>
            <tbody>
              {items.length === 0 ? (
                <tr>
                  <td colSpan={4} className="table-empty">
                    No audit entries
                  </td>
                </tr>
              ) : (
                items.map((entry, index) => (
                  <tr key={`${entry.action}-${entry.at}-${index}`}>
                    <td>
                      <span className="badge neutral">{entry.action}</span>
                    </td>
                    <td className="mono">{entry.target}</td>
                    <td>{entry.at ? new Date(entry.at).toLocaleString() : "—"}</td>
                    <td className="mono">{entry.ip ?? "—"}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      <div className="pagination">
        <button className="btn secondary sm" disabled={page <= 1} onClick={() => setPage((p) => p - 1)}>
          Prev
        </button>
        <span className="muted">
          Page {audit.data?.page ?? page} / {audit.data?.totalPages ?? 1}
        </span>
        <button
          className="btn secondary sm"
          disabled={(audit.data?.page ?? 1) >= (audit.data?.totalPages ?? 1)}
          onClick={() => setPage((p) => p + 1)}
        >
          Next
        </button>
      </div>
    </div>
  );
}
