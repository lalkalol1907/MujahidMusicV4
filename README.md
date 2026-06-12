# MujahidMusicV4

Discord music bot with a web admin panel: slash commands, per-guild Lavalink players, MongoDB playlists, playback buttons, and live session monitoring.

## Stack

| Service | Runtime | Role |
|---|---|---|
| **bot** | Java 24, JDA 6, Gradle | Discord gateway, Lavalink client, Control Plane |
| **lavalink** | Lavalink 4.2 | Audio node (YouTube, SoundCloud, HTTP) |
| **mongo** | MongoDB 7 | Playlists, admin audit log |
| **admin-api** | Python 3.12, FastAPI | BFF: auth, proxy to bot, Mongo CRUD |
| **admin-web** | React 19, Vite, TypeScript | Admin UI |

## Architecture

```
Discord ──► bot (JDA) ──► LavalinkManager ──► Lavalink
                │                │
                │                └── GuildMusicManager / TrackScheduler
                │
                ├── :9090  /health, /metrics        (Prometheus)
                └── :9091  /internal/*              (Control Plane, INTERNAL_API_KEY)

admin-web ──► admin-api ──► bot :9091  (sessions, guilds, moderation)
                 │    └──► bot :9090  (metrics scrape)
                 └──► mongo         (playlists, audit)
```

**Bot internals:** `InteractionListener` → `CommandRegistry` → command impls; `PlaylistRepository` for `/playlist`; `MusicButtonHandler` for embed controls.

**Admin pages:** Dashboard, Live Sessions, Playlists, Guilds, Audit, Settings.

## Slash commands

`play`, `playnext`, `playfile`, `join`, `leave`, `pause`, `resume`, `skip`, `stop`, `volume`, `queue`, `nowplaying`, `loop`, `shuffle`, `seek`, `remove`, `clear`, `filter`, `playlist` (save / load / list / delete)

- **`/playnext`** — insert at front of queue (plays immediately if idle)
- **`/queue [page]`** — paginated view (10 tracks/page) with Prev/Next buttons

## Project layout

```
MujahidMusicV4/
├── src/                    # Java bot
├── admin-api/              # Python FastAPI BFF
├── admin-web/              # React admin UI
├── lavalink/               # Lavalink config
├── docs/openapi/           # API contracts
├── docker-compose.yml      # local dev (build from source)
└── docker-compose.prod.yml # prod (pre-built images from Docker Hub)
```

## Quick start

### Bot only (local)

```bash
cp .env.example .env   # set DISCORD_TOKEN, start mongo + lavalink separately
./gradlew shadowJar
java -jar build/libs/MujahidMusicV4.jar
```

### Full stack (Docker dev)

```bash
cp .env.example .env   # DISCORD_TOKEN, ADMIN_API_KEY, INTERNAL_API_KEY, …
docker compose up -d --build
```

| Service | URL | Notes |
|---|---|---|
| admin-web | http://localhost:3000 | Login with `ADMIN_API_KEY` |
| admin-api | http://localhost:8080/docs | Swagger (disabled when `ENVIRONMENT=production`) |
| bot metrics | http://localhost:9090/metrics | Prometheus |
| bot control | http://localhost:9091/internal/* | `INTERNAL_API_KEY` only |

### Admin local dev (3 terminals)

```bash
# 1 — bot
./gradlew shadowJar && java -jar build/libs/MujahidMusicV4.jar

# 2 — admin-api
cd admin-api && pip install -e ".[dev]"
uvicorn mujahid_admin.main:app --reload --port 8080

# 3 — admin-web (proxies /api → :8080)
cd admin-web && npm install && npm run dev
```

Open http://localhost:5173. Copy vars from `.env.example` into `.env` at repo root.

## Configuration

### Bot

