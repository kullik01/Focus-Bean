package io.github.kullik01.focusbean.view;

import io.github.kullik01.focusbean.model.SessionHistory;
import io.github.kullik01.focusbean.model.UserSettings;
import io.github.kullik01.focusbean.util.AppConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Displays daily progress statistics with circular goal indicator.
 *
 * <p>
 * Shows yesterday's completed time, the daily goal with circular progress,
 * current streak, and today's completed time. Matches the Windows Clock
 * Focus Sessions daily progress design.
 * </p>
 */
public final class DailyProgressView extends VBox {

    private static final double RING_SIZE = 140;
    private static final double RING_STROKE_WIDTH = 5;
    private static final String FONT_FAMILY = "'Segoe UI', 'Helvetica Neue', sans-serif";

    private final Label headerLabel;
    private final HBox headerBar;
    private Button settingsButton;

    private final Label yesterdayValueLabel;
    private final Label yesterdayUnitLabel;
    private final Label dailyGoalValueLabel;
    private final Label dailyGoalUnitLabel;
    private final Label streakValueLabel;
    private final Label streakUnitLabel;
    private final Label completedLabel;
    private final Canvas goalProgressCanvas;

    private int dailyGoalMinutes;
    private int completedTodayMinutes;
    private int yesterdayMinutes;
    private int streakDays;

