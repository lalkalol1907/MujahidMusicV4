import { settings } from "@/core/config";
import { createApp, shutdown } from "@/app";

const app = createApp();

const server = Bun.serve({
  port: settings.port,
  hostname: "0.0.0.0",
  fetch: app.fetch,
});

console.log(`MujahidMusic Admin API listening on :${server.port}`);

async function onShutdown() {
  await shutdown();
  server.stop();
  process.exit(0);
}

process.on("SIGINT", onShutdown);
process.on("SIGTERM", onShutdown);

export { app };
