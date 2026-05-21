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
package io.github.kullik01.focusbean.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link TimerSession}.
 */
class TimerSessionTest {

    /** A constant start time for testing. */
    private static final LocalDateTime START = LocalDateTime.of(2026, 1, 7, 10, 0, 0);
    /** A constant end time for testing. */
    private static final LocalDateTime END = LocalDateTime.of(2026, 1, 7, 10, 25, 0);

    /**
     * Verifies that a completed work session can be created via its factory method.
     */
    @Test
    @DisplayName("Should create completed work session via factory method")
    void createCompletedWorkSession() {
        // Act
        TimerSession session = TimerSession.completedWork(START, END, 25);

        // Assert
        assertEquals(START, session.startTime());
        assertEquals(END, session.endTime());
        assertEquals(TimerState.WORK, session.type());
        assertEquals(25, session.durationMinutes());
        assertTrue(session.completed());
        assertTrue(session.isWorkSession());
        assertFalse(session.isBreakSession());
    }

    /**
     * Verifies that a completed break session can be created via its factory method.
     */
    @Test
    @DisplayName("Should create completed break session via factory method")
    void createCompletedBreakSession() {
        // Act
        TimerSession session = TimerSession.completedBreak(START, END, 5);

        // Assert
        assertEquals(TimerState.BREAK, session.type());
        assertEquals(5, session.durationMinutes());
        assertTrue(session.completed());
        assertFalse(session.isWorkSession());
        assertTrue(session.isBreakSession());
    }

    /**
     * Verifies that an interrupted session can be created via its factory method.
     */
    @Test
    @DisplayName("Should create interrupted session via factory method")
    void createInterruptedSession() {
        // Act
        TimerSession session = TimerSession.interrupted(START, END, TimerState.WORK, 25);

        // Assert
        assertEquals(TimerState.WORK, session.type());
        assertFalse(session.completed());
    }

    /**
     * Verifies that the constructor rejects a null start time.
     */
    @Test
    @DisplayName("Should reject null startTime")
    void rejectNullStartTime() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> new TimerSession(null, END, TimerState.WORK, 25, true));
    }

    /**
     * Verifies that the constructor rejects a null end time.
     */
    @Test
    @DisplayName("Should reject null endTime")
    void rejectNullEndTime() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> new TimerSession(START, null, TimerState.WORK, 25, true));
    }

    /**
     * Verifies that the constructor rejects a null session type.
     */
    @Test
    @DisplayName("Should reject null type")
    void rejectNullType() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> new TimerSession(START, END, null, 25, true));
    }

    /**
     * Verifies that the constructor rejects non-positive durations.
     */
    @Test
    @DisplayName("Should reject non-positive duration")
    void rejectNonPositiveDuration() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new TimerSession(START, END, TimerState.WORK, 0, true));
        assertThrows(IllegalArgumentException.class, () -> new TimerSession(START, END, TimerState.WORK, -1, true));
    }

    /**
     * Verifies that the constructor rejects an end time that is before the start time.
     */
    @Test
    @DisplayName("Should reject endTime before startTime")
    void rejectEndTimeBeforeStartTime() {
        // Arrange
        LocalDateTime endBeforeStart = START.minusMinutes(1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> new TimerSession(START, endBeforeStart, TimerState.WORK, 25, true));
    }

    /**
     * Verifies that the constructor rejects IDLE and PAUSED states as session types.
     */
    @Test
    @DisplayName("Should reject IDLE and PAUSED types")
    void rejectInvalidTypes() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new TimerSession(START, END, TimerState.IDLE, 25, true));
        assertThrows(IllegalArgumentException.class, () -> new TimerSession(START, END, TimerState.PAUSED, 25, true));
    }
}
