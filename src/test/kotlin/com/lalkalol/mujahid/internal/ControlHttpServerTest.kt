package com.lalkalol.mujahid.internal

import com.lalkalol.mujahid.audio.LavalinkManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.junit.jupiter.MockitoExtension
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets

@ExtendWith(MockitoExtension::class)
class ControlHttpServerTest {
    private val lavalinkManager: LavalinkManager = mock()
    private lateinit var server: ControlHttpServer
    private var port: Int = 0

    @BeforeEach
    fun setUp() {
        server = ControlHttpServer(API_KEY, lavalinkManager)
        port = findFreePort()
        server.start(port)
    }

    @AfterEach
    fun tearDown() {
        server.stop()
    }

    @Test
    fun healthIsPublic() {
        val conn = open("/internal/health", "GET", null)
        assertEquals(200, conn.responseCode)
        val body = String(conn.inputStream.readAllBytes(), StandardCharsets.UTF_8)
        assertTrue(body.contains("UP"))
    }

    @Test
    fun guildsRequiresAuth() {
        val conn = open("/internal/guilds", "GET", null)
        assertEquals(401, conn.responseCode)
    }

    @Test
    fun guildsWithValidKey() {
        whenever(lavalinkManager.jda).thenReturn(null)
        val conn = open("/internal/guilds", "GET", API_KEY)
        assertEquals(200, conn.responseCode)
        val body = String(conn.inputStream.readAllBytes(), StandardCharsets.UTF_8)
        assertTrue(body.startsWith("["))
    }

    private fun open(path: String, method: String, apiKey: String?): HttpURLConnection {
        val conn = URI.create("http://localhost:$port$path").toURL().openConnection() as HttpURLConnection
        conn.requestMethod = method
        if (apiKey != null) {
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
        }
        return conn
    }

    companion object {
        private const val API_KEY = "test-internal-key-32-characters-long"

        private fun findFreePort(): Int =
            java.net.ServerSocket(0).use { it.localPort }
    }
}
