package io.github.kullik01.focusbean.view;

import io.github.kullik01.focusbean.model.TimerState;
import io.github.kullik01.focusbean.util.AppConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.Objects;

/**
 * Contains the timer control buttons (Start/Pause, Reset, Skip).
 *
 * <p>
 * The button labels and enabled states change based on the current
 * timer state. The view provides hooks for external event handling.
 * </p>
 */
public final class ControlPanelView extends HBox {

    private static final double BUTTON_WIDTH = 80;
    private static final double BUTTON_HEIGHT = 36;
    private static final double BUTTON_SPACING = 15;
    private static final double PANEL_PADDING = 20;

    private static final String STYLE_BUTTON = """
            -fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;
            -fx-font-size: 14px;
            -fx-font-weight: 500;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            """;

    private static final String STYLE_PRIMARY_BUTTON = STYLE_BUTTON + """
            -fx-background-color: %s;
            -fx-text-fill: white;
            """;

    private static final String STYLE_SECONDARY_BUTTON = STYLE_BUTTON + """
            -fx-background-color: #e0e0e0;
            -fx-text-fill: #333333;
            """;

    private final Button startPauseButton;
    private final Button resetButton;
    private final Button skipButton;

    private Runnable onStart;
    private Runnable onPause;
    private Runnable onResume;
    private Runnable onReset;
    private Runnable onSkip;

    /**
     * Creates a new ControlPanelView with default button layout.
     */
    public ControlPanelView() {
        startPauseButton = createButton(AppConstants.LABEL_START);
        resetButton = createButton(AppConstants.LABEL_RESET);
        skipButton = createButton(AppConstants.LABEL_SKIP);

        startPauseButton.setStyle(String.format(STYLE_PRIMARY_BUTTON, AppConstants.COLOR_ACCENT));
        resetButton.setStyle(STYLE_SECONDARY_BUTTON);
        skipButton.setStyle(STYLE_SECONDARY_BUTTON);

        // Set up event handlers
        startPauseButton.setOnAction(e -> handleStartPauseClick());
        resetButton.setOnAction(e -> {
            if (onReset != null) {
                onReset.run();
            }
        });
        skipButton.setOnAction(e -> {
            if (onSkip != null) {
                onSkip.run();
            }
        });

        // Layout configuration
        setSpacing(BUTTON_SPACING);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(PANEL_PADDING));
        getChildren().addAll(startPauseButton, resetButton, skipButton);

        // Initial state: only start button enabled
        updateForState(TimerState.IDLE);
    }

    /**
     * Updates button labels and enabled states for the given timer state.
     *
     * @param state the current timer state
     */
    public void updateForState(TimerState state) {
        if (state == null) {
            state = TimerState.IDLE;
        }

        switch (state) {
            case IDLE -> {
                startPauseButton.setText(AppConstants.LABEL_START);
                startPauseButton.setDisable(false);
                resetButton.setDisable(true);
                skipButton.setDisable(true);
            }
            case WORK, BREAK -> {
                startPauseButton.setText(AppConstants.LABEL_PAUSE);
                startPauseButton.setDisable(false);
                resetButton.setDisable(false);
                skipButton.setDisable(false);
            }
            case PAUSED -> {
                startPauseButton.setText(AppConstants.LABEL_RESUME);
                startPauseButton.setDisable(false);
                resetButton.setDisable(false);
                skipButton.setDisable(false);
            }
        }
    }

    /**
     * Updates button labels and states for the given timer state,
     * considering the state before pause.
     *
     * @param state            the current timer state
     * @param stateBeforePause the state before pause, for context
     */
    public void updateForState(TimerState state, TimerState stateBeforePause) {
        updateForState(state);
    }

    /**
     * Sets the callback for the start action.
     *
     * @param handler the callback to invoke when start is clicked
     */
    public void setOnStart(Runnable handler) {
        this.onStart = handler;
    }

    /**
     * Sets the callback for the pause action.
     *
     * @param handler the callback to invoke when pause is clicked
     */
    public void setOnPause(Runnable handler) {
        this.onPause = handler;
    }

    /**
     * Sets the callback for the resume action.
     *
     * @param handler the callback to invoke when resume is clicked
     */
    public void setOnResume(Runnable handler) {
        this.onResume = handler;
    }

    /**
     * Sets the callback for the reset action.
     *
     * @param handler the callback to invoke when reset is clicked
     */
    public void setOnReset(Runnable handler) {
        this.onReset = handler;
    }

    /**
     * Sets the callback for the skip action.
     *
     * @param handler the callback to invoke when skip is clicked
     */
    public void setOnSkip(Runnable handler) {
        this.onSkip = handler;
    }

    /**
     * Returns the start/pause button for keyboard shortcut binding.
     *
     * @return the start/pause button
     */
    public Button getStartPauseButton() {
        return startPauseButton;
    }

    /**
     * Returns the reset button for keyboard shortcut binding.
     *
     * @return the reset button
     */
    public Button getResetButton() {
        return resetButton;
    }

    /**
     * Returns the skip button for keyboard shortcut binding.
     *
     * @return the skip button
     */
    public Button getSkipButton() {
        return skipButton;
    }

    /**
     * Handles the start/pause button click based on current label.
     */
    private void handleStartPauseClick() {
        String currentLabel = startPauseButton.getText();

        if (AppConstants.LABEL_START.equals(currentLabel)) {
            if (onStart != null) {
                onStart.run();
            }
        } else if (AppConstants.LABEL_PAUSE.equals(currentLabel)) {
            if (onPause != null) {
                onPause.run();
            }
        } else if (AppConstants.LABEL_RESUME.equals(currentLabel)) {
            if (onResume != null) {
                onResume.run();
            }
        }
    }

    /**
     * Creates a styled button with consistent size.
     *
     * @param text the button label
     * @return the configured button
     */
    private Button createButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(BUTTON_WIDTH);
        button.setPrefHeight(BUTTON_HEIGHT);
        return button;
    }
}
