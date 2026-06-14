import { Activity, Database, Disc3, ListMusic, Users } from "lucide-react";
import { Bar, BarChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { useMetricsSummary, useStatus } from "@/entities/status";
import { usePlaylistStats } from "@/entities/playlist";
import { Badge, PageHeader, Spinner } from "@/shared/ui";

export function DashboardOverview() {
  const status = useStatus();
  const metrics = useMetricsSummary();
  const playlistStats = usePlaylistStats();

  const loading = status.isLoading || metrics.isLoading || playlistStats.isLoading;

  if (loading) {
    return (
      <div>
        <PageHeader title="Dashboard" subtitle="System overview and metrics" />
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
      <PageHeader title="Dashboard" subtitle="System overview and metrics" />

      <div className="grid">
        <div className="card card-stat">
          <div className="card-stat-icon">
            <Users size={20} />
          </div>
          <div>
            <div className="card-label">Guilds</div>
            <div className="card-value">{metrics.data?.guilds_total ?? "—"}</div>
          </div>
        </div>

        <div className="card card-stat">
          <div className="card-stat-icon">
            <Disc3 size={20} />
          </div>
          <div>
            <div className="card-label">Active players</div>
            <div className="card-value">{metrics.data?.active_players ?? "—"}</div>
          </div>
        </div>

        <div className="card card-stat">
          <div className="card-stat-icon">
            <ListMusic size={20} />
          </div>
          <div>
            <div className="card-label">Playlists</div>
            <div className="card-value">{playlistStats.data?.totalPlaylists ?? "—"}</div>
          </div>
        </div>

        <div className="card card-stat">
          <div className={`card-stat-icon ${botOk ? "success" : "danger"}`}>
            <Activity size={20} />
          </div>
          <div>
            <div className="card-label">Bot</div>
            <div className="card-value">
              <Badge variant={botOk ? "success" : "danger"} dot>
                {botOk ? "Online" : "Offline"}
              </Badge>
            </div>
          </div>
        </div>

        <div className="card card-stat">
          <div className={`card-stat-icon ${mongoOk ? "success" : "danger"}`}>
            <Database size={20} />
          </div>
          <div>
            <div className="card-label">MongoDB</div>
            <div className="card-value">
              <Badge variant={mongoOk ? "success" : "danger"} dot>
                {mongoOk ? "Connected" : "Unreachable"}
              </Badge>
            </div>
          </div>
        </div>
      </div>

      <div className="chart-card">
        <h3>Top commands</h3>
        <div style={{ width: "100%", height: 280 }}>
          <ResponsiveContainer>
            <BarChart data={metrics.data?.commands ?? []}>
              <XAxis
                dataKey="command"
                stroke="#71717a"
                tick={{ fill: "#a1a1aa", fontSize: 12 }}
                axisLine={{ stroke: "rgba(255,255,255,0.07)" }}
              />
              <YAxis
                stroke="#71717a"
                tick={{ fill: "#a1a1aa", fontSize: 12 }}
                axisLine={{ stroke: "rgba(255,255,255,0.07)" }}
              />
              <Tooltip
                contentStyle={{
                  background: "#16161f",
                  border: "1px solid rgba(255,255,255,0.1)",
                  borderRadius: "8px",
                  color: "#f4f4f5",
                }}
              />
              <Bar dataKey="count" fill="#8b5cf6" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}
