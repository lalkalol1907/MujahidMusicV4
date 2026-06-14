import type { Context, Next } from "hono";
import { settings } from "./config";
import { HttpError } from "./errors";

const RATE_LIMIT = 10;
const RATE_WINDOW_MS = 60_000;
const rateBuckets = new Map<string, number[]>();

export async function verifyAdmin(c: Context, next: Next) {
  const auth = c.req.header("Authorization");
  if (!auth || auth !== `Bearer ${settings.adminApiKey}`) {
    throw new HttpError(401, "Unauthorized");
  }
  await next();
}

export function clientIp(c: Context): string | null {
  return c.req.header("x-forwarded-for")?.split(",")[0]?.trim() ?? null;
}

export async function checkRateLimit(c: Context, next: Next) {
  const ip = clientIp(c) ?? "unknown";
  const now = Date.now();
  const bucket = (rateBuckets.get(ip) ?? []).filter((stamp) => now - stamp < RATE_WINDOW_MS);
  if (bucket.length >= RATE_LIMIT) {
    throw new HttpError(429, "Rate limit exceeded");
  }
  bucket.push(now);
  rateBuckets.set(ip, bucket);
  await next();
}

export function resetRateLimits() {
  rateBuckets.clear();
}
