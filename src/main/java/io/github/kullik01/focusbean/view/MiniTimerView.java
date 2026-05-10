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
import io.github.kullik01.focusbean.util.AppConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * A compact, always-on-top floating timer widget.
 *
 * <p>
 * This view provides a minimal circular timer display with a play/pause button,
 * designed to float above other windows for constant visibility without taking
 * up significant screen space. It shares the same {@link TimerController} as the
 * main window so both views stay in sync.
 * </p>
 *
 * <p>
 * Features:
 * </p>
 * <ul>
 * <li>Circular progress ring with time display</li>
 * <li>Play/pause button</li>
 * <li>State-aware color coding (work/break/paused)</li>
 * <li>Draggable window</li>
 * <li>Right-click context menu (Show Full Window, Reset, Close)</li>
 * <li>Keyboard shortcuts: Space (start/pause), R (reset)</li>
 * <li>Dark mode support</li>
 * </ul>
 */
public final class MiniTimerView extends StackPane {

    private static final Logger LOGGER = Logger.getLogger(MiniTimerView.class.getName());

    private static final double VIEW_SIZE = 170;
    private static final double RING_SIZE = 142;
    private static final double RING_STROKE_WIDTH = 5;
    private static final double DOT_RADIUS = 6;
    private static final double BUTTON_SIZE = 32;
    private static final String FONT_FAMILY = "'Segoe UI', 'Helvetica Neue', sans-serif";

    private static final String ICON_PLAY = "▶";
    private static final String ICON_PAUSE = "⏸";

    private final TimerController controller;
    private final Canvas progressCanvas;
    private final Label timeLabel;
    private final Label unitLabel;
    private final Label stateLabel;
    private final Button startPauseButton;
    private final ContextMenu contextMenu;

    private int totalSeconds;
    private int remainingSeconds;
    private TimerState currentState;
    private boolean darkMode;

    private Runnable onShowFullWindow;
    private Runnable onCloseMiniMode;
    private Runnable onMinimize;

