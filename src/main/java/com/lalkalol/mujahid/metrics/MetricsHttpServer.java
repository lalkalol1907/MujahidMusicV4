package com.lalkalol.mujahid.metrics;

import com.lalkalol.mujahid.audio.LavalinkManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Lightweight HTTP server that exposes:
 *   GET /metrics  — Prometheus text format
 *   GET /health   — simple liveness check
 */
public class MetricsHttpServer {

    private static final Logger log = LoggerFactory.getLogger(MetricsHttpServer.class);

    private final BotMetrics metrics;
    private HttpServer server;

    public MetricsHttpServer(BotMetrics metrics, LavalinkManager lavalinkManager) {
        this.metrics = metrics;
    }

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/metrics", new PrometheusHandler());
        server.createContext("/health", new HealthHandler());
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(2));
        server.start();
        log.info("Metrics HTTP server started on port {}", port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            log.info("Metrics HTTP server stopped");
        }
    }

    private class PrometheusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            byte[] body = metrics.getRegistry().scrape().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; version=0.0.4; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            try (var out = exchange.getResponseBody()) {
                out.write(body);
            }
        }
    }

    private static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] body = "{\"status\":\"UP\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (var out = exchange.getResponseBody()) {
                out.write(body);
            }
        }
    }
}
