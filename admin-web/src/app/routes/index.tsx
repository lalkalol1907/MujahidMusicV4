import { Route, Routes } from "react-router-dom";
import { RequireAuth } from "@/features/auth/require-auth";
import { AppLayout } from "@/widgets/app-layout";
import { AuditPage } from "@/pages/audit";
import { DashboardPage } from "@/pages/dashboard";
import { GuildsPage } from "@/pages/guilds";
import { LoginPage } from "@/pages/login";
import { PlaylistsPage } from "@/pages/playlists";
import { SessionsPage } from "@/pages/sessions";
import { SettingsPage } from "@/pages/settings";

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        element={
          <RequireAuth>
            <AppLayout />
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
