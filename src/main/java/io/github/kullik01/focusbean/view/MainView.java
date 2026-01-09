package io.github.kullik01.focusbean.view;

import io.github.kullik01.focusbean.controller.TimerController;
import io.github.kullik01.focusbean.model.TimerState;
import io.github.kullik01.focusbean.model.UserSettings;
import io.github.kullik01.focusbean.util.AppConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Objects;
import java.util.Optional;
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

    private static final String STYLE_HEADER_BUTTON = """
            -fx-background-color: transparent;
            -fx-text-fill: %s;
            -fx-cursor: hand;
            -fx-font-size: 14px;
            """;

    private final TimerController controller;
    private final TimerDisplayView timerDisplay;
    private final ControlPanelView controlPanel;
    private final DailyProgressView dailyProgressView;
    private final HistoryView historyView;
    private final Button settingsButton;
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
        settingsButton = createSettingsButton();

        // Create and configure cards
        VBox focusCard = createFocusSessionCard();
        VBox progressCard = createDailyProgressCard();

        // Make cards grow equally
        HBox.setHgrow(focusCard, Priority.ALWAYS);
        HBox.setHgrow(progressCard, Priority.ALWAYS);

        // Create side-by-side card container
        HBox cardContainer = new HBox(15);
        cardContainer.setPadding(new Insets(20));
        cardContainer.setAlignment(Pos.CENTER);
        cardContainer.setStyle("-fx-background-color: " + AppConstants.COLOR_WINDOW_BACKGROUND + ";");
        cardContainer.getChildren().addAll(focusCard, progressCard);

        // Keep TabPane for history access (hidden initially, can be accessed via
        // keyboard)
        Tab timerTab = new Tab("Timer", cardContainer);
        timerTab.setClosable(false);

        Tab historyTab = new Tab(AppConstants.LABEL_HISTORY, historyView);
        historyTab.setClosable(false);

        tabPane = new TabPane(timerTab, historyTab);
        tabPane.setTabMinWidth(80);
        tabPane.getSelectionModel().select(timerTab);

        // Update history when tab is selected
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == historyTab) {
                historyView.update(controller.getHistory());
            }
        });

        // Wire clear history callback
        historyView.setOnClearHistory(() -> {
            controller.clearHistory();
            historyView.update(controller.getHistory());
            updateDailyProgress();
        });

        setCenter(tabPane);

        // Wire up event handlers
        wireEventHandlers();

        // Bind to controller properties
        bindToController();

        // Initialize display with current settings
        timerDisplay.updateState(TimerState.IDLE);
        timerDisplay.showDuration(controller.getSettings().getWorkDurationMinutes());

        // Initialize daily progress
        updateDailyProgress();
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

        HBox headerBar = new HBox();
        headerBar.setAlignment(Pos.CENTER_LEFT);
        headerBar.setPadding(new Insets(15, 15, 0, 15));
        HBox.setHgrow(headerLabel, Priority.ALWAYS);
        headerBar.getChildren().addAll(headerLabel, settingsButton);

        // Timer content - includes timer display and controls within the card
        VBox timerContent = new VBox(0);
        timerContent.setAlignment(Pos.CENTER);
        timerContent.setPadding(new Insets(5, 20, 15, 20));
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
        // Card container
        VBox card = new VBox();
        card.setStyle(String.format(STYLE_CARD,
                AppConstants.COLOR_CARD_BACKGROUND,
                AppConstants.COLOR_CARD_BORDER));
        card.setMinWidth(380);
        card.setMaxWidth(400);
        card.setMinHeight(340);
        card.getChildren().add(dailyProgressView);

        // Wire edit button to settings
        dailyProgressView.setOnEdit(this::showSettingsDialog);

        return card;
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

        settingsButton.setOnAction(e -> showSettingsDialog());
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

            // Set total seconds for progress calculation
            if (newState == TimerState.WORK) {
                timerDisplay.setTotalSeconds(controller.getSettings().getWorkDurationSeconds());
            } else if (newState == TimerState.BREAK) {
                timerDisplay.setTotalSeconds(controller.getSettings().getBreakDurationSeconds());
            }
        });
    }

    /**
     * Shows the settings dialog and applies changes if confirmed.
     */
    private void showSettingsDialog() {
        SettingsDialog dialog = new SettingsDialog(
                controller.getSettings(),
                controller.getNotificationService());
        Optional<UserSettings> result = dialog.showAndGetResult();

        result.ifPresent(settings -> {
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

            // Update display if idle
            if (controller.getCurrentState() == TimerState.IDLE) {
                timerDisplay.showDuration(settings.getWorkDurationMinutes());
            }

            // Update daily progress
            updateDailyProgress();

            // Save updated settings
            controller.saveData();

            LOGGER.info("Settings updated via dialog");
        });
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
            showSettingsDialog();
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
            case IDLE -> controller.startWork();
            case WORK, BREAK -> controller.pause();
            case PAUSED -> controller.resume();
        }
    }

    /**
     * Creates the settings button.
     *
     * @return the configured settings button
     */
    private Button createSettingsButton() {
        Button button = new Button("⚙");
        button.setStyle(String.format(STYLE_HEADER_BUTTON, AppConstants.COLOR_TEXT_SECONDARY));
        return button;
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
