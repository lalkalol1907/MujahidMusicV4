package com.lalkalol.mujahid.audio

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class AudioFilterTest {
    @ParameterizedTest
    @CsvSource(
        "off, OFF",
        "none, OFF",
        "clear, OFF",
        "bassboost, BASSBOOST",
        "bass, BASSBOOST",
        "nightcore, NIGHTCORE",
        "8d, EIGHT_D",
        "eight_d, EIGHT_D",
        "rotation, EIGHT_D",
        "karaoke, KARAOKE",
    )
    fun from_parsesKnownPresets(input: String, expected: AudioFilter) {
        assertEquals(expected, AudioFilter.from(input))
    }

    @Test
    fun from_unknownReturnsNull() {
        assertNull(AudioFilter.from("vaporwave"))
    }

    @Test
    fun build_returnsFiltersForEveryPreset() {
        for (filter in AudioFilter.entries) {
            assertNotNull(filter.build(), filter.name)
        }
    }
}
