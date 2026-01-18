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

import io.github.kullik01.focusbean.model.NotificationSound;
import io.github.kullik01.focusbean.model.UserSettings;
import io.github.kullik01.focusbean.service.NotificationService;
import io.github.kullik01.focusbean.util.AppConstants;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;

import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

/**
 * Dialog for configuring timer settings (work, break durations, daily goal,
 * and notifications).
 *
 * <p>
 * Displays spinners for adjusting work duration, break duration, and daily goal
 * in minutes. Also provides notification settings including sound selection
 * and popup toggle. Returns the new settings if the user confirms, or empty if
 * cancelled.
 * </p>
 */
public final class SettingsDialog extends Dialog<UserSettings> {

    private static final String STYLE_LABEL = """
            -fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;
            -fx-font-size: 14px;
            """;

    private TextField workField;
    private TextField breakField;
    private TextField dailyGoalField;
    private final CheckBox soundNotificationCheckbox;
    private final CheckBox popupNotificationCheckbox;
    private final ComboBox<NotificationSound> soundComboBox;
    private final TextField customSoundPathField;
    private final Button browseButton;
    private final Button previewButton;

    private final NotificationService notificationService;
    private String customSoundPath;

    /**
     * Creates a new SettingsDialog with the current settings.
     *
     * @param currentSettings     the current user settings to display
     * @param notificationService the notification service for sound preview
     * @throws NullPointerException if currentSettings or notificationService is
     *                              null
     */
    public SettingsDialog(UserSettings currentSettings, NotificationService notificationService) {
        Objects.requireNonNull(currentSettings, "currentSettings must not be null");
        Objects.requireNonNull(notificationService, "notificationService must not be null");

        this.notificationService = notificationService;
        this.customSoundPath = currentSettings.getCustomSoundPath();

        setTitle(AppConstants.LABEL_SETTINGS);
        setHeaderText("Configure Timer Settings");

        // Create text fields
        workField = new TextField();
        VBox workBox = createValidatedTextField(
                UserSettings.MIN_DURATION_MINUTES,
                UserSettings.MAX_WORK_DURATION_MINUTES,
                currentSettings.getWorkDurationMinutes(),
                workField);

        breakField = new TextField();
        VBox breakBox = createValidatedTextField(
                UserSettings.MIN_DURATION_MINUTES,
                UserSettings.MAX_BREAK_DURATION_MINUTES,
                currentSettings.getBreakDurationMinutes(),
                breakField);

        dailyGoalField = new TextField();
        VBox dailyGoalBox = createValidatedTextField(
                UserSettings.MIN_DURATION_MINUTES,
                UserSettings.MAX_DAILY_GOAL_MINUTES,
                currentSettings.getDailyGoalMinutes(),
                dailyGoalField);

        // Create labels
        Label workLabel = new Label("Work Duration (min):");
        workLabel.setStyle(STYLE_LABEL);

        Label breakLabel = new Label("Break Duration (min):");
        breakLabel.setStyle(STYLE_LABEL);

        Label dailyGoalLabel = new Label("Daily Goal (min):");
        dailyGoalLabel.setStyle(STYLE_LABEL);

        // Notification settings
        soundNotificationCheckbox = new CheckBox("Enable sound notification");
        soundNotificationCheckbox.setSelected(currentSettings.isSoundNotificationEnabled());
        soundNotificationCheckbox.setStyle(STYLE_LABEL);

        popupNotificationCheckbox = new CheckBox("Show notification");
        popupNotificationCheckbox.setSelected(currentSettings.isPopupNotificationEnabled());
        popupNotificationCheckbox.setStyle(STYLE_LABEL);

        // Sound selection
        Label soundLabel = new Label("Notification Sound:");
        soundLabel.setStyle(STYLE_LABEL);

        // Sound selection - only System Beep and Custom options work reliably
        soundComboBox = new ComboBox<>(FXCollections.observableArrayList(
                NotificationSound.SYSTEM_BEEP,
                NotificationSound.CUSTOM));
        NotificationSound currentSound = currentSettings.getNotificationSound();
        // Ensure value is one of the available options, default to SYSTEM_BEEP if not
        if (currentSound == NotificationSound.SYSTEM_BEEP || currentSound == NotificationSound.CUSTOM) {
            soundComboBox.setValue(currentSound);
        } else {
            soundComboBox.setValue(NotificationSound.SYSTEM_BEEP);
        }
        soundComboBox.setPrefWidth(150);

        // Create a clean play icon using SVG
        javafx.scene.shape.SVGPath playIcon = new javafx.scene.shape.SVGPath();
        playIcon.setContent("M8 5v14l11-7z");
        playIcon.setFill(javafx.scene.paint.Color.web(AppConstants.COLOR_PROGRESS_ACTIVE));
        playIcon.setScaleX(0.85);
        playIcon.setScaleY(0.85);

        previewButton = new Button();
        previewButton.setGraphic(playIcon);
        previewButton.setStyle("""
                -fx-background-color: transparent;
                -fx-cursor: hand;
                -fx-padding: 2 6 2 6;
                """);

        // Add tooltip with warm colors matching the GUI design
        Tooltip playTooltip = new Tooltip("Play");
        playTooltip.setShowDelay(new javafx.util.Duration(0));
        playTooltip.setStyle(String.format("""
                -fx-font-family: 'Segoe UI', sans-serif;
                -fx-font-size: 12px;
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-background-radius: 6;
                -fx-padding: 6 10 6 10;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 1);
                """, AppConstants.COLOR_CARD_BACKGROUND, AppConstants.COLOR_TEXT_PRIMARY));
        previewButton.setTooltip(playTooltip);

        previewButton.setOnMouseEntered(e -> previewButton.setStyle("""
                -fx-background-color: rgba(160, 82, 45, 0.10);
                -fx-background-radius: 6;
                -fx-cursor: hand;
                -fx-padding: 2 6 2 6;
                """));

        previewButton.setOnMouseExited(e -> previewButton.setStyle("""
                -fx-background-color: transparent;
                -fx-cursor: hand;
                -fx-padding: 2 6 2 6;
                """));

        previewButton.setOnAction(e -> previewCurrentSound());

        // Custom sound path
        customSoundPathField = new TextField();
        customSoundPathField.setPromptText("Select custom sound file...");
        customSoundPathField.setPrefWidth(150);
        customSoundPathField.setEditable(false);
        if (customSoundPath != null) {
            customSoundPathField.setText(new File(customSoundPath).getName());
        }

        browseButton = new Button("📂"); // Folder icon
        browseButton.setStyle(String.format("""
                -fx-background-color: transparent;
                -fx-cursor: hand;
                -fx-font-size: 14px;
                -fx-padding: 2 6 2 6;
                -fx-text-fill: %s;
                """, AppConstants.COLOR_PROGRESS_ACTIVE));

        // Add tooltip with warm colors matching the GUI design
        Tooltip browseTooltip = new Tooltip("Browse");
        browseTooltip.setShowDelay(new javafx.util.Duration(0));
        browseTooltip.setStyle(String.format("""
                -fx-font-family: 'Segoe UI', sans-serif;
                -fx-font-size: 12px;
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-background-radius: 6;
                -fx-padding: 6 10 6 10;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 1);
                """, AppConstants.COLOR_CARD_BACKGROUND, AppConstants.COLOR_TEXT_PRIMARY));
        browseButton.setTooltip(browseTooltip);

        browseButton.setOnMouseEntered(e -> browseButton.setStyle(String.format("""
                -fx-background-color: rgba(160, 82, 45, 0.10);
                -fx-background-radius: 6;
                -fx-cursor: hand;
                -fx-font-size: 14px;
                -fx-padding: 2 6 2 6;
                -fx-text-fill: %s;
                """, AppConstants.COLOR_PROGRESS_ACTIVE)));

        browseButton.setOnMouseExited(e -> browseButton.setStyle(String.format("""
                -fx-background-color: transparent;
                -fx-cursor: hand;
                -fx-font-size: 14px;
                -fx-padding: 2 6 2 6;
                -fx-text-fill: %s;
                """, AppConstants.COLOR_PROGRESS_ACTIVE)));

        browseButton.setOnAction(e -> browseForCustomSound());

        // Custom sound row - only visible when CUSTOM is selected
        HBox customSoundRow = new HBox(10, customSoundPathField, browseButton);
        customSoundRow.visibleProperty().bind(
                soundComboBox.valueProperty().isEqualTo(NotificationSound.CUSTOM));
        customSoundRow.managedProperty().bind(customSoundRow.visibleProperty());

        // Sound selection row
        HBox soundRow = new HBox(10, soundComboBox, previewButton);

        // Enable/disable sound controls based on checkbox
        soundComboBox.disableProperty().bind(soundNotificationCheckbox.selectedProperty().not());
        previewButton.disableProperty().bind(soundNotificationCheckbox.selectedProperty().not());
        customSoundPathField.disableProperty().bind(soundNotificationCheckbox.selectedProperty().not());
        browseButton.disableProperty().bind(soundNotificationCheckbox.selectedProperty().not());

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        int row = 0;
        grid.add(new Label("Timer"), 0, row++, 2, 1);
        grid.add(workLabel, 0, row);
        grid.add(workBox, 1, row++);
        grid.add(breakLabel, 0, row);
        grid.add(breakBox, 1, row++);
        grid.add(dailyGoalLabel, 0, row);
        grid.add(dailyGoalBox, 1, row++);

        grid.add(new Label(""), 0, row++); // Spacer

        grid.add(new Label("Notification"), 0, row++, 2, 1);
        grid.add(soundNotificationCheckbox, 0, row++, 2, 1);
        grid.add(soundLabel, 0, row);
        grid.add(soundRow, 1, row++);
        grid.add(new Label(""), 0, row);
        grid.add(customSoundRow, 1, row++);
        grid.add(popupNotificationCheckbox, 0, row++, 2, 1);

        getDialogPane().setContent(grid);

        // Buttons
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Result converter
        setResultConverter(buttonType -> {
            // Stop sound if playing when closing
            if (isPreviewPlaying) {
                notificationService.stopSound();
            }
            if (buttonType == saveButtonType) {
                return new UserSettings(
                        Integer.parseInt(workField.getText()),
                        Integer.parseInt(breakField.getText()),
                        Integer.parseInt(dailyGoalField.getText()),
                        soundNotificationCheckbox.isSelected(),
                        popupNotificationCheckbox.isSelected(),
                        soundComboBox.getValue(),
                        soundComboBox.getValue() == NotificationSound.CUSTOM ? customSoundPath : null);
            }
            return null;
        });
    }

