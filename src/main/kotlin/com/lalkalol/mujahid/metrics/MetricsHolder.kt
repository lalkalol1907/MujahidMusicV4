package com.lalkalol.mujahid.metrics

object MetricsHolder {
    @Volatile
    private var instance: BotMetrics? = null

    fun set(metrics: BotMetrics) {
        instance = metrics
    }

    fun get(): BotMetrics = instance ?: NoOpBotMetrics

    private object NoOpBotMetrics : BotMetrics() {
        override fun recordCommandExecution(cmd: String) {}
        override fun recordCommandError(cmd: String) {}
        override fun recordTrackPlayed(src: String) {}
        override fun recordTrackLoadFailure(reason: String) {}
        override fun recordVoiceSession(action: String) {}
        override fun recordPlaylistOp(op: String) {}
        override fun setActivePlayers(count: Int) {}
    }
}
