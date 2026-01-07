package io.github.kullik01.focusbean.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TimerState}.
 */
class TimerStateTest {

    @Test
    @DisplayName("WORK state should be running")
    void workStateIsRunning() {
        assertTrue(TimerState.WORK.isRunning());
    }

    @Test
    @DisplayName("BREAK state should be running")
    void breakStateIsRunning() {
        assertTrue(TimerState.BREAK.isRunning());
    }

    @Test
    @DisplayName("IDLE state should not be running")
    void idleStateIsNotRunning() {
        assertFalse(TimerState.IDLE.isRunning());
    }

    @Test
    @DisplayName("PAUSED state should not be running")
    void pausedStateIsNotRunning() {
        assertFalse(TimerState.PAUSED.isRunning());
    }

    @Test
    @DisplayName("WORK state should be work phase")
    void workStateIsWorkPhase() {
        assertTrue(TimerState.WORK.isWorkPhase());
        assertFalse(TimerState.BREAK.isWorkPhase());
    }

    @Test
    @DisplayName("BREAK state should be break phase")
    void breakStateIsBreakPhase() {
        assertTrue(TimerState.BREAK.isBreakPhase());
        assertFalse(TimerState.WORK.isBreakPhase());
    }

    @Test
    @DisplayName("All states should have display names")
    void allStatesHaveDisplayNames() {
        for (TimerState state : TimerState.values()) {
            assertNotNull(state.getDisplayName());
            assertFalse(state.getDisplayName().isBlank());
        }
    }
}