    /**
     * Creates a new DailyProgressView with default values.
     */
    public DailyProgressView() {
        this.dailyGoalMinutes = UserSettings.DEFAULT_DAILY_GOAL_MINUTES;
        this.completedTodayMinutes = 0;
        this.yesterdayMinutes = 0;
        this.streakDays = 0;

        // Header
        headerLabel = new Label("Daily progress");
        headerLabel.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 14));
        headerLabel.setTextFill(Color.web(AppConstants.COLOR_TEXT_PRIMARY));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBar = new HBox();
        headerBar.setAlignment(Pos.CENTER_LEFT);
        headerBar.getChildren().addAll(headerLabel, spacer);

        // Stats row
        VBox yesterdayBox = createStatBox("Yesterday");
        yesterdayValueLabel = (Label) ((VBox) yesterdayBox).getChildren().get(1);
        yesterdayUnitLabel = (Label) ((VBox) yesterdayBox).getChildren().get(2);
        yesterdayValueLabel.setText("0");
        yesterdayUnitLabel.setText("minutes");

        // Daily goal with circular progress
        goalProgressCanvas = new Canvas(RING_SIZE, RING_SIZE);
        dailyGoalValueLabel = new Label(formatGoalDisplay(dailyGoalMinutes));
        dailyGoalValueLabel.setFont(Font.font(FONT_FAMILY, FontWeight.LIGHT, 34));
        dailyGoalValueLabel.setTextFill(Color.web(AppConstants.COLOR_TEXT_PRIMARY));

        dailyGoalUnitLabel = new Label(getGoalUnit(dailyGoalMinutes));
        dailyGoalUnitLabel.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 13));
        dailyGoalUnitLabel.setTextFill(Color.web(AppConstants.COLOR_TEXT_SECONDARY));

        VBox goalCenterContent = new VBox(0);
        goalCenterContent.setAlignment(Pos.CENTER);
        goalCenterContent.getChildren().addAll(
                createSmallLabel("Daily goal"),
                dailyGoalValueLabel,
                dailyGoalUnitLabel);

        StackPane goalContainer = new StackPane();
        goalContainer.setAlignment(Pos.CENTER);
        goalContainer.getChildren().addAll(goalProgressCanvas, goalCenterContent);

        VBox streakBox = createStatBox("Streak");
        streakValueLabel = (Label) ((VBox) streakBox).getChildren().get(1);
        streakUnitLabel = (Label) ((VBox) streakBox).getChildren().get(2);
        streakValueLabel.setText("0");
        streakUnitLabel.setText("days");

        HBox statsRow = new HBox(12);
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(35, 0, 0, 0));
        statsRow.getChildren().addAll(yesterdayBox, goalContainer, streakBox);

        // Completed today label
        completedLabel = new Label("Completed: 0 minutes");
        completedLabel.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 13));
        completedLabel.setTextFill(Color.web(AppConstants.COLOR_TEXT_SECONDARY));

        // Layout
        setSpacing(12);
        setPadding(new Insets(15, 20, 15, 20));
        setAlignment(Pos.TOP_CENTER);
        getChildren().addAll(headerBar, statsRow, completedLabel);

        // Initial render
        drawGoalProgress();
    }

    /**
     * Updates the view with data from the session history and settings.
     *
     * @param history  the session history
     * @param settings the user settings containing the daily goal
     */
    public void update(SessionHistory history, UserSettings settings) {
        if (history != null) {
            this.completedTodayMinutes = history.getTodaysTotalWorkMinutes();
            this.yesterdayMinutes = history.getYesterdaysTotalWorkMinutes();
            this.streakDays = history.getCurrentStreak();
        }
        if (settings != null) {
            this.dailyGoalMinutes = settings.getDailyGoalMinutes();
        }
        refresh();
    }

    /**
     * Sets the daily goal in minutes.
     *
     * @param dailyGoalMinutes the daily goal value
     */
    public void setDailyGoalMinutes(int dailyGoalMinutes) {
        this.dailyGoalMinutes = dailyGoalMinutes;
        refresh();
    }

    /**
     * Sets the completed minutes for today.
     *
     * @param minutes the completed minutes
     */
    public void setCompletedTodayMinutes(int minutes) {
        this.completedTodayMinutes = minutes;
        refresh();
    }

    /**
     * Sets the settings button to display in the header bar.
     * The button will be placed at the right end of the header.
     *
     * @param button the settings button to display
     */
    public void setSettingsButton(Button button) {
        if (this.settingsButton != null) {
            headerBar.getChildren().remove(this.settingsButton);
        }
        this.settingsButton = button;
        if (button != null) {
            headerBar.getChildren().add(button);
        }
    }

    /**
     * Refreshes all display elements with current data.
     */
    private void refresh() {
        // Yesterday
        yesterdayValueLabel.setText(String.valueOf(yesterdayMinutes));
        yesterdayUnitLabel.setText("minutes");

        // Daily goal
        dailyGoalValueLabel.setText(formatGoalDisplay(dailyGoalMinutes));
        dailyGoalUnitLabel.setText(getGoalUnit(dailyGoalMinutes));

        // Streak
        streakValueLabel.setText(String.valueOf(streakDays));
        streakUnitLabel.setText(streakDays == 1 ? "day" : "days");

        // Completed today
        completedLabel.setText("Completed: " + completedTodayMinutes + " minutes");

        // Redraw progress
        drawGoalProgress();
    }

    /**
     * Draws the circular goal progress ring.
     */
    private void drawGoalProgress() {
        GraphicsContext gc = goalProgressCanvas.getGraphicsContext2D();
        double width = goalProgressCanvas.getWidth();
        double height = goalProgressCanvas.getHeight();
        double centerX = width / 2;
        double centerY = height / 2;
        double radius = (Math.min(width, height) - RING_STROKE_WIDTH * 2) / 2;

        // Clear canvas
        gc.clearRect(0, 0, width, height);

        // Draw background ring
        gc.setStroke(Color.web(AppConstants.COLOR_PROGRESS_RING));
        gc.setLineWidth(RING_STROKE_WIDTH);
        gc.strokeOval(
                centerX - radius,
                centerY - radius,
                radius * 2,
                radius * 2);

        // Draw progress arc
        if (dailyGoalMinutes > 0) {
            double progress = Math.min(1.0, (double) completedTodayMinutes / dailyGoalMinutes);
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
        }
    }

    /**
     * Creates a stat box with title, value, and unit labels.
     *
     * @param title the stat title
     * @return the configured VBox
     */
    private VBox createStatBox(String title) {
        Label titleLabel = createSmallLabel(title);

        Label valueLabel = new Label("0");
        valueLabel.setFont(Font.font(FONT_FAMILY, FontWeight.LIGHT, 32));
        valueLabel.setTextFill(Color.web(AppConstants.COLOR_TEXT_PRIMARY));

        Label unitLabel = new Label("minutes");
        unitLabel.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 12));
        unitLabel.setTextFill(Color.web(AppConstants.COLOR_TEXT_SECONDARY));

        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(titleLabel, valueLabel, unitLabel);
        return box;
    }

    /**
     * Creates a small label for section titles.
     *
     * @param text the label text
     * @return the configured label
     */
    private Label createSmallLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 11));
        label.setTextFill(Color.web(AppConstants.COLOR_TEXT_SECONDARY));
        return label;
    }

    /**
     * Formats the goal value for display.
     *
     * @param minutes the goal in minutes
     * @return formatted display value
     */
    private String formatGoalDisplay(int minutes) {
        if (minutes >= 60 && minutes % 60 == 0) {
            return String.valueOf(minutes / 60);
        }
        return String.valueOf(minutes);
    }

    /**
     * Gets the appropriate unit label for the goal.
     *
     * @param minutes the goal in minutes
     * @return the unit string
     */
    private String getGoalUnit(int minutes) {
        if (minutes >= 60 && minutes % 60 == 0) {
            int hours = minutes / 60;
            return hours == 1 ? "hour" : "hours";
        }
        return minutes == 1 ? "minute" : "minutes";
    }
}
