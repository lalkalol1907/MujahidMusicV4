package com.lalkalol.mujahid.metrics;

/**
 * Simple singleton holder so static utility classes (PlaySupport, etc.)
 * can access BotMetrics without constructor injection.
 * Set once at startup before handling any events.
 */
public final class MetricsHolder {

    private static volatile BotMetrics instance;

    private MetricsHolder() {}

    public static void set(BotMetrics metrics) {
        instance = metrics;
    }

    /** Returns the BotMetrics instance, or a no-op stand-in if not yet initialized. */
    public static BotMetrics get() {
        BotMetrics m = instance;
        return m != null ? m : NoOpBotMetrics.INSTANCE;
    }

    /** No-op fallback — prevents NPE if metrics not set during tests or if init is skipped. */
    private static final class NoOpBotMetrics extends BotMetrics {
        static final NoOpBotMetrics INSTANCE = new NoOpBotMetrics();
        private NoOpBotMetrics() { super(); }
        @Override public void recordCommandExecution(String cmd) {}
        @Override public void recordCommandError(String cmd) {}
        @Override public void recordTrackPlayed(String src) {}
        @Override public void recordTrackLoadFailure(String reason) {}
        @Override public void recordVoiceSession(String action) {}
        @Override public void recordPlaylistOp(String op) {}
        @Override public void setActivePlayers(int count) {}
    }
}
