import { useState } from "react";
import { useAudit } from "@/entities/audit";

export function AuditLog() {
  const [page, setPage] = useState(1);
  const audit = useAudit(page);

  return (
    <div>
      <h2 className="page-title">Audit Log</h2>
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
          {(audit.data?.items ?? []).map((entry, index) => (
            <tr key={`${entry.action}-${entry.at}-${index}`}>
              <td>{entry.action}</td>
              <td>{entry.target}</td>
              <td>{entry.at ?? "—"}</td>
              <td>{entry.ip ?? "—"}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className="actions" style={{ marginTop: "1rem" }}>
        <button className="btn secondary" disabled={page <= 1} onClick={() => setPage((p) => p - 1)}>
          Prev
        </button>
        <span className="muted">
          Page {audit.data?.page ?? page} / {audit.data?.totalPages ?? 1}
        </span>
        <button
          className="btn secondary"
          disabled={(audit.data?.page ?? 1) >= (audit.data?.totalPages ?? 1)}
          onClick={() => setPage((p) => p + 1)}
        >
          Next
        </button>
      </div>
    </div>
  );
}
