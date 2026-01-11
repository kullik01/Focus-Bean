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
package io.github.kullik01.focusbean.util;

/**
 * Application-wide constants for Focus Bean.
 *
 * <p>
 * This class centralizes magic numbers, string literals, and configuration
 * values to improve maintainability and prevent inconsistencies.
 * </p>
 */
public final class AppConstants {

    /**
     * Private constructor to prevent instantiation of this constants class.
     */
    private AppConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    // =========================================================================
    // Application Metadata
    // =========================================================================

    /** The application name displayed in the window title. */
    public static final String APP_NAME = "Focus Bean";

    /** The application version string. */
    public static final String APP_VERSION = "1.0.0";

    /** The application data directory name (used under %APPDATA%). */
    public static final String APP_DATA_DIR_NAME = "FocusBean";

    /** The filename for persisted session data. */
    public static final String SESSION_HISTORY_FILENAME = "session_history.json";

    // =========================================================================
    // Window Dimensions
    // =========================================================================

    /** Default window width in pixels. */
    public static final int DEFAULT_WINDOW_WIDTH = 820;

    /** Default window height in pixels. */
    public static final int DEFAULT_WINDOW_HEIGHT = 420;

    /** Minimum window width in pixels. */
    public static final int MIN_WINDOW_WIDTH = 700;

    /** Minimum window height in pixels. */
    public static final int MIN_WINDOW_HEIGHT = 380;

    // =========================================================================
    // Timer Configuration
    // =========================================================================

    /** Timer tick interval in milliseconds (1 second). */
    public static final int TIMER_TICK_INTERVAL_MS = 1000;

    /** Animation duration for state transitions in milliseconds. */
    public static final int STATE_TRANSITION_DURATION_MS = 300;

    // =========================================================================
    // UI Labels
    // =========================================================================

    /** Label for the start button. */
    public static final String LABEL_START = "Start";

    /** Label for the pause button. */
    public static final String LABEL_PAUSE = "Pause";

    /** Label for the resume button. */
    public static final String LABEL_RESUME = "Resume";

    /** Label for the reset button. */
    public static final String LABEL_RESET = "Reset";

    /** Label for the skip button. */
    public static final String LABEL_SKIP = "Skip";

    /** Label for the settings button/menu. */
    public static final String LABEL_SETTINGS = "Settings";

    /** Label for the history button/tab. */
    public static final String LABEL_HISTORY = "History";

    /** Label for the clear history button. */
    public static final String LABEL_CLEAR_HISTORY = "Clear History";

    // =========================================================================
    // Color Scheme (CSS Color Values)
    // =========================================================================

    /** Background color for work state (warm caramel). */
    public static final String COLOR_WORK_BACKGROUND = "#E6A779";

    /** Background color for break state (soft green). */
    public static final String COLOR_BREAK_BACKGROUND = "#55efc4";

    /** Background color for idle state (neutral gray). */
    public static final String COLOR_IDLE_BACKGROUND = "#dfe6e9";

    /** Background color for paused state (soft yellow). */
    public static final String COLOR_PAUSED_BACKGROUND = "#ffeaa7";

    /** Primary text color (dark gray). */
    public static final String COLOR_TEXT_PRIMARY = "#2d3436";

    /** Secondary text color (medium gray). */
    public static final String COLOR_TEXT_SECONDARY = "#636e72";

    /** Accent color for buttons and highlights (Coffee Brown). */
    public static final String COLOR_ACCENT = "#A0522D";

    // =========================================================================
    // Logging
    // =========================================================================

    /** Logger name prefix for the application. */
    public static final String LOGGER_PREFIX = "io.github.kullik01.focusbean";

    // =========================================================================
    // Card UI Colors (Modern Design)
    // =========================================================================

    /** Background color for cards (white). */
    public static final String COLOR_CARD_BACKGROUND = "#FFFFFF";

    /** Border color for cards (warm beige). */
    public static final String COLOR_CARD_BORDER = "#E5DCD3";

    /** Window background color (warm off-white). */
    public static final String COLOR_WINDOW_BACKGROUND = "#F5F2EF";

    /** Progress ring inactive color. */
    public static final String COLOR_PROGRESS_RING = "#E0E0E0";

    /** Progress ring active color (Coffee Brown). */
    public static final String COLOR_PROGRESS_ACTIVE = "#8B5A2B";

    /** Tick mark color for timer display. */
    public static final String COLOR_TICK_MARK = "#C0C0C0";
}
