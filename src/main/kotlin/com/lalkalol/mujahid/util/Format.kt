package com.lalkalol.mujahid.util

import java.time.Duration

object Format {

    fun duration(ms: Long, isStream: Boolean = false): String {
        if (isStream) return "LIVE"
        val d = Duration.ofMillis(ms)
        val hours = d.toHours()
        val minutes = d.toMinutesPart()
        val seconds = d.toSecondsPart()
        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%d:%02d".format(minutes, seconds)
        }
    }

    fun progressBar(position: Long, total: Long, length: Int = 18): String {
        if (total <= 0) return "🔘" + "▬".repeat(length - 1)
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
        if (parts.any { it.isBlank() || it.toLongOrNull() == null }) return null
        val numbers = parts.map { it.toLong() }
        val seconds = when (numbers.size) {
            1 -> numbers[0]
            2 -> numbers[0] * 60 + numbers[1]
            3 -> numbers[0] * 3600 + numbers[1] * 60 + numbers[2]
            else -> return null
        }
        return seconds * 1000
    }
}
