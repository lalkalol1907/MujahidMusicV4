package com.lalkalol.mujahid.internal;

import com.lalkalol.mujahid.audio.LavalinkManager;
import com.lalkalol.mujahid.config.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControlHttpServerTest {

    private static final String API_KEY = "test-internal-key-32-characters-long";

    @Mock
    private LavalinkManager lavalinkManager;

    private ControlHttpServer server;
    private int port;

    @BeforeEach
    void setUp() throws Exception {
        server = new ControlHttpServer(API_KEY, lavalinkManager);
        port = findFreePort();
        server.start(port);
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void healthIsPublic() throws Exception {
        HttpURLConnection conn = open("/internal/health", "GET", null);
        assertEquals(200, conn.getResponseCode());
        String body = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(body.contains("UP"));
    }

    @Test
    void guildsRequiresAuth() throws Exception {
        HttpURLConnection conn = open("/internal/guilds", "GET", null);
        assertEquals(401, conn.getResponseCode());
    }

    @Test
    void guildsWithValidKey() throws Exception {
        when(lavalinkManager.getJda()).thenReturn(null);
        HttpURLConnection conn = open("/internal/guilds", "GET", API_KEY);
        assertEquals(200, conn.getResponseCode());
        String body = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(body.startsWith("["));
    }

    private HttpURLConnection open(String path, String method, String apiKey) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create("http://localhost:" + port + path).toURL().openConnection();
        conn.setRequestMethod(method);
        if (apiKey != null) {
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        }
        return conn;
    }

    private static int findFreePort() throws Exception {
        try (var socket = new java.net.ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
