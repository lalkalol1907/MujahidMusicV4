import { useQuery } from "@tanstack/react-query";
import { api } from "../api/client";

export function GuildsPage() {
  const guilds = useQuery({ queryKey: ["guilds"], queryFn: api.guilds });
  const sessions = useQuery({ queryKey: ["sessions"], queryFn: api.sessions, refetchInterval: 10_000 });

  const activeGuildIds = new Set((sessions.data?.sessions ?? []).map((session) => session.guildId));

  return (
    <div>
      <h2 className="page-title">Guilds</h2>
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
          {(guilds.data ?? []).map((guild) => (
            <tr key={guild.id}>
              <td>{guild.id}</td>
              <td>{guild.name}</td>
              <td>{guild.memberCount}</td>
              <td>
                <span className={`badge ${activeGuildIds.has(guild.id) ? "" : "off"}`}>
                  {activeGuildIds.has(guild.id) ? "Active" : "Idle"}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
