package com.lalkalol.mujahid.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.LoggerFactory;

public record Config(
        String discordToken,
        String lavalinkHost,
        int lavalinkPort,
        String lavalinkPassword,
        Long devGuildId,
        String mongoUri,
        String mongoDatabase,
        String healthFile,
        String logLevel
) {
    public String lavalinkUri() {
        return "ws://" + lavalinkHost + ":" + lavalinkPort;
    }

    public static Config load() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        String token = value(dotenv, "DISCORD_TOKEN");
        if (token == null) {
            throw new IllegalStateException(
                    "DISCORD_TOKEN is not set. Copy .env.example to .env and fill it in.");
        }

        String devGuildIdRaw = value(dotenv, "DEV_GUILD_ID");
        Long devGuildId = null;
        if (devGuildIdRaw != null) {
            try {
                devGuildId = Long.parseLong(devGuildIdRaw);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("DEV_GUILD_ID must be a numeric snowflake ID");
            }
        }

        String lavalinkPortRaw = value(dotenv, "LAVALINK_PORT");
        int lavalinkPort = lavalinkPortRaw != null ? Integer.parseInt(lavalinkPortRaw) : 2333;

        return new Config(
                token,
                valueOrDefault(dotenv, "LAVALINK_HOST", "localhost"),
                lavalinkPort,
                valueOrDefault(dotenv, "LAVALINK_PASSWORD", "youshallnotpass"),
                devGuildId,
                valueOrDefault(dotenv, "MONGO_URI", "mongodb://localhost:27017"),
                valueOrDefault(dotenv, "MONGO_DB", "mujahid"),
                valueOrDefault(dotenv, "HEALTH_FILE", "/tmp/mujahid-health"),
                valueOrDefault(dotenv, "LOG_LEVEL", "INFO")
        );
    }

    public static void applyLogLevel(String logLevel) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Level level = Level.toLevel(logLevel, Level.INFO);
        context.getLogger("com.lalkalol").setLevel(level);
        context.getLogger("dev.arbjerg.lavalink").setLevel(level);
        context.getLogger("net.dv8tion").setLevel(level);
    }

    private static String value(Dotenv dotenv, String key) {
        String fromDotenv = dotenv.get(key);
        if (fromDotenv != null && !fromDotenv.isBlank()) {
            return fromDotenv;
        }
        String fromEnv = System.getenv(key);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return null;
    }

    private static String valueOrDefault(Dotenv dotenv, String key, String defaultValue) {
        String value = value(dotenv, key);
        return value != null ? value : defaultValue;
    }
}
