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
import java.util.function.Consumer;

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
            -fx-background-radius: 20;
            -fx-border-color: %s;
            -fx-border-radius: 20;
            -fx-border-width: 1;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);
            """;

    private static final String STYLE_SAVE_BUTTON = """
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-padding: 8 20;
            -fx-background-radius: 25;
            -fx-cursor: hand;
            """;

    private TextField workField;
    private TextField breakField;
    private TextField dailyGoalField;
    private TextField chartDaysField;
    private final CheckBox soundNotificationCheckbox;
    private final CheckBox popupNotificationCheckbox;
    private final CheckBox darkModeCheckbox;
    private final ComboBox<NotificationSound> soundComboBox;
    private final TextField customSoundPathField;
    private final Button browseButton;
    private final Button previewButton;
    private final Button saveButton;

    private final NotificationService notificationService;
    private String customSoundPath;

    private Runnable onSave;
    private UserSettings originalSettings;

    // Card references for theme updates
    private VBox timerSettingsCard;
    private VBox historySettingsCard;
    private VBox notificationsCard;
    private Label timerHeaderLabel;
    private Label historyHeaderLabel;
    private Label notificationsHeaderLabel;
    private Label appearanceHeaderLabel;
    private boolean darkModeEnabled = false;

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
        soundNotificationCheckbox = new CheckBox("Enable sound notification");
        soundNotificationCheckbox.setSelected(currentSettings.isSoundNotificationEnabled());
        soundNotificationCheckbox.setStyle(STYLE_LABEL);

        popupNotificationCheckbox = new CheckBox("Show notification");
        popupNotificationCheckbox.setSelected(currentSettings.isPopupNotificationEnabled());
        popupNotificationCheckbox.setStyle(STYLE_LABEL);

        // Dark mode checkbox
        darkModeCheckbox = new CheckBox("Dark mode");
        darkModeCheckbox.setSelected(currentSettings.isDarkModeEnabled());
        darkModeCheckbox.setStyle(STYLE_LABEL);

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
        soundComboBox.setPrefWidth(155);

        // Create a clean play icon using SVG
        javafx.scene.shape.SVGPath playIcon = new javafx.scene.shape.SVGPath();
        playIcon.setContent("M8 5v14l11-7z");
        playIcon.setFill(javafx.scene.paint.Color.web(AppConstants.COLOR_PROGRESS_ACTIVE));
        playIcon.setScaleX(0.85);
        playIcon.setScaleY(0.85);

        previewButton = new Button();
        previewButton.setGraphic(playIcon);
        previewButton.setPrefWidth(30);
        previewButton.setAlignment(Pos.CENTER);
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
        customSoundPathField.setPrefWidth(155);
        customSoundPathField.setEditable(false);
        if (customSoundPath != null) {
            customSoundPathField.setText(new File(customSoundPath).getName());
        }

        // Create a folder icon using SVG
        javafx.scene.shape.SVGPath folderIcon = new javafx.scene.shape.SVGPath();
        folderIcon.setContent(
                "M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z");
        folderIcon.setFill(javafx.scene.paint.Color.web(AppConstants.COLOR_PROGRESS_ACTIVE));
        folderIcon.setScaleX(0.85);
        folderIcon.setScaleY(0.85);

        browseButton = new Button();
        browseButton.setGraphic(folderIcon);
        browseButton.setPrefWidth(30);
        browseButton.setAlignment(Pos.CENTER);
        browseButton.setStyle(String.format("""
                -fx-background-color: transparent;
                -fx-cursor: hand;
                -fx-padding: 2 6 2 6;
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
        customSoundPathField.disableProperty().bind(soundNotificationCheckbox.selectedProperty().not());
        browseButton.disableProperty().bind(soundNotificationCheckbox.selectedProperty().not());

        // Save button
        saveButton = new Button("Save");
        saveButton.setStyle(String.format(STYLE_SAVE_BUTTON, AppConstants.COLOR_PROGRESS_ACTIVE));
        saveButton.setOnAction(e -> {
            if (onSave != null) {
                onSave.run();
            }
        });

        // Create Timer Settings card
        timerSettingsCard = createTimerSettingsCard();

        // Create combined History + Appearance card (middle card)
        historySettingsCard = createHistoryAndAppearanceCard();

        // Create Notifications card
        notificationsCard = createNotificationsCard(soundSelectionRow, customSoundRow);

        // Cards container - single row with 3 cards
        HBox cardsRow = new HBox(15);
        cardsRow.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(timerSettingsCard, Priority.ALWAYS);
        HBox.setHgrow(historySettingsCard, Priority.ALWAYS);
        HBox.setHgrow(notificationsCard, Priority.ALWAYS);
        cardsRow.getChildren().addAll(timerSettingsCard, historySettingsCard, notificationsCard);

        // Save button container
        HBox saveButtonContainer = new HBox(saveButton);
        saveButtonContainer.setAlignment(Pos.CENTER);
        saveButtonContainer.setPadding(new Insets(10, 0, 0, 0));

        getChildren().addAll(cardsRow, saveButtonContainer);
    }

    /**
     * Applies the specified theme to the settings view cards.
     *
     * @param darkMode true to apply dark theme, false for light theme
     */
    public void applyTheme(boolean darkMode) {
        this.darkModeEnabled = darkMode;
        String cardBg, cardBorder, textColor;
        if (darkMode) {
            cardBg = AppConstants.COLOR_CARD_BACKGROUND_DARK;
            cardBorder = AppConstants.COLOR_CARD_BORDER_DARK;
            textColor = AppConstants.COLOR_TEXT_PRIMARY_DARK;
        } else {
            cardBg = AppConstants.COLOR_CARD_BACKGROUND;
            cardBorder = AppConstants.COLOR_CARD_BORDER;
            textColor = AppConstants.COLOR_TEXT_PRIMARY;
        }

        String cardStyle = String.format(STYLE_CARD, cardBg, cardBorder);
        javafx.scene.paint.Color textColorPaint = javafx.scene.paint.Color.web(textColor);

        if (timerSettingsCard != null) {
            timerSettingsCard.setStyle(cardStyle);
        }
        if (historySettingsCard != null) {
            historySettingsCard.setStyle(cardStyle);
        }
        if (notificationsCard != null) {
            notificationsCard.setStyle(cardStyle);
        }
        if (timerHeaderLabel != null) {
            timerHeaderLabel.setTextFill(textColorPaint);
        }
        if (historyHeaderLabel != null) {
            historyHeaderLabel.setTextFill(textColorPaint);
        }
        if (notificationsHeaderLabel != null) {
            notificationsHeaderLabel.setTextFill(textColorPaint);
        }
        if (appearanceHeaderLabel != null) {
            appearanceHeaderLabel.setTextFill(textColorPaint);
        }
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
        textField.setPrefWidth(70);

        // Error label - ensure it wraps and fits
        Label errorLabel = new Label("Value must be between " + min + " and " + logicalMax);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 10px;");
        errorLabel.setWrapText(true);
        // Give it enough width to wrap if needed
        errorLabel.setPrefWidth(150);
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
                    long valLex = Long.parseLong(newVal); // Use long to detect int overflow
                    if (valLex > logicalMax || valLex < min) {
                        isInvalid = true;
                    }
                } catch (NumberFormatException e) {
                    // If it doesn't parse as long, it's definitely invalid
                    isInvalid = true;
                }
            } else {
                // Empty is invalid
                isInvalid = true;
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
        if (initial > logicalMax || initial < min) {
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
        timerHeaderLabel = new Label("Timer");
        timerHeaderLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        timerHeaderLabel.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_TEXT_PRIMARY));

        VBox card = new VBox(15);
        card.setStyle(String.format(STYLE_CARD,
                AppConstants.COLOR_CARD_BACKGROUND,
                AppConstants.COLOR_CARD_BORDER));
        card.setPadding(new Insets(20));
        card.setMinWidth(220);
        card.setMaxWidth(250);

        // Create text fields first
        // Create text fields first
        workField = new TextField();
        breakField = new TextField();
        dailyGoalField = new TextField();

        card.getChildren().addAll(
                timerHeaderLabel,
                createSettingRow("Work Duration (min):", createValidatedTextField(
                        UserSettings.MIN_DURATION_MINUTES,
                        UserSettings.MAX_WORK_DURATION_MINUTES,
                        25, workField)), // Default 25
                createSettingRow("Break Duration (min):", createValidatedTextField(
                        UserSettings.MIN_DURATION_MINUTES,
                        UserSettings.MAX_BREAK_DURATION_MINUTES,
                        5, breakField)), // Default 5
                createSettingRow("Daily Goal (min):", createValidatedTextField(
                        UserSettings.MIN_DURATION_MINUTES,
                        UserSettings.MAX_DAILY_GOAL_MINUTES,
                        25, dailyGoalField))); // Default 25

        return card;
    }

    /**
     * Creates the combined History and Appearance settings card.
     *
     * @return the configured card VBox
     */
    private VBox createHistoryAndAppearanceCard() {
        // History section
        historyHeaderLabel = new Label("History");
        historyHeaderLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        historyHeaderLabel.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_TEXT_PRIMARY));

        chartDaysField = new TextField();

        VBox historySection = new VBox(10);
        historySection.getChildren().addAll(
                historyHeaderLabel,
                createSettingRow("Chart Days:", createValidatedTextField(
                        UserSettings.MIN_CHART_DAYS,
                        UserSettings.MAX_CHART_DAYS,
                        7, chartDaysField)));

        // Separator line
        javafx.scene.layout.Region separator = new javafx.scene.layout.Region();
        separator.setStyle("-fx-background-color: " + AppConstants.COLOR_CARD_BORDER + "; -fx-pref-height: 1;");
        separator.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(separator, new Insets(5, 0, 5, 0));

        // Appearance section
        appearanceHeaderLabel = new Label("Appearance");
        appearanceHeaderLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        appearanceHeaderLabel.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_TEXT_PRIMARY));

        VBox appearanceSection = new VBox(10);
        appearanceSection.getChildren().addAll(
                appearanceHeaderLabel,
                darkModeCheckbox);

        // Combined card
        VBox card = new VBox(10);
        card.setStyle(String.format(STYLE_CARD,
                AppConstants.COLOR_CARD_BACKGROUND,
                AppConstants.COLOR_CARD_BORDER));
        card.setPadding(new Insets(20));
        card.setMinWidth(220);
        card.setMaxWidth(250);

        card.getChildren().addAll(historySection, separator, appearanceSection);

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
        notificationsHeaderLabel = new Label("Notification");
        notificationsHeaderLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        notificationsHeaderLabel.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_TEXT_PRIMARY));

        Label soundLabel = new Label("Notification Sound:");
        soundLabel.setStyle(STYLE_LABEL);

        VBox soundSection = new VBox(8);
        soundSection.getChildren().addAll(soundLabel, soundSelectionRow, customSoundRow);

        VBox card = new VBox(15);
        card.setStyle(String.format(STYLE_CARD,
                AppConstants.COLOR_CARD_BACKGROUND,
                AppConstants.COLOR_CARD_BORDER));
        card.setPadding(new Insets(20));
        card.setMinWidth(220);
        card.setMaxWidth(250);

        card.getChildren().addAll(
                notificationsHeaderLabel,
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

        // Store a copy of the original settings for change detection
        this.originalSettings = new UserSettings(
                settings.getWorkDurationMinutes(),
                settings.getBreakDurationMinutes(),
                settings.getDailyGoalMinutes(),
                settings.isSoundNotificationEnabled(),
                settings.isPopupNotificationEnabled(),
                settings.getNotificationSound(),
                settings.getCustomSoundPath(),
                settings.getHistoryChartDays());
        this.originalSettings.setDarkModeEnabled(settings.isDarkModeEnabled());

        workField.setText(String.valueOf(settings.getWorkDurationMinutes()));
        breakField.setText(String.valueOf(settings.getBreakDurationMinutes()));
        dailyGoalField.setText(String.valueOf(settings.getDailyGoalMinutes()));
        chartDaysField.setText(String.valueOf(settings.getHistoryChartDays()));
        soundNotificationCheckbox.setSelected(settings.isSoundNotificationEnabled());
        popupNotificationCheckbox.setSelected(settings.isPopupNotificationEnabled());
        darkModeCheckbox.setSelected(settings.isDarkModeEnabled());
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
        UserSettings settings = new UserSettings(
                Integer.parseInt(workField.getText()),
                Integer.parseInt(breakField.getText()),
                Integer.parseInt(dailyGoalField.getText()),
                soundNotificationCheckbox.isSelected(),
                popupNotificationCheckbox.isSelected(),
                soundComboBox.getValue(),
                soundComboBox.getValue() == NotificationSound.CUSTOM ? customSoundPath : null,
                Integer.parseInt(chartDaysField.getText()));
        settings.setDarkModeEnabled(darkModeCheckbox.isSelected());
        return settings;
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
     * Checks if any of the input fields have validation errors.
     *
     * @return true if any field contains an invalid value, false otherwise
     */
    public boolean hasValidationErrors() {
        return isFieldInvalid(workField, UserSettings.MAX_WORK_DURATION_MINUTES)
                || isFieldInvalid(breakField, UserSettings.MAX_BREAK_DURATION_MINUTES)
                || isFieldInvalid(dailyGoalField, UserSettings.MAX_DAILY_GOAL_MINUTES)
                || isFieldInvalid(chartDaysField, UserSettings.MAX_CHART_DAYS);
    }

    private boolean isFieldInvalid(TextField field, int max) {
        if (field == null)
            return false;
        try {
            String text = field.getText();
            if (text == null || text.trim().isEmpty())
                return true; // Empty is invalid
            int val = Integer.parseInt(text);
            return val > max; // Invalid if greater than max
        } catch (NumberFormatException e) {
            return true; // Invalid if not a number
        }
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

    /**
     * Checks if there are unsaved changes in the settings.
     *
     * @return true if current control values differ from the original settings
     */
    public boolean hasUnsavedChanges() {
        if (originalSettings == null) {
            return false;
        }

        try {
            int currentWork = Integer.parseInt(workField.getText());
            int currentBreak = Integer.parseInt(breakField.getText());
            int currentDailyGoal = Integer.parseInt(dailyGoalField.getText());
            int currentChartDays = Integer.parseInt(chartDaysField.getText());

            return currentWork != originalSettings.getWorkDurationMinutes()
                    || currentBreak != originalSettings.getBreakDurationMinutes()
                    || currentDailyGoal != originalSettings.getDailyGoalMinutes()
                    || currentChartDays != originalSettings.getHistoryChartDays()
                    || soundNotificationCheckbox.isSelected() != originalSettings.isSoundNotificationEnabled()
                    || popupNotificationCheckbox.isSelected() != originalSettings.isPopupNotificationEnabled()
                    || soundComboBox.getValue() != originalSettings.getNotificationSound()
                    || !Objects.equals(customSoundPath, originalSettings.getCustomSoundPath())
                    || darkModeCheckbox.isSelected() != originalSettings.isDarkModeEnabled();
        } catch (NumberFormatException e) {
            // If parsing fails, consider it as a change (invalid input)
            return true;
        }
    }

    /**
     * Updates the original settings snapshot to match the current values.
     * Call this after settings are saved to reset the change detection.
     */
    public void markSettingsSaved() {
        this.originalSettings = getCurrentSettings();
    }

    /**
     * Shows a confirmation dialog for unsaved settings.
     * Styled to match the Delete History dialog.
     *
     * @param onResult callback invoked with true if OK (save and proceed),
     *                 false if Cancel (stay on settings)
     */
    public void showUnsavedChangesDialog(Consumer<Boolean> onResult) {
        // Determine colors based on dark mode
        String windowBg = darkModeEnabled ? AppConstants.COLOR_WINDOW_BACKGROUND_DARK : AppConstants.COLOR_WINDOW_BACKGROUND;
        String borderColor = darkModeEnabled ? AppConstants.COLOR_CARD_BORDER_DARK : "#D7B49E";
        String textColor = darkModeEnabled ? AppConstants.COLOR_TEXT_PRIMARY_DARK : "#333333";
        String closeBtnColor = darkModeEnabled ? AppConstants.COLOR_TEXT_PRIMARY_DARK : "#5D4037";
        String okBtnBg = darkModeEnabled ? "#3D332B" : "#E0E0E0";
        String okBtnBgHover = darkModeEnabled ? "#4D4339" : "#D0D0D0";
        String okBtnText = darkModeEnabled ? AppConstants.COLOR_TEXT_PRIMARY_DARK : "#333333";
        String stylesheetPath = darkModeEnabled ? "/io/github/kullik01/focusbean/view/styles-dark.css"
                : "/io/github/kullik01/focusbean/view/styles.css";

        javafx.stage.Stage dialogStage = new javafx.stage.Stage();
        dialogStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Unsaved Settings");

        // Load custom logo if available
        javafx.scene.image.Image logoImage = null;
        try {
            String logoPath = "/io/github/kullik01/focusbean/view/logo.png";
            if (getClass().getResource(logoPath) == null) {
                logoPath = "/logo.png";
            }
            if (getClass().getResource(logoPath) != null) {
                logoImage = new javafx.scene.image.Image(
                        getClass().getResource(logoPath).toExternalForm());
                dialogStage.getIcons().add(logoImage);
            }
        } catch (Exception e) {
            // Ignore
        }

        // --- 1. Title Bar ---
        HBox titleBar = new HBox(10);
        titleBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        titleBar.setPadding(new javafx.geometry.Insets(10, 15, 10, 15));
        titleBar.setStyle("-fx-background-color: transparent;");

        // Logo/Icon
        if (logoImage != null) {
            javafx.scene.image.ImageView logoView = new javafx.scene.image.ImageView(logoImage);
            logoView.setFitHeight(24);
            logoView.setFitWidth(24);
            titleBar.getChildren().add(logoView);
        } else {
            javafx.scene.shape.Circle fallbackIcon = new javafx.scene.shape.Circle(8,
                    javafx.scene.paint.Color.web("#5D4037"));
            titleBar.getChildren().add(fallbackIcon);
        }

        // Title Text
        Label titleLabel = new Label("Unsaved Settings");
        titleLabel.setStyle(
                "-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 14px; -fx-text-fill: " + textColor + ";");
        titleBar.getChildren().add(titleLabel);

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        titleBar.getChildren().add(spacer);

        Button closeBtn = new Button("✕");
        String closeBtnStyle = "-fx-background-color: transparent; -fx-text-fill: " + closeBtnColor + "; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0 5 0 5;";
        String closeBtnHoverStyle = "-fx-background-color: rgba(93, 64, 55, 0.15); -fx-text-fill: " + closeBtnColor + "; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0 5 0 5; -fx-background-radius: 4;";
        closeBtn.setStyle(closeBtnStyle);
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(closeBtnHoverStyle));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle(closeBtnStyle));
        closeBtn.setOnAction(e -> {
            dialogStage.close();
            onResult.accept(false);
        });
        titleBar.getChildren().add(closeBtn);

        // Drag support
        final double[] xOffset = new double[1];
        final double[] yOffset = new double[1];
        titleBar.setOnMousePressed(event -> {
            xOffset[0] = event.getSceneX();
            yOffset[0] = event.getSceneY();
        });
        titleBar.setOnMouseDragged(event -> {
            dialogStage.setX(event.getScreenX() - xOffset[0]);
            dialogStage.setY(event.getScreenY() - yOffset[0]);
        });

        // --- 2. Content ---
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        headerBox.setPadding(new javafx.geometry.Insets(10, 0, 15, 0));

        Label headerText = new Label("You have unsaved settings.");
        headerText.setStyle("-fx-font-size: 16px; -fx-text-fill: " + textColor + ";");

        Label subText = new Label("Save before leaving?");
        String subTextColor = darkModeEnabled ? AppConstants.COLOR_TEXT_SECONDARY_DARK : "#555555";
        subText.setStyle("-fx-font-size: 14px; -fx-text-fill: " + subTextColor + ";");

        VBox textBox = new VBox(5, headerText, subText);
        textBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        javafx.scene.shape.SVGPath questionIcon = new javafx.scene.shape.SVGPath();
        questionIcon.setContent(
                "M11 18h2v-2h-2v2zm1-16C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5 0-2.21-1.79-4-4-4z");
        questionIcon.setFill(javafx.scene.paint.Color.web("#A0522D"));
        questionIcon.setScaleX(1.5);
        questionIcon.setScaleY(1.5);

        javafx.scene.layout.Region headerSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(headerSpacer, javafx.scene.layout.Priority.ALWAYS);

        headerBox.getChildren().addAll(textBox, headerSpacer, questionIcon);

        VBox contentBody = new VBox(0);
        contentBody.setPadding(new javafx.geometry.Insets(0, 20, 20, 20));
        contentBody.getChildren().add(headerBox);

        // --- 3. Button Bar ---
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        buttonBar.setPadding(new javafx.geometry.Insets(20, 20, 20, 20));

        Button okButton = new Button("OK");
        okButton.setDefaultButton(true);
        String okButtonStyle = """
                -fx-background-color: #A0522D;
                -fx-text-fill: white;
                -fx-background-radius: 20;
                -fx-cursor: hand;
                -fx-padding: 6 16 6 16;
                -fx-font-size: 13px;
                -fx-min-width: 70;
                """;
        String okButtonHoverStyle = """
                -fx-background-color: #8B4513;
                -fx-text-fill: white;
                -fx-background-radius: 20;
                -fx-cursor: hand;
                -fx-padding: 6 16 6 16;
                -fx-font-size: 13px;
                -fx-min-width: 70;
                """;
        okButton.setStyle(okButtonStyle);
        okButton.setOnMouseEntered(e -> okButton.setStyle(okButtonHoverStyle));
        okButton.setOnMouseExited(e -> okButton.setStyle(okButtonStyle));
        okButton.setOnAction(e -> {
            dialogStage.close();
            onResult.accept(true);
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);
        String cancelButtonStyle = String.format("""
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-background-radius: 20;
                -fx-cursor: hand;
                -fx-padding: 6 16 6 16;
                -fx-font-size: 13px;
                """, okBtnBg, okBtnText);
        String cancelButtonHoverStyle = String.format("""
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-background-radius: 20;
                -fx-cursor: hand;
                -fx-padding: 6 16 6 16;
                -fx-font-size: 13px;
                """, okBtnBgHover, okBtnText);
        cancelButton.setStyle(cancelButtonStyle);
        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle(cancelButtonHoverStyle));
        cancelButton.setOnMouseExited(e -> cancelButton.setStyle(cancelButtonStyle));
        cancelButton.setOnAction(e -> {
            dialogStage.close();
            onResult.accept(false);
        });

        buttonBar.getChildren().addAll(okButton, cancelButton);

        // --- 4. Main Window Structure ---
        VBox dialogLayout = new VBox(0);
        dialogLayout.getChildren().addAll(titleBar, contentBody, buttonBar);

        // Styling the Visual Box (nested background for perfect border)
        dialogLayout.setStyle(String.format("""
                -fx-background-color: %s, %s;
                -fx-background-insets: 0, 1.5;
                -fx-background-radius: 12, 10.5;
                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 4);
                """, borderColor, windowBg));

        dialogLayout.setMinWidth(380);

        // Root Container (Transparent with Padding) to prevent clipping
        javafx.scene.layout.StackPane root = new javafx.scene.layout.StackPane(dialogLayout);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setStyle("-fx-background-color: transparent;");

        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource(stylesheetPath).toExternalForm());

        dialogStage.setScene(scene);

        // Ensure on top if main window is
        if (getScene() != null && getScene().getWindow() != null) {
            javafx.stage.Stage mainStage = (javafx.stage.Stage) getScene().getWindow();
            if (mainStage.isAlwaysOnTop()) {
                dialogStage.setAlwaysOnTop(true);
            }
        }

        dialogStage.showAndWait();
    }
}
