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

import io.github.kullik01.focusbean.util.AppConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * A custom title bar component for an undecorated window.
 *
 * <p>
 * This title bar provides minimize and close buttons without a maximize button.
 * It also enables window dragging functionality and matches the application's
 * warm, coffee-themed design aesthetic.
 * </p>
 */
public final class CustomTitleBar extends HBox {

    private static final String FONT_FAMILY = "'Segoe UI', 'Helvetica Neue', sans-serif";
    private static final double TITLE_BAR_HEIGHT = 32.0;
    private static final double BUTTON_WIDTH = 46.0;
    private static final double ICON_SCALE = 0.55;

    /**
     * Style for the base title bar background with rounded top corners.
     */
    private static final String STYLE_TITLE_BAR = String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 16 16 0 0;
            -fx-border-color: %s;
            -fx-border-width: 0 0 1 0;
            """, AppConstants.COLOR_WINDOW_BACKGROUND, AppConstants.COLOR_CARD_BORDER);

    /**
     * Default style for window control buttons.
     */
    private static final String STYLE_BUTTON_DEFAULT = """
            -fx-background-color: transparent;
            -fx-background-radius: 0;
            -fx-cursor: hand;
            -fx-padding: 0;
            """;

    /**
     * Hover style for the minimize button.
     */
    private static final String STYLE_BUTTON_MINIMIZE_HOVER = String.format("""
            -fx-background-color: rgba(160, 82, 45, 0.15);
            -fx-background-radius: 0;
            -fx-cursor: hand;
            -fx-padding: 0;
            """);

    /**
     * Hover style for the close button (red tint with rounded top-right corner).
     */
    private static final String STYLE_BUTTON_CLOSE_HOVER = """
            -fx-background-color: #E81123;
            -fx-background-radius: 0 16 0 0;
            -fx-cursor: hand;
            -fx-padding: 0;
            """;

    private final Stage stage;
    private double xOffset;
    private double yOffset;

    /**
     * Creates a custom title bar for the given stage.
     *
     * @param stage the stage this title bar controls
     * @throws NullPointerException if stage is null
     */
    public CustomTitleBar(Stage stage) {
        this.stage = Objects.requireNonNull(stage, "stage must not be null");

        setStyle(STYLE_TITLE_BAR);
        setAlignment(Pos.CENTER_LEFT);
        setPrefHeight(TITLE_BAR_HEIGHT);
        setMinHeight(TITLE_BAR_HEIGHT);
        setMaxHeight(TITLE_BAR_HEIGHT);

        // Create title section with icon and label
        HBox titleSection = createTitleSection();

        // Create spacer to push buttons to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Create window control buttons
        Button minimizeButton = createMinimizeButton();
        Button closeButton = createCloseButton();

        getChildren().addAll(titleSection, spacer, minimizeButton, closeButton);

        // Enable window dragging on the title bar
        enableWindowDragging();
    }

    /**
     * Creates the title section with the application icon and name.
     *
     * @return the configured title section HBox
     */
    private HBox createTitleSection() {
        javafx.scene.Node iconNode;
        // Try to load custom logo
        try {
            String logoPath = "/io/github/kullik01/focusbean/view/logo.png";
            java.net.URL logoUrl = getClass().getResource(logoPath);
            if (logoUrl == null) {
                // Fallback to root
                logoUrl = getClass().getResource("/logo.png");
            }

            if (logoUrl != null) {
                Image logoImage = new Image(logoUrl.toExternalForm());
                ImageView imageView = new ImageView(logoImage);
                imageView.setFitHeight(20);
                imageView.setFitWidth(20);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                iconNode = imageView;
            } else {
                throw new Exception("Logo not found");
            }
        } catch (Exception e) {
            // Coffee bean icon using SVG
            SVGPath coffeeIcon = new SVGPath();
            coffeeIcon.setContent(
                    "M12 2C9.8 2 8 3.8 8 6c0 2.1 1.6 3.8 3.7 4v2.3c-2.9.4-5.2 2.9-5.2 6 "
                            + "0 3.3 2.7 6 6 6s6-2.7 6-6c0-3.1-2.3-5.6-5.2-6V10c2.1-.2 3.7-1.9 "
                            + "3.7-4 0-2.2-1.8-4-4-4z");
            coffeeIcon.setFill(Color.web(AppConstants.COLOR_ACCENT));
            coffeeIcon.setScaleX(0.7);
            coffeeIcon.setScaleY(0.7);
            iconNode = coffeeIcon;
        }

        Label titleLabel = new Label(AppConstants.APP_NAME);
        titleLabel.setFont(Font.font(FONT_FAMILY, FontWeight.MEDIUM, 13));
        titleLabel.setTextFill(Color.web(AppConstants.COLOR_TEXT_PRIMARY));

        HBox titleSection = new HBox(8);
        titleSection.setAlignment(Pos.CENTER_LEFT);
        titleSection.setPadding(new Insets(0, 0, 0, 10));
        titleSection.getChildren().addAll(iconNode, titleLabel);

        return titleSection;
    }

    /**
     * Creates the minimize button with appropriate icon and styling.
     *
     * @return the configured minimize button
     */
    private Button createMinimizeButton() {
        // Minimize icon (horizontal line)
        SVGPath minimizeIcon = new SVGPath();
        minimizeIcon.setContent("M4 12h16");
        minimizeIcon.setStroke(Color.web(AppConstants.COLOR_TEXT_PRIMARY));
        minimizeIcon.setStrokeWidth(1.2);
        minimizeIcon.setFill(Color.TRANSPARENT);
        minimizeIcon.setScaleX(ICON_SCALE);
        minimizeIcon.setScaleY(ICON_SCALE);

        Button minimizeButton = new Button();
        minimizeButton.setGraphic(minimizeIcon);
        minimizeButton.setStyle(STYLE_BUTTON_DEFAULT);
        minimizeButton.setPrefSize(BUTTON_WIDTH, TITLE_BAR_HEIGHT);
        minimizeButton.setMinSize(BUTTON_WIDTH, TITLE_BAR_HEIGHT);
        minimizeButton.setMaxSize(BUTTON_WIDTH, TITLE_BAR_HEIGHT);

        Tooltip tooltip = createTooltip("Minimize");
        minimizeButton.setTooltip(tooltip);

        minimizeButton.setOnMouseEntered(e -> minimizeButton.setStyle(STYLE_BUTTON_MINIMIZE_HOVER));
        minimizeButton.setOnMouseExited(e -> minimizeButton.setStyle(STYLE_BUTTON_DEFAULT));
        minimizeButton.setOnAction(e -> stage.setIconified(true));

        return minimizeButton;
    }

    /**
     * Creates the close button with appropriate icon and styling.
     *
     * @return the configured close button
     */
    private Button createCloseButton() {
        // Close icon (X shape)
        SVGPath closeIcon = new SVGPath();
        closeIcon.setContent("M18 6L6 18M6 6l12 12");
        closeIcon.setStroke(Color.web(AppConstants.COLOR_TEXT_PRIMARY));
        closeIcon.setStrokeWidth(1.2);
        closeIcon.setFill(Color.TRANSPARENT);
        closeIcon.setScaleX(ICON_SCALE);
        closeIcon.setScaleY(ICON_SCALE);

        Button closeButton = new Button();
        closeButton.setGraphic(closeIcon);
        closeButton.setStyle(STYLE_BUTTON_DEFAULT);
        closeButton.setPrefSize(BUTTON_WIDTH, TITLE_BAR_HEIGHT);
        closeButton.setMinSize(BUTTON_WIDTH, TITLE_BAR_HEIGHT);
        closeButton.setMaxSize(BUTTON_WIDTH, TITLE_BAR_HEIGHT);

        Tooltip tooltip = createTooltip("Close");
        closeButton.setTooltip(tooltip);

        closeButton.setOnMouseEntered(e -> {
            closeButton.setStyle(STYLE_BUTTON_CLOSE_HOVER);
            // Change icon color to white on red background
            closeIcon.setStroke(Color.WHITE);
        });
        closeButton.setOnMouseExited(e -> {
            closeButton.setStyle(STYLE_BUTTON_DEFAULT);
            closeIcon.setStroke(Color.web(AppConstants.COLOR_TEXT_PRIMARY));
        });
        closeButton.setOnAction(e -> {
            stage.getProperties().put("close_requested", true);
            stage.close();
        });

        return closeButton;
    }

    /**
     * Creates a styled tooltip matching the application design.
     *
     * @param text the tooltip text
     * @return the configured tooltip
     */
    private Tooltip createTooltip(String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(new javafx.util.Duration(0));
        tooltip.setStyle(String.format("""
                -fx-font-family: 'Segoe UI', sans-serif;
                -fx-font-size: 12px;
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-background-radius: 4;
                -fx-padding: 4 8 4 8;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 3, 0, 0, 1);
                """, AppConstants.COLOR_CARD_BACKGROUND, AppConstants.COLOR_TEXT_PRIMARY));
        return tooltip;
    }

    /**
     * Enables window dragging functionality on the title bar.
     *
     * <p>
     * When the user presses the mouse on the title bar and drags,
     * the window moves accordingly.
     * </p>
     */
    private void enableWindowDragging() {
        setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }
}
