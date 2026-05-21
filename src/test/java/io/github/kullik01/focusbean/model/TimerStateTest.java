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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link TimerState}.
 */
class TimerStateTest {

    /**
     * Verifies that the WORK state is considered a running state.
     */
    @Test
    @DisplayName("WORK state should be running")
    void workStateIsRunning() {
        // Act & Assert
        assertTrue(TimerState.WORK.isRunning());
    }

    /**
     * Verifies that the BREAK state is considered a running state.
     */
    @Test
    @DisplayName("BREAK state should be running")
    void breakStateIsRunning() {
        // Act & Assert
        assertTrue(TimerState.BREAK.isRunning());
    }

    /**
     * Verifies that the IDLE state is not considered a running state.
     */
    @Test
    @DisplayName("IDLE state should not be running")
    void idleStateIsNotRunning() {
        // Act & Assert
        assertFalse(TimerState.IDLE.isRunning());
    }

    /**
     * Verifies that the PAUSED state is not considered a running state.
     */
    @Test
    @DisplayName("PAUSED state should not be running")
    void pausedStateIsNotRunning() {
        // Act & Assert
        assertFalse(TimerState.PAUSED.isRunning());
    }

    /**
     * Verifies that the WORK state is correctly identified as a work phase.
     */
    @Test
    @DisplayName("WORK state should be work phase")
    void workStateIsWorkPhase() {
        // Act & Assert
        assertTrue(TimerState.WORK.isWorkPhase());
        assertFalse(TimerState.BREAK.isWorkPhase());
    }

    /**
     * Verifies that the BREAK state is correctly identified as a break phase.
     */
    @Test
    @DisplayName("BREAK state should be break phase")
    void breakStateIsBreakPhase() {
        // Act & Assert
        assertTrue(TimerState.BREAK.isBreakPhase());
        assertFalse(TimerState.WORK.isBreakPhase());
    }

    /**
     * Verifies that all timer states have a valid display name.
     */
    @Test
    @DisplayName("All states should have display names")
    void allStatesHaveDisplayNames() {
        // Act & Assert
        for (TimerState state : TimerState.values()) {
            assertNotNull(state.getDisplayName());
            assertFalse(state.getDisplayName().isBlank());
        }
    }
}
