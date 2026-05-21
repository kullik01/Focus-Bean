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
import javafx.scene.paint.Color;
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

    /** Logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(MainView.class.getName());
    /** The font family used across the main view. */
    private static final String FONT_FAMILY = "'Segoe UI', 'Helvetica Neue', sans-serif";

    /** CSS template for card-style layout components. */
    private static final String STYLE_CARD = """
            -fx-background-color: %s;
            -fx-background-radius: 20;
            -fx-border-color: %s;
            -fx-border-radius: 20;
            -fx-border-width: 1;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);
            """;

    /** The controller managing timer logic and state. */
    private final TimerController controller;
    /** The view displaying the countdown timer and session status. */
    private final TimerDisplayView timerDisplay;
    /** The view containing timer control buttons (start, pause, etc.). */
    private final ControlPanelView controlPanel;
    /** The view displaying daily progress and goals. */
    private final DailyProgressView dailyProgressView;
    /** The view displaying session history statistics. */
    private final HistoryView historyView;
    /** The view for configuring application settings. */
    private final SettingsView settingsView;
    /** The view displaying application information. */
    private final AboutView aboutView;
    /** The tab pane used for navigation between different views. */
    private final TabPane tabPane;
    /** The container for the focus session timer card. */
    private VBox focusCard;
    /** The container for the daily progress card. */
    private VBox progressCard;
    /** The header label for the focus session card. */
    private Label focusHeaderLabel;

    /** The region used for drawing the window border. */
    private Region windowBorderOverlay;
    /** Flag indicating if dark mode is currently active. */
    private boolean darkMode;

    /** The settings buttons present on the UI. */
    private final java.util.List<Button> settingsButtons = new java.util.ArrayList<>();
    /** The button for switching to mini mode. */
    private Button miniModeButton;

    /** Callback invoked when mini mode is requested by the user. */
    private Runnable onMiniModeRequested;
    
    /** The canvas used for the confetti celebration animation. */
    private javafx.scene.canvas.Canvas celebrationCanvas;
    /** The timer managing the celebration animation loop. */
    private javafx.animation.AnimationTimer celebrationTimer;
    /** The list of active confetti particles for the celebration. */
    private java.util.List<ConfettiParticle> particles;
    /** The label displaying a congratulatory message. */
    private Label congratsLabel;
    /** The elapsed time in seconds since the celebration started. */
    private double celebrationElapsedSeconds = 0;
    /** The total number of confetti particles to generate. */
    private static final int PARTICLE_COUNT = 1500;
    /** The total duration of the celebration animation in seconds. */
    private static final double CELEBRATION_DURATION_SECONDS = 15.0;
    /** The duration of the fade-in and fade-out effects in seconds. */
    private static final double FADE_DURATION_SECONDS = 2.0;
    /** The target frames per second for the animation. */
    private static final double FRAMES_PER_SECOND = 60.0;

    /** Flag indicating if a tab switch is happening automatically after a settings save. */
    private boolean isSwitchingAfterSave = false;

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
        aboutView = new AboutView();

        // Create and configure cards
        focusCard = createFocusSessionCard();
        progressCard = createDailyProgressCard();

        // Make cards grow equally
        HBox.setHgrow(focusCard, Priority.ALWAYS);
        HBox.setHgrow(progressCard, Priority.ALWAYS);

        // Create side-by-side card container with transparent background
        HBox cardContainer = new HBox(15);
        cardContainer.setPadding(new Insets(20));
        cardContainer.setAlignment(Pos.TOP_CENTER);
        cardContainer.setStyle("-fx-background-color: transparent;");
        cardContainer.getChildren().addAll(focusCard, progressCard);

        // Wrap cardContainer in a VBox that grows to fill the tab
        VBox timerLayout = new VBox();
        timerLayout.setAlignment(Pos.TOP_CENTER);
        timerLayout.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(cardContainer, Priority.ALWAYS);
        timerLayout.getChildren().add(cardContainer);

        // Create tabs
        Tab timerTab = new Tab("Timer", timerLayout);
        timerTab.setClosable(false);

        Tab historyTab = new Tab(AppConstants.LABEL_HISTORY, historyView);
        historyTab.setClosable(false);

        Tab settingsTab = new Tab(AppConstants.LABEL_SETTINGS, settingsView);
        settingsTab.setClosable(false);

        Tab aboutTab = new Tab(AppConstants.LABEL_ABOUT, aboutView);
        aboutTab.setClosable(false);

        tabPane = new TabPane(timerTab, historyTab, settingsTab, aboutTab);
        tabPane.setTabMinWidth(80);
        tabPane.getSelectionModel().select(timerTab);

        // Flag to prevent recursive listener calls when programmatically reverting
        // selection
        final boolean[] handlingTabChange = { false };

        // Update views when tab is selected, with unsaved settings check
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (handlingTabChange[0]) {
                // Skip if we're programmatically reverting
                return;
            }

            // Check for unsaved settings when leaving the Settings tab
            // Skip check if we are explicitly switching after a save
            if (!isSwitchingAfterSave && oldTab == settingsTab && newTab != settingsTab && settingsView.hasUnsavedChanges()) {
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
                        
                        // Set flag to bypass the unsaved check on the re-triggered listener
                        isSwitchingAfterSave = true;
                        // Allows recursive listener to fire and update pages
                        // specific fix: Run later to let dialog close fully
                        javafx.application.Platform.runLater(() -> {
                            tabPane.getSelectionModel().select(targetTab);
                            // Reset flag in another runLater to ensure tab change listener has fully processed
                            javafx.application.Platform.runLater(() -> isSwitchingAfterSave = false);
                        });
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
            // Reset timer to Focus (Work) state
            controller.resetToFocus();
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

        // Initialize celebration components
        celebrationCanvas = new javafx.scene.canvas.Canvas();
        celebrationCanvas.setMouseTransparent(true);
        // Bind canvas size to parent stack pane (which will fill the BorderPane center)
        celebrationCanvas.managedProperty().bind(celebrationCanvas.visibleProperty());

        // Initialize congratulations message (simple text, no icons)
        congratsLabel = new Label("Great job! You've reached your daily goal, keep it up!");
        congratsLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        congratsLabel.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        congratsLabel.setStyle("""
                -fx-background-color: rgba(255, 255, 255, 0.95);
                -fx-background-radius: 12;
                -fx-padding: 12 20 12 20;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);
                """);
        congratsLabel.setVisible(false);
        congratsLabel.setMouseTransparent(true);

        // Wire daily goal reached callback from DailyProgressView
        dailyProgressView.setOnDailyGoalReached(this::startCelebration);

        // Create a StackPane to hold the TabPane and the celebration overlay
        // Order: confetti canvas first, then congratsLabel on top (in front)
        javafx.scene.layout.StackPane mainStack = new javafx.scene.layout.StackPane();
        mainStack.getChildren().addAll(tabPane, celebrationCanvas, congratsLabel);
        javafx.scene.layout.StackPane.setAlignment(congratsLabel, Pos.CENTER);
        
        // Bind canvas dimensions to stack pane
        celebrationCanvas.widthProperty().bind(mainStack.widthProperty());
        celebrationCanvas.heightProperty().bind(mainStack.heightProperty());

        setCenter(mainStack);

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

        // Load CSS styles based on current dark mode setting
        applyTheme(controller.getSettings().isDarkModeEnabled());
    }

    /**
     * Applies the specified theme.
     *
     * @param darkMode true to apply dark theme, false for light theme
     */
    public void applyTheme(boolean darkMode) {
        getStylesheets().clear();
        String cardBg, cardBorder, windowBg, textColor;
        if (darkMode) {
            java.net.URL darkCss = getClass().getResource("styles-dark.css");
            if (darkCss != null) {
                getStylesheets().add(darkCss.toExternalForm());
            } else {
                LOGGER.warning("Could not find styles-dark.css");
            }
            cardBg = AppConstants.COLOR_CARD_BACKGROUND_DARK;
            cardBorder = AppConstants.COLOR_CARD_BORDER_DARK;
            windowBg = AppConstants.COLOR_WINDOW_BACKGROUND_DARK;
            textColor = AppConstants.COLOR_TEXT_PRIMARY_DARK;
        } else {
            java.net.URL lightCss = getClass().getResource("styles.css");
            if (lightCss != null) {
                getStylesheets().add(lightCss.toExternalForm());
            } else {
                 LOGGER.warning("Could not find styles.css");
            }
            cardBg = AppConstants.COLOR_CARD_BACKGROUND;
            cardBorder = AppConstants.COLOR_CARD_BORDER;
            windowBg = AppConstants.COLOR_WINDOW_BACKGROUND;
            textColor = AppConstants.COLOR_TEXT_PRIMARY;
        }
        setStyle(String.format("-fx-background-color: %s;", windowBg));

        // Update card styles
        String cardStyle = String.format(STYLE_CARD, cardBg, cardBorder);
        if (focusCard != null) {
            focusCard.setStyle(cardStyle);
        }
        if (progressCard != null) {
            progressCard.setStyle(cardStyle);
        }
        if (focusHeaderLabel != null) {
            focusHeaderLabel.setTextFill(javafx.scene.paint.Color.web(textColor));
        }

        // Update window border overlay if present
        if (windowBorderOverlay != null) {
            // Use specific dark border color (#3D332B) to match FocusBeanApplication init logic
            String windowBorderColor = darkMode ? "#3D332B" : AppConstants.COLOR_CARD_BORDER;
            windowBorderOverlay.setStyle(String.format("""
                    -fx-background-color: transparent;
                    -fx-border-color: %s;
                    -fx-border-width: 1;
                    -fx-border-radius: 16;
                    """, windowBorderColor));
        }

        // Update SettingsView cards
        if (settingsView != null) {
            settingsView.applyTheme(darkMode);
        }

        // Update HistoryView
        if (historyView != null) {
            historyView.applyTheme(darkMode);
        }

        // Update AboutView
        if (aboutView != null) {
            aboutView.applyTheme(darkMode);
        }

        this.darkMode = darkMode;
        this.updateButtonThemes();
    }

    /**
     * Updates tooltip and hover styles on the settings and mini-mode buttons
     * to match the current theme.
     */
    private void updateButtonThemes() {
        String tooltipBackground = this.darkMode
                ? AppConstants.COLOR_CARD_BACKGROUND_DARK
                : AppConstants.COLOR_CARD_BACKGROUND;
        String tooltipTextColor = this.darkMode
                ? AppConstants.COLOR_TEXT_PRIMARY_DARK
                : AppConstants.COLOR_TEXT_PRIMARY;
        String tooltipStyle = String.format("""
                -fx-font-family: 'Segoe UI', sans-serif;
                -fx-font-size: 12px;
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-background-radius: 6;
                -fx-padding: 6 10 6 10;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 1);
                """, tooltipBackground, tooltipTextColor);

        for (Button settingsButtonElement : this.settingsButtons) {
            javafx.scene.control.Tooltip settingsTooltip =
                    new javafx.scene.control.Tooltip("Open Settings");
            settingsTooltip.setShowDelay(new javafx.util.Duration(0));
            settingsTooltip.setStyle(tooltipStyle);
            settingsButtonElement.setTooltip(settingsTooltip);
        }

        if (this.miniModeButton != null) {
            javafx.scene.control.Tooltip miniTooltip =
                    new javafx.scene.control.Tooltip("Mini Mode (M)");
            miniTooltip.setShowDelay(new javafx.util.Duration(0));
            miniTooltip.setStyle(tooltipStyle);
            this.miniModeButton.setTooltip(miniTooltip);
        }
    }

    /**
     * Sets the region used for the window border overlay.
     * This allows the view to update the border color when the theme changes.
     *
     * @param windowBorderOverlay the border overlay region
     */
    public void setWindowBorderOverlay(Region windowBorderOverlay) {
        this.windowBorderOverlay = windowBorderOverlay;
        // Apply current theme to the new overlay immediately
        if (windowBorderOverlay != null) {
            boolean currentDarkMode = controller.getSettings().isDarkModeEnabled();
            String windowBorderColor = currentDarkMode ? "#3D332B" : AppConstants.COLOR_CARD_BORDER;
            windowBorderOverlay.setStyle(String.format("""
                    -fx-background-color: transparent;
                    -fx-border-color: %s;
                    -fx-border-width: 1;
                    -fx-border-radius: 16;
                    """, windowBorderColor));
        }
    }

    /**
     * Creates the Focus Session card containing timer and controls.
     *
     * @return the configured card VBox
     */
    private VBox createFocusSessionCard() {
        // Header
        focusHeaderLabel = new Label("Focus session");
        focusHeaderLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        focusHeaderLabel.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_TEXT_PRIMARY));

        Button settingsButton = createSettingsButton();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerBar = new HBox();
        headerBar.setAlignment(Pos.CENTER_LEFT);
        headerBar.setPadding(new Insets(20, 20, 0, 20));
        headerBar.getChildren().addAll(focusHeaderLabel, spacer, createMiniModeButton(), settingsButton);

        // Timer content - includes timer display and controls within the card
        VBox timerContent = new VBox(0);
        timerContent.setAlignment(Pos.TOP_CENTER);
        timerContent.setPadding(new Insets(20));
        VBox.setVgrow(timerContent, Priority.ALWAYS);

        Region timerSpacer = new Region();
        VBox.setVgrow(timerSpacer, Priority.ALWAYS);

        Region bottomSpacer = new Region();
        bottomSpacer.setMinHeight(50);

        timerContent.getChildren().addAll(timerDisplay, timerSpacer, controlPanel, bottomSpacer);

        // Card container
        VBox card = new VBox();
        card.setStyle(String.format(STYLE_CARD,
                AppConstants.COLOR_CARD_BACKGROUND,
                AppConstants.COLOR_CARD_BORDER));
        card.setMinWidth(380);
        card.setMaxWidth(400);
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
        VBox.setVgrow(dailyProgressView, Priority.ALWAYS);
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
        javafx.scene.shape.SVGPath gearIcon = new javafx.scene.shape.SVGPath();
        // Clean gear/cog icon path (outline style matching the reference)
        gearIcon.setContent("M12 15.5A3.5 3.5 0 0 1 8.5 12 3.5 3.5 0 0 1 12 8.5a3.5 3.5 0 0 1 3.5 3.5 "
                + "3.5 3.5 0 0 1-3.5 3.5m7.43-2.53c.04-.32.07-.64.07-.97 0-.33-.03-.66-.07-1l2.11-1.63"
                + "c.19-.15.24-.42.12-.64l-2-3.46c-.12-.22-.39-.31-.61-.22l-2.49 1c-.52-.39-1.06-.73"
                + "-1.69-.98l-.37-2.65A.506.506 0 0 0 14 2h-4c-.25 0-.46.18-.5.42l-.37 2.65c-.63.25"
                + "-1.17.59-1.69.98l-2.49-1c-.22-.09-.49 0-.61.22l-2 3.46c-.13.22-.07.49.12.64L4.57 11"
                + "c-.04.34-.07.67-.07 1 0 .33.03.65.07.97l-2.11 1.66c-.19.15-.25.42-.12.64l2 3.46c.12"
                + ".22.39.3.61.22l2.49-1.01c.52.4 1.06.74 1.69.99l.37 2.65c.04.24.25.42.5.42h4c.25 0 "
                + ".46-.18.5-.42l.37-2.65c.63-.26 1.17-.59 1.69-.99l2.49 1.01c.22.08.49 0 .61-.22l2-3.46"
                + "c.12-.22.07-.49-.12-.64l-2.11-1.66Z");
        gearIcon.setFill(javafx.scene.paint.Color.web(AppConstants.COLOR_ACCENT));
        gearIcon.setScaleX(0.7);
        gearIcon.setScaleY(0.7);

        Button settingsButton = new Button();
        settingsButton.setGraphic(gearIcon);
        settingsButton.setStyle("""
                -fx-background-color: transparent;
                -fx-cursor: hand;
                -fx-padding: 2 6 2 6;
                """);

        settingsButton.setOnMouseEntered(event -> {
            String hoverBackground = this.darkMode
                    ? "rgba(255, 255, 255, 0.08)"
                    : "rgba(160, 82, 45, 0.10)";
            settingsButton.setStyle(String.format("""
                    -fx-background-color: %s;
                    -fx-background-radius: 6;
                    -fx-cursor: hand;
                    -fx-padding: 2 6 2 6;
                    """, hoverBackground));
        });

        settingsButton.setOnMouseExited(event -> {
            settingsButton.setStyle("""
                    -fx-background-color: transparent;
                    -fx-cursor: hand;
                    -fx-padding: 2 6 2 6;
                    """);
        });

        settingsButton.setOnAction(event -> tabPane.getSelectionModel().select(2));

        this.settingsButtons.add(settingsButton);
        return settingsButton;
    }

    /**
     * Creates a styled mini mode button that shrinks the app into a compact floating widget.
     *
     * <p>The button uses a picture-in-picture collapse icon.</p>
     *
     * @return The configured mini mode button
     */
    private Button createMiniModeButton() {
        javafx.scene.shape.SVGPath miniIcon = new javafx.scene.shape.SVGPath();
        miniIcon.setContent("M19 11h-8v6h8v-6zm4 8V4.98C23 3.88 22.1 3 21 3H3c-1.1 0-2 .88-2 1.98V19c0 "
                + "1.1.9 2 2 2h18c1.1 0 2-.9 2-2zm-2 .02H3V4.97h18v14.05z");
        miniIcon.setFill(javafx.scene.paint.Color.web(AppConstants.COLOR_ACCENT));
        miniIcon.setScaleX(0.65);
        miniIcon.setScaleY(0.65);

        this.miniModeButton = new Button();
        this.miniModeButton.setGraphic(miniIcon);
        this.miniModeButton.setStyle("""
                -fx-background-color: transparent;
                -fx-cursor: hand;
                -fx-padding: 2 6 2 6;
                """);

        this.miniModeButton.setOnMouseEntered(event -> {
            String hoverBackground = this.darkMode
                    ? "rgba(255, 255, 255, 0.08)"
                    : "rgba(160, 82, 45, 0.10)";
            this.miniModeButton.setStyle(String.format("""
                    -fx-background-color: %s;
                    -fx-background-radius: 6;
                    -fx-cursor: hand;
                    -fx-padding: 2 6 2 6;
                    """, hoverBackground));
        });

        this.miniModeButton.setOnMouseExited(event -> this.miniModeButton.setStyle("""
                -fx-background-color: transparent;
                -fx-cursor: hand;
                -fx-padding: 2 6 2 6;
                """));

        this.miniModeButton.setOnAction(event -> {
            if (this.onMiniModeRequested != null) {
                this.onMiniModeRequested.run();
            }
        });

        return this.miniModeButton;
    }

    /**
     * Updates the daily progress view with current data.
     */
    private void updateDailyProgress() {
        // Calculate base history total
        int totalMinutes = controller.getHistory().getTodaysTotalWorkMinutes();

        // Add currently running session progress if applicable
        if (controller.getCurrentState() == TimerState.WORK) {
            int totalSeconds = controller.getSettings().getWorkDurationSeconds();
            int elapsedSeconds = Math.max(0, totalSeconds - controller.getRemainingSeconds());
            totalMinutes += elapsedSeconds / 60;
        }

        dailyProgressView.update(controller.getHistory(), controller.getSettings());
        // Force update with the live total to ensure visual continuity
        dailyProgressView.setCompletedTodayMinutes(totalMinutes);

        // Update round progress indicator
        dailyProgressView.updateRoundProgress(
                controller.getCurrentRound(),
                controller.getSettings().getRoundsBeforeLongBreak(),
                controller.getSettings().isAutoCycleEnabled());
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
                .addListener((observable, oldValue, newValue) -> {
                    timerDisplay.updateTime(newValue.intValue());
                    
                    // Calculate (current accumulated history) + (current running session elapsed)
                    int currentSessionMinutes = 0;
                    if (controller.getCurrentState() == TimerState.WORK) {
                         // Add elapsed time from current session to the progress
                         int totalSeconds = controller.getSettings().getWorkDurationSeconds();
                         int elapsedSeconds = Math.max(0, totalSeconds - newValue.intValue());
                         currentSessionMinutes = elapsedSeconds / 60;
                    }

                    dailyProgressView.setCompletedTodayMinutes(
                            controller.getHistory().getTodaysTotalWorkMinutes() + currentSessionMinutes);
                });

        // Update state display and button states when state changes
        controller.currentStateProperty().addListener((observable, oldState, newState) -> {
            timerDisplay.updateState(newState);
            controlPanel.updateForState(newState, controller.getStateBeforePause());

            // When returning to IDLE, show duration based on pending session type
            if (newState == TimerState.IDLE) {
                if (controller.getPendingSessionType() == TimerState.BREAK) {
                    timerDisplay.showDuration(controller.getSettings().getBreakDurationMinutes(), "Break");
                } else if (controller.getPendingSessionType() == TimerState.LONG_BREAK) {
                    timerDisplay.showDuration(controller.getSettings().getLongBreakDurationMinutes(), "Long Break");
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
                } else if (newState == TimerState.LONG_BREAK) {
                    timerDisplay.setTotalSeconds(controller.getSettings().getLongBreakDurationSeconds());
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

        // Update dark mode and apply theme
        boolean darkModeChanged = controller.getSettings().isDarkModeEnabled() != settings.isDarkModeEnabled();
        controller.getSettings().setDarkModeEnabled(settings.isDarkModeEnabled());
        if (darkModeChanged) {
            applyTheme(settings.isDarkModeEnabled());
        }

        // Update auto-cycle settings
        controller.getSettings().setAutoCycleEnabled(settings.isAutoCycleEnabled());
        controller.getSettings().setRoundsBeforeLongBreak(settings.getRoundsBeforeLongBreak());
        controller.getSettings().setLongBreakDurationMinutes(settings.getLongBreakDurationMinutes());

        // Update display if idle - respect pending session type
        if (controller.getCurrentState() == TimerState.IDLE) {
            if (controller.getPendingSessionType() == TimerState.BREAK) {
                timerDisplay.showDuration(settings.getBreakDurationMinutes(), "Break");
            } else if (controller.getPendingSessionType() == TimerState.LONG_BREAK) {
                timerDisplay.showDuration(settings.getLongBreakDurationMinutes(), "Long Break");
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
            } else if (controller.getPendingSessionType() == TimerState.LONG_BREAK) {
                timerDisplay.showDuration(controller.getSettings().getLongBreakDurationMinutes(), "Long Break");
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
        } else if (event.getCode() == KeyCode.M && !event.isControlDown()) {
            // Switch to mini mode
            if (onMiniModeRequested != null) {
                onMiniModeRequested.run();
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
            case WORK, BREAK, LONG_BREAK -> controller.pause();
            case PAUSED -> controller.resume();
        }
    }

    /**
     * Starts the confetti celebration animation.
     */
    private void startCelebration() {
        if (celebrationTimer != null) {
            celebrationTimer.stop();
        }

        // Show congratulations message with theme-aware styling
        boolean isDarkMode = controller.getSettings().isDarkModeEnabled();
        congratsLabel.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 12;
                -fx-padding: 12 20 12 20;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,%s), 8, 0, 0, 2);
                """, 
                isDarkMode ? "rgba(50, 40, 35, 0.95)" : "rgba(255, 255, 255, 0.95)",
                isDarkMode ? "0.3" : "0.15"));
        congratsLabel.setVisible(true);

        // Initialize particles
        particles = new java.util.ArrayList<>(PARTICLE_COUNT);
        Color[] colors = {
            javafx.scene.paint.Color.web("#E6A779"), // Work bg
            javafx.scene.paint.Color.web("#55efc4"), // Break bg
            javafx.scene.paint.Color.web("#A0522D"), // Accent
            javafx.scene.paint.Color.web("#fdcb6e"), // Bright yellow/gold
            javafx.scene.paint.Color.web("#74b9ff")  // Soft blue
        };

        // Use MainView dimensions for particle positioning (more reliable than canvas)
        double width = getWidth() > 0 ? getWidth() : 850;
        double height = getHeight() > 0 ? getHeight() : 450;

        java.util.Random random = new java.util.Random();

        // Calculate velocity so particles take 15 seconds to travel from top to bottom
        // At 60fps, 15 seconds = 900 frames. velocity = height / 900 frames
        double totalFrames = CELEBRATION_DURATION_SECONDS * FRAMES_PER_SECOND;
        double baseVelocity = height / totalFrames;
        
        // Distribute particles uniformly from above screen to bottom of screen
        // So at any point during 15 seconds, there are particles visible
        // Particles travel from -height to +height
        double totalJourneyHeight = height * 2;
        
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            // Stagger particles uniformly across the journey
            double startY = -height + (random.nextDouble() * totalJourneyHeight);
            particles.add(new ConfettiParticle(
                // Spawn across full width
                random.nextDouble() * width,
                // Distributed across full journey
                startY,
                colors[random.nextInt(colors.length)],
                // Very subtle horizontal drift
                (random.nextDouble() - 0.5) * 0.3,
                // Tiny velocity variation
                baseVelocity + random.nextDouble() * 0.2
            ));
        }

        celebrationTimer = new javafx.animation.AnimationTimer() {
            private long animationStartTime = -1;
            
            @Override
            public void handle(long now) {
                if (animationStartTime == -1) animationStartTime = now;
                celebrationElapsedSeconds = (now - animationStartTime) / 1_000_000_000.0;

                // Run for 15 seconds
                if (celebrationElapsedSeconds > CELEBRATION_DURATION_SECONDS) {
                    stop();
                    celebrationCanvas.getGraphicsContext2D().clearRect(0, 0, celebrationCanvas.getWidth(), celebrationCanvas.getHeight());
                    congratsLabel.setVisible(false);
                    // Reset opacity for next time
                    congratsLabel.setOpacity(1.0);
                    celebrationElapsedSeconds = 0;
                    return;
                }

                // Fade out congrats label during last 2 seconds
                if (celebrationElapsedSeconds > CELEBRATION_DURATION_SECONDS - FADE_DURATION_SECONDS) {
                    double fadeProgress = (CELEBRATION_DURATION_SECONDS - celebrationElapsedSeconds) / FADE_DURATION_SECONDS;
                    congratsLabel.setOpacity(Math.max(0, fadeProgress));
                }

                updateParticles();
                drawParticles();
            }
        };
        celebrationTimer.start();
    }

    /**
     * Updates the position of all active confetti particles.
     */
    private void updateParticles() {
        for (ConfettiParticle particle : particles) {
            // Simple straight falling - no wiggle or drift
            particle.y += particle.vy;
        }
    }

    /**
     * Draws the confetti particles on the celebration canvas.
     */
    private void drawParticles() {
        javafx.scene.canvas.GraphicsContext graphicsContext = celebrationCanvas.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, celebrationCanvas.getWidth(), celebrationCanvas.getHeight());

        // Calculate opacity for fade in/out effect
        double opacity = 1.0;
        if (celebrationElapsedSeconds < FADE_DURATION_SECONDS) {
            // Fade in during first 2 seconds
            opacity = celebrationElapsedSeconds / FADE_DURATION_SECONDS;
        } else if (celebrationElapsedSeconds > CELEBRATION_DURATION_SECONDS - FADE_DURATION_SECONDS) {
            // Fade out during last 2 seconds
            opacity = (CELEBRATION_DURATION_SECONDS - celebrationElapsedSeconds) / FADE_DURATION_SECONDS;
        }
        // Clamp to [0, 1]
        opacity = Math.max(0, Math.min(1, opacity));

        graphicsContext.setGlobalAlpha(opacity);
        for (ConfettiParticle particle : particles) {
            graphicsContext.setFill(particle.color);
            graphicsContext.fillOval(particle.x, particle.y, 6, 6);
        }
        // Reset global alpha
        graphicsContext.setGlobalAlpha(1.0);
    }

    /**
     * Represents a single confetti particle in the celebration animation.
     */
    private static class ConfettiParticle {
        /** The horizontal position of the particle. */
        double x;
        /** The vertical position of the particle. */
        double y;
        /** The color of the particle. */
        Color color;
        /** The horizontal velocity of the particle. */
        double vx;
        /** The vertical velocity of the particle. */
        double vy;

        /**
         * Creates a new ConfettiParticle.
         *
         * @param x     the initial x position
         * @param y     the initial y position
         * @param color the color of the particle
         * @param vx    the horizontal velocity
         * @param vy    the vertical velocity
         */
        ConfettiParticle(double x, double y, Color color, double vx, double vy) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.vx = vx;
            this.vy = vy;
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

    /**
     * Returns the about view for external wiring.
     *
     * @return the about view
     */
    public AboutView getAboutView() {
        return aboutView;
    }

    /**
     * Sets the callback for when mini mode is requested.
     *
     * @param handler the callback to invoke
     */
    public void setOnMiniModeRequested(Runnable handler) {
        this.onMiniModeRequested = handler;
    }
}
