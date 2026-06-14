package com.lalkalol.mujahid.util

import java.time.Duration

object Format {
    fun duration(ms: Long): String = duration(ms, false)

    fun duration(ms: Long, isStream: Boolean): String {
        if (isStream) {
            return "LIVE"
        }
        val d = Duration.ofMillis(ms)
        val hours = d.toHours()
        val minutes = d.toMinutesPart()
        val seconds = d.toSecondsPart()
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    fun progressBar(position: Long, total: Long): String = progressBar(position, total, 18)

    fun progressBar(position: Long, total: Long, length: Int): String {
        if (total <= 0) {
            return "🔘" + "▬".repeat(length - 1)
        }
        val ratio = (position.toDouble() / total).coerceIn(0.0, 1.0)
        val pos = (ratio * (length - 1)).toInt()
        return buildString {
            for (i in 0 until length) {
                append(if (i == pos) "🔘" else "▬")
            }
        }
    }

    fun parseTimestamp(input: String): Long? {
        val parts = input.trim().split(":")
        for (part in parts) {
            if (part.isBlank()) {
                return null
            }
            part.toLongOrNull() ?: return null
        }

        val seconds = when (parts.size) {
            1 -> parts[0].toLong()
            2 -> parts[0].toLong() * 60 + parts[1].toLong()
            3 -> parts[0].toLong() * 3600 + parts[1].toLong() * 60 + parts[2].toLong()
            else -> return null
        }
        return seconds * 1000
    }
}
