import { Bar, BarChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { useMetricsSummary, useStatus } from "@/entities/status";
import { usePlaylistStats } from "@/entities/playlist";

export function DashboardOverview() {
  const status = useStatus();
  const metrics = useMetricsSummary();
  const playlistStats = usePlaylistStats();

  return (
    <div>
      <h2 className="page-title">Dashboard</h2>
      <div className="grid">
        <div className="card">
          <div className="card-label">Guilds</div>
          <div className="card-value">{metrics.data?.guilds_total ?? "—"}</div>
        </div>
        <div className="card">
          <div className="card-label">Active players</div>
          <div className="card-value">{metrics.data?.active_players ?? "—"}</div>
        </div>
        <div className="card">
          <div className="card-label">Playlists</div>
          <div className="card-value">{playlistStats.data?.totalPlaylists ?? "—"}</div>
        </div>
        <div className="card">
          <div className="card-label">Bot</div>
          <div className="card-value">{status.data?.botReachable ? "OK" : "DOWN"}</div>
        </div>
        <div className="card">
          <div className="card-label">Mongo</div>
          <div className="card-value">{status.data?.mongoReachable ? "OK" : "DOWN"}</div>
        </div>
      </div>

      <div className="chart-card">
        <h3>Top commands</h3>
        <div style={{ width: "100%", height: 280 }}>
          <ResponsiveContainer>
            <BarChart data={metrics.data?.commands ?? []}>
              <XAxis dataKey="command" stroke="#9ca3af" />
              <YAxis stroke="#9ca3af" />
              <Tooltip />
              <Bar dataKey="count" fill="#2563eb" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}
