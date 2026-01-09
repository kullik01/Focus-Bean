package io.github.kullik01.focusbean.view;

import io.github.kullik01.focusbean.model.TimerState;
import io.github.kullik01.focusbean.util.AppConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Displays the timer countdown with a circular progress ring and tick marks.
 *
 * <p>
 * This view component shows the remaining time in a stylized format (e.g., "20
 * min")
 * inside a circular progress indicator. The progress ring shows the
 * elapsed/remaining
 * time visually, matching the Windows Clock Focus Sessions design.
 * </p>
 */
public final class TimerDisplayView extends StackPane {

    private static final double RING_SIZE = 200;
    private static final double RING_STROKE_WIDTH = 4;
    private static final int TICK_COUNT = 60;
    private static final double TICK_LENGTH_MAJOR = 12;
    private static final double TICK_LENGTH_MINOR = 6;
    private static final double TICK_WIDTH = 2;

    private static final String FONT_FAMILY = "'Segoe UI', 'Helvetica Neue', sans-serif";

    private final Canvas progressCanvas;
    private final Label timeLabel;
    private final Label unitLabel;
    private final VBox centerContent;

    private int totalSeconds;
    private int remainingSeconds;
    private TimerState currentState;

    /**
     * Creates a new TimerDisplayView with default styling.
     */
    public TimerDisplayView() {
        this.totalSeconds = 25 * 60;
        this.remainingSeconds = totalSeconds;
        this.currentState = TimerState.IDLE;

        // Create the circular progress canvas
        progressCanvas = new Canvas(RING_SIZE, RING_SIZE);
        drawProgressRing();

        // Time display in center
        timeLabel = new Label("25");
        timeLabel.setFont(Font.font(FONT_FAMILY, FontWeight.LIGHT, 48));
        timeLabel.setTextFill(Color.web(AppConstants.COLOR_TEXT_PRIMARY));

        unitLabel = new Label("min");
        unitLabel.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 18));
        unitLabel.setTextFill(Color.web(AppConstants.COLOR_TEXT_SECONDARY));

        centerContent = new VBox(0);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.getChildren().addAll(timeLabel, unitLabel);

        // Stack the canvas and center content
        setAlignment(Pos.CENTER);
        setPadding(new Insets(10));
        getChildren().addAll(progressCanvas, centerContent);

        setMinWidth(RING_SIZE + 40);
        setMinHeight(RING_SIZE + 40);
    }

    /**
     * Updates the displayed time.
     *
     * @param remainingSeconds the remaining seconds to display
     */
    public void updateTime(int remainingSeconds) {
        this.remainingSeconds = Math.max(0, remainingSeconds);
        updateTimeDisplay();
        drawProgressRing();
    }

    /**
     * Sets the total duration for progress calculation.
     *
     * @param totalSeconds the total duration in seconds
     */
    public void setTotalSeconds(int totalSeconds) {
        this.totalSeconds = Math.max(1, totalSeconds);
        drawProgressRing();
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
        this.currentState = state;
        drawProgressRing();
    }

    /**
     * Updates the time display to show the configured duration.
     *
     * @param durationMinutes the duration in minutes
     */
    public void showDuration(int durationMinutes) {
        showDuration(durationMinutes, "min");
    }

    /**
     * Updates the time display to show the configured duration with a custom label.
     *
     * @param durationMinutes the duration in minutes
     * @param label           the label to display below the time (e.g., "min",
     *                        "Break")
     */
    public void showDuration(int durationMinutes, String label) {
        this.totalSeconds = durationMinutes * 60;
        this.remainingSeconds = totalSeconds;
        timeLabel.setText(String.valueOf(durationMinutes));
        unitLabel.setText(label);
        drawProgressRing();
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
     * For compatibility, returns the unit label.
     *
     * @return the unit label
     */
    public Label getStateLabel() {
        return unitLabel;
    }

    /**
     * Updates the time display labels based on remaining seconds.
     */
    private void updateTimeDisplay() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;

        if (remainingSeconds >= 60) {
            // Show minutes format
            timeLabel.setText(String.valueOf(minutes));
            unitLabel.setText("min");
        } else {
            // Show seconds format when under a minute
            timeLabel.setText(String.valueOf(seconds));
            unitLabel.setText("sec");
        }
    }

    /**
     * Draws the circular progress ring with tick marks.
     */
    private void drawProgressRing() {
        GraphicsContext gc = progressCanvas.getGraphicsContext2D();
        double width = progressCanvas.getWidth();
        double height = progressCanvas.getHeight();
        double centerX = width / 2;
        double centerY = height / 2;
        double radius = (Math.min(width, height) - RING_STROKE_WIDTH * 2 - TICK_LENGTH_MAJOR * 2) / 2;

        // Clear canvas
        gc.clearRect(0, 0, width, height);

        // Draw tick marks positioned inside the ring, extending toward center
        drawTickMarks(gc, centerX, centerY, radius - RING_STROKE_WIDTH);

        // Draw background ring
        gc.setStroke(Color.web(AppConstants.COLOR_PROGRESS_RING));
        gc.setLineWidth(RING_STROKE_WIDTH);
        gc.strokeOval(
                centerX - radius,
                centerY - radius,
                radius * 2,
                radius * 2);

        // Draw progress arc (only when timer is active)
        if (currentState != TimerState.IDLE && totalSeconds > 0) {
            double progress = 1.0 - ((double) remainingSeconds / totalSeconds);
            double sweepAngle = progress * 360;

            gc.setStroke(Color.web(AppConstants.COLOR_PROGRESS_ACTIVE));
            gc.setLineWidth(RING_STROKE_WIDTH);
            gc.strokeArc(
                    centerX - radius,
                    centerY - radius,
                    radius * 2,
                    radius * 2,
                    90,
                    -sweepAngle,
                    javafx.scene.shape.ArcType.OPEN);

            // Draw progress indicator dot
            drawProgressIndicator(gc, centerX, centerY, radius, progress);
        }
    }

    /**
     * Draws the tick marks around the progress ring.
     *
     * @param gc      the graphics context
     * @param centerX the center X coordinate
     * @param centerY the center Y coordinate
     * @param radius  the radius for tick mark placement
     */
    private void drawTickMarks(GraphicsContext gc, double centerX, double centerY, double radius) {
        gc.setStroke(Color.web(AppConstants.COLOR_TICK_MARK));
        gc.setLineWidth(TICK_WIDTH);

        for (int i = 0; i < TICK_COUNT; i++) {
            double angle = Math.toRadians(i * (360.0 / TICK_COUNT) - 90);
            boolean isMajor = (i % 5 == 0);
            double tickLength = isMajor ? TICK_LENGTH_MAJOR : TICK_LENGTH_MINOR;

            // Tick marks now point inward: start from outer edge and extend toward center
            double outerRadius = radius;
            double innerRadius = radius - tickLength;

            double x1 = centerX + outerRadius * Math.cos(angle);
            double y1 = centerY + outerRadius * Math.sin(angle);
            double x2 = centerX + innerRadius * Math.cos(angle);
            double y2 = centerY + innerRadius * Math.sin(angle);

            gc.strokeLine(x1, y1, x2, y2);
        }
    }

    /**
     * Draws the progress indicator dot at the current position.
     *
     * @param gc       the graphics context
     * @param centerX  the center X coordinate
     * @param centerY  the center Y coordinate
     * @param radius   the radius of the progress ring
     * @param progress the current progress (0.0 to 1.0)
     */
    private void drawProgressIndicator(GraphicsContext gc, double centerX, double centerY,
            double radius, double progress) {
        double angle = Math.toRadians(progress * 360 - 90);
        double indicatorX = centerX + radius * Math.cos(angle);
        double indicatorY = centerY + radius * Math.sin(angle);
        double indicatorRadius = 8;

        gc.setFill(Color.web(AppConstants.COLOR_PROGRESS_ACTIVE));
        gc.fillOval(
                indicatorX - indicatorRadius,
                indicatorY - indicatorRadius,
                indicatorRadius * 2,
                indicatorRadius * 2);
    }
}
