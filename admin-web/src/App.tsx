import { Navigate, Route, Routes } from "react-router-dom";
import { getToken } from "./api/client";
import { Layout } from "./components/Layout";
import { AuditPage } from "./pages/Audit";
import { DashboardPage } from "./pages/Dashboard";
import { GuildsPage } from "./pages/Guilds";
import { LoginPage } from "./pages/Login";
import { PlaylistsPage } from "./pages/Playlists";
import { SessionsPage } from "./pages/Sessions";
import { SettingsPage } from "./pages/Settings";

function RequireAuth({ children }: { children: React.ReactNode }) {
  if (!getToken()) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        element={
          <RequireAuth>
            <Layout />
          </RequireAuth>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="sessions" element={<SessionsPage />} />
        <Route path="playlists" element={<PlaylistsPage />} />
        <Route path="guilds" element={<GuildsPage />} />
        <Route path="audit" element={<AuditPage />} />
        <Route path="settings" element={<SettingsPage />} />
      </Route>
    </Routes>
  );
}
