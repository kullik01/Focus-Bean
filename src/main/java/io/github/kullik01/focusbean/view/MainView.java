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

import io.github.kullik01.focusbean.controller.TimerController;
import io.github.kullik01.focusbean.model.TimerState;
import io.github.kullik01.focusbean.model.UserSettings;
import io.github.kullik01.focusbean.util.AppConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * The main view assembling all UI components with modern card-based layout.
 *
 * <p>
 * This view contains the timer display, control panel, and daily progress view
 * arranged in side-by-side cards. It handles keyboard shortcuts and coordinates
 * updates between view components and the controller.
 * </p>
 */
public final class MainView extends BorderPane {

    private static final Logger LOGGER = Logger.getLogger(MainView.class.getName());
    private static final String FONT_FAMILY = "'Segoe UI', 'Helvetica Neue', sans-serif";

    private static final String STYLE_CARD = """
            -fx-background-color: %s;
            -fx-background-radius: 8;
            -fx-border-color: %s;
            -fx-border-radius: 8;
            -fx-border-width: 1;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);
            """;

    private final TimerController controller;
    private final TimerDisplayView timerDisplay;
    private final ControlPanelView controlPanel;
    private final DailyProgressView dailyProgressView;
    private final HistoryView historyView;
    private final SettingsView settingsView;
    private final TabPane tabPane;

