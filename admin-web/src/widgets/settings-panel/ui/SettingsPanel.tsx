import { useStatus } from "@/entities/status";
import { Badge, PageHeader, Spinner } from "@/shared/ui";

export function SettingsPanel() {
  const status = useStatus();

  if (status.isLoading) {
    return (
      <div>
        <PageHeader title="Settings" subtitle="Runtime configuration and service status" />
        <div className="page-loading">
          <Spinner />
          Loading…
        </div>
      </div>
    );
  }

  const botOk = status.data?.botReachable ?? false;
  const mongoOk = status.data?.mongoReachable ?? false;

  return (
    <div>
      <PageHeader title="Settings" subtitle="Runtime configuration and service status" />

      <div className="card">
        <div className="settings-grid">
          <div className="settings-row">
            <span className="settings-label">Environment</span>
            <span className="settings-value">{status.data?.environment ?? "—"}</span>
          </div>
          <div className="settings-row">
            <span className="settings-label">Uptime</span>
            <span className="settings-value">{status.data?.uptimeSeconds ?? "—"}s</span>
          </div>
          <div className="settings-row">
            <span className="settings-label">Bot reachable</span>
            <Badge variant={botOk ? "success" : "danger"} dot>
              {botOk ? "Yes" : "No"}
            </Badge>
          </div>
          <div className="settings-row">
            <span className="settings-label">Mongo reachable</span>
            <Badge variant={mongoOk ? "success" : "danger"} dot>
              {mongoOk ? "Yes" : "No"}
            </Badge>
          </div>
        </div>

        <p className="muted" style={{ marginTop: "1.25rem", marginBottom: 0, fontSize: "0.85rem" }}>
          Config values are read-only. Update <code>.env</code> and restart services to change them.
        </p>
      </div>
    </div>
  );
}
