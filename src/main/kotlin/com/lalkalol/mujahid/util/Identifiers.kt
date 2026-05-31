package com.lalkalol.mujahid.util

object Identifiers {
    private val URL = Regex("^https?://.*", RegexOption.IGNORE_CASE)

    fun fromQuery(query: String): String {
        val trimmed = query.trim()
        return if (URL.matches(trimmed)) trimmed else "ytsearch:$trimmed"
    }
}
