import { NavLink, Outlet } from "react-router-dom";
import { clearToken } from "../api/client";

export function Layout() {
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <h1>MujahidMusic Admin</h1>
        <NavLink className="nav-link" to="/">
          Dashboard
        </NavLink>
        <NavLink className="nav-link" to="/sessions">
          Sessions
        </NavLink>
        <NavLink className="nav-link" to="/playlists">
          Playlists
        </NavLink>
        <NavLink className="nav-link" to="/guilds">
          Guilds
        </NavLink>
        <NavLink className="nav-link" to="/audit">
          Audit
        </NavLink>
        <NavLink className="nav-link" to="/settings">
          Settings
        </NavLink>
        <button
          className="btn secondary"
          style={{ marginTop: "1.5rem", width: "100%" }}
          onClick={() => {
            clearToken();
            window.location.href = "/login";
          }}
        >
          Logout
        </button>
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
