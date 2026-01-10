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
    public static final int MAX_WORK_DURATION_MINUTES = 900;

    /** Maximum allowed duration for break sessions (in minutes). */
    public static final int MAX_BREAK_DURATION_MINUTES = 900;

    /** Maximum allowed daily goal (in minutes). */
    public static final int MAX_DAILY_GOAL_MINUTES = 900;

    /** Default work session duration (in minutes). */
    public static final int DEFAULT_WORK_DURATION_MINUTES = 25;

    /** Default break session duration (in minutes). */
    public static final int DEFAULT_BREAK_DURATION_MINUTES = 5;

    /** Default daily goal (in minutes). */
    public static final int DEFAULT_DAILY_GOAL_MINUTES = 25;

    /** Default value for sound notification. */
    public static final boolean DEFAULT_SOUND_NOTIFICATION_ENABLED = true;

    /** Default value for popup notification. */
    public static final boolean DEFAULT_POPUP_NOTIFICATION_ENABLED = true;

    /** Default notification sound. */
    public static final NotificationSound DEFAULT_NOTIFICATION_SOUND = NotificationSound.CHIME;

    private int workDurationMinutes;
    private int breakDurationMinutes;
    private int dailyGoalMinutes;
    private boolean soundNotificationEnabled;
    private boolean popupNotificationEnabled;
    private NotificationSound notificationSound;
    private String customSoundPath;

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
        this.soundNotificationEnabled = DEFAULT_SOUND_NOTIFICATION_ENABLED;
        this.popupNotificationEnabled = DEFAULT_POPUP_NOTIFICATION_ENABLED;
        this.notificationSound = DEFAULT_NOTIFICATION_SOUND;
        this.customSoundPath = null;
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
        this.soundNotificationEnabled = DEFAULT_SOUND_NOTIFICATION_ENABLED;
        this.popupNotificationEnabled = DEFAULT_POPUP_NOTIFICATION_ENABLED;
        this.notificationSound = DEFAULT_NOTIFICATION_SOUND;
        this.customSoundPath = null;
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
        this.soundNotificationEnabled = DEFAULT_SOUND_NOTIFICATION_ENABLED;
        this.popupNotificationEnabled = DEFAULT_POPUP_NOTIFICATION_ENABLED;
        this.notificationSound = DEFAULT_NOTIFICATION_SOUND;
        this.customSoundPath = null;
    }

    /**
     * Creates a new UserSettings instance with full configuration.
     *
     * @param workDurationMinutes      the work session duration in minutes
     * @param breakDurationMinutes     the break session duration in minutes
     * @param dailyGoalMinutes         the daily goal in minutes
     * @param soundNotificationEnabled whether sound notifications are enabled
     * @param popupNotificationEnabled whether popup notifications are enabled
     * @throws IllegalArgumentException if any duration is outside the allowed range
     */
    public UserSettings(int workDurationMinutes, int breakDurationMinutes, int dailyGoalMinutes,
            boolean soundNotificationEnabled, boolean popupNotificationEnabled) {
        setWorkDurationMinutes(workDurationMinutes);
        setBreakDurationMinutes(breakDurationMinutes);
        setDailyGoalMinutes(dailyGoalMinutes);
        this.soundNotificationEnabled = soundNotificationEnabled;
        this.popupNotificationEnabled = popupNotificationEnabled;
        this.notificationSound = DEFAULT_NOTIFICATION_SOUND;
        this.customSoundPath = null;
    }

    /**
     * Creates a new UserSettings instance with full configuration including sound
     * selection.
     *
     * @param workDurationMinutes      the work session duration in minutes
     * @param breakDurationMinutes     the break session duration in minutes
     * @param dailyGoalMinutes         the daily goal in minutes
     * @param soundNotificationEnabled whether sound notifications are enabled
     * @param popupNotificationEnabled whether popup notifications are enabled
     * @param notificationSound        the selected notification sound
     * @param customSoundPath          the path to custom sound file, or null
     * @throws IllegalArgumentException if any duration is outside the allowed range
     */
    public UserSettings(int workDurationMinutes, int breakDurationMinutes, int dailyGoalMinutes,
            boolean soundNotificationEnabled, boolean popupNotificationEnabled,
            NotificationSound notificationSound, String customSoundPath) {
        setWorkDurationMinutes(workDurationMinutes);
        setBreakDurationMinutes(breakDurationMinutes);
        setDailyGoalMinutes(dailyGoalMinutes);
        this.soundNotificationEnabled = soundNotificationEnabled;
        this.popupNotificationEnabled = popupNotificationEnabled;
        this.notificationSound = notificationSound != null ? notificationSound : DEFAULT_NOTIFICATION_SOUND;
        this.customSoundPath = customSoundPath;
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
        return new UserSettings(
                other.workDurationMinutes,
                other.breakDurationMinutes,
                other.dailyGoalMinutes,
                other.soundNotificationEnabled,
                other.popupNotificationEnabled,
                other.notificationSound,
                other.customSoundPath);
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
     * Returns whether sound notifications are enabled.
     *
     * @return {@code true} if sound notifications are enabled
     */
    public boolean isSoundNotificationEnabled() {
        return soundNotificationEnabled;
    }

    /**
     * Sets whether sound notifications are enabled.
     *
     * @param soundNotificationEnabled {@code true} to enable sound notifications
     */
    public void setSoundNotificationEnabled(boolean soundNotificationEnabled) {
        this.soundNotificationEnabled = soundNotificationEnabled;
    }

    /**
     * Returns whether popup notifications are enabled.
     *
     * @return {@code true} if popup notifications are enabled
     */
    public boolean isPopupNotificationEnabled() {
        return popupNotificationEnabled;
    }

    /**
     * Sets whether popup notifications are enabled.
     *
     * @param popupNotificationEnabled {@code true} to enable popup notifications
     */
    public void setPopupNotificationEnabled(boolean popupNotificationEnabled) {
        this.popupNotificationEnabled = popupNotificationEnabled;
    }

    /**
     * Returns the selected notification sound.
     *
     * @return the notification sound
     */
    public NotificationSound getNotificationSound() {
        return notificationSound;
    }

    /**
     * Sets the notification sound.
     *
     * @param notificationSound the notification sound to use
     */
    public void setNotificationSound(NotificationSound notificationSound) {
        this.notificationSound = notificationSound != null ? notificationSound : DEFAULT_NOTIFICATION_SOUND;
    }

    /**
     * Returns the custom sound file path.
     *
     * @return the custom sound path, or null if not using custom sound
     */
    public String getCustomSoundPath() {
        return customSoundPath;
    }

    /**
     * Sets the custom sound file path.
     *
     * @param customSoundPath the path to the custom sound file
     */
    public void setCustomSoundPath(String customSoundPath) {
        this.customSoundPath = customSoundPath;
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
                && dailyGoalMinutes == that.dailyGoalMinutes
                && soundNotificationEnabled == that.soundNotificationEnabled
                && popupNotificationEnabled == that.popupNotificationEnabled
                && notificationSound == that.notificationSound
                && Objects.equals(customSoundPath, that.customSoundPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workDurationMinutes, breakDurationMinutes, dailyGoalMinutes,
                soundNotificationEnabled, popupNotificationEnabled, notificationSound, customSoundPath);
    }

    @Override
    public String toString() {
        return String.format(
                "UserSettings[work=%dmin, break=%dmin, dailyGoal=%dmin, sound=%s, popup=%s, notifSound=%s, customPath=%s]",
                workDurationMinutes, breakDurationMinutes, dailyGoalMinutes,
                soundNotificationEnabled, popupNotificationEnabled, notificationSound, customSoundPath);
    }
}
