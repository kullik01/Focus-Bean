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

import io.github.kullik01.focusbean.model.TimerState;
import io.github.kullik01.focusbean.util.AppConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.Objects;

/**
 * Contains the timer control buttons with modern icon-based design.
 *
 * <p>
 * Features a primary circular button for start/pause/resume actions
 * and a secondary reset button to return the timer to its initial state.
 * The design follows the Windows Clock Focus Sessions style.
 * </p>
 */
public final class ControlPanelView extends HBox {

    private static final double ICON_BUTTON_SIZE = 44;
    private static final double MENU_BUTTON_SIZE = 36;
    private static final double BUTTON_SPACING = 12;
    private static final double PANEL_PADDING = 0;

    private static final String STYLE_PRIMARY_BUTTON = """
            -fx-font-size: 18px;
            -fx-background-radius: 50;
            -fx-min-width: %fpx;
            -fx-min-height: %fpx;
            -fx-max-width: %fpx;
            -fx-max-height: %fpx;
            -fx-cursor: hand;
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-alignment: center;
            -fx-content-display: center;
            """;

    private static final String STYLE_SECONDARY_BUTTON = """
            -fx-font-size: 14px;
            -fx-background-radius: 50;
            -fx-min-width: %fpx;
            -fx-min-height: %fpx;
            -fx-max-width: %fpx;
            -fx-max-height: %fpx;
            -fx-cursor: hand;
            -fx-background-color: transparent;
            -fx-text-fill: %s;
            -fx-border-color: %s;
            -fx-border-radius: 50;
            -fx-border-width: 1;
            """;

    private static final String ICON_PLAY = "▶";
    private static final String ICON_PAUSE = "⏸";
    private static final String ICON_RESET = "⟲";

    private final Button startPauseButton;
    private final Button resetButton;
    private final Button skipButton;

    private Runnable onStart;
    private Runnable onPause;
    private Runnable onResume;
    private Runnable onReset;
    private Runnable onSkip;

    /**
     * Creates a new ControlPanelView with modern icon-based layout.
     */
    public ControlPanelView() {
        startPauseButton = createPrimaryButton(ICON_PLAY);
        resetButton = createSecondaryButton(ICON_RESET);
        skipButton = createSecondaryButton("⏭");

        // Hide skip button by default for cleaner look
        skipButton.setVisible(false);
        skipButton.setManaged(false);

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
        getChildren().addAll(startPauseButton, resetButton);

        // Initial state: ready to start
        updateForState(TimerState.IDLE);
    }

    /**
     * Updates button icons and enabled states for the given timer state.
     *
     * @param state the current timer state
     */
    public void updateForState(TimerState state) {
        if (state == null) {
            state = TimerState.IDLE;
        }

        switch (state) {
            case IDLE -> {
                startPauseButton.setText(ICON_PLAY);
                startPauseButton.setPadding(new Insets(0, 0, 0, 2)); // Left padding for play icon
                startPauseButton.setDisable(false);
                resetButton.setDisable(true);
                resetButton.setOpacity(0.4);
            }
            case WORK, BREAK -> {
                startPauseButton.setText(ICON_PAUSE);
                startPauseButton.setPadding(new Insets(0, 0, 0, 0)); // No padding for pause icon
                startPauseButton.setDisable(false);
                resetButton.setDisable(false);
                resetButton.setOpacity(1.0);
            }
            case PAUSED -> {
                startPauseButton.setText(ICON_PLAY);
                startPauseButton.setPadding(new Insets(0, 0, 0, 2)); // Left padding for play icon
                startPauseButton.setDisable(false);
                resetButton.setDisable(false);
                resetButton.setOpacity(1.0);
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
     * Handles the start/pause button click based on current icon.
     */
    private void handleStartPauseClick() {
        String currentIcon = startPauseButton.getText();

        if (ICON_PLAY.equals(currentIcon)) {
            // Could be start or resume
            if (onStart != null) {
                onStart.run();
            }
        } else if (ICON_PAUSE.equals(currentIcon)) {
            if (onPause != null) {
                onPause.run();
            }
        }
    }

    /**
     * Creates a primary (circular, filled) button with the given icon.
     *
     * @param icon the button icon text
     * @return the configured button
     */
    private Button createPrimaryButton(String icon) {
        Button button = new Button(icon);
        button.setStyle(String.format(STYLE_PRIMARY_BUTTON,
                ICON_BUTTON_SIZE, ICON_BUTTON_SIZE,
                ICON_BUTTON_SIZE, ICON_BUTTON_SIZE,
                AppConstants.COLOR_PROGRESS_ACTIVE));
        button.setAlignment(Pos.CENTER);
        button.setContentDisplay(javafx.scene.control.ContentDisplay.CENTER);
        return button;
    }

    /**
     * Creates a secondary (outlined) button with the given icon.
     *
     * @param icon the button icon text
     * @return the configured button
     */
    private Button createSecondaryButton(String icon) {
        Button button = new Button(icon);
        button.setStyle(String.format(STYLE_SECONDARY_BUTTON,
                MENU_BUTTON_SIZE, MENU_BUTTON_SIZE,
                MENU_BUTTON_SIZE, MENU_BUTTON_SIZE,
                AppConstants.COLOR_TEXT_SECONDARY,
                AppConstants.COLOR_CARD_BORDER));
        return button;
    }
}
