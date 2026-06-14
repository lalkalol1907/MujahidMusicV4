import { settings } from "@/core/config";
import { BotError, BotNotFoundError } from "@/core/errors";

class BotClient {
  private headers(): HeadersInit {
    return {
      Authorization: `Bearer ${settings.internalApiKey}`,
      "Content-Type": "application/json",
    };
  }

  private async request<T>(url: string, init?: RequestInit): Promise<T> {
    const response = await fetch(url, {
      ...init,
      headers: { ...this.headers(), ...init?.headers },
    });
    if (response.status === 404) {
      throw new BotNotFoundError(await response.text());
    }
    if (!response.ok) {
      throw new BotError(`Bot request failed: ${response.status} ${await response.text()}`);
    }
    if (response.status === 204) {
      return undefined as T;
    }
    return (await response.json()) as T;
  }

  async healthCheck(): Promise<boolean> {
    try {
      const response = await fetch(`${settings.botInternalUrl}/internal/health`, {
        headers: this.headers(),
      });
      return response.ok;
    } catch {
      return false;
    }
  }

  async fetchMetricsText(): Promise<string> {
    const response = await fetch(`${settings.botMetricsUrl}/metrics`);
    if (!response.ok) {
      throw new BotError(`Metrics scrape failed: ${response.status}`);
    }
    return response.text();
  }

  get<T>(path: string): Promise<T> {
    return this.request<T>(`${settings.botInternalUrl}${path}`);
  }

  post<T>(path: string): Promise<T> {
    return this.request<T>(`${settings.botInternalUrl}${path}`, { method: "POST" });
  }
}

export const botClient = new BotClient();
