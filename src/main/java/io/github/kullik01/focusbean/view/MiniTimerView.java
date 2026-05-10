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
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.util.Duration;
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
 * <p>This view provides a minimal circular timer display with a play/pause button,
 * designed to float above other windows for constant visibility without taking
 * up significant screen space. It shares the same {@link TimerController} as the
 * main window so both views stay in sync.</p>
 *
 * @author Hannah Kullik
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
     * @param controller The timer controller to bind to
     * @param darkMode Whether dark mode is enabled
     * @throws NullPointerException If controller is null
     */
    public MiniTimerView(TimerController controller, boolean darkMode) {
        this.controller = Objects.requireNonNull(controller, "controller must not be null");
        this.darkMode = darkMode;
        this.totalSeconds = this.controller.getSettings().getWorkDurationSeconds();
        this.remainingSeconds = this.controller.getRemainingSeconds();
        this.currentState = this.controller.getCurrentState();

        this.progressCanvas = new Canvas(RING_SIZE, RING_SIZE);

        this.timeLabel = new Label();
        this.timeLabel.setFont(Font.font(FONT_FAMILY, FontWeight.LIGHT, 32));

        this.unitLabel = new Label("min");
        this.unitLabel.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 12));

        this.stateLabel = new Label();
        this.stateLabel.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 10));

        VBox tmpCenterContent = new VBox(0);
        tmpCenterContent.setAlignment(Pos.CENTER);
        tmpCenterContent.setMouseTransparent(true);
        tmpCenterContent.getChildren().addAll(this.stateLabel, this.timeLabel, this.unitLabel);

        this.startPauseButton = new Button(ICON_PLAY);
        this.startPauseButton.setFont(Font.font(12));
        this.startPauseButton.setAlignment(Pos.CENTER);
        this.startPauseButton.setContentDisplay(ContentDisplay.CENTER);
        this.startPauseButton.setOnAction(event -> this.handleStartPauseClick());

        StackPane tmpRingStack = new StackPane(this.progressCanvas, tmpCenterContent);
        tmpRingStack.setAlignment(Pos.CENTER);

        Button tmpExpandButton = this.createExpandButton();

        HBox tmpTopBar = new HBox(2);
        tmpTopBar.setAlignment(Pos.CENTER_RIGHT);
        tmpTopBar.setPadding(new Insets(4, 4, 0, 4));
        Region tmpTopSpacer = new Region();
        HBox.setHgrow(tmpTopSpacer, Priority.ALWAYS);
        tmpTopBar.getChildren().addAll(
                tmpTopSpacer, this.createMinimizeButton(), tmpExpandButton);

        VBox tmpLayout = new VBox(2);
        tmpLayout.setAlignment(Pos.CENTER);
        tmpLayout.setPadding(new Insets(0, 10, 10, 10));
        tmpLayout.getChildren().addAll(
                tmpTopBar, tmpRingStack, this.startPauseButton);

        this.setAlignment(Pos.CENTER);
        this.setPrefSize(VIEW_SIZE, VIEW_SIZE + 30);
        this.getChildren().add(tmpLayout);

        this.contextMenu = this.createContextMenu();
        this.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                this.contextMenu.show(this, event.getScreenX(), event.getScreenY());
            } else if (event.getButton() == MouseButton.PRIMARY
                    && event.getClickCount() == 2) {
                if (this.onShowFullWindow != null) {
                    this.onShowFullWindow.run();
                }
            } else {
                this.contextMenu.hide();
            }
        });

        this.applyTheme(darkMode);
        this.bindToController();
        this.refreshDisplay();

        LOGGER.fine("MiniTimerView initialized");
    }

    /**
     * Sets the callback for showing the full application window.
     *
     * @param handler The callback to invoke
     */
    public void setOnShowFullWindow(Runnable handler) {
        this.onShowFullWindow = handler;
    }

    /**
     * Sets the callback for closing mini mode.
     *
     * @param handler The callback to invoke
     */
    public void setOnCloseMiniMode(Runnable handler) {
        this.onCloseMiniMode = handler;
    }

    /**
     * Sets the callback for minimizing the mini window.
     *
     * @param handler The callback to invoke
     */
    public void setOnMinimize(Runnable handler) {
        this.onMinimize = handler;
    }

    /**
     * Applies the given theme to all visual elements.
     *
     * @param darkMode True for dark theme, false for light theme
     */
    public void applyTheme(boolean darkMode) {
        this.darkMode = darkMode;

        String tmpBgColor = darkMode
                ? AppConstants.COLOR_WINDOW_BACKGROUND_DARK
                : AppConstants.COLOR_WINDOW_BACKGROUND;

        this.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 20;
                """, tmpBgColor));

        String tmpTextPrimary = darkMode
                ? AppConstants.COLOR_TEXT_PRIMARY_DARK
                : AppConstants.COLOR_TEXT_PRIMARY;
        String tmpTextSecondary = darkMode
                ? AppConstants.COLOR_TEXT_SECONDARY_DARK
                : AppConstants.COLOR_TEXT_SECONDARY;

        this.timeLabel.setTextFill(Color.web(tmpTextPrimary));
        this.unitLabel.setTextFill(Color.web(tmpTextSecondary));

        this.updateButtonStyle();
        this.updateStateLabel();
        this.drawProgressRing();
    }

    /**
     * Handles keyboard shortcuts within the mini view.
     *
     * @param event The key event
     */
    public void handleKeyPress(KeyEvent event) {
        switch (event.getCode()) {
            case SPACE -> {
                this.handleStartPauseClick();
                event.consume();
            }
            case R -> {
                if (!event.isControlDown()) {
                    this.controller.reset();
                    event.consume();
                }
            }
            default -> {
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
        this.controller.remainingSecondsProperty().addListener((obs, oldVal, newVal) -> {
            this.remainingSeconds = newVal.intValue();
            this.updateTimeDisplay();
            this.drawProgressRing();
        });

        this.controller.currentStateProperty().addListener((obs, oldState, newState) -> {
            this.currentState = newState;

            if (oldState == TimerState.IDLE) {
                if (newState == TimerState.WORK) {
                    this.totalSeconds = this.controller.getSettings().getWorkDurationSeconds();
                } else if (newState == TimerState.BREAK) {
                    this.totalSeconds = this.controller.getSettings().getBreakDurationSeconds();
                }
            }

            if (newState == TimerState.IDLE) {
                if (this.controller.getPendingSessionType() == TimerState.BREAK) {
                    this.showDuration(this.controller.getSettings().getBreakDurationMinutes(), "Break");
                } else {
                    this.showDuration(this.controller.getSettings().getWorkDurationMinutes(), "min");
                }
            }

            this.updateButtonIcon();
            this.updateStateLabel();
            this.drawProgressRing();
        });
    }

    /**
     * Refreshes all display elements to match the current controller state.
     */
    private void refreshDisplay() {
        this.currentState = this.controller.getCurrentState();
        this.remainingSeconds = this.controller.getRemainingSeconds();

        if (this.currentState == TimerState.IDLE) {
            if (this.controller.getPendingSessionType() == TimerState.BREAK) {
                this.showDuration(this.controller.getSettings().getBreakDurationMinutes(), "Break");
            } else {
                this.showDuration(this.controller.getSettings().getWorkDurationMinutes(), "min");
            }
        } else {
            if (this.currentState == TimerState.WORK
                    || (this.currentState == TimerState.PAUSED
                            && this.controller.getStateBeforePause() == TimerState.WORK)) {
                this.totalSeconds = this.controller.getSettings().getWorkDurationSeconds();
            } else {
                this.totalSeconds = this.controller.getSettings().getBreakDurationSeconds();
            }
            this.updateTimeDisplay();
        }

        this.updateButtonIcon();
        this.updateStateLabel();
        this.drawProgressRing();
    }

    /**
     * Shows a static duration (used when IDLE).
     */
    private void showDuration(int durationMinutes, String label) {
        this.totalSeconds = durationMinutes * 60;
        this.remainingSeconds = this.totalSeconds;
        this.timeLabel.setText(String.valueOf(durationMinutes));
        this.unitLabel.setText(label);
    }

    /**
     * Updates the time label from remaining seconds.
     */
    private void updateTimeDisplay() {
        if (this.remainingSeconds >= 60) {
            int tmpDisplayMinutes = (this.remainingSeconds + 59) / 60;
            this.timeLabel.setText(String.valueOf(tmpDisplayMinutes));
            this.unitLabel.setText("min");
        } else if (this.remainingSeconds >= 10) {
            this.timeLabel.setText(String.format("%02d", this.remainingSeconds));
            this.unitLabel.setText("sec");
        } else {
            this.timeLabel.setText(String.valueOf(this.remainingSeconds));
            this.unitLabel.setText("sec");
        }
    }

    /**
     * Updates the state label text and color.
     */
    private void updateStateLabel() {
        if (this.currentState == null) {
            this.stateLabel.setText("");
            return;
        }

        String tmpStateColor;
        switch (this.currentState) {
            case WORK -> {
                this.stateLabel.setText("Focus");
                tmpStateColor = AppConstants.COLOR_ACCENT;
            }
            case BREAK -> {
                this.stateLabel.setText("Break");
                tmpStateColor = this.darkMode
                        ? AppConstants.COLOR_TEXT_SECONDARY_DARK
                        : AppConstants.COLOR_TEXT_SECONDARY;
            }
            case PAUSED -> {
                this.stateLabel.setText("Paused");
                tmpStateColor = this.darkMode
                        ? AppConstants.COLOR_TEXT_SECONDARY_DARK
                        : AppConstants.COLOR_TEXT_SECONDARY;
            }
            case IDLE -> {
                if (this.controller.getPendingSessionType() == TimerState.BREAK) {
                    this.stateLabel.setText("Break");
                } else {
                    this.stateLabel.setText("Focus");
                }
                tmpStateColor = this.darkMode
                        ? AppConstants.COLOR_TEXT_SECONDARY_DARK
                        : AppConstants.COLOR_TEXT_SECONDARY;
            }
            default -> {
                this.stateLabel.setText("");
                tmpStateColor = this.darkMode
                        ? AppConstants.COLOR_TEXT_SECONDARY_DARK
                        : AppConstants.COLOR_TEXT_SECONDARY;
            }
        }
        this.stateLabel.setTextFill(Color.web(tmpStateColor));
    }

    /**
     * Updates the start/pause button icon based on the current state.
     */
    private void updateButtonIcon() {
        switch (this.currentState) {
            case WORK, BREAK -> {
                this.startPauseButton.setText(ICON_PAUSE);
                this.startPauseButton.setPadding(new Insets(0));
            }
            case IDLE, PAUSED -> {
                this.startPauseButton.setText(ICON_PLAY);
                this.startPauseButton.setPadding(new Insets(0, 0, 0, 2));
            }
        }
        this.updateButtonStyle();
    }

    /**
     * Applies theme-aware styling to the start/pause button.
     */
    private void updateButtonStyle() {
        this.startPauseButton.setStyle(String.format("""
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
        switch (this.currentState) {
            case IDLE -> this.controller.startOrResume();
            case WORK, BREAK -> this.controller.pause();
            case PAUSED -> this.controller.resume();
        }
    }

    /**
     * Draws the circular progress ring on the canvas.
     */
    private void drawProgressRing() {
        GraphicsContext tmpGraphicsContext =
                this.progressCanvas.getGraphicsContext2D();
        double tmpWidth = this.progressCanvas.getWidth();
        double tmpHeight = this.progressCanvas.getHeight();
        double tmpCenterX = tmpWidth / 2;
        double tmpCenterY = tmpHeight / 2;
        double tmpRadius = (Math.min(tmpWidth, tmpHeight)
                - RING_STROKE_WIDTH * 2 - DOT_RADIUS * 2) / 2;

        tmpGraphicsContext.clearRect(0, 0, tmpWidth, tmpHeight);

        String tmpRingBgColor = this.darkMode
                ? AppConstants.COLOR_PROGRESS_RING_DARK
                : AppConstants.COLOR_PROGRESS_RING;
        tmpGraphicsContext.setStroke(Color.web(tmpRingBgColor));
        tmpGraphicsContext.setLineWidth(RING_STROKE_WIDTH);
        tmpGraphicsContext.strokeOval(
                tmpCenterX - tmpRadius,
                tmpCenterY - tmpRadius,
                tmpRadius * 2,
                tmpRadius * 2);

        if (this.currentState != null
                && this.currentState != TimerState.IDLE
                && this.totalSeconds > 0) {
            double tmpProgress =
                    1.0 - ((double) this.remainingSeconds / this.totalSeconds);
            double tmpSweepAngle = tmpProgress * 360;

            String tmpArcColor;
            if (this.currentState == TimerState.BREAK) {
                tmpArcColor = AppConstants.COLOR_BREAK_BACKGROUND;
            } else {
                tmpArcColor = AppConstants.COLOR_PROGRESS_ACTIVE;
            }

            double tmpArcOpacity =
                    (this.currentState == TimerState.PAUSED) ? 0.5 : 1.0;

            tmpGraphicsContext.setGlobalAlpha(tmpArcOpacity);
            tmpGraphicsContext.setStroke(Color.web(tmpArcColor));
            tmpGraphicsContext.setLineWidth(RING_STROKE_WIDTH);
            tmpGraphicsContext.strokeArc(
                    tmpCenterX - tmpRadius,
                    tmpCenterY - tmpRadius,
                    tmpRadius * 2,
                    tmpRadius * 2,
                    90,
                    -tmpSweepAngle,
                    ArcType.OPEN);

            double tmpAngle =
                    Math.toRadians(tmpProgress * 360 - 90);
            double tmpDotX =
                    tmpCenterX + tmpRadius * Math.cos(tmpAngle);
            double tmpDotY =
                    tmpCenterY + tmpRadius * Math.sin(tmpAngle);

            tmpGraphicsContext.setFill(Color.web(tmpArcColor));
            tmpGraphicsContext.fillOval(
                    tmpDotX - DOT_RADIUS,
                    tmpDotY - DOT_RADIUS,
                    DOT_RADIUS * 2,
                    DOT_RADIUS * 2);
            tmpGraphicsContext.setGlobalAlpha(1.0);
        }
    }

    /**
     * Creates the right-click context menu.
     *
     * @return The configured context menu
     */
    private ContextMenu createContextMenu() {
        MenuItem tmpShowFullItem = new MenuItem("Show Full Window");
        tmpShowFullItem.setOnAction(event -> {
            if (this.onShowFullWindow != null) {
                this.onShowFullWindow.run();
            }
        });

        MenuItem tmpResetItem = new MenuItem("Reset Timer");
        tmpResetItem.setOnAction(event -> this.controller.reset());

        MenuItem tmpCloseItem = new MenuItem("Close Mini Mode");
        tmpCloseItem.setOnAction(event -> {
            if (this.onCloseMiniMode != null) {
                this.onCloseMiniMode.run();
            }
        });

        ContextMenu tmpMenu = new ContextMenu();
        tmpMenu.getItems().addAll(
                tmpShowFullItem, tmpResetItem,
                new SeparatorMenuItem(), tmpCloseItem);
        return tmpMenu;
    }

    /**
     * Creates the expand button to restore the full application window.
     *
     * <p>Uses a full-screen expand icon so users have a visible way to
     * return to the main window without needing the right-click context menu.</p>
     *
     * @return The configured expand button
     */
    private Button createExpandButton() {
        SVGPath tmpExpandIcon = new SVGPath();
        tmpExpandIcon.setContent(
                "M7 14H5v5h5v-2H7v-3zm-2-4h2V7h3V5H5v5zm12"
                + " 7h-3v2h5v-5h-2v3zM14 5v2h3v3h2V5h-5z");

        String tmpIconColor = this.darkMode
                ? AppConstants.COLOR_TEXT_SECONDARY_DARK
                : AppConstants.COLOR_ACCENT;
        String tmpIconHoverColor = this.darkMode
                ? AppConstants.COLOR_TEXT_PRIMARY_DARK
                : AppConstants.COLOR_PROGRESS_ACTIVE;
        String tmpHoverBg = this.darkMode
                ? "rgba(255, 255, 255, 0.08)"
                : "rgba(160, 82, 45, 0.12)";
        String tmpTooltipBg = this.darkMode
                ? AppConstants.COLOR_CARD_BACKGROUND_DARK
                : AppConstants.COLOR_CARD_BACKGROUND;
        String tmpTooltipText = this.darkMode
                ? AppConstants.COLOR_TEXT_PRIMARY_DARK
                : AppConstants.COLOR_TEXT_PRIMARY;

        tmpExpandIcon.setFill(Color.web(tmpIconColor));
        tmpExpandIcon.setScaleX(0.6);
        tmpExpandIcon.setScaleY(0.6);

        Button tmpButton = new Button();
        tmpButton.setGraphic(tmpExpandIcon);
        tmpButton.setStyle("""
                -fx-background-color: transparent;
                -fx-cursor: hand;
                -fx-padding: 2;
                """);

        tmpButton.setOnMouseEntered(event -> {
            tmpButton.setStyle(String.format("""
                    -fx-background-color: %s;
                    -fx-background-radius: 6;
                    -fx-cursor: hand;
                    -fx-padding: 2;
                    """, tmpHoverBg));
            tmpExpandIcon.setFill(Color.web(tmpIconHoverColor));
        });

        tmpButton.setOnMouseExited(event -> {
            tmpButton.setStyle("""
                    -fx-background-color: transparent;
                    -fx-cursor: hand;
                    -fx-padding: 2;
                    """);
            tmpExpandIcon.setFill(Color.web(tmpIconColor));
        });

        tmpButton.setOnAction(event -> {
            if (this.onShowFullWindow != null) {
                this.onShowFullWindow.run();
            }
        });

        Tooltip tmpTooltip = new Tooltip("Expand");
        tmpTooltip.setShowDelay(new Duration(0));
        tmpTooltip.setStyle(String.format("""
                -fx-font-family: 'Segoe UI', sans-serif;
                -fx-font-size: 12px;
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-background-radius: 6;
                -fx-padding: 6 10 6 10;
                -fx-effect: dropshadow(gaussian,
                    rgba(0,0,0,0.15), 4, 0, 0, 1);
                """, tmpTooltipBg, tmpTooltipText));
        tmpButton.setTooltip(tmpTooltip);

        return tmpButton;
    }

    /**
     * Creates the minimize button for the mini window.
     *
     * <p>Uses a horizontal line icon matching the main window's minimize button
     * style, with theme-aware colors.</p>
     *
     * @return The configured minimize button
     */
    private Button createMinimizeButton() {
        SVGPath tmpMinimizeIcon = new SVGPath();
        tmpMinimizeIcon.setContent("M4 12h16");

        String tmpIconColor = this.darkMode
                ? AppConstants.COLOR_TEXT_SECONDARY_DARK
                : AppConstants.COLOR_ACCENT;
        String tmpIconHoverColor = this.darkMode
                ? AppConstants.COLOR_TEXT_PRIMARY_DARK
                : AppConstants.COLOR_PROGRESS_ACTIVE;
        String tmpHoverBg = this.darkMode
                ? "rgba(255, 255, 255, 0.08)"
                : "rgba(160, 82, 45, 0.12)";
        String tmpTooltipBg = this.darkMode
                ? AppConstants.COLOR_CARD_BACKGROUND_DARK
                : AppConstants.COLOR_CARD_BACKGROUND;
        String tmpTooltipText = this.darkMode
                ? AppConstants.COLOR_TEXT_PRIMARY_DARK
                : AppConstants.COLOR_TEXT_PRIMARY;

        tmpMinimizeIcon.setStroke(Color.web(tmpIconColor));
        tmpMinimizeIcon.setStrokeWidth(1.5);
        tmpMinimizeIcon.setFill(Color.TRANSPARENT);
        tmpMinimizeIcon.setScaleX(0.55);
        tmpMinimizeIcon.setScaleY(0.55);

        Button tmpButton = new Button();
        tmpButton.setGraphic(tmpMinimizeIcon);
        tmpButton.setStyle("""
                -fx-background-color: transparent;
                -fx-cursor: hand;
                -fx-padding: 2;
                """);

        tmpButton.setOnMouseEntered(event -> {
            tmpButton.setStyle(String.format("""
                    -fx-background-color: %s;
                    -fx-background-radius: 6;
                    -fx-cursor: hand;
                    -fx-padding: 2;
                    """, tmpHoverBg));
            tmpMinimizeIcon.setStroke(Color.web(tmpIconHoverColor));
        });

        tmpButton.setOnMouseExited(event -> {
            tmpButton.setStyle("""
                    -fx-background-color: transparent;
                    -fx-cursor: hand;
                    -fx-padding: 2;
                    """);
            tmpMinimizeIcon.setStroke(Color.web(tmpIconColor));
        });

        tmpButton.setOnAction(event -> {
            if (this.onMinimize != null) {
                this.onMinimize.run();
            }
        });

        Tooltip tmpTooltip = new Tooltip("Minimize");
        tmpTooltip.setShowDelay(new Duration(0));
        tmpTooltip.setStyle(String.format("""
                -fx-font-family: 'Segoe UI', sans-serif;
                -fx-font-size: 12px;
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-background-radius: 6;
                -fx-padding: 6 10 6 10;
                -fx-effect: dropshadow(gaussian,
                    rgba(0,0,0,0.15), 4, 0, 0, 1);
                """, tmpTooltipBg, tmpTooltipText));
        tmpButton.setTooltip(tmpTooltip);

        return tmpButton;
    }
}
