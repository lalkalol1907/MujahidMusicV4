import { Hono } from "hono";
import { cors } from "hono/cors";
import { settings } from "@/core/config";
import { HttpError } from "@/core/errors";
import { closeMongo } from "@/db/connection";
import { createAdminRoutes } from "@/routes/admin";

export function createApp() {
  const app = new Hono();

  app.use(
    "*",
    cors({
      origin: settings.adminCorsOrigin,
      allowMethods: ["GET", "POST", "DELETE", "OPTIONS"],
      allowHeaders: ["Authorization", "Content-Type"],
      credentials: true,
    }),
  );

  app.onError((error, c) => {
    if (error instanceof HttpError) {
      return c.json({ detail: error.message }, error.status);
    }
    console.error(error);
    return c.json({ detail: "Internal Server Error" }, 500);
  });

  app.route("/api/admin", createAdminRoutes());

  if (settings.environment !== "production") {
    app.get("/docs", (c) =>
      c.json({
        title: "MujahidMusic Admin API",
        version: "1.0.0",
        openapi: "/docs/openapi/admin-api.yaml",
      }),
    );
  }

  return app;
}

export async function shutdown() {
  await closeMongo();
}
