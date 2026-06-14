import { Hono } from "hono";
import { checkRateLimit, clientIp, verifyAdmin } from "@/core/auth";
import { BotNotFoundError, HttpError } from "@/core/errors";
import * as auditService from "@/services/audit";
import * as guildsService from "@/services/guilds";
import * as metricsService from "@/services/metrics";
import * as playlistsService from "@/services/playlists";
import * as sessionsService from "@/services/sessions";
import * as statusService from "@/services/status";

function parsePage(value: string | undefined, fallback = 1): number {
  const page = Number(value ?? fallback);
  if (!Number.isFinite(page) || page < 1) {
    throw new HttpError(400, "Invalid page");
  }
  return page;
}

function parsePageSize(value: string | undefined, fallback = 50): number {
  const size = Number(value ?? fallback);
  if (!Number.isFinite(size) || size < 1 || size > 100) {
    throw new HttpError(400, "Invalid pageSize");
  }
  return size;
}

export function createAdminRoutes() {
  const admin = new Hono();

  admin.use("*", verifyAdmin);

  admin.get("/status", async (c) => c.json(await statusService.getStatus()));

  admin.get("/metrics/summary", async (c) => c.json(await metricsService.getMetricsSummary()));

  admin.get("/sessions", async (c) => c.json(await sessionsService.listSessions()));

  admin.get("/sessions/:guildId", async (c) => {
    try {
      return c.json(await sessionsService.getSessionQueue(c.req.param("guildId")));
    } catch (error) {
      if (error instanceof BotNotFoundError) {
        throw new HttpError(404, "No active session");
      }
      throw error;
    }
  });

  admin.post("/sessions/:guildId/skip", checkRateLimit, async (c) => {
    const guildId = c.req.param("guildId")!;
    return c.json(
      await sessionsService.moderateSession(
        guildId,
        "SKIP",
        `/internal/sessions/${guildId}/skip`,
        clientIp(c),
      ),
    );
  });

  admin.post("/sessions/:guildId/stop", checkRateLimit, async (c) => {
    const guildId = c.req.param("guildId")!;
    return c.json(
      await sessionsService.moderateSession(
        guildId,
        "STOP",
        `/internal/sessions/${guildId}/stop`,
        clientIp(c),
      ),
    );
  });

  admin.post("/sessions/:guildId/leave", checkRateLimit, async (c) => {
    const guildId = c.req.param("guildId")!;
    return c.json(
      await sessionsService.moderateSession(
        guildId,
        "LEAVE",
        `/internal/sessions/${guildId}/leave`,
        clientIp(c),
      ),
    );
  });

  admin.get("/guilds", async (c) => c.json(await guildsService.listGuilds()));

  admin.get("/playlists/stats", async (c) => c.json(await playlistsService.getPlaylistStats()));

  admin.get("/playlists", async (c) => {
    const page = parsePage(c.req.query("page"));
    const pageSize = parsePageSize(c.req.query("pageSize"));
    return c.json(
      await playlistsService.listPlaylists(
        page,
        pageSize,
        c.req.query("ownerId") ?? null,
        c.req.query("name") ?? null,
      ),
    );
  });

  admin.get("/playlists/:ownerId/:name", async (c) => {
    const doc = await playlistsService.getPlaylist(c.req.param("ownerId"), c.req.param("name"));
    if (!doc) {
      throw new HttpError(404, "Playlist not found");
    }
    return c.json(doc);
  });

  admin.delete("/playlists/:ownerId/:name", checkRateLimit, async (c) => {
    const deleted = await playlistsService.deletePlaylist(
      c.req.param("ownerId")!,
      c.req.param("name")!,
      clientIp(c),
    );
    if (!deleted) {
      throw new HttpError(404, "Playlist not found");
    }
    return c.body(null, 204);
  });

  admin.get("/audit", async (c) => {
    const page = parsePage(c.req.query("page"));
    const pageSize = parsePageSize(c.req.query("pageSize"));
    return c.json(await auditService.listAudit(page, pageSize));
  });

  return admin;
}
