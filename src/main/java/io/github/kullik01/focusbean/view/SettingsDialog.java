package io.github.kullik01.focusbean.view;

import io.github.kullik01.focusbean.model.UserSettings;
import io.github.kullik01.focusbean.util.AppConstants;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;

import java.util.Objects;
import java.util.Optional;

/**
 * Dialog for configuring timer settings (work, break durations, and daily
 * goal).
 *
 * <p>
 * Displays spinners for adjusting work duration, break duration, and daily goal
 * in minutes. Returns the new settings if the user confirms, or empty if
 * cancelled.
 * </p>
 */
public final class SettingsDialog extends Dialog<UserSettings> {

    private static final String STYLE_LABEL = """
            -fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;
            -fx-font-size: 14px;
            """;

    private final Spinner<Integer> workSpinner;
    private final Spinner<Integer> breakSpinner;
    private final Spinner<Integer> dailyGoalSpinner;

    /**
     * Creates a new SettingsDialog with the current settings.
     *
     * @param currentSettings the current user settings to display
     * @throws NullPointerException if currentSettings is null
     */
    public SettingsDialog(UserSettings currentSettings) {
        Objects.requireNonNull(currentSettings, "currentSettings must not be null");

        setTitle(AppConstants.LABEL_SETTINGS);
        setHeaderText("Configure Timer Settings");

        // Create spinners
        workSpinner = new Spinner<>();
        workSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                UserSettings.MIN_DURATION_MINUTES,
                UserSettings.MAX_WORK_DURATION_MINUTES,
                currentSettings.getWorkDurationMinutes()));
        workSpinner.setEditable(true);
        workSpinner.setPrefWidth(100);

        breakSpinner = new Spinner<>();
        breakSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                UserSettings.MIN_DURATION_MINUTES,
                UserSettings.MAX_BREAK_DURATION_MINUTES,
                currentSettings.getBreakDurationMinutes()));
        breakSpinner.setEditable(true);
        breakSpinner.setPrefWidth(100);

        dailyGoalSpinner = new Spinner<>();
        dailyGoalSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                UserSettings.MIN_DURATION_MINUTES,
                UserSettings.MAX_DAILY_GOAL_MINUTES,
                currentSettings.getDailyGoalMinutes()));
        dailyGoalSpinner.setEditable(true);
        dailyGoalSpinner.setPrefWidth(100);

        // Create labels
        Label workLabel = new Label("Work Duration (minutes):");
        workLabel.setStyle(STYLE_LABEL);

        Label breakLabel = new Label("Break Duration (minutes):");
        breakLabel.setStyle(STYLE_LABEL);

        Label dailyGoalLabel = new Label("Daily Goal (minutes):");
        dailyGoalLabel.setStyle(STYLE_LABEL);

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        grid.add(workLabel, 0, 0);
        grid.add(workSpinner, 1, 0);
        grid.add(breakLabel, 0, 1);
        grid.add(breakSpinner, 1, 1);
        grid.add(dailyGoalLabel, 0, 2);
        grid.add(dailyGoalSpinner, 1, 2);

        getDialogPane().setContent(grid);

        // Buttons
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Result converter
        setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                return new UserSettings(
                        workSpinner.getValue(),
                        breakSpinner.getValue(),
                        dailyGoalSpinner.getValue());
            }
            return null;
        });
    }

    /**
     * Shows the dialog and returns the new settings if confirmed.
     *
     * @return the new settings wrapped in Optional, or empty if cancelled
     */
    public Optional<UserSettings> showAndGetResult() {
        return showAndWait();
    }

    /**
     * Returns the work duration spinner for testing purposes.
     *
     * @return the work duration spinner
     */
    public Spinner<Integer> getWorkSpinner() {
        return workSpinner;
    }

    /**
     * Returns the break duration spinner for testing purposes.
     *
     * @return the break duration spinner
     */
    public Spinner<Integer> getBreakSpinner() {
        return breakSpinner;
    }

    /**
     * Returns the daily goal spinner for testing purposes.
     *
     * @return the daily goal spinner
     */
    public Spinner<Integer> getDailyGoalSpinner() {
        return dailyGoalSpinner;
    }
}