    /**
     * Creates a new MiniTimerView wired to the given controller.
     *
     * @param controller the timer controller to bind to
     * @param darkMode   whether dark mode is enabled
     * @throws NullPointerException if controller is null
     */
    public MiniTimerView(TimerController controller, boolean darkMode) {
        this.controller = Objects.requireNonNull(controller, "controller must not be null");
        this.darkMode = darkMode;
        this.totalSeconds = controller.getSettings().getWorkDurationSeconds();
        this.remainingSeconds = controller.getRemainingSeconds();
        this.currentState = controller.getCurrentState();

        // --- Build UI ---

        // Progress canvas
        progressCanvas = new Canvas(RING_SIZE, RING_SIZE);

        // Time label
        timeLabel = new Label();
        timeLabel.setFont(Font.font(FONT_FAMILY, FontWeight.LIGHT, 32));

        // Unit label (min / sec)
        unitLabel = new Label("min");
        unitLabel.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 12));

        // State label (Working / Break / Paused)
        stateLabel = new Label();
        stateLabel.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 10));

        // Center content over the ring
        VBox centerContent = new VBox(0);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setMouseTransparent(true);
        centerContent.getChildren().addAll(stateLabel, timeLabel, unitLabel);

        // Start/Pause button
        startPauseButton = new Button(ICON_PLAY);
        startPauseButton.setFont(Font.font(12));
        startPauseButton.setAlignment(Pos.CENTER);
        startPauseButton.setContentDisplay(javafx.scene.control.ContentDisplay.CENTER);
        startPauseButton.setOnAction(e -> handleStartPauseClick());

        // Stack the ring, center text, and position button at bottom
        StackPane ringStack = new StackPane(progressCanvas, centerContent);
        ringStack.setAlignment(Pos.CENTER);

        // Expand button (restore full window) - small icon at top-right
        Button expandButton = createExpandButton();

        // Top bar with minimize and expand buttons pushed to the right
        HBox topBar = new HBox(2);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(4, 4, 0, 4));
        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);
        topBar.getChildren().addAll(topSpacer, createMinimizeButton(), expandButton);

        VBox layout = new VBox(2);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(0, 10, 10, 10));
        layout.getChildren().addAll(topBar, ringStack, startPauseButton);

        setAlignment(Pos.CENTER);
        setPrefSize(VIEW_SIZE, VIEW_SIZE + 30);
        getChildren().add(layout);

        // Context menu
        contextMenu = createContextMenu();
        setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(this, event.getScreenX(), event.getScreenY());
            } else if (event.getButton() == MouseButton.PRIMARY
                    && event.getClickCount() == 2) {
                // Double-click to restore full window
                if (onShowFullWindow != null) {
                    onShowFullWindow.run();
                }
            } else {
                contextMenu.hide();
            }
        });

        // Apply theme & initial display
        applyTheme(darkMode);
        bindToController();
        refreshDisplay();

        LOGGER.fine("MiniTimerView initialized");
    }

    /**
     * Sets the callback for showing the full application window.
     *
     * @param handler the callback to invoke
     */
    public void setOnShowFullWindow(Runnable handler) {
        this.onShowFullWindow = handler;
    }

    /**
     * Sets the callback for closing mini mode.
     *
     * @param handler the callback to invoke
     */
    public void setOnCloseMiniMode(Runnable handler) {
        this.onCloseMiniMode = handler;
    }

    /**
     * Sets the callback for minimizing the mini window.
     *
     * @param handler the callback to invoke
     */
    public void setOnMinimize(Runnable handler) {
        this.onMinimize = handler;
    }

    /**
     * Applies the given theme to all visual elements.
     *
     * @param darkMode true for dark theme, false for light theme
     */
    public void applyTheme(boolean darkMode) {
        this.darkMode = darkMode;

        String bgColor = darkMode
                ? AppConstants.COLOR_WINDOW_BACKGROUND_DARK
                : AppConstants.COLOR_WINDOW_BACKGROUND;

        setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 20;
                """, bgColor));

        String textPrimary = darkMode
                ? AppConstants.COLOR_TEXT_PRIMARY_DARK
                : AppConstants.COLOR_TEXT_PRIMARY;
        String textSecondary = darkMode
                ? AppConstants.COLOR_TEXT_SECONDARY_DARK
                : AppConstants.COLOR_TEXT_SECONDARY;

        timeLabel.setTextFill(Color.web(textPrimary));
        unitLabel.setTextFill(Color.web(textSecondary));

        updateButtonStyle();
        updateStateLabel();
        drawProgressRing();
    }

    /**
     * Handles keyboard shortcuts within the mini view.
     *
     * @param event the key event
     */
    public void handleKeyPress(javafx.scene.input.KeyEvent event) {
        switch (event.getCode()) {
            case SPACE -> {
                handleStartPauseClick();
                event.consume();
            }
            case R -> {
                if (!event.isControlDown()) {
                    controller.reset();
                    event.consume();
                }
            }
            default -> {
                // No action
            }
        }
    }

    // =========================================================================
    // Private Methods
    // =========================================================================

    /**
     * Binds to the shared TimerController's observable properties.
     */
    private void bindToController() {
        // Remaining seconds updates
        controller.remainingSecondsProperty().addListener((obs, oldVal, newVal) -> {
            this.remainingSeconds = newVal.intValue();
            updateTimeDisplay();
            drawProgressRing();
        });

        // State changes
        controller.currentStateProperty().addListener((obs, oldState, newState) -> {
            this.currentState = newState;

            // When entering a new session from IDLE, capture total seconds
            if (oldState == TimerState.IDLE) {
                if (newState == TimerState.WORK) {
                    this.totalSeconds = controller.getSettings().getWorkDurationSeconds();
                } else if (newState == TimerState.BREAK) {
                    this.totalSeconds = controller.getSettings().getBreakDurationSeconds();
                }
            }

            // When returning to IDLE, show the pending duration
            if (newState == TimerState.IDLE) {
                if (controller.getPendingSessionType() == TimerState.BREAK) {
                    showDuration(controller.getSettings().getBreakDurationMinutes(), "Break");
                } else {
                    showDuration(controller.getSettings().getWorkDurationMinutes(), "min");
                }
            }

            updateButtonIcon();
            updateStateLabel();
            drawProgressRing();
        });
    }

    /**
     * Refreshes all display elements to match the current controller state.
     */
    private void refreshDisplay() {
        this.currentState = controller.getCurrentState();
        this.remainingSeconds = controller.getRemainingSeconds();

        if (currentState == TimerState.IDLE) {
            if (controller.getPendingSessionType() == TimerState.BREAK) {
                showDuration(controller.getSettings().getBreakDurationMinutes(), "Break");
            } else {
                showDuration(controller.getSettings().getWorkDurationMinutes(), "min");
            }
        } else {
            if (currentState == TimerState.WORK
                    || (currentState == TimerState.PAUSED
                            && controller.getStateBeforePause() == TimerState.WORK)) {
                this.totalSeconds = controller.getSettings().getWorkDurationSeconds();
            } else {
                this.totalSeconds = controller.getSettings().getBreakDurationSeconds();
            }
            updateTimeDisplay();
        }

        updateButtonIcon();
        updateStateLabel();
        drawProgressRing();
    }

    /**
     * Shows a static duration (used when IDLE).
     */
    private void showDuration(int durationMinutes, String label) {
        this.totalSeconds = durationMinutes * 60;
        this.remainingSeconds = totalSeconds;
        timeLabel.setText(String.valueOf(durationMinutes));
        unitLabel.setText(label);
    }

    /**
     * Updates the time label from remaining seconds.
     */
    private void updateTimeDisplay() {
        if (remainingSeconds >= 60) {
            int displayMinutes = (remainingSeconds + 59) / 60;
            timeLabel.setText(String.valueOf(displayMinutes));
            unitLabel.setText("min");
        } else if (remainingSeconds >= 10) {
            timeLabel.setText(String.format("%02d", remainingSeconds));
            unitLabel.setText("sec");
        } else {
            timeLabel.setText(String.valueOf(remainingSeconds));
            unitLabel.setText("sec");
        }
    }

    /**
     * Updates the state label text and color.
     */
    private void updateStateLabel() {
        if (currentState == null) {
            stateLabel.setText("");
            return;
        }

        String stateColor;
        switch (currentState) {
            case WORK -> {
                stateLabel.setText("Focus");
                stateColor = AppConstants.COLOR_ACCENT;
            }
            case BREAK -> {
                stateLabel.setText("Break");
                stateColor = darkMode
                        ? AppConstants.COLOR_TEXT_SECONDARY_DARK
                        : AppConstants.COLOR_TEXT_SECONDARY;
            }
            case PAUSED -> {
                stateLabel.setText("Paused");
                stateColor = darkMode
                        ? AppConstants.COLOR_TEXT_SECONDARY_DARK
                        : AppConstants.COLOR_TEXT_SECONDARY;
            }
            case IDLE -> {
                if (controller.getPendingSessionType() == TimerState.BREAK) {
                    stateLabel.setText("Break");
                } else {
                    stateLabel.setText("Focus");
                }
                stateColor = darkMode
                        ? AppConstants.COLOR_TEXT_SECONDARY_DARK
                        : AppConstants.COLOR_TEXT_SECONDARY;
            }
            default -> {
                stateLabel.setText("");
                stateColor = darkMode
                        ? AppConstants.COLOR_TEXT_SECONDARY_DARK
                        : AppConstants.COLOR_TEXT_SECONDARY;
            }
        }
        stateLabel.setTextFill(Color.web(stateColor));
    }

    /**
     * Updates the start/pause button icon based on the current state.
     */
    private void updateButtonIcon() {
        switch (currentState) {
            case WORK, BREAK -> {
                startPauseButton.setText(ICON_PAUSE);
                startPauseButton.setPadding(new Insets(0));
            }
            case IDLE, PAUSED -> {
                startPauseButton.setText(ICON_PLAY);
                startPauseButton.setPadding(new Insets(0, 0, 0, 2));
            }
        }
        updateButtonStyle();
    }

    /**
     * Applies theme-aware styling to the start/pause button.
     */
    private void updateButtonStyle() {
        startPauseButton.setStyle(String.format("""
                -fx-font-size: 12px;
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
                """, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE,
                AppConstants.COLOR_PROGRESS_ACTIVE));
    }

    /**
     * Handles the start/pause button click.
     */
    private void handleStartPauseClick() {
        switch (currentState) {
            case IDLE -> controller.startOrResume();
            case WORK, BREAK -> controller.pause();
            case PAUSED -> controller.resume();
        }
    }

    /**
     * Draws the circular progress ring on the canvas.
     */
    private void drawProgressRing() {
        GraphicsContext gc = progressCanvas.getGraphicsContext2D();
        double width = progressCanvas.getWidth();
        double height = progressCanvas.getHeight();
        double centerX = width / 2;
        double centerY = height / 2;
        double radius = (Math.min(width, height) - RING_STROKE_WIDTH * 2 - DOT_RADIUS * 2) / 2;

        // Clear canvas
        gc.clearRect(0, 0, width, height);

        // Background ring
        String ringBgColor = darkMode
                ? AppConstants.COLOR_PROGRESS_RING_DARK
                : AppConstants.COLOR_PROGRESS_RING;
        gc.setStroke(Color.web(ringBgColor));
        gc.setLineWidth(RING_STROKE_WIDTH);
        gc.strokeOval(
                centerX - radius,
                centerY - radius,
                radius * 2,
                radius * 2);

        // Progress arc (only when timer is active)
        if (currentState != null && currentState != TimerState.IDLE && totalSeconds > 0) {
            double progress = 1.0 - ((double) remainingSeconds / totalSeconds);
            double sweepAngle = progress * 360;

            // Determine arc color based on state
            String arcColor;
            if (currentState == TimerState.BREAK) {
                arcColor = AppConstants.COLOR_BREAK_BACKGROUND;
            } else {
                arcColor = AppConstants.COLOR_PROGRESS_ACTIVE;
            }

            // When paused, use reduced opacity to visually distinguish
            double arcOpacity = (currentState == TimerState.PAUSED) ? 0.5 : 1.0;

            gc.setGlobalAlpha(arcOpacity);
            gc.setStroke(Color.web(arcColor));
            gc.setLineWidth(RING_STROKE_WIDTH);
            gc.strokeArc(
                    centerX - radius,
                    centerY - radius,
                    radius * 2,
                    radius * 2,
                    90,
                    -sweepAngle,
                    ArcType.OPEN);

            // Progress indicator dot
            double angle = Math.toRadians(progress * 360 - 90);
            double dotX = centerX + radius * Math.cos(angle);
            double dotY = centerY + radius * Math.sin(angle);

            gc.setFill(Color.web(arcColor));
            gc.fillOval(dotX - DOT_RADIUS, dotY - DOT_RADIUS, DOT_RADIUS * 2, DOT_RADIUS * 2);
            gc.setGlobalAlpha(1.0);
        }
    }

    /**
     * Creates the right-click context menu.
     *
     * @return the configured context menu
     */
    private ContextMenu createContextMenu() {
        MenuItem showFullItem = new MenuItem("Show Full Window");
        showFullItem.setOnAction(e -> {
            if (onShowFullWindow != null) {
                onShowFullWindow.run();
            }
        });

        MenuItem resetItem = new MenuItem("Reset Timer");
        resetItem.setOnAction(e -> controller.reset());

        MenuItem closeItem = new MenuItem("Close Mini Mode");
        closeItem.setOnAction(e -> {
            if (onCloseMiniMode != null) {
                onCloseMiniMode.run();
            }
        });

        ContextMenu menu = new ContextMenu();
        menu.getItems().addAll(showFullItem, resetItem, new SeparatorMenuItem(), closeItem);
        return menu;
    }

    /**
     * Creates the expand button to restore the full application window.
     *
     * <p>
     * Uses a "full screen" / expand icon so users have a visible way to
     * return to the main window without needing the right-click context menu.
     * </p>
     *
     * @return the configured expand button
     */
    private Button createExpandButton() {
        // Expand / full-screen icon (arrows pointing outward)
        SVGPath expandIcon = new SVGPath();
        expandIcon.setContent(
                "M7 14H5v5h5v-2H7v-3zm-2-4h2V7h3V5H5v5zm12 7h-3v2h5v-5h-2v3zM14 5v2h3v3h2V5h-5z");

        // Theme-aware icon color
        String iconColor = darkMode
                ? AppConstants.COLOR_TEXT_SECONDARY_DARK
                : AppConstants.COLOR_ACCENT;
        String iconHoverColor = darkMode
                ? AppConstants.COLOR_TEXT_PRIMARY_DARK
                : AppConstants.COLOR_PROGRESS_ACTIVE;
        String hoverBg = darkMode
                ? "rgba(255, 255, 255, 0.08)"
                : "rgba(160, 82, 45, 0.12)";
        String tooltipBg = darkMode
                ? AppConstants.COLOR_CARD_BACKGROUND_DARK
                : AppConstants.COLOR_CARD_BACKGROUND;
        String tooltipText = darkMode
                ? AppConstants.COLOR_TEXT_PRIMARY_DARK
                : AppConstants.COLOR_TEXT_PRIMARY;

        expandIcon.setFill(Color.web(iconColor));
        expandIcon.setScaleX(0.6);
        expandIcon.setScaleY(0.6);

        Button button = new Button();
        button.setGraphic(expandIcon);
        button.setStyle("""
                -fx-background-color: transparent;
                -fx-cursor: hand;
                -fx-padding: 2;
                """);

        button.setOnMouseEntered(e -> {
            button.setStyle(String.format("""
                    -fx-background-color: %s;
                    -fx-background-radius: 6;
                    -fx-cursor: hand;
                    -fx-padding: 2;
                    """, hoverBg));
            expandIcon.setFill(Color.web(iconHoverColor));
        });

        button.setOnMouseExited(e -> {
            button.setStyle("""
                    -fx-background-color: transparent;
                    -fx-cursor: hand;
                    -fx-padding: 2;
                    """);
            expandIcon.setFill(Color.web(iconColor));
        });

        button.setOnAction(e -> {
            if (onShowFullWindow != null) {
                onShowFullWindow.run();
            }
        });

        // Tooltip
        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip("Expand");
        tooltip.setShowDelay(new javafx.util.Duration(0));
        tooltip.setStyle(String.format("""
                -fx-font-family: 'Segoe UI', sans-serif;
                -fx-font-size: 12px;
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-background-radius: 6;
                -fx-padding: 6 10 6 10;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 1);
                """, tooltipBg, tooltipText));
        button.setTooltip(tooltip);

        return button;
    }

    /**
     * Creates the minimize button for the mini window.
     *
     * <p>
     * Uses a horizontal line icon matching the main window's minimize button
     * style, with theme-aware colors.
     * </p>
     *
     * @return the configured minimize button
     */
    private Button createMinimizeButton() {
        // Minimize icon (horizontal line)
        SVGPath minimizeIcon = new SVGPath();
        minimizeIcon.setContent("M4 12h16");

        // Theme-aware colors
        String iconColor = darkMode
                ? AppConstants.COLOR_TEXT_SECONDARY_DARK
                : AppConstants.COLOR_ACCENT;
        String iconHoverColor = darkMode
                ? AppConstants.COLOR_TEXT_PRIMARY_DARK
                : AppConstants.COLOR_PROGRESS_ACTIVE;
        String hoverBg = darkMode
                ? "rgba(255, 255, 255, 0.08)"
                : "rgba(160, 82, 45, 0.12)";
        String tooltipBg = darkMode
                ? AppConstants.COLOR_CARD_BACKGROUND_DARK
                : AppConstants.COLOR_CARD_BACKGROUND;
        String tooltipText = darkMode
                ? AppConstants.COLOR_TEXT_PRIMARY_DARK
                : AppConstants.COLOR_TEXT_PRIMARY;

        minimizeIcon.setStroke(Color.web(iconColor));
        minimizeIcon.setStrokeWidth(1.5);
        minimizeIcon.setFill(Color.TRANSPARENT);
        minimizeIcon.setScaleX(0.55);
        minimizeIcon.setScaleY(0.55);

        Button button = new Button();
        button.setGraphic(minimizeIcon);
        button.setStyle("""
                -fx-background-color: transparent;
                -fx-cursor: hand;
                -fx-padding: 2;
                """);

        button.setOnMouseEntered(e -> {
            button.setStyle(String.format("""
                    -fx-background-color: %s;
                    -fx-background-radius: 6;
                    -fx-cursor: hand;
                    -fx-padding: 2;
                    """, hoverBg));
            minimizeIcon.setStroke(Color.web(iconHoverColor));
        });

        button.setOnMouseExited(e -> {
            button.setStyle("""
                    -fx-background-color: transparent;
                    -fx-cursor: hand;
                    -fx-padding: 2;
                    """);
            minimizeIcon.setStroke(Color.web(iconColor));
        });

        button.setOnAction(e -> {
            if (onMinimize != null) {
                onMinimize.run();
            }
        });

        // Tooltip
        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip("Minimize");
        tooltip.setShowDelay(new javafx.util.Duration(0));
        tooltip.setStyle(String.format("""
                -fx-font-family: 'Segoe UI', sans-serif;
                -fx-font-size: 12px;
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-background-radius: 6;
                -fx-padding: 6 10 6 10;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 1);
                """, tooltipBg, tooltipText));
        button.setTooltip(tooltip);

        return button;
    }
}
