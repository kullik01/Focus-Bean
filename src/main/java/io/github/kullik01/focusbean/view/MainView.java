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
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
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

    private final StackPane contentRoot;
    private int lastCelebratedGoalMinutes = -1;
    private int completedTodayMinutesBeforeWork = 0;

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

        contentRoot = new StackPane(tabPane);
        contentRoot.setStyle("-fx-background-color: transparent;");

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

            lastCelebratedGoalMinutes = -1;
            completedTodayMinutesBeforeWork = controller.getHistory().getTodaysTotalWorkMinutes();

            controller.setPendingSessionType(TimerState.WORK);
            if (controller.getCurrentState() == TimerState.IDLE) {
                timerDisplay.showDuration(controller.getSettings().getWorkDurationMinutes());
            }
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

        setCenter(contentRoot);

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

        int completedNow = controller.getHistory().getTodaysTotalWorkMinutes();
        int goalNow = controller.getSettings().getDailyGoalMinutes();
        lastCelebratedGoalMinutes = goalNow > 0 && completedNow >= goalNow ? goalNow : -1;
        completedTodayMinutesBeforeWork = completedNow;

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
        // Create gear icon using SVG (same style as HistoryView)
        javafx.scene.shape.SVGPath icon = new javafx.scene.shape.SVGPath();
        // Gear icon
        icon.setContent(
                "M19.14,12.94c0.04-0.3,0.06-0.61,0.06-0.94c0-0.32-0.02-0.64-0.06-0.94l2.03-1.58c0.18-0.14,0.23-0.41,0.12-0.61 l-1.92-3.32c-0.12-0.22-0.37-0.29-0.59-0.22l-2.39,0.96c-0.5-0.38-1.03-0.7-1.62-0.94L14.4,2.81c-0.04-0.24-0.24-0.41-0.48-0.41 h-3.84c-0.24,0-0.43,0.17-0.47,0.41L9.25,5.35C8.66,5.59,8.12,5.92,7.63,6.29L5.24,5.33c-0.22-0.08-0.47,0-0.59,0.22L2.73,8.87 C2.62,9.08,2.66,9.34,2.86,9.48l2.03,1.58C4.84,11.36,4.8,11.69,4.8,12s0.02,0.64,0.06,0.94l-2.03,1.58 c-0.18,0.14-0.23,0.41-0.12,0.61l1.92,3.32c0.12,0.22,0.37,0.29,0.59,0.22l2.39-0.96c0.5,0.38,1.03,0.7,1.62,0.94l0.36,2.54 c0.05,0.24,0.24,0.41,0.48,0.41h3.84c0.24,0,0.44-0.17,0.47-0.41l0.36-2.54c0.59-0.24,1.13-0.56,1.62-0.94l2.39,0.96 c0.22,0.08,0.47,0,0.59-0.22l1.92-3.32c0.12-0.22,0.07-0.47-0.12-0.61L19.14,12.94z M12,15.6c-1.98,0-3.6-1.62-3.6-3.6 s1.62-3.6,3.6-3.6s3.6,1.62,3.6,3.6S13.98,15.6,12,15.6z");
        icon.setFill(javafx.scene.paint.Color.web(AppConstants.COLOR_PROGRESS_ACTIVE));
        icon.setScaleX(0.85);
        icon.setScaleY(0.85);

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
                    completedTodayMinutesBeforeWork = controller.getHistory().getTodaysTotalWorkMinutes();
                    timerDisplay.setTotalSeconds(controller.getSettings().getWorkDurationSeconds());
                } else if (newState == TimerState.BREAK) {
                    timerDisplay.setTotalSeconds(controller.getSettings().getBreakDurationSeconds());
                }
            }

            if (newState == TimerState.IDLE && oldState == TimerState.WORK) {
                int completed = controller.getHistory().getTodaysTotalWorkMinutes();
                int goal = controller.getSettings().getDailyGoalMinutes();
                if (goal > 0
                        && goal != lastCelebratedGoalMinutes
                        && completedTodayMinutesBeforeWork < goal
                        && completed >= goal) {
                    showCongratsOverlay();
                    lastCelebratedGoalMinutes = goal;
                }
            }
        });
    }

    private void showCongratsOverlay() {
        for (Node child : contentRoot.getChildren()) {
            if (child instanceof CongratsOverlay) {
                return;
            }
        }

        CongratsOverlay overlay = new CongratsOverlay();
        overlay.prefWidthProperty().bind(contentRoot.widthProperty());
        overlay.prefHeightProperty().bind(contentRoot.heightProperty());
        contentRoot.getChildren().add(overlay);
        overlay.play();
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

        int completedNow = controller.getHistory().getTodaysTotalWorkMinutes();
        int goalNow = controller.getSettings().getDailyGoalMinutes();
        if (goalNow <= 0) {
            lastCelebratedGoalMinutes = -1;
        } else if (completedNow >= goalNow) {
            lastCelebratedGoalMinutes = goalNow;
        }

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
