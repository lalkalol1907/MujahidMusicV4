package com.lalkalol.mujahid.metrics

import com.lalkalol.mujahid.audio.LavalinkManager
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

class MetricsHttpServer(
    private val metrics: BotMetrics,
    @Suppress("UNUSED_PARAMETER") lavalinkManager: LavalinkManager,
) {
    private val log = LoggerFactory.getLogger(MetricsHttpServer::class.java)
    private var server: HttpServer? = null

    fun start(port: Int) {
        server = HttpServer.create(InetSocketAddress("0.0.0.0", port), 0).apply {
            createContext("/metrics", PrometheusHandler())
            createContext("/health", HealthHandler())
            executor = Executors.newFixedThreadPool(2)
            start()
        }
        log.info("Metrics HTTP server started on port {}", port)
    }

    fun stop() {
        server?.let {
            it.stop(0)
            log.info("Metrics HTTP server stopped")
        }
    }

    private inner class PrometheusHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (!"GET".equals(exchange.requestMethod, ignoreCase = true)) {
                exchange.sendResponseHeaders(405, -1)
                return
            }
            val body = metrics.registry.scrape().toByteArray(StandardCharsets.UTF_8)
            exchange.responseHeaders.set("Content-Type", "text/plain; version=0.0.4; charset=utf-8")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
    }

    private class HealthHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            val body = """{"status":"UP"}""".toByteArray(StandardCharsets.UTF_8)
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
    }
}
