import { useState } from "react";
import { usePlaylistDetail, usePlaylists, usePlaylistStats } from "@/entities/playlist";
import { usePlaylistDelete } from "@/features/playlist-delete";

export function PlaylistsManager() {
  const [page, setPage] = useState(1);
  const [ownerId, setOwnerId] = useState("");
  const [name, setName] = useState("");
  const [detail, setDetail] = useState<{ ownerId: string; name: string } | null>(null);

  const playlists = usePlaylists(page, ownerId, name);
  const stats = usePlaylistStats();
  const playlistDetail = usePlaylistDetail(detail?.ownerId, detail?.name);
  const remove = usePlaylistDelete();

  return (
    <div>
      <h2 className="page-title">Playlists</h2>
      <div className="grid" style={{ marginBottom: "1rem" }}>
        <div className="card">
          <div className="card-label">Total playlists</div>
          <div className="card-value">{stats.data?.totalPlaylists ?? "—"}</div>
        </div>
        <div className="card">
          <div className="card-label">Total tracks saved</div>
          <div className="card-value">{stats.data?.totalTracks ?? "—"}</div>
        </div>
      </div>

      <div className="form-row">
        <input
          placeholder="Owner ID"
          value={ownerId}
          onChange={(event) => {
            setPage(1);
            setOwnerId(event.target.value);
          }}
        />
        <input
          placeholder="Name filter"
          value={name}
          onChange={(event) => {
            setPage(1);
            setName(event.target.value);
          }}
        />
      </div>

      <table>
        <thead>
          <tr>
            <th>Owner ID</th>
            <th>Name</th>
            <th>Tracks</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {(playlists.data?.items ?? []).map((item) => (
            <tr key={`${item.ownerId}-${item.name}`}>
              <td>{item.ownerId}</td>
              <td>{item.name}</td>
              <td>{item.trackCount}</td>
              <td>
                <div className="actions">
                  <button className="btn secondary" onClick={() => setDetail(item)}>
                    View
                  </button>
                  <button
                    className="btn danger"
                    onClick={() => {
                      if (confirm(`Delete playlist ${item.name}?`)) {
                        remove.mutate(item);
                      }
                    }}
                  >
                    Delete
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="actions" style={{ marginTop: "1rem" }}>
        <button className="btn secondary" disabled={page <= 1} onClick={() => setPage((p) => p - 1)}>
          Prev
        </button>
        <span className="muted">
          Page {playlists.data?.page ?? page} / {playlists.data?.totalPages ?? 1}
        </span>
        <button
          className="btn secondary"
          disabled={(playlists.data?.page ?? 1) >= (playlists.data?.totalPages ?? 1)}
          onClick={() => setPage((p) => p + 1)}
        >
          Next
        </button>
      </div>

      {detail && (
        <div className="modal-backdrop" onClick={() => setDetail(null)}>
          <div className="modal" onClick={(event) => event.stopPropagation()}>
            <h3>
              {detail.name} ({detail.ownerId})
            </h3>
            <table>
              <thead>
                <tr>
                  <th>Title</th>
                  <th>URI</th>
                </tr>
              </thead>
              <tbody>
                {(playlistDetail.data?.tracks ?? []).map((track, index) => (
                  <tr key={`${track.encoded}-${index}`}>
                    <td>{track.title}</td>
                    <td>{track.uri}</td>
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
