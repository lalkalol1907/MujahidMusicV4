import { useState } from "react";
import { usePlaylistDetail, usePlaylists, usePlaylistStats } from "@/entities/playlist";
import { usePlaylistDelete } from "@/features/playlist-delete";
import { Modal, PageHeader, Spinner } from "@/shared/ui";

export function PlaylistsManager() {
  const [page, setPage] = useState(1);
  const [ownerId, setOwnerId] = useState("");
  const [name, setName] = useState("");
  const [detail, setDetail] = useState<{ ownerId: string; name: string } | null>(null);
  const [confirmDelete, setConfirmDelete] = useState<{ ownerId: string; name: string } | null>(
    null,
  );

  const playlists = usePlaylists(page, ownerId, name);
  const stats = usePlaylistStats();
  const playlistDetail = usePlaylistDetail(detail?.ownerId, detail?.name);
  const remove = usePlaylistDelete();

  const items = playlists.data?.items ?? [];

  return (
    <div>
      <PageHeader title="Playlists" subtitle="Browse and manage saved user playlists" />

      <div className="grid" style={{ marginBottom: "1.25rem" }}>
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
          placeholder="Filter by owner ID"
          value={ownerId}
          onChange={(event) => {
            setPage(1);
            setOwnerId(event.target.value);
          }}
        />
        <input
          placeholder="Filter by name"
          value={name}
          onChange={(event) => {
            setPage(1);
            setName(event.target.value);
          }}
        />
      </div>

      {playlists.isLoading ? (
        <div className="page-loading">
          <Spinner />
          Loading playlists…
        </div>
      ) : (
        <div className="table-wrap">
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
              {items.length === 0 ? (
                <tr>
                  <td colSpan={4} className="table-empty">
                    No playlists found
                  </td>
                </tr>
              ) : (
                items.map((item) => (
                  <tr key={`${item.ownerId}-${item.name}`}>
                    <td className="mono">{item.ownerId}</td>
                    <td>{item.name}</td>
                    <td>{item.trackCount}</td>
                    <td>
                      <div className="actions">
                        <button className="btn secondary sm" onClick={() => setDetail(item)}>
                          View
                        </button>
                        <button
                          className="btn danger sm"
                          onClick={() => setConfirmDelete(item)}
                        >
                          Delete
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

      <div className="pagination">
        <button className="btn secondary sm" disabled={page <= 1} onClick={() => setPage((p) => p - 1)}>
          Prev
        </button>
        <span className="muted">
          Page {playlists.data?.page ?? page} / {playlists.data?.totalPages ?? 1}
        </span>
        <button
          className="btn secondary sm"
          disabled={(playlists.data?.page ?? 1) >= (playlists.data?.totalPages ?? 1)}
          onClick={() => setPage((p) => p + 1)}
        >
          Next
        </button>
      </div>

      {detail && (
        <Modal title={`${detail.name} (${detail.ownerId})`} onClose={() => setDetail(null)}>
          {playlistDetail.isLoading ? (
            <div className="page-loading">
              <Spinner />
            </div>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Title</th>
                    <th>URI</th>
                  </tr>
                </thead>
                <tbody>
                  {(playlistDetail.data?.tracks ?? []).length === 0 ? (
                    <tr>
                      <td colSpan={2} className="table-empty">
                        No tracks
                      </td>
                    </tr>
                  ) : (
                    (playlistDetail.data?.tracks ?? []).map((track, index) => (
                      <tr key={`${track.encoded}-${index}`}>
                        <td>{track.title}</td>
                        <td className="mono">{track.uri}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          )}
        </Modal>
      )}

      {confirmDelete && (
        <Modal title="Delete playlist?" onClose={() => setConfirmDelete(null)}>
          <div style={{ padding: "1.25rem 1.5rem" }}>
            <p style={{ margin: "0 0 1.25rem" }}>
              Permanently delete <strong>{confirmDelete.name}</strong> owned by{" "}
              <span className="mono">{confirmDelete.ownerId}</span>?
            </p>
            <div className="actions">
              <button className="btn secondary" onClick={() => setConfirmDelete(null)}>
                Cancel
              </button>
              <button
                className="btn danger"
                disabled={remove.isPending}
                onClick={() => {
                  remove.mutate(confirmDelete, {
                    onSuccess: () => setConfirmDelete(null),
                  });
                }}
              >
                {remove.isPending ? "Deleting…" : "Delete"}
              </button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
}
