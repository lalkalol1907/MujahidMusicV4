from pydantic import BaseModel


class CommandMetric(BaseModel):
    command: str
    count: int | None = None
    errors: int | None = None


class LabeledCount(BaseModel):
    reason: str | None = None
    op: str | None = None
    count: int


class MetricsSummary(BaseModel):
    guilds_total: int
    active_players: int
    commands: list[CommandMetric]
    track_load_failures: list[LabeledCount]
    playlist_ops: list[LabeledCount]
