import { beforeEach, describe, expect, mock, test } from "bun:test";
import { createApp } from "../src/app";
import { settings } from "../src/core/config";
import { resetRateLimits } from "../src/core/auth";

const authHeaders = { Authorization: "Bearer test-admin-key" };

describe("admin api", () => {
  beforeEach(() => {
    settings.adminApiKey = "test-admin-key";
    resetRateLimits();
  });

  test("status unauthorized", async () => {
    const app = createApp();
    const res = await app.request("/api/admin/status");
    expect(res.status).toBe(401);
  });

  test("status authorized", async () => {
    mock.module("../src/services/status", () => ({
      getStatus: async () => ({
        status: "UP",
        uptimeSeconds: 1,
        botReachable: true,
        mongoReachable: true,
        environment: "test",
      }),
    }));

    const app = createApp();
    const res = await app.request("/api/admin/status", { headers: authHeaders });
    expect(res.status).toBe(200);
    const body = await res.json();
    expect(body.botReachable).toBe(true);
    expect(body.mongoReachable).toBe(true);

    mock.restore();
  });

  test("sessions skip", async () => {
    mock.module("../src/services/sessions", () => ({
      listSessions: async () => ({ nodes: [], sessions: [] }),
      getSessionQueue: async () => [],
      moderateSession: async (_guildId: string, action: string) => {
        expect(action).toBe("SKIP");
        return { status: "ok" };
      },
    }));

    const app = createApp();
    const res = await app.request("/api/admin/sessions/123/skip", {
      method: "POST",
      headers: authHeaders,
    });
    expect(res.status).toBe(200);
    expect((await res.json()).status).toBe("ok");

    mock.restore();
  });

  test("audit list", async () => {
    mock.module("../src/services/audit", () => ({
      listAudit: async () => ({
        items: [{ action: "SKIP", target: "123", at: "2026-01-01T00:00:00.000Z", ip: "127.0.0.1" }],
        page: 1,
        pageSize: 50,
        total: 1,
        totalPages: 1,
      }),
    }));

    const app = createApp();
    const res = await app.request("/api/admin/audit", { headers: authHeaders });
    expect(res.status).toBe(200);
    const body = await res.json();
    expect(body.total).toBe(1);
    expect(body.items[0].action).toBe("SKIP");

    mock.restore();
  });

  test("playlists requires auth", async () => {
    const app = createApp();
    const res = await app.request("/api/admin/playlists");
    expect(res.status).toBe(401);
  });

  test("playlists list", async () => {
    mock.module("../src/services/playlists", () => ({
      listPlaylists: async () => ({
        items: [],
        page: 1,
        pageSize: 50,
        total: 0,
        totalPages: 1,
      }),
      getPlaylist: async () => null,
      deletePlaylist: async () => false,
      getPlaylistStats: async () => ({
        totalPlaylists: 0,
        totalTracks: 0,
        topOwners: [],
      }),
    }));

    const app = createApp();
    const res = await app.request("/api/admin/playlists", { headers: authHeaders });
    expect(res.status).toBe(200);
    expect((await res.json()).total).toBe(0);

    mock.restore();
  });
});
