from pydantic import BaseModel, ConfigDict, Field


class PlaylistTrack(BaseModel):
    encoded: str
    title: str
    uri: str


class PlaylistSummary(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    owner_id: str = Field(alias="ownerId")
    name: str
    track_count: int = Field(alias="trackCount")


class PlaylistDetail(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    owner_id: str = Field(alias="ownerId")
    name: str
    tracks: list[PlaylistTrack]


class TopOwner(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    owner_id: str = Field(alias="ownerId")
    playlist_count: int = Field(alias="playlistCount")
    track_count: int = Field(alias="trackCount")


class PlaylistStats(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    total_playlists: int = Field(alias="totalPlaylists")
    total_tracks: int = Field(alias="totalTracks")
    top_owners: list[TopOwner] = Field(alias="topOwners")


class PaginatedPlaylists(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    items: list[PlaylistSummary]
    page: int
    page_size: int = Field(alias="pageSize")
    total: int
    total_pages: int = Field(alias="totalPages")
