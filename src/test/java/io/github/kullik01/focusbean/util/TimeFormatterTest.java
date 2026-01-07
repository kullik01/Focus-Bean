package io.github.kullik01.focusbean.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TimeFormatter}.
 */
class TimeFormatterTest {

    @ParameterizedTest
    @CsvSource({
            "0, 00:00",
            "59, 00:59",
            "60, 01:00",
            "90, 01:30",
            "3600, 60:00",
            "1500, 25:00"
    })
    @DisplayName("formatSeconds should format correctly")
    void formatSeconds(int seconds, String expected) {
        assertEquals(expected, TimeFormatter.formatSeconds(seconds));
    }

    @Test
    @DisplayName("formatSeconds should reject negative values")
    void formatSecondsRejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> TimeFormatter.formatSeconds(-1));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0 min 0 sec",
            "45, 0 min 45 sec",
            "90, 1 min 30 sec",
            "3600, 60 min 0 sec"
    })
    @DisplayName("formatSecondsReadable should format correctly")
    void formatSecondsReadable(int seconds, String expected) {
        assertEquals(expected, TimeFormatter.formatSecondsReadable(seconds));
    }

    @Test
    @DisplayName("minutesToSeconds should convert correctly")
    void minutesToSeconds() {
        assertEquals(0, TimeFormatter.minutesToSeconds(0));
        assertEquals(60, TimeFormatter.minutesToSeconds(1));
        assertEquals(1500, TimeFormatter.minutesToSeconds(25));
    }

    @Test
    @DisplayName("minutesToSeconds should reject negative values")
    void minutesToSecondsRejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> TimeFormatter.minutesToSeconds(-1));
    }

    @Test
    @DisplayName("secondsToMinutes should truncate correctly")
    void secondsToMinutes() {
        assertEquals(0, TimeFormatter.secondsToMinutes(0));
        assertEquals(0, TimeFormatter.secondsToMinutes(59));
        assertEquals(1, TimeFormatter.secondsToMinutes(60));
        assertEquals(1, TimeFormatter.secondsToMinutes(119));
        assertEquals(25, TimeFormatter.secondsToMinutes(1500));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0 minutes",
            "1, 1 minute",
            "25, 25 minutes"
    })
    @DisplayName("formatMinutes should use correct pluralization")
    void formatMinutes(int minutes, String expected) {
        assertEquals(expected, TimeFormatter.formatMinutes(minutes));
    }
}