    /**
     * Creates the main view wired to the given controller.
     *
     * @param controller the timer controller
     * @throws NullPointerException if controller is null
     */
    public MainView(TimerController controller) {
        this.controller = Objects.requireNonNull(controller, "controller must not be null");

        // Create components
        timerDisplay = new TimerDisplayView();
        controlPanel = new ControlPanelView();
        dailyProgressView = new DailyProgressView();
        historyView = new HistoryView();
        settingsView = new SettingsView(controller.getSettings(), controller.getNotificationService());

        // Create and configure cards
        VBox focusCard = createFocusSessionCard();
        VBox progressCard = createDailyProgressCard();

        // Make cards grow equally
        HBox.setHgrow(focusCard, Priority.ALWAYS);
        HBox.setHgrow(progressCard, Priority.ALWAYS);

        // Create side-by-side card container with transparent background
        HBox cardContainer = new HBox(15);
        cardContainer.setPadding(new Insets(20));
        cardContainer.setAlignment(Pos.CENTER);
        cardContainer.setStyle("-fx-background-color: transparent;");
        cardContainer.getChildren().addAll(focusCard, progressCard);

        // Create tabs
        Tab timerTab = new Tab("Timer", cardContainer);
        timerTab.setClosable(false);

        Tab historyTab = new Tab(AppConstants.LABEL_HISTORY, historyView);
        historyTab.setClosable(false);

        Tab settingsTab = new Tab(AppConstants.LABEL_SETTINGS, settingsView);
        settingsTab.setClosable(false);

        tabPane = new TabPane(timerTab, historyTab, settingsTab);
        tabPane.setTabMinWidth(80);
        tabPane.getSelectionModel().select(timerTab);

        // Flag to prevent recursive listener calls when programmatically reverting
        // selection
        final boolean[] handlingTabChange = { false };

        // Update views when tab is selected, with unsaved settings check
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (handlingTabChange[0]) {
                return; // Skip if we're programmatically reverting
            }

            // Check for unsaved settings when leaving the Settings tab
            if (oldTab == settingsTab && newTab != settingsTab && settingsView.hasUnsavedChanges()) {
                handlingTabChange[0] = true;
                // Revert selection temporarily while dialog is shown
                tabPane.getSelectionModel().select(settingsTab);
                handlingTabChange[0] = false;

                // Show the unsaved changes dialog
                final Tab targetTab = newTab;
                settingsView.showUnsavedChangesDialog(saveAndProceed -> {
                    if (saveAndProceed) {
                        // Check validation first - if invalid, abort switch
                        if (settingsView.hasValidationErrors()) {
                            return;
                        }

                        // Save settings and switch to target tab
                        applySettings();
                        settingsView.markSettingsSaved();
                        // Allows recursive listener to fire and update pages
                        tabPane.getSelectionModel().select(targetTab);
                    }
                    // If Cancel, we already reverted to settingsTab, so do nothing
                });
                return;
            }

            // Normal tab change handling
            if (newTab == historyTab) {
                historyView.setHistoryChartDays(controller.getSettings().getHistoryChartDays());
                historyView.update(controller.getHistory());
            } else if (newTab == settingsTab) {
                settingsView.update(controller.getSettings());
            } else if (newTab == timerTab) {
                // Refresh timer display to ensure sync after tab switch
                refreshTimerDisplay();
            }
        });

        // Wire clear history callback
        historyView.setOnClearHistory(() -> {
            controller.clearHistory();
            historyView.update(controller.getHistory());
            updateDailyProgress();
        });

        // Wire history view mode change handling
        historyView.setHistoryChartDays(controller.getSettings().getHistoryChartDays());
        historyView.setHistoryViewMode(controller.getSettings().getHistoryViewMode());
        historyView.setOnViewModeChanged(mode -> {
            controller.getSettings().setHistoryViewMode(mode);
            controller.saveData();
        });

        // Wire settings button to switch to settings tab
        historyView.setOnSettingsClicked(() -> {
            // Settings tab is at index 2
            tabPane.getSelectionModel().select(2);
        });

        // Wire settings save callback with change tracking
        settingsView.setOnSave(() -> {
            applySettings();
            settingsView.markSettingsSaved();
        });

        setCenter(tabPane);

        // Style TabPane for transparent background (corners handled by clip)
        tabPane.setStyle("""
                -fx-background-color: transparent;
                -fx-tab-header-area-background: transparent;
                """);

        // Apply background to MainView (corners handled by clip in
        // FocusBeanApplication)
        setStyle(String.format("-fx-background-color: %s;", AppConstants.COLOR_WINDOW_BACKGROUND));

        // Wire up event handlers
        wireEventHandlers();

        // Bind to controller properties
        bindToController();

        // Initialize display with current settings
        timerDisplay.updateState(TimerState.IDLE);
        timerDisplay.showDuration(controller.getSettings().getWorkDurationMinutes());

        // Initialize daily progress
        updateDailyProgress();

        // Load CSS styles
        getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
    }

    /**
     * Creates the Focus Session card containing timer and controls.
     *
     * @return the configured card VBox
     */
    private VBox createFocusSessionCard() {
        // Header
        Label headerLabel = new Label("Focus session");
        headerLabel.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 14));
        headerLabel.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_TEXT_PRIMARY));

        Button settingsButton = createSettingsButton();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerBar = new HBox();
        headerBar.setAlignment(Pos.CENTER_LEFT);
        headerBar.setPadding(new Insets(15, 15, 0, 15));
        headerBar.getChildren().addAll(headerLabel, spacer, settingsButton);

        // Timer content - includes timer display and controls within the card
        VBox timerContent = new VBox(0);
        timerContent.setAlignment(Pos.CENTER);
        timerContent.setPadding(new Insets(0, 20, 25, 20));
        VBox.setVgrow(timerContent, Priority.ALWAYS);
        timerContent.getChildren().addAll(timerDisplay, controlPanel);

        // Card container
        VBox card = new VBox();
        card.setStyle(String.format(STYLE_CARD,
                AppConstants.COLOR_CARD_BACKGROUND,
                AppConstants.COLOR_CARD_BORDER));
        card.setMinWidth(380);
        card.setMaxWidth(400);
        card.setMinHeight(340);
        card.getChildren().addAll(headerBar, timerContent);

        return card;
    }

    /**
     * Creates the Daily Progress card.
     *
     * @return the configured card VBox
     */
    private VBox createDailyProgressCard() {
        // Add settings button to daily progress header
        dailyProgressView.setSettingsButton(createSettingsButton());

        // Card container
        VBox card = new VBox();
        card.setStyle(String.format(STYLE_CARD,
                AppConstants.COLOR_CARD_BACKGROUND,
                AppConstants.COLOR_CARD_BORDER));
        card.setMinWidth(380);
        card.setMaxWidth(400);
        card.setMinHeight(340);
        card.getChildren().add(dailyProgressView);

        return card;
    }

    /**
     * Creates a styled settings button with a gear icon.
     * The button navigates to the Settings tab when clicked.
     *
     * @return the configured settings button
     */
    private Button createSettingsButton() {
        // Create a clean outline-style gear icon using SVG
        javafx.scene.shape.SVGPath icon = new javafx.scene.shape.SVGPath();
        // Clean gear/cog icon path (outline style matching the reference)
        icon.setContent("M12 15.5A3.5 3.5 0 0 1 8.5 12 3.5 3.5 0 0 1 12 8.5a3.5 3.5 0 0 1 3.5 3.5 "
                + "3.5 3.5 0 0 1-3.5 3.5m7.43-2.53c.04-.32.07-.64.07-.97 0-.33-.03-.66-.07-1l2.11-1.63"
                + "c.19-.15.24-.42.12-.64l-2-3.46c-.12-.22-.39-.31-.61-.22l-2.49 1c-.52-.39-1.06-.73"
                + "-1.69-.98l-.37-2.65A.506.506 0 0 0 14 2h-4c-.25 0-.46.18-.5.42l-.37 2.65c-.63.25"
                + "-1.17.59-1.69.98l-2.49-1c-.22-.09-.49 0-.61.22l-2 3.46c-.13.22-.07.49.12.64L4.57 11"
                + "c-.04.34-.07.67-.07 1 0 .33.03.65.07.97l-2.11 1.66c-.19.15-.25.42-.12.64l2 3.46c.12"
                + ".22.39.3.61.22l2.49-1.01c.52.4 1.06.74 1.69.99l.37 2.65c.04.24.25.42.5.42h4c.25 0 "
                + ".46-.18.5-.42l.37-2.65c.63-.26 1.17-.59 1.69-.99l2.49 1.01c.22.08.49 0 .61-.22l2-3.46"
                + "c.12-.22.07-.49-.12-.64l-2.11-1.66Z");
        icon.setFill(javafx.scene.paint.Color.web(AppConstants.COLOR_ACCENT));
        icon.setScaleX(0.7);
        icon.setScaleY(0.7);

        Button settingsButton = new Button();
        settingsButton.setGraphic(icon);
        settingsButton.setStyle("""
                -fx-background-color: transparent;
                -fx-cursor: hand;
                -fx-padding: 2 6 2 6;
                """);

        // Add tooltip with warm colors matching the GUI design
        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip("Open Settings");
        tooltip.setShowDelay(new javafx.util.Duration(0));
        tooltip.setStyle(String.format("""
                -fx-font-family: 'Segoe UI', sans-serif;
                -fx-font-size: 12px;
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-background-radius: 6;
                -fx-padding: 6 10 6 10;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 1);
                """, AppConstants.COLOR_CARD_BACKGROUND, AppConstants.COLOR_TEXT_PRIMARY));
        settingsButton.setTooltip(tooltip);

        settingsButton.setOnMouseEntered(e -> {
            settingsButton.setStyle("""
                    -fx-background-color: rgba(160, 82, 45, 0.10);
                    -fx-background-radius: 6;
                    -fx-cursor: hand;
                    -fx-padding: 2 6 2 6;
                    """);
        });

        settingsButton.setOnMouseExited(e -> {
            settingsButton.setStyle("""
                    -fx-background-color: transparent;
                    -fx-cursor: hand;
                    -fx-padding: 2 6 2 6;
                    """);
        });

        settingsButton.setOnAction(e -> tabPane.getSelectionModel().select(2));

        return settingsButton;
    }

    /**
     * Updates the daily progress view with current data.
     */
    private void updateDailyProgress() {
        dailyProgressView.update(controller.getHistory(), controller.getSettings());
    }

    /**
     * Wires event handlers from view components to controller actions.
     */
    private void wireEventHandlers() {
        controlPanel.setOnStart(controller::startOrResume);
        controlPanel.setOnPause(controller::pause);
        controlPanel.setOnResume(controller::resume);
        controlPanel.setOnReset(controller::reset);
        controlPanel.setOnSkip(controller::skip);
    }

    /**
     * Binds view components to controller properties for automatic updates.
     */
    private void bindToController() {
        // Update time display when remaining seconds change
        controller.remainingSecondsProperty()
                .addListener((obs, oldVal, newVal) -> {
                    timerDisplay.updateTime(newVal.intValue());
                    // Update completed minutes in daily progress
                    dailyProgressView.setCompletedTodayMinutes(
                            controller.getHistory().getTodaysTotalWorkMinutes());
                });

        // Update state display and button states when state changes
        controller.currentStateProperty().addListener((obs, oldState, newState) -> {
            timerDisplay.updateState(newState);
            controlPanel.updateForState(newState, controller.getStateBeforePause());

            // When returning to IDLE, show duration based on pending session type
            if (newState == TimerState.IDLE) {
                if (controller.getPendingSessionType() == TimerState.BREAK) {
                    timerDisplay.showDuration(controller.getSettings().getBreakDurationMinutes(), "Break");
                } else {
                    timerDisplay.showDuration(controller.getSettings().getWorkDurationMinutes());
                }
                updateDailyProgress();
            }

            // Set total seconds for progress calculation ONLY when starting a NEW session
            // (from IDLE), not when resuming from PAUSED
            if (oldState == TimerState.IDLE) {
                if (newState == TimerState.WORK) {
                    timerDisplay.setTotalSeconds(controller.getSettings().getWorkDurationSeconds());
                } else if (newState == TimerState.BREAK) {
                    timerDisplay.setTotalSeconds(controller.getSettings().getBreakDurationSeconds());
                }
            }
        });
    }

    /**
     * Applies settings from the SettingsView.
     */
    private void applySettings() {
        UserSettings settings = settingsView.getCurrentSettings();

        controller.updateSettings(
                settings.getWorkDurationMinutes(),
                settings.getBreakDurationMinutes());

        // Update daily goal if changed
        controller.getSettings().setDailyGoalMinutes(settings.getDailyGoalMinutes());

        // Update notification settings
        controller.getSettings().setSoundNotificationEnabled(settings.isSoundNotificationEnabled());
        controller.getSettings().setPopupNotificationEnabled(settings.isPopupNotificationEnabled());
        controller.getSettings().setNotificationSound(settings.getNotificationSound());
        controller.getSettings().setCustomSoundPath(settings.getCustomSoundPath());
        controller.getSettings().setHistoryChartDays(settings.getHistoryChartDays());

        // Update display if idle - respect pending session type
        if (controller.getCurrentState() == TimerState.IDLE) {
            if (controller.getPendingSessionType() == TimerState.BREAK) {
                timerDisplay.showDuration(settings.getBreakDurationMinutes(), "Break");
            } else {
                timerDisplay.showDuration(settings.getWorkDurationMinutes());
            }
        }

        // Update daily progress
        updateDailyProgress();

        // Save updated settings
        controller.saveData();

        LOGGER.info("Settings applied from Settings tab");
    }

    /**
     * Refreshes the timer display to ensure sync after tab switch.
     * Only updates when idle - running sessions are kept in sync by property
     * bindings.
     */
    private void refreshTimerDisplay() {
        TimerState state = controller.getCurrentState();

        // Only refresh display when idle (running sessions sync via property bindings)
        if (state == TimerState.IDLE) {
            if (controller.getPendingSessionType() == TimerState.BREAK) {
                timerDisplay.showDuration(controller.getSettings().getBreakDurationMinutes(), "Break");
            } else {
                timerDisplay.showDuration(controller.getSettings().getWorkDurationMinutes());
            }
        }

        // Always update daily progress
        updateDailyProgress();
    }

    /**
     * Handles keyboard shortcuts.
     *
     * @param event the key event
     */
    public void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.SPACE) {
            handleSpaceKey();
            event.consume();
        } else if (event.getCode() == KeyCode.R && !event.isControlDown()) {
            controller.reset();
            event.consume();
        } else if (event.getCode() == KeyCode.S && !event.isControlDown()) {
            // Switch to settings tab
            tabPane.getSelectionModel().select(2);
            event.consume();
        } else if (event.getCode() == KeyCode.H && !event.isControlDown()) {
            // Toggle to history tab
            if (tabPane.getSelectionModel().getSelectedIndex() == 0) {
                tabPane.getSelectionModel().select(1);
            } else {
                tabPane.getSelectionModel().select(0);
            }
            event.consume();
        }
    }

    /**
     * Handles the space key for start/pause/resume toggle.
     */
    private void handleSpaceKey() {
        TimerState state = controller.getCurrentState();

        switch (state) {
            case IDLE -> controller.startOrResume();
            case WORK, BREAK -> controller.pause();
            case PAUSED -> controller.resume();
        }
    }

    /**
     * Returns the timer display for testing purposes.
     *
     * @return the timer display view
     */
    public TimerDisplayView getTimerDisplay() {
        return timerDisplay;
    }

    /**
     * Returns the control panel for testing purposes.
     *
     * @return the control panel view
     */
    public ControlPanelView getControlPanel() {
        return controlPanel;
    }

    /**
     * Returns the history view for testing purposes.
     *
     * @return the history view
     */
    public HistoryView getHistoryView() {
        return historyView;
    }

    /**
     * Returns the tab pane for testing purposes.
     *
     * @return the tab pane
     */
    public TabPane getTabPane() {
        return tabPane;
    }
}
