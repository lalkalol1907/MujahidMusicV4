package com.lalkalol.mujahid.audio

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class LoopModeTest {
    @ParameterizedTest
    @CsvSource(
        "off, OFF",
        "TRACK, TRACK",
        "queue, QUEUE",
    )
    fun from_parsesKnownModes(input: String, expected: LoopMode) {
        assertEquals(expected, LoopMode.from(input))
    }

    @Test
    fun from_unknownReturnsNull() {
        assertNull(LoopMode.from("repeat-one"))
    }
}
