package com.lalkalol.mujahid.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class IdentifiersTest {
    @ParameterizedTest
    @CsvSource(
        "https://youtu.be/abc, https://youtu.be/abc",
        "http://example.com/track.mp3, http://example.com/track.mp3",
        "HTTPS://SoundCloud.com/song, HTTPS://SoundCloud.com/song",
    )
    fun fromQuery_passesThroughUrls(input: String, expected: String) {
        assertEquals(expected, Identifiers.fromQuery(input))
    }

    @Test
    fun fromQuery_wrapsSearchTerms() {
        assertEquals("ytsearch:never gonna give you up", Identifiers.fromQuery("never gonna give you up"))
    }

    @Test
    fun fromQuery_trimsWhitespace() {
        assertEquals("ytsearch:hello", Identifiers.fromQuery("  hello  "))
        assertEquals("https://x.com", Identifiers.fromQuery("  https://x.com  "))
    }
}
