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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TimerSession}.
 */
class TimerSessionTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 1, 7, 10, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 1, 7, 10, 25, 0);

    @Test
    @DisplayName("Should create completed work session via factory method")
    void createCompletedWorkSession() {
        TimerSession session = TimerSession.completedWork(START, END, 25);

        assertEquals(START, session.startTime());
        assertEquals(END, session.endTime());
        assertEquals(TimerState.WORK, session.type());
        assertEquals(25, session.durationMinutes());
        assertTrue(session.completed());
        assertTrue(session.isWorkSession());
        assertFalse(session.isBreakSession());
    }

    @Test
    @DisplayName("Should create completed break session via factory method")
    void createCompletedBreakSession() {
        TimerSession session = TimerSession.completedBreak(START, END, 5);

        assertEquals(TimerState.BREAK, session.type());
        assertEquals(5, session.durationMinutes());
        assertTrue(session.completed());
        assertFalse(session.isWorkSession());
        assertTrue(session.isBreakSession());
    }

    @Test
    @DisplayName("Should create interrupted session via factory method")
    void createInterruptedSession() {
        TimerSession session = TimerSession.interrupted(START, END, TimerState.WORK, 25);

        assertEquals(TimerState.WORK, session.type());
        assertFalse(session.completed());
    }

    @Test
    @DisplayName("Should reject null startTime")
    void rejectNullStartTime() {
        assertThrows(NullPointerException.class, () -> new TimerSession(null, END, TimerState.WORK, 25, true));
    }

    @Test
    @DisplayName("Should reject null endTime")
    void rejectNullEndTime() {
        assertThrows(NullPointerException.class, () -> new TimerSession(START, null, TimerState.WORK, 25, true));
    }

    @Test
    @DisplayName("Should reject null type")
    void rejectNullType() {
        assertThrows(NullPointerException.class, () -> new TimerSession(START, END, null, 25, true));
    }

    @Test
    @DisplayName("Should reject non-positive duration")
    void rejectNonPositiveDuration() {
        assertThrows(IllegalArgumentException.class, () -> new TimerSession(START, END, TimerState.WORK, 0, true));
        assertThrows(IllegalArgumentException.class, () -> new TimerSession(START, END, TimerState.WORK, -1, true));
    }

    @Test
    @DisplayName("Should reject endTime before startTime")
    void rejectEndTimeBeforeStartTime() {
        LocalDateTime endBeforeStart = START.minusMinutes(1);
        assertThrows(IllegalArgumentException.class,
                () -> new TimerSession(START, endBeforeStart, TimerState.WORK, 25, true));
    }

    @Test
    @DisplayName("Should reject IDLE and PAUSED types")
    void rejectInvalidTypes() {
        assertThrows(IllegalArgumentException.class, () -> new TimerSession(START, END, TimerState.IDLE, 25, true));
        assertThrows(IllegalArgumentException.class, () -> new TimerSession(START, END, TimerState.PAUSED, 25, true));
    }
}
