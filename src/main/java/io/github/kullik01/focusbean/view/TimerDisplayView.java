package io.github.kullik01.focusbean.view;

import io.github.kullik01.focusbean.model.TimerState;
import io.github.kullik01.focusbean.util.AppConstants;
import io.github.kullik01.focusbean.util.TimeFormatter;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Displays the timer countdown and current state.
 *
 * <p>
 * This view component shows the remaining time in MM:SS format
 * and the current timer state (Ready, Working, Break, Paused).
 * The display updates automatically when bound to the controller properties.
 * </p>
 */
public final class TimerDisplayView extends StackPane {

    private static final String STYLE_TIME_LABEL = """
            -fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;
            -fx-font-size: 72px;
            -fx-font-weight: 300;
            -fx-text-fill: %s;
            """;

    private static final String STYLE_STATE_LABEL = """
            -fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;
            -fx-font-size: 18px;
            -fx-font-weight: 400;
            -fx-text-fill: %s;
            """;

    private static final String INITIAL_TIME_DISPLAY = "00:00";

    private final Label timeLabel;
    private final Label stateLabel;
    private final VBox container;

    /**
     * Creates a new TimerDisplayView with default styling.
     */
    public TimerDisplayView() {
        timeLabel = new Label(INITIAL_TIME_DISPLAY);
        timeLabel.setStyle(String.format(STYLE_TIME_LABEL, AppConstants.COLOR_TEXT_PRIMARY));

        stateLabel = new Label(TimerState.IDLE.getDisplayName());
        stateLabel.setStyle(String.format(STYLE_STATE_LABEL, AppConstants.COLOR_TEXT_SECONDARY));

        container = new VBox(10);
        container.setAlignment(Pos.CENTER);
        container.getChildren().addAll(stateLabel, timeLabel);

        setAlignment(Pos.CENTER);
        getChildren().add(container);

        // Set minimum size to prevent layout jumps
        setMinWidth(300);
        setMinHeight(150);
    }

    /**
     * Updates the displayed time.
     *
     * @param remainingSeconds the remaining seconds to display
     */
    public void updateTime(int remainingSeconds) {
        String formatted = TimeFormatter.formatSeconds(Math.max(0, remainingSeconds));
        timeLabel.setText(formatted);
    }

    /**
     * Updates the displayed state and adjusts styling accordingly.
     *
     * @param state the new timer state
     */
    public void updateState(TimerState state) {
        if (state == null) {
            state = TimerState.IDLE;
        }

        stateLabel.setText(state.getDisplayName());

        // Update background color based on state
        String backgroundColor = switch (state) {
            case WORK -> AppConstants.COLOR_WORK_BACKGROUND;
            case BREAK -> AppConstants.COLOR_BREAK_BACKGROUND;
            case PAUSED -> AppConstants.COLOR_PAUSED_BACKGROUND;
            case IDLE -> AppConstants.COLOR_IDLE_BACKGROUND;
        };

        setStyle("-fx-background-color: " + backgroundColor + ";");
    }

    /**
     * Updates the time display to show the configured duration.
     *
     * @param durationMinutes the duration in minutes
     */
    public void showDuration(int durationMinutes) {
        updateTime(durationMinutes * 60);
    }

    /**
     * Returns the time label for external styling.
     *
     * @return the time label
     */
    public Label getTimeLabel() {
        return timeLabel;
    }

    /**
     * Returns the state label for external styling.
     *
     * @return the state label
     */
    public Label getStateLabel() {
        return stateLabel;
    }
}
