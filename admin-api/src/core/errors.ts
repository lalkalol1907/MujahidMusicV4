import type { ContentfulStatusCode } from "hono/utils/http-status";

export class BotError extends Error {
  constructor(message: string) {
    super(message);
    this.name = "BotError";
  }
}

export class BotNotFoundError extends BotError {
  constructor(message: string) {
    super(message);
    this.name = "BotNotFoundError";
  }
}

export class HttpError extends Error {
  status: ContentfulStatusCode;

  constructor(status: ContentfulStatusCode, message: string) {
    super(message);
    this.name = "HttpError";
    this.status = status;
  }
}
