# MujahidMusicV4

Discord music bot: slash commands, per-guild Lavalink players, MongoDB playlists.

## Stack

| Component | Version / lib |
|---|---|
| Runtime | Java 24 |
| Build | Gradle (Kotlin DSL), Shadow JAR |
| Discord | JDA 6 |
| Audio | Lavalink 4.2 + lavalink-client 3.4 |
| DB | MongoDB 7, sync driver 5.x |
| Config | dotenv-java |

## Architecture

```
Discord (JDA) ──► InteractionListener ──► CommandRegistry ──► Command impl
                      │                         │
                      ▼                         ▼
               LavalinkManager            PlaylistRepository
                      │                         │
                      ▼                         ▼
         GuildMusicManager / TrackScheduler   MongoDB
                      │
                      ▼
                 Lavalink node
```

- **One `GuildMusicManager` per guild** — queue, loop mode, filters, volume.
- **`TrackScheduler`** — thread-safe queue; handles track end / skip / loop.
- **Playlists** — user-scoped documents in MongoDB (`ownerId` + `name`).
- **Health** — writes timestamp to `HEALTH_FILE` while JDA is connected (Docker `HEALTHCHECK`).

## Slash commands

`play`, `playfile`, `join`, `leave`, `pause`, `resume`, `skip`, `stop`, `volume`, `queue`, `nowplaying`, `loop`, `shuffle`, `seek`, `remove`, `clear`, `filter`, `playlist` (save/load/list/delete)

## Quick start

```bash
cp .env.example .env   # set DISCORD_TOKEN
./gradlew shadowJar
java -jar build/libs/MujahidMusicV4.jar
```

**Docker (dev, builds locally):**

```bash
docker compose up -d --build
```

**Prod** — pre-built image via `docker-compose.prod.yml`; see CI below.

## Configuration

Bot reads `.env` and process env (env wins for missing `.env` keys).

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

`MONGO_USER` / `MONGO_PASSWORD` are used by Compose to wire Mongo and build `MONGO_URI` for the bot container.

## CI / deploy

GitHub Actions (`.github/workflows/ci.yml`):

1. **Test** — `./gradlew test` on push / PR
2. **Build** — Docker image push on tag `v*`
3. **Deploy** — SSH + `docker compose -f docker-compose.prod.yml up` (secrets: `DEPLOY_*`, `DOCKERHUB_*`)

## Tests

```bash
./gradlew test
```

JUnit 5 + Mockito — utilities, enums, `CommandContext`, `CommandRegistry`, command impl logic.

## Layout

```
src/main/java/com/lalkalol/mujahid/
  audio/          Lavalink integration, queue scheduler
  commands/       Command interface, registry, impl/*
  config/         Config loader
  db/             MongoDB playlists
  health/         HealthMonitor
  listeners/      JDA event handlers
  util/           Embeds, formatting, identifiers
lavalink/         Lavalink server config
```
