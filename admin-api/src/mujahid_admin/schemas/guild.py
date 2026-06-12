from pydantic import BaseModel, ConfigDict, Field


class Guild(BaseModel):
    id: str
    name: str
    member_count: int = Field(alias="memberCount")

    model_config = ConfigDict(populate_by_name=True)
