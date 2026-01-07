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
