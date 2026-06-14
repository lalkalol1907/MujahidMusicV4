package com.lalkalol.mujahid.config

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import io.github.cdimascio.dotenv.Dotenv
import org.slf4j.LoggerFactory

data class Config(
    val discordToken: String,
    val lavalinkHost: String,
    val lavalinkPort: Int,
    val lavalinkPassword: String,
    val devGuildId: Long?,
    val mongoUri: String,
    val mongoDatabase: String,
    val healthFile: String,
    val logLevel: String,
    val metricsPort: Int,
    val internalPort: Int,
    val internalApiKey: String,
) {
    fun lavalinkUri(): String = "ws://$lavalinkHost:$lavalinkPort"

    companion object {
        fun load(): Config {
            val dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load()

            val token = value(dotenv, "DISCORD_TOKEN")
                ?: throw IllegalStateException(
                    "DISCORD_TOKEN is not set. Copy .env.example to .env and fill it in.",
                )

            val devGuildIdRaw = value(dotenv, "DEV_GUILD_ID")
            val devGuildId = devGuildIdRaw?.let {
                try {
                    it.toLong()
                } catch (_: NumberFormatException) {
                    throw IllegalStateException("DEV_GUILD_ID must be a numeric snowflake ID")
                }
            }

            val lavalinkPort = value(dotenv, "LAVALINK_PORT")?.toInt() ?: 2333
            val metricsPort = value(dotenv, "METRICS_PORT")?.toInt() ?: 9090
            val internalPort = value(dotenv, "INTERNAL_PORT")?.toInt() ?: 9091
            val internalApiKey = valueOrDefault(dotenv, "INTERNAL_API_KEY", "change-me-internal-key-min-32-chars")

            return Config(
                discordToken = token,
                lavalinkHost = valueOrDefault(dotenv, "LAVALINK_HOST", "localhost"),
                lavalinkPort = lavalinkPort,
                lavalinkPassword = valueOrDefault(dotenv, "LAVALINK_PASSWORD", "youshallnotpass"),
                devGuildId = devGuildId,
                mongoUri = valueOrDefault(dotenv, "MONGO_URI", "mongodb://localhost:27017"),
                mongoDatabase = valueOrDefault(dotenv, "MONGO_DB", "mujahid"),
                healthFile = valueOrDefault(dotenv, "HEALTH_FILE", "/tmp/mujahid-health"),
                logLevel = valueOrDefault(dotenv, "LOG_LEVEL", "INFO"),
                metricsPort = metricsPort,
                internalPort = internalPort,
                internalApiKey = internalApiKey,
            )
        }

        fun applyLogLevel(logLevel: String) {
            val context = LoggerFactory.getILoggerFactory() as LoggerContext
            val level = Level.toLevel(logLevel, Level.INFO)
            context.getLogger("com.lalkalol").level = level
            context.getLogger("dev.arbjerg.lavalink").level = level
            context.getLogger("net.dv8tion").level = level
        }

        private fun value(dotenv: Dotenv, key: String): String? {
            val fromDotenv = dotenv[key]
            if (!fromDotenv.isNullOrBlank()) {
                return fromDotenv
            }
            val fromEnv = System.getenv(key)
            if (!fromEnv.isNullOrBlank()) {
                return fromEnv
            }
            return null
        }

        private fun valueOrDefault(dotenv: Dotenv, key: String, defaultValue: String): String =
            value(dotenv, key) ?: defaultValue
    }
}
