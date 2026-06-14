import { useGuilds } from "@/entities/guild";
import { useSessions } from "@/entities/session";
import { Badge, PageHeader, Spinner } from "@/shared/ui";

export function GuildsTable() {
  const guilds = useGuilds();
  const sessions = useSessions(10_000);

  const activeGuildIds = new Set((sessions.data?.sessions ?? []).map((session) => session.guildId));
  const rows = guilds.data ?? [];

  return (
    <div>
      <PageHeader title="Guilds" subtitle="All connected Discord servers" />

      {guilds.isLoading ? (
        <div className="page-loading">
          <Spinner />
          Loading guilds…
        </div>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Members</th>
                <th>Session</th>
              </tr>
            </thead>
            <tbody>
              {rows.length === 0 ? (
                <tr>
                  <td colSpan={4} className="table-empty">
                    No guilds found
                  </td>
                </tr>
              ) : (
                rows.map((guild) => {
                  const active = activeGuildIds.has(guild.id);
                  return (
                    <tr key={guild.id}>
                      <td className="mono">{guild.id}</td>
                      <td>{guild.name}</td>
                      <td>{guild.memberCount.toLocaleString()}</td>
                      <td>
                        <Badge variant={active ? "success" : "neutral"} dot>
                          {active ? "Active" : "Idle"}
                        </Badge>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