    private boolean isPreviewPlaying = false;

    /**
     * Legacy constructor for backward compatibility.
     *
     * @param currentSettings the current user settings
     * @deprecated Use {@link #SettingsDialog(UserSettings, NotificationService)}
     *             instead
     */
    @Deprecated
    public SettingsDialog(UserSettings currentSettings) {
        this(currentSettings, new NotificationService());
    }

    /**
     * Previews the currently selected sound.
     */
    private void previewCurrentSound() {
        if (isPreviewPlaying) {
            // Stop logic
            notificationService.stopSound();
            updatePreviewButtonState(false);
        } else {
            // Play logic
            NotificationSound sound = soundComboBox.getValue();
            String path = (sound == NotificationSound.CUSTOM) ? customSoundPath : null;

            updatePreviewButtonState(true);
            notificationService.previewSound(sound, path, () -> {
                updatePreviewButtonState(false);
            });
        }
    }

    private void updatePreviewButtonState(boolean playing) {
        isPreviewPlaying = playing;
        javafx.scene.shape.SVGPath icon = (javafx.scene.shape.SVGPath) previewButton.getGraphic();
        if (playing) {
            icon.setContent("M6 6h12v12H6z"); // Stop icon (square)
            previewButton.getTooltip().setText("Stop");
        } else {
            icon.setContent("M8 5v14l11-7z"); // Play icon
            previewButton.getTooltip().setText("Play");
        }
    }

