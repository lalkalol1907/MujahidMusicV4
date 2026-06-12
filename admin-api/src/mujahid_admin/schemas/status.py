from pydantic import BaseModel, ConfigDict, Field


class StatusResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    status: str
    uptime_seconds: int = Field(alias="uptimeSeconds")
    bot_reachable: bool = Field(alias="botReachable")
    mongo_reachable: bool = Field(alias="mongoReachable")
    environment: str
