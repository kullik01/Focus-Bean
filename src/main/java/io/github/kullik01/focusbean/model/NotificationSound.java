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
