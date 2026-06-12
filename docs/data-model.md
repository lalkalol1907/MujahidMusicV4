# Data Model

MongoDB database name: `mujahid` (configurable via `MONGO_DB`).

## Collection: `playlists`

Used by Java bot (`PlaylistRepository`) and admin-api (`repositories/playlists.py`).

| Field | Type | Description |
|---|---|---|
| `ownerId` | long (int64) | Discord user ID who owns the playlist |
| `name` | string | Playlist name (unique per owner) |
| `tracks` | array | List of saved tracks |

### Track object

| Field | Type | Description |
|---|---|---|
| `encoded` | string | Lavalink track identifier |
| `title` | string | Display title |
| `uri` | string | Source URI |

### Indexes

- Unique compound index on `(ownerId, name)` — created by bot on startup

## Collection: `admin_audit`

Written only by admin-api for moderation and admin actions.

| Field | Type | Description |
|---|---|---|
| `action` | string | e.g. `SKIP`, `STOP`, `LEAVE`, `DELETE_PLAYLIST` |
| `target` | string | Guild ID or `ownerId/playlistName` |
| `at` | datetime (UTC) | Timestamp |
| `ip` | string \| null | Client IP of admin request |

No indexes required for current query patterns (paginated sort by `at` desc).
