package com.lalkalol.mujahid.internal;

import com.lalkalol.mujahid.audio.GuildMusicManager;
import com.lalkalol.mujahid.audio.LavalinkManager;
import com.lalkalol.mujahid.internal.dto.GuildDto;
import com.lalkalol.mujahid.internal.dto.QueueTrackDto;
import com.lalkalol.mujahid.internal.dto.SessionsResponse;
import com.lalkalol.mujahid.metrics.MetricsHolder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Internal Control Plane HTTP server (port 9091).
 * Service-to-service only — protected by INTERNAL_API_KEY.
 */
public class ControlHttpServer {

    private static final Logger log = LoggerFactory.getLogger(ControlHttpServer.class);

    private final String apiKey;
    private final LavalinkManager lavalinkManager;
    private final LavalinkStateService stateService;
    private HttpServer server;

    public ControlHttpServer(String apiKey, LavalinkManager lavalinkManager) {
        this.apiKey = apiKey;
        this.lavalinkManager = lavalinkManager;
        this.stateService = new LavalinkStateService(lavalinkManager);
    }

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/internal/health", new HealthHandler());
        server.createContext("/internal/guilds", new AuthenticatedHandler(new GuildsHandler()));
        server.createContext("/internal/sessions", new AuthenticatedHandler(new SessionsRouter()));
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(4));
        server.start();
        log.info("Control Plane HTTP server started on port {}", port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            log.info("Control Plane HTTP server stopped");
        }
    }

    private boolean authorize(HttpExchange exchange) {
        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        return auth != null && auth.equals("Bearer " + apiKey);
    }

    private void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = InternalJson.GSON.toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int status, String message) throws IOException {
        sendJson(exchange, status, Map.of("error", message));
    }

    private long parseGuildId(String segment) {
        return Long.parseLong(segment);
    }

    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            sendJson(exchange, 200, Map.of("status", "UP"));
        }
    }

    private class AuthenticatedHandler implements HttpHandler {
        private final HttpHandler delegate;

        AuthenticatedHandler(HttpHandler delegate) {
            this.delegate = delegate;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!authorize(exchange)) {
                sendError(exchange, 401, "Unauthorized");
                return;
            }
            delegate.handle(exchange);
        }
    }

    private class GuildsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            JDA jda = lavalinkManager.getJda();
            if (jda == null) {
                sendJson(exchange, 200, List.of());
                return;
            }
            List<GuildDto> guilds = new ArrayList<>();
            for (Guild guild : jda.getGuilds()) {
                guilds.add(new GuildDto(
                        guild.getId(),
                        guild.getName(),
                        guild.getMemberCount()
                ));
            }
            sendJson(exchange, 200, guilds);
        }
    }

    private class SessionsRouter implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equalsIgnoreCase(method) && "/internal/sessions".equals(path)) {
                SessionsResponse response = stateService.getSessions();
                sendJson(exchange, 200, response);
                return;
            }

            // /internal/sessions/{guildId}/queue|skip|stop|leave
            String prefix = "/internal/sessions/";
            if (!path.startsWith(prefix)) {
                sendError(exchange, 404, "Not found");
                return;
            }

            String remainder = path.substring(prefix.length());
            String[] parts = remainder.split("/");
            if (parts.length < 2) {
                sendError(exchange, 404, "Not found");
                return;
            }

            long guildId;
            try {
                guildId = parseGuildId(parts[0]);
            } catch (NumberFormatException e) {
                sendError(exchange, 400, "Invalid guild ID");
                return;
            }

            String action = parts[1];
            switch (action) {
                case "queue" -> handleQueue(exchange, method, guildId);
                case "skip" -> handleSkip(exchange, method, guildId);
                case "stop" -> handleStop(exchange, method, guildId);
                case "leave" -> handleLeave(exchange, method, guildId);
                default -> sendError(exchange, 404, "Not found");
            }
        }

        private void handleQueue(HttpExchange exchange, String method, long guildId) throws IOException {
            if (!"GET".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            List<QueueTrackDto> queue = stateService.getQueue(guildId);
            if (queue == null) {
                sendError(exchange, 404, "No active session");
                return;
            }
            sendJson(exchange, 200, queue);
        }

        private void handleSkip(HttpExchange exchange, String method, long guildId) throws IOException {
            if (!"POST".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            GuildMusicManager gm = lavalinkManager.getExisting(guildId);
            if (gm == null || gm.getPlayer() == null) {
                sendError(exchange, 404, "No active player");
                return;
            }
            gm.getScheduler().skip();
            sendJson(exchange, 200, Map.of("status", "skipped"));
        }

        private void handleStop(HttpExchange exchange, String method, long guildId) throws IOException {
            if (!"POST".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            GuildMusicManager gm = lavalinkManager.getExisting(guildId);
            if (gm == null || gm.getPlayer() == null) {
                sendError(exchange, 404, "No active player");
                return;
            }
            gm.getScheduler().stop();
            sendJson(exchange, 200, Map.of("status", "stopped"));
        }

        private void handleLeave(HttpExchange exchange, String method, long guildId) throws IOException {
            if (!"POST".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            JDA jda = lavalinkManager.getJda();
            if (jda == null) {
                sendError(exchange, 503, "JDA not ready");
                return;
            }
            var guild = jda.getGuildById(guildId);
            if (guild == null) {
                sendError(exchange, 404, "Guild not found");
                return;
            }
            if (guild.getSelfMember().getVoiceState().getChannel() == null) {
                sendError(exchange, 404, "Not in voice channel");
                return;
            }
            var existing = lavalinkManager.getExisting(guildId);
            if (existing != null) {
                existing.getScheduler().stop();
            }
            jda.getDirectAudioController().disconnect(guild);
            lavalinkManager.destroy(guildId);
            MetricsHolder.get().recordVoiceSession("leave");
            MetricsHolder.get().setActivePlayers(lavalinkManager.activePlayerCount());
            sendJson(exchange, 200, Map.of("status", "left"));
        }
    }
}
