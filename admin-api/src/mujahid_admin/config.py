from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    admin_api_key: str = "change-me-admin-key-min-32-chars"
    admin_cors_origin: str = "http://localhost:5173"
    bot_internal_url: str = "http://localhost:9091"
    bot_metrics_url: str = "http://localhost:9090"
    internal_api_key: str = "change-me-internal-key-min-32-chars"
    mongo_uri: str = "mongodb://localhost:27017"
    mongo_db: str = "mujahid"
    environment: str = "development"


settings = Settings()
