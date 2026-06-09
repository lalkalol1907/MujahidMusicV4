package com.lalkalol.mujahid.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FormatTest {

    @Test
    void duration_streamReturnsLive() {
        assertEquals("LIVE", Format.duration(0, true));
        assertEquals("LIVE", Format.duration(120_000, true));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0:00",
            "90000, 1:30",
            "125000, 2:05",
            "3661000, 1:01:01",
    })
    void duration_formatsCorrectly(long ms, String expected) {
        assertEquals(expected, Format.duration(ms));
    }

    @Test
    void progressBar_zeroTotalShowsKnobAtStart() {
        assertEquals("🔘" + "▬".repeat(17), Format.progressBar(0, 0));
    }

    @Test
    void progressBar_atStart() {
        String bar = Format.progressBar(0, 100_000, 5);
        assertEquals("🔘▬▬▬▬", bar);
    }

    @Test
    void progressBar_atEnd() {
        String bar = Format.progressBar(100_000, 100_000, 5);
        assertEquals("▬▬▬▬🔘", bar);
    }

    @Test
    void progressBar_clampsPastEnd() {
        String bar = Format.progressBar(200_000, 100_000, 5);
        assertEquals("▬▬▬▬🔘", bar);
    }

    @ParameterizedTest
    @CsvSource({
            "90, 90000",
            "1:30, 90000",
            "1:02:03, 3723000",
    })
    void parseTimestamp_validInputs(String input, long expectedMs) {
        assertEquals(expectedMs, Format.parseTimestamp(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abc", ":30", "1:xx", "1:2:3:4"})
    void parseTimestamp_invalidInputs(String input) {
        assertNull(Format.parseTimestamp(input));
    }
}
