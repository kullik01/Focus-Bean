package io.github.kullik01.focusbean.model;

import java.util.Objects;

/**
 * Enumeration of available notification sounds.
 *
 * <p>
 * This enum defines the built-in notification sounds that users can choose
 * from when a timer session completes. The {@link #CUSTOM} option allows
 * users to specify their own sound file.
 * </p>
 */
public enum NotificationSound {

    /**
     * A pleasant chime sound (default).
     */
    CHIME("Chime", "/io/github/kullik01/focusbean/sounds/chime.wav"),

    /**
     * A bell ring sound.
     */
    BELL("Bell", "/io/github/kullik01/focusbean/sounds/bell.wav"),

    /**
     * A simple ding sound.
     */
    DING("Ding", "/io/github/kullik01/focusbean/sounds/ding.wav"),

    /**
     * A soft notification sound.
     */
    SOFT("Soft", "/io/github/kullik01/focusbean/sounds/soft.wav"),

    /**
     * System beep (no audio file, uses AWT beep).
     */
    SYSTEM_BEEP("System Beep", null),

    /**
     * No sound.
     */
    NONE("None", null),

    /**
     * Custom sound file selected by the user.
     */
    CUSTOM("Custom...", null);

    private final String displayName;
    private final String resourcePath;

    /**
     * Creates a NotificationSound with the specified display name and resource
     * path.
     *
     * @param displayName  the human-readable name for UI display
     * @param resourcePath the classpath resource path, or null for special handling
     */
    NotificationSound(String displayName, String resourcePath) {
        this.displayName = Objects.requireNonNull(displayName, "displayName must not be null");
        this.resourcePath = resourcePath;
    }

    /**
     * Returns the human-readable display name.
     *
     * @return the display name for UI purposes
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the classpath resource path for the sound file.
     *
     * @return the resource path, or null if this sound uses special handling
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * Checks if this sound option has an associated audio file.
     *
     * @return true if this sound has a playable resource file
     */
    public boolean hasAudioFile() {
        return resourcePath != null;
    }

    /**
     * Checks if this is the custom sound option.
     *
     * @return true if this is the CUSTOM option
     */
    public boolean isCustom() {
        return this == CUSTOM;
    }

    /**
     * Checks if this is a silent option (no sound).
     *
     * @return true if this is the NONE option
     */
    public boolean isSilent() {
        return this == NONE;
    }

    /**
     * Checks if this uses the system beep.
     *
     * @return true if this is the SYSTEM_BEEP option
     */
    public boolean isSystemBeep() {
        return this == SYSTEM_BEEP;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