| Variable | Required | Default |
|---|---|---|
| `DISCORD_TOKEN` | yes | — |
| `LAVALINK_HOST` | no | `localhost` |
| `LAVALINK_PORT` | no | `2333` |
| `LAVALINK_PASSWORD` | no | `youshallnotpass` |
| `DEV_GUILD_ID` | no | global command registration |
| `MONGO_URI` | no | `mongodb://localhost:27017` |
| `MONGO_DB` | no | `mujahid` |
| `METRICS_PORT` | no | `9090` |
| `INTERNAL_PORT` | no | `9091` |
| `INTERNAL_API_KEY` | yes (with admin) | — |
| `HEALTH_FILE` | no | `/tmp/mujahid-health` |
| `LOG_LEVEL` | no | `INFO` |

### Admin

| Variable | Required | Default |
|---|---|---|
| `ADMIN_API_KEY` | yes | — (UI login) |
| `INTERNAL_API_KEY` | yes | — (admin-api → bot; must differ from `ADMIN_API_KEY`) |
| `ADMIN_CORS_ORIGIN` | no | `http://localhost:5173` |
| `ADMIN_WEB_PORT` | no | `3000` (Docker) |
| `BOT_INTERNAL_URL` | no | set by compose (`http://bot:9091`) |
| `BOT_METRICS_URL` | no | set by compose (`http://bot:9090`) |
| `ENVIRONMENT` | no | `development` / `production` |

### Production images (Docker Hub)

| Variable | Example |
|---|---|
| `IMAGE_NAME` | `user/mujahidmusicv4` |
| `ADMIN_API_IMAGE_NAME` | `user/mujahidmusicv4-admin-api` |
| `ADMIN_WEB_IMAGE_NAME` | `user/mujahidmusicv4-admin-web` |
| `IMAGE_TAG` | `v1.0.0` (set by CI deploy job) |

See [`.env.example`](.env.example) for the full template.

## CI / deploy

GitHub Actions (`.github/workflows/ci.yml`):

| Stage | Trigger | What runs |
|---|---|---|
| **Test** | push, PR | `./gradlew test`, `pytest admin-api/tests` |
| **Build** | tag `v*` | Build & push 3 images to Docker Hub |
| **Deploy** | tag `v*` | SSH → write `.env` → `compose pull` → `up -d` |
| **Release** | tag `v*` | GitHub Release with auto-generated notes + Docker image list |

Docker Hub image names (repo = `MujahidMusicV4`, user = `johndoe`):

| Image | Tag |
|---|---|
| `johndoe/MujahidMusicV4` | `v1.0.0`, `latest` |
| `johndoe/MujahidMusicV4-admin-api` | same |
| `johndoe/MujahidMusicV4-admin-web` | same |

**GitHub secrets:** `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`, `DEPLOY_HOST`, `DEPLOY_USER`, `DEPLOY_SSH_KEY`, `DEPLOY_PATH`, `DEPLOY_DOTENV`.

**`DEPLOY_DOTENV` must include** (in addition to bot secrets):

```bash
IMAGE_NAME=your-dockerhub-username/mujahidmusicv4
ADMIN_API_IMAGE_NAME=your-dockerhub-username/mujahidmusicv4-admin-api
ADMIN_WEB_IMAGE_NAME=your-dockerhub-username/mujahidmusicv4-admin-web

INTERNAL_API_KEY=...
ADMIN_API_KEY=...
ADMIN_CORS_ORIGIN=http://your-server:3000
```

`IMAGE_TAG` is injected by the deploy job from the git tag — do not hardcode it in the secret.

**Server needs only:** `docker-compose.prod.yml`, `lavalink/application.yml`, `.env`. No source checkout required for admin services.

Release (build, deploy, and [GitHub Release](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository) are created automatically):

```bash
git tag v1.0.0
git push origin v1.0.0
```

## Tests

```bash
./gradlew test
pip install -e "./admin-api[dev]" && pytest admin-api/tests -q
cd admin-web && npm run build
```

## API docs

- Bot Control Plane: [`docs/openapi/bot-control.yaml`](docs/openapi/bot-control.yaml)
- Admin API: [`docs/openapi/admin-api.yaml`](docs/openapi/admin-api.yaml) (also `/docs` in dev)
