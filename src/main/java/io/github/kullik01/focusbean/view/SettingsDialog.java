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
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
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

    private Spinner<Integer> workSpinner;
    private Spinner<Integer> breakSpinner;
    private Spinner<Integer> dailyGoalSpinner;
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

        // Create spinners
        workSpinner = new Spinner<>();
        VBox workBox = createValidatedSpinner(
                UserSettings.MIN_DURATION_MINUTES,
                UserSettings.MAX_WORK_DURATION_MINUTES,
                currentSettings.getWorkDurationMinutes(),
                workSpinner);

        breakSpinner = new Spinner<>();
        VBox breakBox = createValidatedSpinner(
                UserSettings.MIN_DURATION_MINUTES,
                UserSettings.MAX_BREAK_DURATION_MINUTES,
                currentSettings.getBreakDurationMinutes(),
                breakSpinner);

        dailyGoalSpinner = new Spinner<>();
        VBox dailyGoalBox = createValidatedSpinner(
                UserSettings.MIN_DURATION_MINUTES,
                UserSettings.MAX_DAILY_GOAL_MINUTES,
                currentSettings.getDailyGoalMinutes(),
                dailyGoalSpinner);

        // Create labels
        Label workLabel = new Label("Work Duration (minutes):");
        workLabel.setStyle(STYLE_LABEL);

        Label breakLabel = new Label("Break Duration (minutes):");
        breakLabel.setStyle(STYLE_LABEL);

        Label dailyGoalLabel = new Label("Daily Goal (minutes):");
        dailyGoalLabel.setStyle(STYLE_LABEL);

        // Notification settings
        soundNotificationCheckbox = new CheckBox("Enable sound notifications");
        soundNotificationCheckbox.setSelected(currentSettings.isSoundNotificationEnabled());
        soundNotificationCheckbox.setStyle(STYLE_LABEL);

        popupNotificationCheckbox = new CheckBox("Show Windows notification");
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
        customSoundPathField.setPrefWidth(200);
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

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        int row = 0;
        grid.add(new Label("Timer Settings"), 0, row++, 2, 1);
        grid.add(workLabel, 0, row);
        grid.add(workBox, 1, row++);
        grid.add(breakLabel, 0, row);
        grid.add(breakBox, 1, row++);
        grid.add(dailyGoalLabel, 0, row);
        grid.add(dailyGoalBox, 1, row++);

        grid.add(new Label(""), 0, row++); // Spacer

        grid.add(new Label("Notifications"), 0, row++, 2, 1);
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
            if (buttonType == saveButtonType) {
                return new UserSettings(
                        workSpinner.getValue(),
                        breakSpinner.getValue(),
                        dailyGoalSpinner.getValue(),
                        soundNotificationCheckbox.isSelected(),
                        popupNotificationCheckbox.isSelected(),
                        soundComboBox.getValue(),
                        soundComboBox.getValue() == NotificationSound.CUSTOM ? customSoundPath : null);
            }
            return null;
        });
    }

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
        NotificationSound sound = soundComboBox.getValue();
        String path = (sound == NotificationSound.CUSTOM) ? customSoundPath : null;
        notificationService.previewSound(sound, path);
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
     * Creates a styled integer spinner with numeric-only input restriction and
     * visual validation.
     *
     * @param min        minimum value
     * @param logicalMax the logical maximum value for validation (e.g., 900)
     * @param initial    initial value
     * @param spinner    the spinner to configure (must be non-null)
     * @return the container holding the spinner and error message
     */
    private javafx.scene.layout.VBox createValidatedSpinner(int min, int logicalMax, int initial,
            Spinner<Integer> spinner) {
        // Allow typing larger values to show validation error
        int technicalMax = 10000;
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, technicalMax, initial));
        spinner.setEditable(true);
        spinner.setPrefWidth(100);

        // Error label - ensure it wraps and fits
        Label errorLabel = new Label("Value cannot exceed " + logicalMax + " minutes!");
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 10px;");
        errorLabel.setWrapText(true);
        errorLabel.setPrefWidth(150); // Give it enough width to wrap if needed
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Container
        javafx.scene.layout.VBox container = new javafx.scene.layout.VBox(2, errorLabel, spinner);
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
        spinner.getEditor().setTextFormatter(formatter);
        spinner.getValueFactory().valueProperty().bindBidirectional(formatter.valueProperty());

        // Synchronous Validation listener on text property
        spinner.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
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
                spinner.setStyle("-fx-border-color: red; -fx-border-radius: 3;");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            } else {
                spinner.setStyle("");
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
            }
        });

        // Trigger initial validation
        if (initial > logicalMax) {
            spinner.setStyle("-fx-border-color: red; -fx-border-radius: 3;");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }

        return container;
    }
}