    /**
     * Opens a file chooser to select a custom sound file.
     */
    private void browseForCustomSound() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Notification Sound");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aiff"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        if (customSoundPath != null) {
            File current = new File(customSoundPath);
            if (current.getParentFile() != null && current.getParentFile().exists()) {
                fileChooser.setInitialDirectory(current.getParentFile());
            }
        }

        File selectedFile = fileChooser.showOpenDialog(getDialogPane().getScene().getWindow());
        if (selectedFile != null) {
            customSoundPath = selectedFile.getAbsolutePath();
            customSoundPathField.setText(selectedFile.getName());
        }
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
     * Returns the work duration text field for testing purposes.
     *
     * @return the work duration text field
     */
    public TextField getWorkField() {
        return workField;
    }

    /**
     * Returns the break duration text field for testing purposes.
     *
     * @return the break duration text field
     */
    public TextField getBreakField() {
        return breakField;
    }

    /**
     * Returns the daily goal text field for testing purposes.
     *
     * @return the daily goal text field
     */
    public TextField getDailyGoalField() {
        return dailyGoalField;
    }

    /**
     * Returns the sound notification checkbox for testing purposes.
     *
     * @return the sound notification checkbox
     */
    public CheckBox getSoundNotificationCheckbox() {
        return soundNotificationCheckbox;
    }

