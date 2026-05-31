package com.lalkalol.mujahid.config

import io.github.cdimascio.dotenv.dotenv

data class Config(
    val discordToken: String,
    val lavalinkHost: String,
    val lavalinkPort: Int,
    val lavalinkPassword: String,
    val devGuildId: Long?,
    val mongoUri: String,
    val mongoDatabase: String,
    val healthFile: String,
) {
    val lavalinkUri: String
        get() = "ws://$lavalinkHost:$lavalinkPort"

    companion object {
        fun load(): Config {
            val dotenv = dotenv {
                ignoreIfMissing = true
            }

            fun value(key: String): String? =
                dotenv[key]?.takeIf { it.isNotBlank() } ?: System.getenv(key)?.takeIf { it.isNotBlank() }

            val token = value("DISCORD_TOKEN")
                ?: error("DISCORD_TOKEN is not set. Copy .env.example to .env and fill it in.")

            return Config(
                discordToken = token,
                lavalinkHost = value("LAVALINK_HOST") ?: "localhost",
                lavalinkPort = value("LAVALINK_PORT")?.toInt() ?: 2333,
                lavalinkPassword = value("LAVALINK_PASSWORD") ?: "youshallnotpass",
                devGuildId = value("DEV_GUILD_ID")?.toLongOrNull(),
                mongoUri = value("MONGO_URI") ?: "mongodb://localhost:27017",
                mongoDatabase = value("MONGO_DB") ?: "mujahid",
                healthFile = value("HEALTH_FILE") ?: "/tmp/mujahid-health",
            )
        }
    }
}
