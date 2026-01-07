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
