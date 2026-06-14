package com.lalkalol.mujahid.health

import net.dv8tion.jda.api.JDA
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class HealthMonitor(
    private val jda: JDA,
    private val file: Path,
    private val intervalSeconds: Long = 15,
) {
    private val log = LoggerFactory.getLogger(HealthMonitor::class.java)
    private val scheduler = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "health-monitor").apply { isDaemon = true }
    }

    fun start() {
        scheduler.scheduleAtFixedRate({ tick() }, 0, intervalSeconds, TimeUnit.SECONDS)
        log.info("Health monitor writing to {} every {}s", file, intervalSeconds)
    }

    fun stop() {
        scheduler.shutdownNow()
    }

    private fun tick() {
        if (jda.status != JDA.Status.CONNECTED) {
            return
        }
        try {
            file.parent?.let { Files.createDirectories(it) }
            Files.writeString(file, Instant.now().toString())
        } catch (e: Exception) {
            log.warn("Failed to write health file {}", file, e)
        }
    }
}
