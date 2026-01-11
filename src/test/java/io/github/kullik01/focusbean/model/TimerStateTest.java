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
