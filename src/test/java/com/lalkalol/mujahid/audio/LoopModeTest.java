package com.lalkalol.mujahid.audio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LoopModeTest {

    @ParameterizedTest
    @CsvSource({
            "off, OFF",
            "TRACK, TRACK",
            "queue, QUEUE",
    })
    void from_parsesKnownModes(String input, LoopMode expected) {
        assertEquals(expected, LoopMode.from(input));
    }

    @Test
    void from_unknownReturnsNull() {
        assertNull(LoopMode.from("repeat-one"));
    }
}
