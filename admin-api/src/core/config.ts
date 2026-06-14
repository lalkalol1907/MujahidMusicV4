function env(key: string, fallback?: string): string {
  const value = process.env[key];
  if (value !== undefined && value !== "") {
    return value;
  }
  if (fallback !== undefined) {
    return fallback;
  }
  throw new Error(`${key} is not set`);
}

export const settings = {
  adminApiKey: env("ADMIN_API_KEY", "change-me-admin-key-min-32-chars"),
  adminCorsOrigin: env("ADMIN_CORS_ORIGIN", "http://localhost:5173"),
  botInternalUrl: env("BOT_INTERNAL_URL", "http://localhost:9091").replace(/\/$/, ""),
  botMetricsUrl: env("BOT_METRICS_URL", "http://localhost:9090").replace(/\/$/, ""),
  internalApiKey: env("INTERNAL_API_KEY", "change-me-internal-key-min-32-chars"),
  mongoUri: env("MONGO_URI", "mongodb://localhost:27017"),
  mongoDb: env("MONGO_DB", "mujahid"),
  environment: env("ENVIRONMENT", "development"),
  port: Number(env("PORT", "8080")),
};

export type Settings = typeof settings;
