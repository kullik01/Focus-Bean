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

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class ToastNotification extends Stage {

    private static final double TOAST_WIDTH = 350;
    private static final double TOAST_HEIGHT = 100;
    private static final double SCREEN_MARGIN = 20;

    private final Runnable onCloseAction;

    public ToastNotification(String title, String message, Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
        initStyle(StageStyle.TRANSPARENT);
        setAlwaysOnTop(true);

        // Main container with styling
        VBox root = new VBox(8); // Start with some spacing
        root.setAlignment(Pos.CENTER_LEFT);
        root.setStyle("-fx-background-color: #F5F2EF;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: #D7B49E;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 12;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        root.setPadding(new javafx.geometry.Insets(15));
        root.setPrefWidth(TOAST_WIDTH);
        root.setMinHeight(TOAST_HEIGHT); // Allow growth, but set min height

        // Header: Icon + "Focus Bean" title
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Brown point icon
        Circle icon = new Circle(4);
        icon.setFill(Color.web("#5D4037")); // Dark Coffee Brown

        Label appTitle = new Label("Focus Bean");
        appTitle.setTextFill(Color.web("#5D4037"));
        appTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        header.getChildren().addAll(icon, appTitle);

        // Content
        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.web("#2d3436"));
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        titleLabel.setWrapText(true);

        Label messageLabel = new Label(message);
        messageLabel.setTextFill(Color.web("#636e72"));
        messageLabel.setFont(Font.font("Segoe UI", 13));
        messageLabel.setWrapText(true);

        root.getChildren().addAll(header, titleLabel, messageLabel);

        // Close button (Top Right overlay)
        Label closeBtn = new Label("✕");
        closeBtn.setTextFill(Color.web("#a4a4a4"));
        closeBtn.setCursor(javafx.scene.Cursor.HAND);
        closeBtn.setOnMouseClicked(e -> closeToast());
        StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(closeBtn, new javafx.geometry.Insets(15)); // Adjust for wrapper padding if needed, relative
                                                                       // to StackPane edge

        // Wrapper to overlay close button AND provide padding for shadow
        // JavaFX effects draw outside layout bounds, so we need padding in the Scene's
        // root to avoid clipping
        StackPane contentWrapper = new StackPane(root, closeBtn);
        contentWrapper.setStyle("-fx-background-color: transparent;");
        contentWrapper.setPadding(new javafx.geometry.Insets(20)); // Margin for shadow

        Scene scene = new Scene(contentWrapper);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);

        // Position on screen (Bottom Right)
        // Window has 20px transparent padding on all sides.
        // We want the *visual* box to be close to the edge.
        // Screen Max X - Visual Margin - (Visual Width + Left Padding)
        // Visual Width = TOAST_WIDTH (350). Left Padding = 20.
        // So X = MaxX - 10 - 370.
        double visualMargin = 10;
        double totalWidth = TOAST_WIDTH + 40;

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        // Align visual right edge to screen right edge - margin
        setX(bounds.getMaxX() - visualMargin - (TOAST_WIDTH + 20));

        // Align visual bottom edge to screen bottom edge - margin
        // Estimating height since it's dynamic, but removing the large buffer to move
        // it down
        // 130 is approx visual height (100 content + 30 padding/text) -> Window Height
        // ~ 170
        // Window Y = MaxY - Margin - (Visual Height + Top Padding)
        // We'll trust the visual bounds max Y to be the top of taskbar
        // Removed extra 5px cushion to let shadow overlap margin slightly (tighter
        // look)
        double estimatedVisualHeight = 100;
        setY(bounds.getMaxY() - visualMargin - (estimatedVisualHeight + 20));

        // Show animation
        root.setOpacity(0);
        show();

        Timeline fadeIn = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(root.opacityProperty(), 0)),
                new KeyFrame(Duration.millis(300), new KeyValue(root.opacityProperty(), 1)));
        fadeIn.play();

        // Auto close removed per user request - user must manually close
        // Timeline autoClose = new Timeline(new KeyFrame(Duration.seconds(5), e ->
        // closeToast()));
        // autoClose.play();
    }

    private void closeToast() {
        // Stop sound immediately
        if (onCloseAction != null) {
            onCloseAction.run();
        }

        Timeline fadeOut = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(getScene().getRoot().opacityProperty(), 1)),
                new KeyFrame(Duration.millis(300), e -> close(),
                        new KeyValue(getScene().getRoot().opacityProperty(), 0)));
        fadeOut.play();
    }

    public static void show(String title, String message) {
        show(title, message, null);
    }

    public static void show(String title, String message, Runnable onCloseAction) {
        Platform.runLater(() -> new ToastNotification(title, message, onCloseAction));
    }
}
