# Architecture

MujahidMusicV4 is a multi-service Discord music bot with a web admin panel.

## Services

```
Discord ──► bot (JDA) ──► LavalinkManager ──► Lavalink
                │
                ├── :9090  /health, /metrics        (Prometheus)
                └── :9091  /internal/*              (Control Plane, INTERNAL_API_KEY)

admin-web ──► admin-api ──► bot :9091  (sessions, guilds, moderation)
                 │    └──► bot :9090  (metrics scrape)
                 └──► mongo         (playlists, audit)
```

| Service | Runtime | Role |
|---|---|---|
| **bot** | Kotlin, Java 24, JDA 6 | Discord gateway, Lavalink client, Control Plane |
| **lavalink** | Lavalink 4.2 | Audio node |
| **mongo** | MongoDB 7 | Playlists, admin audit log |
| **admin-api** | Bun, Hono, TypeScript | BFF: auth, proxy to bot, Mongo CRUD |
| **admin-web** | Node 24 LTS, React 19, Vite | Admin UI (FSD) |

## admin-api layers

```
routes  →  services  →  repositories / clients
              ↓
           types (TypeScript)
```

- **Routes** — Hono handlers, auth, rate limiting
- **Services** — orchestration (e.g. session moderation + audit)
- **Repositories** — MongoDB I/O
- **Clients** — bot HTTP client (fetch)
- **Types** — request/response shapes (camelCase JSON)

## admin-web (FSD)

```
app → pages → widgets → features → entities → shared
```

- **app** — providers, routes, global styles
- **pages** — thin route wrappers
- **widgets** — composed UI blocks (tables, dashboard)
- **features** — user actions (auth, session control, playlist delete)
- **entities** — domain data, API, TanStack Query hooks
- **shared** — API client, utilities

## Auth flow

1. User enters `ADMIN_API_KEY` in admin-web login
2. Key stored in `localStorage`, sent as `Authorization: Bearer`
3. admin-api validates against `ADMIN_API_KEY` env var
4. admin-api calls bot with `INTERNAL_API_KEY` (must differ from admin key)

## Deployment

Tag push (`v*`) triggers CI: test → build 3 Docker images → SSH deploy → GitHub Release.

See [README](../README.md) for env vars and secrets.
