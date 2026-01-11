package io.github.kullik01.focusbean.model;

/**
 * Represents the display mode for the session history view.
 *
 * <p>
 * Users can switch between a tabular view showing individual sessions
 * or a bar chart visualization showing aggregated daily work minutes.
 * </p>
 */
public enum HistoryViewMode {

    /**
     * Display sessions in a tabular format with individual entries.
     */
    TABLE("Table"),

    /**
     * Display sessions as a bar chart showing daily work minutes.
     */
    CHART("Chart");

    private final String displayName;

    /**
     * Constructs a HistoryViewMode with the given display name.
     *
     * @param displayName the human-readable name for this mode
     */
    HistoryViewMode(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the human-readable display name for this mode.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }
}
