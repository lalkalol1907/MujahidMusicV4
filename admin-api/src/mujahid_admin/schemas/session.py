from pydantic import BaseModel, ConfigDict, Field


class SessionNode(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    name: str
    connected: bool
    players: int
    playing: int


class Session(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    guild_id: str = Field(alias="guildId")
    guild_name: str = Field(alias="guildName")
    track_title: str | None = Field(default=None, alias="trackTitle")
    track_author: str | None = Field(default=None, alias="trackAuthor")
    position_ms: int = Field(alias="positionMs")
    length_ms: int = Field(alias="lengthMs")
    paused: bool
    volume: int
    queue_size: int = Field(alias="queueSize")
    loop_mode: str = Field(alias="loopMode")


class SessionsResponse(BaseModel):
    nodes: list[SessionNode]
    sessions: list[Session]


class QueueTrack(BaseModel):
    position: int
    title: str
    author: str
    uri: str
    duration_ms: int = Field(alias="durationMs")

    model_config = ConfigDict(populate_by_name=True)


class ActionResponse(BaseModel):
    status: str = "ok"
