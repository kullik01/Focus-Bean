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

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * An immutable record representing a completed timer session.
 *
 * <p>
 * Each session captures the timing details of either a work or break period,
 * including when it started, when it ended, its duration, and whether the user
 * completed it or interrupted it early.
 * </p>
 *
 * <p>
 * This record is designed for persistence and can be serialized to JSON using
 * Gson.
 * </p>
 *
 * @param startTime       the date/time when the session started, never
 *                        {@code null}
 * @param endTime         the date/time when the session ended, never
 *                        {@code null}
 * @param type            the type of session (WORK or BREAK), never
 *                        {@code null}
 * @param durationMinutes the configured duration in minutes (not the actual
 *                        elapsed time)
 * @param completed       {@code true} if the session ran to completion,
 *                        {@code false} if interrupted
 */
public record TimerSession(
        LocalDateTime startTime,
        LocalDateTime endTime,
        TimerState type,
        int durationMinutes,
        boolean completed) {

    /**
     * Canonical constructor with validation.
     *
     * @param startTime       the date/time when the session started
     * @param endTime         the date/time when the session ended
     * @param type            the type of session (WORK or BREAK)
     * @param durationMinutes the configured duration in minutes
     * @param completed       whether the session completed normally
     * @throws NullPointerException     if startTime, endTime, or type is null
     * @throws IllegalArgumentException if durationMinutes is not positive,
     *                                  or if endTime is before startTime
     */
    public TimerSession {
        Objects.requireNonNull(startTime, "startTime must not be null");
        Objects.requireNonNull(endTime, "endTime must not be null");
        Objects.requireNonNull(type, "type must not be null");

        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("durationMinutes must be positive, was: " + durationMinutes);
        }

        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("endTime must not be before startTime");
        }

        if (type != TimerState.WORK && type != TimerState.BREAK) {
            throw new IllegalArgumentException("type must be WORK or BREAK, was: " + type);
        }
    }

    /**
     * Creates a completed work session record.
     *
     * @param startTime       when the work session started
     * @param endTime         when the work session ended
     * @param durationMinutes the configured work duration
     * @return a new TimerSession representing a completed work session
     */
    public static TimerSession completedWork(LocalDateTime startTime, LocalDateTime endTime, int durationMinutes) {
        return new TimerSession(startTime, endTime, TimerState.WORK, durationMinutes, true);
    }

    /**
     * Creates a completed break session record.
     *
     * @param startTime       when the break session started
     * @param endTime         when the break session ended
     * @param durationMinutes the configured break duration
     * @return a new TimerSession representing a completed break session
     */
    public static TimerSession completedBreak(LocalDateTime startTime, LocalDateTime endTime, int durationMinutes) {
        return new TimerSession(startTime, endTime, TimerState.BREAK, durationMinutes, true);
    }

    /**
     * Creates an interrupted session record (user stopped before completion).
     *
     * @param startTime       when the session started
     * @param endTime         when the session was interrupted
     * @param type            WORK or BREAK
     * @param durationMinutes the originally configured duration
     * @return a new TimerSession marked as incomplete
     */
    public static TimerSession interrupted(LocalDateTime startTime, LocalDateTime endTime, TimerState type,
            int durationMinutes) {
        return new TimerSession(startTime, endTime, type, durationMinutes, false);
    }

    /**
     * Checks if this session is a work session.
     *
     * @return {@code true} if this is a work session
     */
    public boolean isWorkSession() {
        return type == TimerState.WORK;
    }

    /**
     * Checks if this session is a break session.
     *
     * @return {@code true} if this is a break session
     */
    public boolean isBreakSession() {
        return type == TimerState.BREAK;
    }
}
