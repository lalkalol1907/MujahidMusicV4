# MujahidMusicV4

Discord music bot: slash commands, per-guild Lavalink players, MongoDB playlists, playback buttons.

## Stack

| Component | Version / lib |
|---|---|
| Runtime | Java 24 |
| Build | Gradle (Groovy DSL), Shadow JAR |
| Discord | JDA 6 |
| Audio | Lavalink 4.2 + lavalink-client 3.4 |
| DB | MongoDB 7, sync driver 5.x |
| Config | dotenv-java |

## Architecture

```
Discord (JDA) ──► InteractionListener ──► CommandRegistry ──► Command impl
                      │                         │
                      ├─ MusicButtonHandler     ▼
                      ▼                   PlaylistRepository
               LavalinkManager
                      │
                      ▼
         GuildMusicManager / TrackScheduler
                      │
                      ▼
                 Lavalink node
```

- **`TrackScheduler`** — synchronized queue (max 500 tracks), loop modes, auto-advance announcements.
- **`MusicButtonHandler`** — Skip / Pause / Resume / Stop under playback embeds; queue page navigation.
- **Playlists** — user-scoped documents in MongoDB (`ownerId` + `name`).

## Slash commands

`play`, `playnext`, `playfile`, `join`, `leave`, `pause`, `resume`, `skip`, `stop`, `volume`, `queue`, `nowplaying`, `loop`, `shuffle`, `seek`, `remove`, `clear`, `filter`, `playlist` (save/load/list/delete)

- **`/playnext`** — inserts a track at the front of the queue (plays immediately if idle).
- **`/queue [page]`** — paginated view (10 tracks/page) with Prev/Next buttons.

## Quick start

```bash
cp .env.example .env   # set DISCORD_TOKEN
./gradlew shadowJar
java -jar build/libs/MujahidMusicV4.jar
```

**Docker (dev):** `docker compose up -d --build`

**Admin panel (full stack):** `docker compose up -d --build` — starts `bot`, `lavalink`, `mongo`, `admin-api`, `admin-web`.

| Service | URL | Notes |
|---|---|---|
| admin-web | http://localhost:3000 | React UI; login with `ADMIN_API_KEY` |
| admin-api | http://localhost:8080/docs | FastAPI BFF (dev only) |
| bot metrics | http://localhost:9090/metrics | Prometheus |
| bot control | http://localhost:9091/internal/* | `INTERNAL_API_KEY` only |

**Local dev (without Docker for admin):**

```bash
# terminal 1 — bot (needs DISCORD_TOKEN, mongo, lavalink)
./gradlew shadowJar && java -jar build/libs/MujahidMusicV4.jar

# terminal 2 — admin-api
cd admin-api && pip install -e ".[dev]"
uvicorn mujahid_admin.main:app --reload --port 8080

# terminal 3 — admin-web
cd admin-web && npm install && npm run dev
```

Set `ADMIN_API_KEY`, `INTERNAL_API_KEY`, `BOT_INTERNAL_URL`, `BOT_METRICS_URL`, `MONGO_URI` in `.env` (see `.env.example`).

## Configuration

| Variable | Required | Default |
|---|---|---|
| `DISCORD_TOKEN` | yes | — |
| `LAVALINK_HOST` | no | `localhost` |
| `LAVALINK_PORT` | no | `2333` |
| `LAVALINK_PASSWORD` | no | `youshallnotpass` |
| `DEV_GUILD_ID` | no | global command registration |
| `MONGO_URI` | no | `mongodb://localhost:27017` |
| `MONGO_DB` | no | `mujahid` |
| `HEALTH_FILE` | no | `/tmp/mujahid-health` |
| `LOG_LEVEL` | no | `INFO` (`DEBUG`, `TRACE`, …) |
| `INTERNAL_PORT` | no | `9091` (Control Plane) |
| `INTERNAL_API_KEY` | no | service-to-service key for admin-api → bot |
| `ADMIN_API_KEY` | no | admin-web → admin-api |
| `ADMIN_CORS_ORIGIN` | no | `http://localhost:5173` |

## Build options

This project uses **Gradle Groovy DSL** (`build.gradle`). Alternatives considered:

| Option | Pros | Cons |
|---|---|---|
| **Groovy `build.gradle`** (current) | Standard for Java, version catalog, Shadow plugin | Groovy syntax |
| Maven `pom.xml` | Familiar XML, wide CI support | Verbose; Shadow needs plugin config |
| Gradle Kotlin DSL | Type-safe DSL | Kotlin syntax in a Java-only project |

## CI / deploy

1. **Test** — `./gradlew test` + `pytest admin-api/tests` on push / PR
2. **Build** — push 3 images to Docker Hub on tag `v*`:
   - `{user}/{repo}` — bot
   - `{user}/{repo}-admin-api`
   - `{user}/{repo}-admin-web`
3. **Deploy** — SSH + `docker compose -f docker-compose.prod.yml pull && up -d`

Prod `.env` must include `IMAGE_NAME`, `ADMIN_API_IMAGE_NAME`, `ADMIN_WEB_IMAGE_NAME`, `IMAGE_TAG` (set by CI deploy to the git tag, e.g. `v1.2.0`).

## Tests

```bash
./gradlew test
cd admin-api && pytest
```

Bot tests — utilities, commands, Control Plane auth. Admin-api tests — auth, playlists, metrics parser.
