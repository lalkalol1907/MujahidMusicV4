import {
  ClipboardList,
  LayoutDashboard,
  ListMusic,
  LogOut,
  Music2,
  Radio,
  Settings,
  Users,
} from "lucide-react";
import { NavLink, Outlet } from "react-router-dom";
import { logout } from "@/features/auth/logout";

const navItems = [
  { to: "/", label: "Dashboard", icon: LayoutDashboard, end: true as const },
  { to: "/sessions", label: "Sessions", icon: Radio },
  { to: "/playlists", label: "Playlists", icon: ListMusic },
  { to: "/guilds", label: "Guilds", icon: Users },
  { to: "/audit", label: "Audit", icon: ClipboardList },
  { to: "/settings", label: "Settings", icon: Settings },
];

export function AppLayout() {
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <div className="sidebar-brand-icon">
            <Music2 size={18} />
          </div>
          <div>
            <div className="sidebar-brand-text">MujahidMusic</div>
            <div className="sidebar-brand-sub">Admin Panel</div>
          </div>
        </div>

        <nav className="sidebar-nav">
          {navItems.map(({ to, label, icon: Icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <button className="btn secondary" style={{ width: "100%" }} onClick={logout}>
            <LogOut size={16} />
            Logout
          </button>
        </div>
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
