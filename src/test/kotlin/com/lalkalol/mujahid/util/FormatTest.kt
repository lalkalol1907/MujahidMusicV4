package com.lalkalol.mujahid.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class FormatTest {
    @Test
    fun duration_streamReturnsLive() {
        assertEquals("LIVE", Format.duration(0, true))
        assertEquals("LIVE", Format.duration(120_000, true))
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0:00",
        "90000, 1:30",
        "125000, 2:05",
        "3661000, 1:01:01",
    )
    fun duration_formatsCorrectly(ms: Long, expected: String) {
        assertEquals(expected, Format.duration(ms))
    }

    @Test
    fun progressBar_zeroTotalShowsKnobAtStart() {
        assertEquals("🔘" + "▬".repeat(17), Format.progressBar(0, 0))
    }

    @Test
    fun progressBar_atStart() {
        val bar = Format.progressBar(0, 100_000, 5)
        assertEquals("🔘▬▬▬▬", bar)
    }

    @Test
    fun progressBar_atEnd() {
        val bar = Format.progressBar(100_000, 100_000, 5)
        assertEquals("▬▬▬▬🔘", bar)
    }

    @Test
    fun progressBar_clampsPastEnd() {
        val bar = Format.progressBar(200_000, 100_000, 5)
        assertEquals("▬▬▬▬🔘", bar)
    }

    @ParameterizedTest
    @CsvSource(
        "90, 90000",
        "1:30, 90000",
        "1:02:03, 3723000",
    )
    fun parseTimestamp_validInputs(input: String, expectedMs: Long) {
        assertEquals(expectedMs, Format.parseTimestamp(input))
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "abc", ":30", "1:xx", "1:2:3:4"])
    fun parseTimestamp_invalidInputs(input: String) {
        assertNull(Format.parseTimestamp(input))
    }
}
