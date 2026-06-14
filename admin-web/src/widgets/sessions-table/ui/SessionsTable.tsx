import { useState } from "react";
import { type Session, useSessionQueue, useSessions } from "@/entities/session";
import { useSessionControl } from "@/features/session-control";
import { formatMs } from "@/shared/lib";
import { Modal, PageHeader, Spinner } from "@/shared/ui";

export function SessionsTable() {
  const [selected, setSelected] = useState<Session | null>(null);
  const sessions = useSessions();
  const queue = useSessionQueue(selected?.guildId);
  const action = useSessionControl(() => setSelected(null));

  const rows = sessions.data?.sessions ?? [];

  return (
    <div>
      <PageHeader
        title="Live Sessions"
        subtitle="Monitor and control active playback across guilds"
      />

      {sessions.isLoading ? (
        <div className="page-loading">
          <Spinner />
          Loading sessions…
        </div>
      ) : (
        <div className="table-wrap">
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
              {rows.length === 0 ? (
                <tr>
                  <td colSpan={7} className="table-empty">
                    No active sessions
                  </td>
                </tr>
              ) : (
                rows.map((session) => (
                  <tr key={session.guildId}>
                    <td>
                      <button className="btn ghost sm" onClick={() => setSelected(session)}>
                        {session.guildName}
                      </button>
                      <div className="subtle">{session.guildId}</div>
                    </td>
                    <td>
                      {session.trackTitle ?? "—"}
                      {session.trackAuthor ? ` — ${session.trackAuthor}` : ""}
                    </td>
                    <td>
                      {formatMs(session.positionMs)} / {formatMs(session.lengthMs)}
                    </td>
                    <td>{session.queueSize}</td>
                    <td>{session.volume}%</td>
                    <td>{session.paused ? "Yes" : "No"}</td>
                    <td>
                      <div className="actions">
                        <button
                          className="btn secondary sm"
                          onClick={() => action.mutate({ guildId: session.guildId, type: "skip" })}
                        >
                          Skip
                        </button>
                        <button
                          className="btn secondary sm"
                          onClick={() => action.mutate({ guildId: session.guildId, type: "stop" })}
                        >
                          Stop
                        </button>
                        <button
                          className="btn danger sm"
                          onClick={() => action.mutate({ guildId: session.guildId, type: "leave" })}
                        >
                          Leave
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {selected && (
        <Modal title={`${selected.guildName} — Queue`} onClose={() => setSelected(null)}>
          {queue.isLoading ? (
            <div className="page-loading">
              <Spinner />
            </div>
          ) : (
            <div className="table-wrap">
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
                  {(queue.data ?? []).length === 0 ? (
                    <tr>
                      <td colSpan={4} className="table-empty">
                        Queue is empty
                      </td>
                    </tr>
                  ) : (
                    (queue.data ?? []).map((track) => (
                      <tr key={`${track.position}-${track.uri}`}>
                        <td>{track.position}</td>
                        <td>{track.title}</td>
                        <td>{track.author}</td>
                        <td>{formatMs(track.durationMs)}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          )}
        </Modal>
      )}
    </div>
  );
}
