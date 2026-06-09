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

## Build options

This project uses **Gradle Groovy DSL** (`build.gradle`). Alternatives considered:

| Option | Pros | Cons |
|---|---|---|
| **Groovy `build.gradle`** (current) | Standard for Java, version catalog, Shadow plugin | Groovy syntax |
| Maven `pom.xml` | Familiar XML, wide CI support | Verbose; Shadow needs plugin config |
| Gradle Kotlin DSL | Type-safe DSL | Kotlin syntax in a Java-only project |

## CI / deploy

1. **Test** — `./gradlew test` on push / PR
2. **Build** — Docker push on tag `v*`
3. **Deploy** — SSH + `docker-compose.prod.yml`

## Tests

```bash
./gradlew test
```

79 tests — utilities, command logic, `CommandContext`, `CommandRegistry`.
