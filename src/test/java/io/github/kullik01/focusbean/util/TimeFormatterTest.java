/**
 * BSD 3-Clause License
 *
 * Copyright (c) 2026, Hannah Kullik
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
