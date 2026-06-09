package com.lalkalol.mujahid.audio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AudioFilterTest {

    @ParameterizedTest
    @CsvSource({
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
    })
    void from_parsesKnownPresets(String input, AudioFilter expected) {
        assertEquals(expected, AudioFilter.from(input));
    }

    @Test
    void from_unknownReturnsNull() {
        assertNull(AudioFilter.from("vaporwave"));
    }

    @Test
    void build_returnsFiltersForEveryPreset() {
        for (AudioFilter filter : AudioFilter.values()) {
            assertNotNull(filter.build(), filter.name());
        }
    }
}
