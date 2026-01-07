package io.github.kullik01.focusbean.view;

import io.github.kullik01.focusbean.controller.TimerController;
import io.github.kullik01.focusbean.model.TimerState;
import io.github.kullik01.focusbean.model.UserSettings;
import io.github.kullik01.focusbean.util.AppConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * The main view assembling all UI components.
 *
 * <p>
 * This view contains the timer display, control panel, history view,
 * and settings button. It handles keyboard shortcuts and coordinates
 * updates between view components and the controller.
 * </p>
 */
public final class MainView extends BorderPane {

    private static final Logger LOGGER = Logger.getLogger(MainView.class.getName());

    private static final String STYLE_SETTINGS_BUTTON = """
            -fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;
            -fx-font-size: 12px;
            -fx-background-color: transparent;
            -fx-text-fill: %s;
            -fx-cursor: hand;
            """;

    private final TimerController controller;
    private final TimerDisplayView timerDisplay;
    private final ControlPanelView controlPanel;
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
        historyView = new HistoryView();
        settingsButton = createSettingsButton();

        // Create timer tab content
        VBox timerPane = new VBox(20);
        timerPane.setAlignment(Pos.CENTER);
        timerPane.setPadding(new Insets(30, 20, 20, 20));

        HBox settingsBar = new HBox(settingsButton);
        settingsBar.setAlignment(Pos.TOP_RIGHT);
        settingsBar.setPadding(new Insets(10));

        VBox.setVgrow(timerDisplay, Priority.ALWAYS);
        timerPane.getChildren().addAll(settingsBar, timerDisplay, controlPanel);

        // Create tabs
        Tab timerTab = new Tab("Timer", timerPane);
        timerTab.setClosable(false);

        Tab historyTab = new Tab(AppConstants.LABEL_HISTORY, historyView);
        historyTab.setClosable(false);

        tabPane = new TabPane(timerTab, historyTab);
        tabPane.getSelectionModel().select(timerTab);

        // Update history when tab is selected
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == historyTab) {
                historyView.update(controller.getHistory());
            }
        });

        setCenter(tabPane);

        // Wire up event handlers
        wireEventHandlers();

        // Bind to controller properties
        bindToController();

        // Initialize display with current settings
        timerDisplay.updateState(TimerState.IDLE);
        timerDisplay.showDuration(controller.getSettings().getWorkDurationMinutes());
    }

    /**
     * Wires event handlers from view components to controller actions.
     */
    private void wireEventHandlers() {
        controlPanel.setOnStart(controller::startWork);
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
                .addListener((obs, oldVal, newVal) -> timerDisplay.updateTime(newVal.intValue()));

        // Update state display and button states when state changes
        controller.currentStateProperty().addListener((obs, oldState, newState) -> {
            timerDisplay.updateState(newState);
            controlPanel.updateForState(newState, controller.getStateBeforePause());

            // When returning to IDLE, show configured work duration
            if (newState == TimerState.IDLE) {
                timerDisplay.showDuration(controller.getSettings().getWorkDurationMinutes());
            }
        });
    }

    /**
     * Shows the settings dialog and applies changes if confirmed.
     */
    private void showSettingsDialog() {
        SettingsDialog dialog = new SettingsDialog(controller.getSettings());
        Optional<UserSettings> result = dialog.showAndGetResult();

        result.ifPresent(settings -> {
            controller.updateSettings(
                    settings.getWorkDurationMinutes(),
                    settings.getBreakDurationMinutes());

            // Update display if idle
            if (controller.getCurrentState() == TimerState.IDLE) {
                timerDisplay.showDuration(settings.getWorkDurationMinutes());
            }

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
        Button button = new Button("⚙ " + AppConstants.LABEL_SETTINGS);
        button.setStyle(String.format(STYLE_SETTINGS_BUTTON, AppConstants.COLOR_TEXT_SECONDARY));
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
