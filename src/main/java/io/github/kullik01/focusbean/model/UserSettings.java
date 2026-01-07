package io.github.kullik01.focusbean.model;

import java.util.Objects;

/**
 * Holds user-configurable settings for the Pomodoro timer.
 *
 * <p>
 * This class stores the durations for work and break sessions. All values
 * are validated to ensure they fall within acceptable ranges. The settings
 * can be persisted to JSON and restored on application restart.
 * </p>
 *
 * <p>
 * Thread safety: This class is not thread-safe. External synchronization
 * is required if accessed from multiple threads.
 * </p>
 */
public final class UserSettings {

    /** Minimum allowed duration for any session (in minutes). */
    public static final int MIN_DURATION_MINUTES = 1;

    /** Maximum allowed duration for work sessions (in minutes). */
    public static final int MAX_WORK_DURATION_MINUTES = 120;

    /** Maximum allowed duration for break sessions (in minutes). */
    public static final int MAX_BREAK_DURATION_MINUTES = 60;

    /** Maximum allowed daily goal (in minutes). */
    public static final int MAX_DAILY_GOAL_MINUTES = 480;

    /** Default work session duration (in minutes). */
    public static final int DEFAULT_WORK_DURATION_MINUTES = 25;

    /** Default break session duration (in minutes). */
    public static final int DEFAULT_BREAK_DURATION_MINUTES = 5;

    /** Default daily goal (in minutes). */
    public static final int DEFAULT_DAILY_GOAL_MINUTES = 25;

    private int workDurationMinutes;
    private int breakDurationMinutes;
    private int dailyGoalMinutes;

    /**
     * Creates a new UserSettings instance with default values.
     *
     * <p>
     * Default values:
     * </p>
     * <ul>
     * <li>Work duration: 25 minutes</li>
     * <li>Break duration: 5 minutes</li>
     * </ul>
     */
    public UserSettings() {
        this.workDurationMinutes = DEFAULT_WORK_DURATION_MINUTES;
        this.breakDurationMinutes = DEFAULT_BREAK_DURATION_MINUTES;
        this.dailyGoalMinutes = DEFAULT_DAILY_GOAL_MINUTES;
    }

    /**
     * Creates a new UserSettings instance with the specified durations.
     *
     * @param workDurationMinutes  the work session duration in minutes
     * @param breakDurationMinutes the break session duration in minutes
     * @throws IllegalArgumentException if either duration is outside the allowed
     *                                  range
     */
    public UserSettings(int workDurationMinutes, int breakDurationMinutes) {
        setWorkDurationMinutes(workDurationMinutes);
        setBreakDurationMinutes(breakDurationMinutes);
        this.dailyGoalMinutes = DEFAULT_DAILY_GOAL_MINUTES;
    }

    /**
     * Creates a new UserSettings instance with the specified durations and daily
     * goal.
     *
     * @param workDurationMinutes  the work session duration in minutes
     * @param breakDurationMinutes the break session duration in minutes
     * @param dailyGoalMinutes     the daily goal in minutes
     * @throws IllegalArgumentException if any duration is outside the allowed range
     */
    public UserSettings(int workDurationMinutes, int breakDurationMinutes, int dailyGoalMinutes) {
        setWorkDurationMinutes(workDurationMinutes);
        setBreakDurationMinutes(breakDurationMinutes);
        setDailyGoalMinutes(dailyGoalMinutes);
    }

    /**
     * Creates a defensive copy of the given settings.
     *
     * @param other the settings to copy
     * @return a new UserSettings instance with the same values
     * @throws NullPointerException if other is null
     */
    public static UserSettings copyOf(UserSettings other) {
        Objects.requireNonNull(other, "other must not be null");
        return new UserSettings(other.workDurationMinutes, other.breakDurationMinutes, other.dailyGoalMinutes);
    }

    /**
     * Returns the configured work session duration.
     *
     * @return the work duration in minutes
     */
    public int getWorkDurationMinutes() {
        return workDurationMinutes;
    }

    /**
     * Sets the work session duration.
     *
     * @param workDurationMinutes the duration in minutes
     * @throws IllegalArgumentException if the duration is outside the range
     *                                  [{@value #MIN_DURATION_MINUTES},
     *                                  {@value #MAX_WORK_DURATION_MINUTES}]
     */
    public void setWorkDurationMinutes(int workDurationMinutes) {
        validateDuration(workDurationMinutes, MIN_DURATION_MINUTES, MAX_WORK_DURATION_MINUTES, "workDurationMinutes");
        this.workDurationMinutes = workDurationMinutes;
    }

    /**
     * Returns the configured break session duration.
     *
     * @return the break duration in minutes
     */
    public int getBreakDurationMinutes() {
        return breakDurationMinutes;
    }

    /**
     * Sets the break session duration.
     *
     * @param breakDurationMinutes the duration in minutes
     * @throws IllegalArgumentException if the duration is outside the range
     *                                  [{@value #MIN_DURATION_MINUTES},
     *                                  {@value #MAX_BREAK_DURATION_MINUTES}]
     */
    public void setBreakDurationMinutes(int breakDurationMinutes) {
        validateDuration(breakDurationMinutes, MIN_DURATION_MINUTES, MAX_BREAK_DURATION_MINUTES,
                "breakDurationMinutes");
        this.breakDurationMinutes = breakDurationMinutes;
    }

    /**
     * Returns the work duration in seconds.
     *
     * @return the work duration converted to seconds
     */
    public int getWorkDurationSeconds() {
        return workDurationMinutes * 60;
    }

    /**
     * Returns the break duration in seconds.
     *
     * @return the break duration converted to seconds
     */
    public int getBreakDurationSeconds() {
        return breakDurationMinutes * 60;
    }

    /**
     * Returns the configured daily goal.
     *
     * @return the daily goal in minutes
     */
    public int getDailyGoalMinutes() {
        return dailyGoalMinutes;
    }

    /**
     * Sets the daily goal.
     *
     * @param dailyGoalMinutes the goal in minutes
     * @throws IllegalArgumentException if the value is outside the range
     *                                  [{@value #MIN_DURATION_MINUTES},
     *                                  {@value #MAX_DAILY_GOAL_MINUTES}]
     */
    public void setDailyGoalMinutes(int dailyGoalMinutes) {
        validateDuration(dailyGoalMinutes, MIN_DURATION_MINUTES, MAX_DAILY_GOAL_MINUTES, "dailyGoalMinutes");
        this.dailyGoalMinutes = dailyGoalMinutes;
    }

    /**
     * Validates that a duration value falls within the specified range.
     *
     * @param value     the value to validate
     * @param min       the minimum allowed value (inclusive)
     * @param max       the maximum allowed value (inclusive)
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the value is outside the allowed range
     */
    private void validateDuration(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    String.format("%s must be between %d and %d, was: %d", fieldName, min, max, value));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UserSettings that = (UserSettings) obj;
        return workDurationMinutes == that.workDurationMinutes
                && breakDurationMinutes == that.breakDurationMinutes
                && dailyGoalMinutes == that.dailyGoalMinutes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(workDurationMinutes, breakDurationMinutes, dailyGoalMinutes);
    }

    @Override
    public String toString() {
        return String.format("UserSettings[work=%dmin, break=%dmin, dailyGoal=%dmin]",
                workDurationMinutes, breakDurationMinutes, dailyGoalMinutes);
    }
}
