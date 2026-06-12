import { useStatus } from "@/entities/status";

export function SettingsPanel() {
  const status = useStatus();

  return (
    <div>
      <h2 className="page-title">Settings</h2>
      <div className="card">
        <p>
          <strong>Environment:</strong> {status.data?.environment ?? "—"}
        </p>
        <p>
          <strong>Uptime:</strong> {status.data?.uptimeSeconds ?? "—"}s
        </p>
        <p>
          <strong>Bot reachable:</strong> {status.data?.botReachable ? "yes" : "no"}
        </p>
        <p>
          <strong>Mongo reachable:</strong> {status.data?.mongoReachable ? "yes" : "no"}
        </p>
        <p className="muted" style={{ marginTop: "1rem" }}>
          Config values are read-only. Update `.env` and restart services to change them.
        </p>
      </div>
    </div>
  );
}
