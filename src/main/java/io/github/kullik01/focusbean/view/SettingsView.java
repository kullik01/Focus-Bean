package io.github.kullik01.focusbean.view;

import io.github.kullik01.focusbean.model.NotificationSound;
import io.github.kullik01.focusbean.model.UserSettings;
import io.github.kullik01.focusbean.service.NotificationService;
import io.github.kullik01.focusbean.util.AppConstants;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Objects;

/**
 * View component for displaying and editing application settings.
 *
 * <p>
 * This view provides an embedded settings panel for use within a tab,
 * allowing users to configure timer durations, daily goals, and notification
 * preferences without opening a modal dialog.
 * </p>
 */
public final class SettingsView extends VBox {

    private static final String FONT_FAMILY = "'Segoe UI', 'Helvetica Neue', sans-serif";
    private static final String STYLE_LABEL = """
            -fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;
            -fx-font-size: 14px;
            """;

    private static final String STYLE_CARD = """
            -fx-background-color: %s;
            -fx-background-radius: 8;
            -fx-border-color: %s;
            -fx-border-radius: 8;
            -fx-border-width: 1;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);
            """;

    private static final String STYLE_SAVE_BUTTON = """
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-padding: 10 30;
            -fx-background-radius: 6;
            -fx-cursor: hand;
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
    private final Button saveButton;

    private final NotificationService notificationService;
    private String customSoundPath;

    private Runnable onSave;

    /**
     * Creates a new SettingsView with the given settings and notification service.
     *
     * @param currentSettings     the current user settings to display
     * @param notificationService the notification service for sound preview
     * @throws NullPointerException if any parameter is null
     */
    public SettingsView(UserSettings currentSettings, NotificationService notificationService) {
        Objects.requireNonNull(currentSettings, "currentSettings must not be null");
        Objects.requireNonNull(notificationService, "notificationService must not be null");

        this.notificationService = notificationService;
        this.customSoundPath = currentSettings.getCustomSoundPath();

        // Configure root container
        setStyle("-fx-background-color: transparent;");
        setPadding(new Insets(20));
        setSpacing(15);
        setAlignment(Pos.TOP_CENTER);

        // Notification checkboxes
        soundNotificationCheckbox = new CheckBox("Enable sound notifications");
        soundNotificationCheckbox.setSelected(currentSettings.isSoundNotificationEnabled());
        soundNotificationCheckbox.setStyle(STYLE_LABEL);

        popupNotificationCheckbox = new CheckBox("Show Windows notification");
        popupNotificationCheckbox.setSelected(currentSettings.isPopupNotificationEnabled());
        popupNotificationCheckbox.setStyle(STYLE_LABEL);

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
        soundComboBox.setPrefWidth(180);

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
        customSoundPathField.setPrefWidth(180);
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

        // Custom sound row visibility
        HBox customSoundRow = new HBox(10, customSoundPathField, browseButton);
        customSoundRow.setAlignment(Pos.CENTER_LEFT);
        customSoundRow.visibleProperty().bind(
                soundComboBox.valueProperty().isEqualTo(NotificationSound.CUSTOM));
        customSoundRow.managedProperty().bind(customSoundRow.visibleProperty());

        // Sound row
        HBox soundSelectionRow = new HBox(10, soundComboBox, previewButton);
        soundSelectionRow.setAlignment(Pos.CENTER_LEFT);

        // Enable/disable sound controls based on checkbox
        soundComboBox.disableProperty().bind(soundNotificationCheckbox.selectedProperty().not());
        previewButton.disableProperty().bind(soundNotificationCheckbox.selectedProperty().not());

        // Save button
        saveButton = new Button("Save Settings");
        saveButton.setStyle(String.format(STYLE_SAVE_BUTTON, AppConstants.COLOR_PROGRESS_ACTIVE));
        saveButton.setOnAction(e -> {
            if (onSave != null) {
                onSave.run();
            }
        });

        // Create Timer Settings card
        VBox timerSettingsCard = createTimerSettingsCard();

        // Create Notifications card
        VBox notificationsCard = createNotificationsCard(soundSelectionRow, customSoundRow);

        // Cards container
        HBox cardsContainer = new HBox(15);
        cardsContainer.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(timerSettingsCard, Priority.ALWAYS);
        HBox.setHgrow(notificationsCard, Priority.ALWAYS);
        cardsContainer.getChildren().addAll(timerSettingsCard, notificationsCard);

        // Save button container
        HBox saveButtonContainer = new HBox(saveButton);
        saveButtonContainer.setAlignment(Pos.CENTER);
        saveButtonContainer.setPadding(new Insets(10, 0, 0, 0));

        getChildren().addAll(cardsContainer, saveButtonContainer);
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
    private VBox createValidatedTextField(int min, int logicalMax, int initial, TextField textField) {
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
        VBox container = new VBox(2, errorLabel, textField);
        container.setAlignment(Pos.CENTER_LEFT);

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

    /**
     * Creates the Timer Settings card.
     *
     * @return the configured card VBox
     */
    private VBox createTimerSettingsCard() {
        Label headerLabel = new Label("Timer Settings");
        headerLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        headerLabel.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_TEXT_PRIMARY));

        VBox card = new VBox(15);
        card.setStyle(String.format(STYLE_CARD,
                AppConstants.COLOR_CARD_BACKGROUND,
                AppConstants.COLOR_CARD_BORDER));
        card.setPadding(new Insets(20));
        card.setMinWidth(320);
        card.setMaxWidth(380);

        // Create text fields first
        workField = new TextField();
        breakField = new TextField();
        dailyGoalField = new TextField();

        card.getChildren().addAll(
                headerLabel,
                createSettingRow("Work Duration (minutes):", createValidatedTextField(
                        UserSettings.MIN_DURATION_MINUTES,
                        UserSettings.MAX_WORK_DURATION_MINUTES,
                        25, workField)), // Default 25
                createSettingRow("Break Duration (minutes):", createValidatedTextField(
                        UserSettings.MIN_DURATION_MINUTES,
                        UserSettings.MAX_BREAK_DURATION_MINUTES,
                        5, breakField)), // Default 5
                createSettingRow("Daily Goal (minutes):", createValidatedTextField(
                        UserSettings.MIN_DURATION_MINUTES,
                        UserSettings.MAX_DAILY_GOAL_MINUTES,
                        25, dailyGoalField))); // Default 25

        return card;
    }

    /**
     * Creates the Notifications card.
     *
     * @param soundSelectionRow the sound selection row
     * @param customSoundRow    the custom sound row
     * @return the configured card VBox
     */
    private VBox createNotificationsCard(HBox soundSelectionRow, HBox customSoundRow) {
        Label headerLabel = new Label("Notifications");
        headerLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        headerLabel.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_TEXT_PRIMARY));

        Label soundLabel = new Label("Notification Sound:");
        soundLabel.setStyle(STYLE_LABEL);

        VBox soundSection = new VBox(8);
        soundSection.getChildren().addAll(soundLabel, soundSelectionRow, customSoundRow);

        VBox card = new VBox(15);
        card.setStyle(String.format(STYLE_CARD,
                AppConstants.COLOR_CARD_BACKGROUND,
                AppConstants.COLOR_CARD_BORDER));
        card.setPadding(new Insets(20));
        card.setMinWidth(320);
        card.setMaxWidth(380);

        card.getChildren().addAll(
                headerLabel,
                soundNotificationCheckbox,
                soundSection,
                popupNotificationCheckbox);

        return card;
    }

    /**
     * Creates a setting row with label and control.
     *
     * @param labelText the label text
     * @param control   the control node
     * @return the configured HBox row
     */
    private HBox createSettingRow(String labelText, javafx.scene.Node control) {
        Label label = new Label(labelText);
        label.setStyle(STYLE_LABEL);
        label.setPrefWidth(180);

        HBox row = new HBox(10, label, control);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * Previews the currently selected sound.
     */
    private boolean isPreviewPlaying = false;

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

        File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());
        if (selectedFile != null) {
            customSoundPath = selectedFile.getAbsolutePath();
            customSoundPathField.setText(selectedFile.getName());
        }
    }

    /**
     * Updates the view with new settings values.
     *
     * @param settings the settings to display
     */
    public void update(UserSettings settings) {
        Objects.requireNonNull(settings, "settings must not be null");

        workField.setText(String.valueOf(settings.getWorkDurationMinutes()));
        breakField.setText(String.valueOf(settings.getBreakDurationMinutes()));
        dailyGoalField.setText(String.valueOf(settings.getDailyGoalMinutes()));
        soundNotificationCheckbox.setSelected(settings.isSoundNotificationEnabled());
        popupNotificationCheckbox.setSelected(settings.isPopupNotificationEnabled());
        soundComboBox.setValue(settings.getNotificationSound());
        customSoundPath = settings.getCustomSoundPath();
        if (customSoundPath != null) {
            customSoundPathField.setText(new File(customSoundPath).getName());
        } else {
            customSoundPathField.clear();
        }
    }

    /**
     * Returns the current settings from the view controls.
     *
     * @return the current UserSettings based on control values
     */
    public UserSettings getCurrentSettings() {
        return new UserSettings(
                Integer.parseInt(workField.getText()),
                Integer.parseInt(breakField.getText()),
                Integer.parseInt(dailyGoalField.getText()),
                soundNotificationCheckbox.isSelected(),
                popupNotificationCheckbox.isSelected(),
                soundComboBox.getValue(),
                soundComboBox.getValue() == NotificationSound.CUSTOM ? customSoundPath : null);
    }

    /**
     * Sets the callback to be invoked when settings are saved.
     *
     * @param onSave the save callback
     */
    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
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
     * Returns the save button for testing purposes.
     *
     * @return the save button
     */
    public Button getSaveButton() {
        return saveButton;
    }
}
