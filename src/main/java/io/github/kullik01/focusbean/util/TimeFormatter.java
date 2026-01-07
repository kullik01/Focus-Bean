package io.github.kullik01.focusbean.util;

/**
 * Utility class for formatting time durations.
 *
 * <p>
 * Provides conversion methods between seconds and human-readable
 * time formats (MM:SS). All methods are stateless and thread-safe.
 * </p>
 */
public final class TimeFormatter {

    private static final int SECONDS_PER_MINUTE = 60;

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private TimeFormatter() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Formats seconds as MM:SS.
     *
     * <p>
     * Examples:
     * </p>
     * <ul>
     * <li>90 seconds → "01:30"</li>
     * <li>3600 seconds → "60:00"</li>
     * <li>0 seconds → "00:00"</li>
     * </ul>
     *
     * @param totalSeconds the total number of seconds to format
     * @return the formatted string in MM:SS format
     * @throws IllegalArgumentException if totalSeconds is negative
     */
    public static String formatSeconds(int totalSeconds) {
        if (totalSeconds < 0) {
            throw new IllegalArgumentException("totalSeconds must not be negative, was: " + totalSeconds);
        }

        int minutes = totalSeconds / SECONDS_PER_MINUTE;
        int seconds = totalSeconds % SECONDS_PER_MINUTE;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Formats seconds as a human-readable duration string.
     *
     * <p>
     * Examples:
     * </p>
     * <ul>
     * <li>90 seconds → "1 min 30 sec"</li>
     * <li>3600 seconds → "60 min 0 sec"</li>
     * <li>45 seconds → "0 min 45 sec"</li>
     * </ul>
     *
     * @param totalSeconds the total number of seconds to format
     * @return the formatted string in human-readable format
     * @throws IllegalArgumentException if totalSeconds is negative
     */
    public static String formatSecondsReadable(int totalSeconds) {
        if (totalSeconds < 0) {
            throw new IllegalArgumentException("totalSeconds must not be negative, was: " + totalSeconds);
        }

        int minutes = totalSeconds / SECONDS_PER_MINUTE;
        int seconds = totalSeconds % SECONDS_PER_MINUTE;
        return String.format("%d min %d sec", minutes, seconds);
    }

    /**
     * Converts minutes to seconds.
     *
     * @param minutes the number of minutes
     * @return the equivalent number of seconds
     * @throws IllegalArgumentException if minutes is negative
     */
    public static int minutesToSeconds(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("minutes must not be negative, was: " + minutes);
        }
        return minutes * SECONDS_PER_MINUTE;
    }

    /**
     * Converts seconds to minutes (truncating any remainder).
     *
     * @param seconds the number of seconds
     * @return the number of complete minutes
     * @throws IllegalArgumentException if seconds is negative
     */
    public static int secondsToMinutes(int seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("seconds must not be negative, was: " + seconds);
        }
        return seconds / SECONDS_PER_MINUTE;
    }

    /**
     * Formats minutes as a duration string.
     *
     * <p>
     * Examples:
     * </p>
     * <ul>
     * <li>25 minutes → "25 minutes"</li>
     * <li>1 minute → "1 minute"</li>
     * </ul>
     *
     * @param minutes the number of minutes
     * @return the formatted string with proper pluralization
     * @throws IllegalArgumentException if minutes is negative
     */
    public static String formatMinutes(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("minutes must not be negative, was: " + minutes);
        }
        return minutes + (minutes == 1 ? " minute" : " minutes");
    }
}
