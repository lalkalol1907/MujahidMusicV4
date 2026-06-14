import { settings } from "../core/config";
import { BotError, BotNotFoundError } from "../core/errors";

class BotClient {
  private headers: Record<string, string>;

  constructor() {
    this.headers = { Authorization: `Bearer ${settings.internalApiKey}` };
  }

  async get<T>(path: string): Promise<T> {
    const response = await fetch(`${settings.botInternalUrl}${path}`, {
      headers: this.headers,
      signal: AbortSignal.timeout(10_000),
    });
    if (response.status === 404) {
      throw new BotNotFoundError(await response.text());
    }
    if (!response.ok) {
      throw new BotError(await response.text());
    }
    return response.json() as Promise<T>;
  }

  async post<T>(path: string): Promise<T> {
    const response = await fetch(`${settings.botInternalUrl}${path}`, {
      method: "POST",
      headers: this.headers,
      signal: AbortSignal.timeout(10_000),
    });
    if (response.status === 404) {
      throw new BotNotFoundError(await response.text());
    }
    if (!response.ok) {
      throw new BotError(await response.text());
    }
    const text = await response.text();
    if (!text) {
      return { status: "ok" } as T;
    }
    return JSON.parse(text) as T;
  }

  async fetchMetricsText(): Promise<string> {
    const response = await fetch(`${settings.botMetricsUrl}/metrics`, {
      signal: AbortSignal.timeout(10_000),
    });
    if (!response.ok) {
      throw new BotError(await response.text());
    }
    return response.text();
  }

  async healthCheck(): Promise<boolean> {
    try {
      const response = await fetch(`${settings.botInternalUrl}/internal/health`, {
        signal: AbortSignal.timeout(5_000),
      });
      return response.status === 200;
    } catch {
      return false;
    }
  }
}

export const botClient = new BotClient();