    /**
     * Returns the popup notification checkbox for testing purposes.
     *
     * @return the popup notification checkbox
     */
    public CheckBox getPopupNotificationCheckbox() {
        return popupNotificationCheckbox;
    }

    /**
     * Returns the sound selection combo box for testing purposes.
     *
     * @return the sound combo box
     */
    public ComboBox<NotificationSound> getSoundComboBox() {
        return soundComboBox;
    }

    /**
     * Creates a styled text field with numeric-only input restriction and
     * visual validation.
     *
     * @param min        minimum value
     * @param logicalMax the logical maximum value for validation (e.g., 900)
     * @param initial    initial value
     * @param textField  the text field to configure (must be non-null)
     * @return the container holding the text field and error message
     */
    private javafx.scene.layout.VBox createValidatedTextField(int min, int logicalMax, int initial,
            TextField textField) {
        textField.setText(String.valueOf(initial));
        textField.setPrefWidth(40);

        // Error label - ensure it wraps and fits
        Label errorLabel = new Label("Value cannot exceed " + logicalMax + " minutes!");
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 10px;");
        errorLabel.setWrapText(true);
        errorLabel.setPrefWidth(150); // Give it enough width to wrap if needed
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Container
        javafx.scene.layout.VBox container = new javafx.scene.layout.VBox(2, errorLabel, textField);
        container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Restrict input to numbers only
        javafx.scene.control.TextFormatter<Integer> formatter = new javafx.scene.control.TextFormatter<>(
                new javafx.util.converter.IntegerStringConverter(),
                initial,
                c -> {
                    if (c.getControlNewText().matches("\\d*")) {
                        return c;
                    }
                    return null;
                });
        textField.setTextFormatter(formatter);

        // Synchronous Validation listener on text property
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean isInvalid = false;
            if (newVal != null && !newVal.isEmpty()) {
                try {
                    int val = Integer.parseInt(newVal);
                    if (val > logicalMax) {
                        isInvalid = true;
                    }
                } catch (NumberFormatException e) {
                    // Ignore parsing errors, cleaner handles non-digits
                }
            }

            if (isInvalid) {
                textField.setStyle("-fx-border-color: red; -fx-border-radius: 3;");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            } else {
                textField.setStyle("");
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
            }
        });

        // Trigger initial validation
        if (initial > logicalMax) {
            textField.setStyle("-fx-border-color: red; -fx-border-radius: 3;");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }

        return container;
    }
}
