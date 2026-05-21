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
import javafx.scene.shape.ArcType;
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

    /** The size of the progress ring canvas. */
    private static final double RING_SIZE = 200;
    /** The stroke width of the progress ring. */
    private static final double RING_STROKE_WIDTH = 4;
    /** The number of tick marks around the ring. */
    private static final int TICK_COUNT = 60;
    /** The length of major tick marks. */
    private static final double TICK_LENGTH_MAJOR = 12;
    /** The length of minor tick marks. */
    private static final double TICK_LENGTH_MINOR = 6;
    /** The width of the tick marks. */
    private static final double TICK_WIDTH = 2;

    /** The font family used for the timer display. */
    private static final String FONT_FAMILY = "'Segoe UI', 'Helvetica Neue', sans-serif";

    /** The canvas used to draw the progress ring and ticks. */
    private final Canvas progressCanvas;
    /** The label displaying the time remaining. */
    private final Label timeLabel;
    /** The label displaying the unit (e.g., "min", "sec"). */
    private final Label unitLabel;
    /** The container for the labels in the center of the ring. */
    private final VBox centerContent;

    /** The total seconds for the current session. */
    private int totalSeconds;
    /** The remaining seconds in the current session. */
    private int remainingSeconds;
    /** The current state of the timer. */
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
        if (remainingSeconds >= 60) {
            // Show ceiling-rounded minutes (e.g., 119 sec = 2 min, 60 sec = 1 min)
            int displayMinutes = (remainingSeconds + 59) / 60;
            timeLabel.setText(String.valueOf(displayMinutes));
            unitLabel.setText("min");
        } else if (remainingSeconds >= 10) {
            // Show SS format for 10-59 seconds
            timeLabel.setText(String.format("%02d", remainingSeconds));
            unitLabel.setText("sec");
        } else {
            // Show S format for last 9 seconds
            timeLabel.setText(String.valueOf(remainingSeconds));
            unitLabel.setText("sec");
        }
    }

    /**
     * Draws the circular progress ring with tick marks.
     */
    private void drawProgressRing() {
        GraphicsContext graphicsContext = progressCanvas.getGraphicsContext2D();
        double width = progressCanvas.getWidth();
        double height = progressCanvas.getHeight();
        double centerX = width / 2;
        double centerY = height / 2;
        double radius = (Math.min(width, height) - RING_STROKE_WIDTH * 2 - TICK_LENGTH_MAJOR * 2) / 2;

        // Clear canvas
        graphicsContext.clearRect(0, 0, width, height);

        // Draw tick marks positioned inside the ring, extending toward center
        drawTickMarks(graphicsContext, centerX, centerY, radius - RING_STROKE_WIDTH);

        // Draw background ring
        graphicsContext.setStroke(Color.web(AppConstants.COLOR_PROGRESS_RING));
        graphicsContext.setLineWidth(RING_STROKE_WIDTH);
        graphicsContext.strokeOval(
                centerX - radius,
                centerY - radius,
                radius * 2,
                radius * 2);

        // Draw progress arc (only when timer is active)
        if (currentState != TimerState.IDLE && totalSeconds > 0) {
            double progress = 1.0 - ((double) remainingSeconds / totalSeconds);
            double sweepAngle = progress * 360;

            graphicsContext.setStroke(Color.web(AppConstants.COLOR_PROGRESS_ACTIVE));
            graphicsContext.setLineWidth(RING_STROKE_WIDTH);
            graphicsContext.strokeArc(
                    centerX - radius,
                    centerY - radius,
                    radius * 2,
                    radius * 2,
                    90,
                    -sweepAngle,
                    ArcType.OPEN);

            // Draw progress indicator dot
            drawProgressIndicator(graphicsContext, centerX, centerY, radius, progress);
        }
    }

    /**
     * Draws the tick marks around the progress ring.
     *
     * @param graphicsContext the graphics context
     * @param centerX         the center X coordinate
     * @param centerY         the center Y coordinate
     * @param radius          the radius for tick mark placement
     */
    private void drawTickMarks(GraphicsContext graphicsContext, double centerX, double centerY, double radius) {
        graphicsContext.setStroke(Color.web(AppConstants.COLOR_TICK_MARK));
        graphicsContext.setLineWidth(TICK_WIDTH);

        for (int i = 0; i < TICK_COUNT; i++) {
            double angle = Math.toRadians(i * (360.0 / TICK_COUNT) - 90);
            boolean isMajor = (i % 5 == 0);
            double tickLength = isMajor ? TICK_LENGTH_MAJOR : TICK_LENGTH_MINOR;

            // Tick marks now point inward: start from outer edge and extend toward center
            double outerRadius = radius;
            double innerRadius = radius - tickLength;

            double startX = centerX + outerRadius * Math.cos(angle);
            double startY = centerY + outerRadius * Math.sin(angle);
            double endX = centerX + innerRadius * Math.cos(angle);
            double endY = centerY + innerRadius * Math.sin(angle);

            graphicsContext.strokeLine(startX, startY, endX, endY);
        }
    }

    /**
     * Draws the progress indicator dot at the current position.
     *
     * @param graphicsContext the graphics context
     * @param centerX         the center X coordinate
     * @param centerY         the center Y coordinate
     * @param radius          the radius of the progress ring
     * @param progress        the current progress (0.0 to 1.0)
     */
    private void drawProgressIndicator(GraphicsContext graphicsContext, double centerX, double centerY,
            double radius, double progress) {
        double angle = Math.toRadians(progress * 360 - 90);
        double indicatorX = centerX + radius * Math.cos(angle);
        double indicatorY = centerY + radius * Math.sin(angle);
        double indicatorRadius = 8;

        graphicsContext.setFill(Color.web(AppConstants.COLOR_PROGRESS_ACTIVE));
        graphicsContext.fillOval(
                indicatorX - indicatorRadius,
                indicatorY - indicatorRadius,
                indicatorRadius * 2,
                indicatorRadius * 2);
    }
}
