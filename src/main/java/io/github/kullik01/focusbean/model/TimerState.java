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

/**
 * Represents the possible states of the Pomodoro timer.
 *
 * <p>
 * The timer transitions between these states based on user actions
 * and timer completion events. The state machine follows this pattern:
 * </p>
 * <ul>
 * <li>{@link #IDLE} → {@link #WORK} (user starts timer)</li>
 * <li>{@link #WORK} → {@link #BREAK} (work session completes)</li>
 * <li>{@link #BREAK} → {@link #WORK} (break session completes)</li>
 * <li>Any state → {@link #PAUSED} (user pauses)</li>
 * <li>{@link #PAUSED} → previous state (user resumes)</li>
 * <li>Any state → {@link #IDLE} (user resets)</li>
 * </ul>
 */
public enum TimerState {

    /**
     * Timer is not running and no session is active.
     * This is the initial state when the application starts.
     */
    IDLE("Ready"),

    /**
     * Timer is actively counting down a work session.
     * The user should be focused on their task during this state.
     */
    WORK("Working"),

    /**
     * Timer is actively counting down a break session.
     * The user should rest during this state.
     */
    BREAK("Break"),

    /**
     * Timer is temporarily paused mid-session.
     * The remaining time is preserved and will resume when the user continues.
     */
    PAUSED("Paused");

    private final String displayName;

    /**
     * Constructs a TimerState with the specified display name.
     *
     * @param displayName the human-readable name shown in the UI
     */
    TimerState(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the human-readable display name for this state.
     *
     * @return the display name suitable for UI presentation
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Determines if this state represents an active timing session.
     *
     * @return {@code true} if the timer is actively counting down (WORK or BREAK),
     *         {@code false} otherwise
     */
    public boolean isRunning() {
        return this == WORK || this == BREAK;
    }

    /**
     * Determines if this state represents a work session (active or paused from
     * work).
     *
     * @return {@code true} if currently in a work session
     */
    public boolean isWorkPhase() {
        return this == WORK;
    }

    /**
     * Determines if this state represents a break session (active or paused from
     * break).
     *
     * @return {@code true} if currently in a break session
     */
    public boolean isBreakPhase() {
        return this == BREAK;
    }
}
