package com.lalkalol.mujahid.config;

import io.github.cdimascio.dotenv.Dotenv;

public record Config(
        String discordToken,
        String lavalinkHost,
        int lavalinkPort,
        String lavalinkPassword,
        Long devGuildId,
        String mongoUri,
        String mongoDatabase,
        String healthFile
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
        Long devGuildId = devGuildIdRaw != null ? Long.parseLong(devGuildIdRaw) : null;

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
                valueOrDefault(dotenv, "HEALTH_FILE", "/tmp/mujahid-health")
        );
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
