package com.lalkalol.mujahid.health;

import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HealthMonitor {
    private static final Logger log = LoggerFactory.getLogger(HealthMonitor.class);

    private final JDA jda;
    private final Path file;
    private final long intervalSeconds;
    private final ScheduledExecutorService scheduler;

    public HealthMonitor(JDA jda, Path file) {
        this(jda, file, 15);
    }

    public HealthMonitor(JDA jda, Path file, long intervalSeconds) {
        this.jda = jda;
        this.file = file;
        this.intervalSeconds = intervalSeconds;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "health-monitor");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::tick, 0, intervalSeconds, TimeUnit.SECONDS);
        log.info("Health monitor writing to {} every {}s", file, intervalSeconds);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    private void tick() {
        if (jda.getStatus() != JDA.Status.CONNECTED) {
            return;
        }
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            Files.writeString(file, Instant.now().toString());
        } catch (Exception e) {
            log.warn("Failed to write health file {}", file, e);
        }
    }
}
