import type { Context, Next } from "hono";
import { settings } from "@/core/config";
import { HttpError } from "@/core/errors";

const rateLimits = new Map<string, number[]>();
const WINDOW_MS = 60_000;
const MAX_REQUESTS = 30;

export function resetRateLimits(): void {
  rateLimits.clear();
}

export async function verifyAdmin(c: Context, next: Next): Promise<Response | void> {
  const header = c.req.header("Authorization");
  if (!header?.startsWith("Bearer ")) {
    throw new HttpError(401, "Missing or invalid Authorization header");
  }
  const token = header.slice("Bearer ".length);
  if (token !== settings.adminApiKey) {
    throw new HttpError(401, "Invalid API key");
  }
  await next();
}

export async function checkRateLimit(c: Context, next: Next): Promise<Response | void> {
  const ip = clientIp(c);
  const now = Date.now();
  const timestamps = (rateLimits.get(ip) ?? []).filter((t) => now - t < WINDOW_MS);
  if (timestamps.length >= MAX_REQUESTS) {
    throw new HttpError(429, "Rate limit exceeded");
  }
  timestamps.push(now);
  rateLimits.set(ip, timestamps);
  await next();
}

export function clientIp(c: Context): string {
  return c.req.header("x-forwarded-for")?.split(",")[0]?.trim() ?? "unknown";
}
