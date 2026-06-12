import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { api } from "../api/client";

export function PlaylistsPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(1);
  const [ownerId, setOwnerId] = useState("");
  const [name, setName] = useState("");
  const [detail, setDetail] = useState<{ ownerId: string; name: string } | null>(null);

  const params = new URLSearchParams({ page: String(page), pageSize: "50" });
  if (ownerId) params.set("ownerId", ownerId);
  if (name) params.set("name", name);

  const playlists = useQuery({
    queryKey: ["playlists", page, ownerId, name],
    queryFn: () => api.playlists(params),
  });

  const stats = useQuery({
    queryKey: ["playlist-stats"],
    queryFn: api.playlistStats,
  });

  const playlistDetail = useQuery({
    queryKey: ["playlist", detail?.ownerId, detail?.name],
    queryFn: () => api.playlist(detail!.ownerId, detail!.name),
    enabled: !!detail,
  });

  const remove = useMutation({
    mutationFn: ({ ownerId, name }: { ownerId: string; name: string }) =>
      api.deletePlaylist(ownerId, name),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["playlists"] });
      queryClient.invalidateQueries({ queryKey: ["playlist-stats"] });
    },
  });

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
