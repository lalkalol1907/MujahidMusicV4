package com.lalkalol.mujahid.internal

import com.lalkalol.mujahid.audio.GuildMusicManager
import com.lalkalol.mujahid.audio.LavalinkManager
import com.lalkalol.mujahid.internal.dto.ActionResponse
import com.lalkalol.mujahid.internal.dto.ErrorResponse
import com.lalkalol.mujahid.internal.dto.GuildDto
import com.lalkalol.mujahid.internal.dto.HealthResponse
import com.lalkalol.mujahid.metrics.MetricsHolder
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

class ControlHttpServer(
    private val apiKey: String,
    private val lavalinkManager: LavalinkManager,
) {
    private val log = LoggerFactory.getLogger(ControlHttpServer::class.java)
    private val stateService = LavalinkStateService(lavalinkManager)
    private var server: HttpServer? = null

    fun start(port: Int) {
        server = HttpServer.create(InetSocketAddress("0.0.0.0", port), 0).apply {
            createContext("/internal/health", HealthHandler())
            createContext("/internal/guilds", AuthenticatedHandler(GuildsHandler()))
            createContext("/internal/sessions", AuthenticatedHandler(SessionsRouter()))
            executor = Executors.newFixedThreadPool(4)
            start()
        }
        log.info("Control Plane HTTP server started on port {}", port)
    }

    fun stop() {
        server?.let {
            it.stop(0)
            log.info("Control Plane HTTP server stopped")
        }
    }

    private fun authorize(exchange: HttpExchange): Boolean {
        if (apiKey.isBlank()) {
            return false
        }
        val auth = exchange.requestHeaders.getFirst("Authorization")
        return auth == "Bearer $apiKey"
    }

    private inline fun <reified T> sendJson(exchange: HttpExchange, status: Int, body: T) {
        val bytes = InternalJson.encode(body).toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
        exchange.sendResponseHeaders(status, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }

    private fun sendError(exchange: HttpExchange, status: Int, message: String) {
        sendJson(exchange, status, ErrorResponse(message))
    }

    private inner class HealthHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (!"GET".equals(exchange.requestMethod, ignoreCase = true)) {
                exchange.sendResponseHeaders(405, -1)
                return
            }
            sendJson(exchange, 200, HealthResponse("UP"))
        }
    }

    private inner class AuthenticatedHandler(
        private val delegate: HttpHandler,
    ) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (!authorize(exchange)) {
                sendError(exchange, 401, "Unauthorized")
                return
            }
            delegate.handle(exchange)
        }
    }

    private inner class GuildsHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (!"GET".equals(exchange.requestMethod, ignoreCase = true)) {
                exchange.sendResponseHeaders(405, -1)
                return
            }
            val jda = lavalinkManager.jda
            if (jda == null) {
                sendJson(exchange, 200, emptyList<GuildDto>())
                return
            }
            val guilds = jda.guilds.map { guild ->
                GuildDto(
                    id = guild.id,
                    name = guild.name,
                    memberCount = guild.memberCount,
                )
            }
            sendJson(exchange, 200, guilds)
        }
    }

    private inner class SessionsRouter : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            val method = exchange.requestMethod
            val path = exchange.requestURI.path

            if ("GET".equals(method, ignoreCase = true) && path == "/internal/sessions") {
                sendJson(exchange, 200, stateService.getSessions())
                return
            }

            val prefix = "/internal/sessions/"
            if (!path.startsWith(prefix)) {
                sendError(exchange, 404, "Not found")
                return
            }

            val remainder = path.substring(prefix.length)
            val parts = remainder.split("/")
            if (parts.size < 2) {
                sendError(exchange, 404, "Not found")
                return
            }

            val guildId = try {
                parts[0].toLong()
            } catch (_: NumberFormatException) {
                sendError(exchange, 400, "Invalid guild ID")
                return
            }

            when (parts[1]) {
                "queue" -> handleQueue(exchange, method, guildId)
                "skip" -> handleSkip(exchange, method, guildId)
                "stop" -> handleStop(exchange, method, guildId)
                "leave" -> handleLeave(exchange, method, guildId)
                else -> sendError(exchange, 404, "Not found")
            }
        }

        private fun handleQueue(exchange: HttpExchange, method: String, guildId: Long) {
            if (!"GET".equals(method, ignoreCase = true)) {
                exchange.sendResponseHeaders(405, -1)
                return
            }
            val queue = stateService.getQueue(guildId)
            if (queue == null) {
                sendError(exchange, 404, "No active session")
                return
            }
            sendJson(exchange, 200, queue)
        }

        private fun handleSkip(exchange: HttpExchange, method: String, guildId: Long) {
            if (!"POST".equals(method, ignoreCase = true)) {
                exchange.sendResponseHeaders(405, -1)
                return
            }
            val gm = lavalinkManager.getExisting(guildId)
            if (gm == null || gm.getPlayer() == null) {
                sendError(exchange, 404, "No active player")
                return
            }
            gm.scheduler.skip()
            sendJson(exchange, 200, ActionResponse("skipped"))
        }

        private fun handleStop(exchange: HttpExchange, method: String, guildId: Long) {
            if (!"POST".equals(method, ignoreCase = true)) {
                exchange.sendResponseHeaders(405, -1)
                return
            }
            val gm = lavalinkManager.getExisting(guildId)
            if (gm == null || gm.getPlayer() == null) {
                sendError(exchange, 404, "No active player")
                return
            }
            gm.scheduler.stop()
            sendJson(exchange, 200, ActionResponse("stopped"))
        }

        private fun handleLeave(exchange: HttpExchange, method: String, guildId: Long) {
            if (!"POST".equals(method, ignoreCase = true)) {
                exchange.sendResponseHeaders(405, -1)
                return
            }
            val jda = lavalinkManager.jda
            if (jda == null) {
                sendError(exchange, 503, "JDA not ready")
                return
            }
            val guild = jda.getGuildById(guildId)
            if (guild == null) {
                sendError(exchange, 404, "Guild not found")
                return
            }
            if (guild.selfMember.voiceState?.channel == null) {
                sendError(exchange, 404, "Not in voice channel")
                return
            }
            lavalinkManager.getExisting(guildId)?.scheduler?.stop()
            jda.directAudioController.disconnect(guild)
            lavalinkManager.destroy(guildId)
            MetricsHolder.get().recordVoiceSession("leave")
            MetricsHolder.get().setActivePlayers(lavalinkManager.activePlayerCount())
            sendJson(exchange, 200, ActionResponse("left"))
        }
    }
}
