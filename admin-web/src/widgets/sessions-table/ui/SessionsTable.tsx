import { useState } from "react";
import { type Session, useSessionQueue, useSessions } from "@/entities/session";
import { useSessionControl } from "@/features/session-control";
import { formatMs } from "@/shared/lib";

export function SessionsTable() {
  const [selected, setSelected] = useState<Session | null>(null);
  const sessions = useSessions();
  const queue = useSessionQueue(selected?.guildId);
  const action = useSessionControl(() => setSelected(null));

  return (
    <div>
      <h2 className="page-title">Live Sessions</h2>
      <table>
        <thead>
          <tr>
            <th>Guild</th>
            <th>Track</th>
            <th>Position</th>
            <th>Queue</th>
            <th>Volume</th>
            <th>Paused</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {(sessions.data?.sessions ?? []).map((session) => (
            <tr key={session.guildId}>
              <td>
                <button className="btn secondary" onClick={() => setSelected(session)}>
                  {session.guildName}
                </button>
                <div className="muted">{session.guildId}</div>
              </td>
              <td>
                {session.trackTitle ?? "—"}
                {session.trackAuthor ? ` — ${session.trackAuthor}` : ""}
              </td>
              <td>
                {formatMs(session.positionMs)} / {formatMs(session.lengthMs)}
              </td>
              <td>{session.queueSize}</td>
              <td>{session.volume}</td>
              <td>{session.paused ? "Yes" : "No"}</td>
              <td>
                <div className="actions">
                  <button
                    className="btn secondary"
                    onClick={() => action.mutate({ guildId: session.guildId, type: "skip" })}
                  >
                    Skip
                  </button>
                  <button
                    className="btn secondary"
                    onClick={() => action.mutate({ guildId: session.guildId, type: "stop" })}
                  >
                    Stop
                  </button>
                  <button
                    className="btn danger"
                    onClick={() => action.mutate({ guildId: session.guildId, type: "leave" })}
                  >
                    Leave
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {selected && (
        <div className="modal-backdrop" onClick={() => setSelected(null)}>
          <div className="modal" onClick={(event) => event.stopPropagation()}>
            <h3>{selected.guildName} queue</h3>
            <table>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Title</th>
                  <th>Author</th>
                  <th>Duration</th>
                </tr>
              </thead>
              <tbody>
                {(queue.data ?? []).map((track) => (
                  <tr key={`${track.position}-${track.uri}`}>
                    <td>{track.position}</td>
                    <td>{track.title}</td>
                    <td>{track.author}</td>
                    <td>{formatMs(track.durationMs)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
