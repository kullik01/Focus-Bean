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

import io.github.kullik01.focusbean.model.SessionHistory;
import io.github.kullik01.focusbean.model.TimerSession;
import io.github.kullik01.focusbean.model.HistoryViewMode;
import io.github.kullik01.focusbean.util.AppConstants;
import io.github.kullik01.focusbean.util.TimeFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Displays session history in a table with statistics summary.
 *
 * <p>
 * Shows completed work and break sessions with date, type, and duration.
 * Includes summary statistics for today and this week, plus a button to
 * clear all history.
 * </p>
 */
public final class HistoryView extends VBox {

        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
        private static final double TABLE_COLUMN_DATE_WIDTH = 100;
        private static final double TABLE_COLUMN_TIME_WIDTH = 60;
        private static final double TABLE_COLUMN_TYPE_WIDTH = 80;
        private static final double TABLE_COLUMN_DURATION_WIDTH = 80;
        private static final double TABLE_COLUMN_STATUS_WIDTH = 100;

        private static final String STYLE_STATS_LABEL = """
                        -fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;
                        -fx-font-size: 14px;
                        -fx-text-fill: %s;
                        """;

        private final TableView<TimerSession> sessionTable;
        private final BarChart<String, Number> barChart;
        private final StackPane viewContainer;
        private final Label todayStatsLabel;
        private final Label weekStatsLabel;
        private final Button clearButton;
        private final Button settingsButton;
        private final Button viewToggleButton;

        private Runnable onClearHistory;
        private Consumer<HistoryViewMode> onViewModeChanged;
        private Runnable onSettingsClicked;
        private HistoryViewMode currentMode = HistoryViewMode.TABLE;
        private int historyChartDays = 7; // Default value
        private boolean darkModeEnabled = false;
        private Region tableBorderOverlay;

        /**
         * Creates a new HistoryView with empty data.
         */
        public HistoryView() {

                todayStatsLabel = new Label();
                todayStatsLabel.setStyle(String.format(STYLE_STATS_LABEL, AppConstants.COLOR_TEXT_SECONDARY));

                weekStatsLabel = new Label();
                weekStatsLabel.setStyle(String.format(STYLE_STATS_LABEL, AppConstants.COLOR_TEXT_SECONDARY));

                // 1. Setup Table View
                sessionTable = new TableView<>();
                setupTableColumns();

                // Set table width to exactly fit all columns (no empty space on right)
                double totalColumnWidth = TABLE_COLUMN_DATE_WIDTH + TABLE_COLUMN_TIME_WIDTH
                                + TABLE_COLUMN_TYPE_WIDTH + TABLE_COLUMN_DURATION_WIDTH
                                + TABLE_COLUMN_STATUS_WIDTH + 2; // +2 for border
                // Apply rounded clip to table to prevent square content from overflowing corners
                sessionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Restored
                Rectangle clip = new Rectangle();
                clip.widthProperty().bind(sessionTable.widthProperty());
                clip.heightProperty().bind(sessionTable.heightProperty());
                clip.setArcWidth(40);
                clip.setArcHeight(40);
                sessionTable.setClip(clip);

                // Create border overlay for smooth edges
                tableBorderOverlay = new Region();
                tableBorderOverlay.setMouseTransparent(true);
                tableBorderOverlay.setPickOnBounds(false);
                updateTableBorderColor(); // Set initial color

                StackPane tableContainer = new StackPane(sessionTable, tableBorderOverlay);
                tableContainer.setMaxWidth(totalColumnWidth);


                // 2. Setup Bar Chart
                CategoryAxis xAxis = new CategoryAxis();
                xAxis.setLabel("Date (Last " + historyChartDays + " Days)");
                NumberAxis yAxis = new NumberAxis();
                yAxis.setLabel("Minutes Focus");

                // Dynamically rotate labels if showing many days
                if (historyChartDays > 14) {
                        xAxis.setTickLabelRotation(-45);
                } else {
                        xAxis.setTickLabelRotation(0);
                }

                barChart = new BarChart<>(xAxis, yAxis);
                barChart.setTitle("Last 7 Days Activity");
                barChart.setLegendSide(Side.BOTTOM);
                barChart.setAnimated(false); // Disable animation for smoother updates
                barChart.setMaxWidth(totalColumnWidth);

                // 3. Setup Container
                viewContainer = new StackPane(tableContainer, barChart);
                viewContainer.setAlignment(Pos.TOP_CENTER);

                // Initial visibility
                updateViewVisibility();

                VBox statsBox = new VBox(5, todayStatsLabel, weekStatsLabel);
                statsBox.setAlignment(Pos.CENTER_LEFT);

                viewToggleButton = createViewToggleButton();
                settingsButton = createSettingsButton();
                clearButton = createClearHistoryButton();

                // Header with stats and buttons
                HBox headerBox = new HBox(15);
                headerBox.setAlignment(Pos.CENTER_LEFT);
                headerBox.setPadding(new Insets(0, 0, 8, 0));
                headerBox.setMaxWidth(totalColumnWidth);

                javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                // Buttons container: Switch View -> Settings -> Clear History
                HBox buttonContainer = new HBox(8);
                buttonContainer.setAlignment(Pos.CENTER);
                buttonContainer.getChildren().addAll(viewToggleButton, settingsButton, clearButton);

                // Wrap buttons in VBox with CENTER alignment for vertical centering
                VBox buttonWrapper = new VBox(buttonContainer);
                buttonWrapper.setAlignment(Pos.CENTER);

                headerBox.getChildren().addAll(statsBox, spacer, buttonWrapper);

                setSpacing(15);
                setPadding(new Insets(20));
                setAlignment(Pos.TOP_CENTER);
                getChildren().addAll(headerBox, viewContainer);

                // Initial empty state
                updateStats(0, 0, 0, 0);
        }

        /**
         * Creates and configures the clear history button as a recycle bin icon.
         *
         * @return the configured button
         */
        private Button createClearHistoryButton() {
                // Create a clean outline-style trash can icon using SVG
                javafx.scene.shape.SVGPath icon = new javafx.scene.shape.SVGPath();
                // Trash can icon path (outline style matching the settings gear)
                icon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
                icon.setFill(javafx.scene.paint.Color.web(AppConstants.COLOR_PROGRESS_ACTIVE));
                icon.setScaleX(0.85);
                icon.setScaleY(0.85);

                Button button = new Button();
                button.setGraphic(icon);
                button.setStyle("""
                                -fx-background-color: transparent;
                                -fx-cursor: hand;
                                -fx-padding: 2 6 2 6;
                                """);

                // Add tooltip with warm colors matching the GUI design
                javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip("Clear History");
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
                button.setTooltip(tooltip);

                button.setOnMouseEntered(e -> button.setStyle("""
                                -fx-background-color: rgba(160, 82, 45, 0.10);
                                -fx-background-radius: 6;
                                -fx-cursor: hand;
                                -fx-padding: 2 6 2 6;
                                """));

                button.setOnMouseExited(e -> button.setStyle("""
                                -fx-background-color: transparent;
                                -fx-cursor: hand;
                                -fx-padding: 2 6 2 6;
                                """));

                button.setOnAction(e -> handleClearHistoryClick());

                return button;
        }

        /**
         * Creates a settings button to jump to configuration.
         * 
         * @return the configured settings button
         */
        private Button createSettingsButton() {
                javafx.scene.shape.SVGPath icon = new javafx.scene.shape.SVGPath();
                // Gear icon
                icon.setContent("M19.14,12.94c0.04-0.3,0.06-0.61,0.06-0.94c0-0.32-0.02-0.64-0.06-0.94l2.03-1.58c0.18-0.14,0.23-0.41,0.12-0.61 l-1.92-3.32c-0.12-0.22-0.37-0.29-0.59-0.22l-2.39,0.96c-0.5-0.38-1.03-0.7-1.62-0.94L14.4,2.81c-0.04-0.24-0.24-0.41-0.48-0.41 h-3.84c-0.24,0-0.43,0.17-0.47,0.41L9.25,5.35C8.66,5.59,8.12,5.92,7.63,6.29L5.24,5.33c-0.22-0.08-0.47,0-0.59,0.22L2.73,8.87 C2.62,9.08,2.66,9.34,2.86,9.48l2.03,1.58C4.84,11.36,4.8,11.69,4.8,12s0.02,0.64,0.06,0.94l-2.03,1.58 c-0.18,0.14-0.23,0.41-0.12,0.61l1.92,3.32c0.12,0.22,0.37,0.29,0.59,0.22l2.39-0.96c0.5,0.38,1.03,0.7,1.62,0.94l0.36,2.54 c0.05,0.24,0.24,0.41,0.48,0.41h3.84c0.24,0,0.44-0.17,0.47-0.41l0.36-2.54c0.59-0.24,1.13-0.56,1.62-0.94l2.39,0.96 c0.22,0.08,0.47,0,0.59-0.22l1.92-3.32c0.12-0.22,0.07-0.47-0.12-0.61L19.14,12.94z M12,15.6c-1.98,0-3.6-1.62-3.6-3.6 s1.62-3.6,3.6-3.6s3.6,1.62,3.6,3.6S13.98,15.6,12,15.6z");
                icon.setFill(javafx.scene.paint.Color.web(AppConstants.COLOR_PROGRESS_ACTIVE));
                icon.setScaleX(0.85);
                icon.setScaleY(0.85);

                Button button = new Button();
                button.setGraphic(icon);
                button.setStyle("""
                                -fx-background-color: transparent;
                                -fx-cursor: hand;
                                -fx-padding: 2 6 2 6;
                                """);

                Tooltip tooltip = new Tooltip("Configure History");
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
                button.setTooltip(tooltip);

                button.setOnMouseEntered(e -> button.setStyle("""
                                        -fx-background-color: rgba(160, 82, 45, 0.10);
                                        -fx-background-radius: 6;
                                        -fx-cursor: hand;
                                        -fx-padding: 2 6 2 6;
                                """));

                button.setOnMouseExited(e -> button.setStyle("""
                                        -fx-background-color: transparent;
                                        -fx-cursor: hand;
                                        -fx-padding: 2 6 2 6;
                                """));

                button.setOnAction(e -> {
                        if (onSettingsClicked != null) {
                                onSettingsClicked.run();
                        }
                });

                return button;
        }

        /**
         * Creates the toggle button to switch between list and chart views.
         * 
         * @return the configured toggle button
         */
        private Button createViewToggleButton() {
                Button button = new Button();
                button.setStyle("""
                                -fx-background-color: transparent;
                                -fx-cursor: hand;
                                -fx-padding: 2 6 2 6;
                                """);

                // Initial tooltip, icon will be set by updateToggleButtonState
                Tooltip tooltip = new Tooltip("Switch View");
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
                button.setTooltip(tooltip);

                button.setOnMouseEntered(e -> button.setStyle("""
                                        -fx-background-color: rgba(160, 82, 45, 0.10);
                                        -fx-background-radius: 6;
                                        -fx-cursor: hand;
                                        -fx-padding: 2 6 2 6;
                                """));

                button.setOnMouseExited(e -> button.setStyle("""
                                        -fx-background-color: transparent;
                                        -fx-cursor: hand;
                                        -fx-padding: 2 6 2 6;
                                """));

                button.setOnAction(e -> toggleViewMode());

                return button;
        }

        /**
         * Handles the clear history button click by showing a confirmation dialog.
         */
        private void handleClearHistoryClick() {
                // Determine colors based on dark mode
                String windowBg = darkModeEnabled ? AppConstants.COLOR_WINDOW_BACKGROUND_DARK : AppConstants.COLOR_WINDOW_BACKGROUND;
                String borderColor = darkModeEnabled ? AppConstants.COLOR_CARD_BORDER_DARK : "#D7B49E";
                String textColor = darkModeEnabled ? AppConstants.COLOR_TEXT_PRIMARY_DARK : "#333333";
                String closeBtnColor = darkModeEnabled ? AppConstants.COLOR_TEXT_PRIMARY_DARK : "#5D4037";
                String okBtnBg = darkModeEnabled ? "#3D332B" : "#E0E0E0";
                String okBtnBgHover = darkModeEnabled ? "#4D4339" : "#D0D0D0";
                String okBtnText = darkModeEnabled ? AppConstants.COLOR_TEXT_PRIMARY_DARK : "#333333";
                String stylesheetPath = darkModeEnabled ? "/io/github/kullik01/focusbean/view/styles-dark.css"
                                : "/io/github/kullik01/focusbean/view/styles.css";

                javafx.stage.Stage dialogStage = new javafx.stage.Stage();
                dialogStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
                dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                dialogStage.setTitle("Clear History");

                javafx.scene.image.Image logoImage = null;
                try {
                        String logoPath = "/io/github/kullik01/focusbean/view/logo.png";
                        if (getClass().getResource(logoPath) == null) {
                                logoPath = "/logo.png";
                        }
                        if (getClass().getResource(logoPath) != null) {
                                logoImage = new javafx.scene.image.Image(
                                                getClass().getResource(logoPath).toExternalForm());
                                dialogStage.getIcons().add(logoImage);
                        }
                } catch (Exception e) {
                        // Ignore
                }

                HBox titleBar = new HBox(10);
                titleBar.setAlignment(Pos.CENTER_LEFT);
                titleBar.setPadding(new Insets(10, 15, 10, 15));
                titleBar.setStyle("-fx-background-color: transparent;");

                if (logoImage != null) {
                        javafx.scene.image.ImageView logoView = new javafx.scene.image.ImageView(logoImage);
                        logoView.setFitHeight(24);
                        logoView.setFitWidth(24);
                        titleBar.getChildren().add(logoView);
                } else {
                        javafx.scene.shape.Circle fallbackIcon = new javafx.scene.shape.Circle(8,
                                        javafx.scene.paint.Color.web("#5D4037"));
                        titleBar.getChildren().add(fallbackIcon);
                }

                Label titleLabel = new Label("Clear History");
                titleLabel.setStyle(
                                "-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 14px; -fx-text-fill: " + textColor + ";");
                titleBar.getChildren().add(titleLabel);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                titleBar.getChildren().add(spacer);

                Button closeBtn = new Button("✕");
                String closeBtnStyle = "-fx-background-color: transparent; -fx-text-fill: " + closeBtnColor + "; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0 5 0 5;";
                String closeBtnHoverStyle = "-fx-background-color: rgba(93, 64, 55, 0.15); -fx-text-fill: " + closeBtnColor + "; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0 5 0 5; -fx-background-radius: 4;";
                closeBtn.setStyle(closeBtnStyle);
                closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(closeBtnHoverStyle));
                closeBtn.setOnMouseExited(e -> closeBtn.setStyle(closeBtnStyle));
                closeBtn.setOnAction(e -> dialogStage.close());
                titleBar.getChildren().add(closeBtn);

                final double[] xOffset = new double[1];
                final double[] yOffset = new double[1];
                titleBar.setOnMousePressed(event -> {
                        xOffset[0] = event.getSceneX();
                        yOffset[0] = event.getSceneY();
                });
                titleBar.setOnMouseDragged(event -> {
                        dialogStage.setX(event.getScreenX() - xOffset[0]);
                        dialogStage.setY(event.getScreenY() - yOffset[0]);
                });

                HBox legacyHeaderBox = new HBox(15);
                legacyHeaderBox.setAlignment(Pos.CENTER_LEFT);
                legacyHeaderBox.setPadding(new Insets(10, 0, 15, 0));

                Label legacyHeaderText = new Label("Clear all session history?");
                legacyHeaderText.setStyle("-fx-font-size: 16px; -fx-text-fill: " + textColor + ";");

                javafx.scene.shape.SVGPath questionIcon = new javafx.scene.shape.SVGPath();
                questionIcon.setContent(
                                "M11 18h2v-2h-2v2zm1-16C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5 0-2.21-1.79-4-4-4z");
                questionIcon.setFill(javafx.scene.paint.Color.web("#A0522D"));
                questionIcon.setScaleX(1.5);
                questionIcon.setScaleY(1.5);

                Region headerSpacer = new Region();
                HBox.setHgrow(headerSpacer, Priority.ALWAYS);

                legacyHeaderBox.getChildren().addAll(legacyHeaderText, headerSpacer, questionIcon);

                Label warningLabel = new Label("This cannot be undone!");
                warningLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 13px;");

                VBox contentBody = new VBox(0);
                contentBody.setPadding(new Insets(0, 20, 20, 20));
                contentBody.getChildren().addAll(legacyHeaderBox, warningLabel);

                HBox buttonBar = new HBox(10);
                buttonBar.setAlignment(Pos.CENTER_RIGHT);
                buttonBar.setPadding(new Insets(20, 20, 20, 20));

                Button okButton = new Button("OK");
                okButton.setDefaultButton(true);
                String okButtonStyle = String.format("""
                                -fx-background-color: %s;
                                -fx-text-fill: %s;
                                -fx-background-radius: 20;
                                -fx-cursor: hand;
                                -fx-padding: 6 16 6 16;
                                -fx-font-size: 13px;
                                -fx-min-width: 70;
                                """, okBtnBg, okBtnText);
                String okButtonHoverStyle = String.format("""
                                -fx-background-color: %s;
                                -fx-text-fill: %s;
                                -fx-background-radius: 20;
                                -fx-cursor: hand;
                                -fx-padding: 6 16 6 16;
                                -fx-font-size: 13px;
                                -fx-min-width: 70;
                                """, okBtnBgHover, okBtnText);
                okButton.setStyle(okButtonStyle);
                okButton.setOnMouseEntered(e -> okButton.setStyle(okButtonHoverStyle));
                okButton.setOnMouseExited(e -> okButton.setStyle(okButtonStyle));
                okButton.setOnAction(e -> {
                        dialogStage.close();
                        if (onClearHistory != null) {
                                onClearHistory.run();
                        }
                });

                Button cancelButton = new Button("Cancel");
                cancelButton.setCancelButton(true);
                String cancelButtonStyle = """
                                -fx-background-color: #A0522D;
                                -fx-text-fill: white;
                                -fx-background-radius: 20;
                                -fx-cursor: hand;
                                -fx-padding: 6 16 6 16;
                                -fx-font-size: 13px;
                                """;
                String cancelButtonHoverStyle = """
                                -fx-background-color: #8B4513;
                                -fx-text-fill: white;
                                -fx-background-radius: 20;
                                -fx-cursor: hand;
                                -fx-padding: 6 16 6 16;
                                -fx-font-size: 13px;
                                """;
                cancelButton.setStyle(cancelButtonStyle);
                cancelButton.setOnMouseEntered(e -> cancelButton.setStyle(cancelButtonHoverStyle));
                cancelButton.setOnMouseExited(e -> cancelButton.setStyle(cancelButtonStyle));
                cancelButton.setOnAction(e -> dialogStage.close());

                buttonBar.getChildren().addAll(okButton, cancelButton);

                VBox dialogLayout = new VBox(0);
                dialogLayout.getChildren().addAll(titleBar, contentBody, buttonBar);

                dialogLayout.setStyle(String.format("""
                                -fx-background-color: %s, %s;
                                -fx-background-insets: 0, 1.5;
                                -fx-background-radius: 12, 10.5;
                                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 4);
                                """, borderColor, windowBg));

                dialogLayout.setMinWidth(320);

                javafx.scene.layout.StackPane root = new javafx.scene.layout.StackPane(dialogLayout);
                root.setPadding(new Insets(20));
                root.setStyle("-fx-background-color: transparent;");

                javafx.scene.Scene scene = new javafx.scene.Scene(root);
                scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
                scene.getStylesheets().add(getClass().getResource(stylesheetPath).toExternalForm());

                dialogStage.setScene(scene);

                javafx.stage.Stage mainStage = (javafx.stage.Stage) this.getScene().getWindow();
                if (mainStage != null && mainStage.isAlwaysOnTop()) {
                        dialogStage.setAlwaysOnTop(true);
                }

                dialogStage.showAndWait();
        }

        /**
         * Sets the callback to be invoked when history is cleared.
         *
         * @param callback the callback to invoke on clear, may be null
         */
        public void setOnClearHistory(Runnable callback) {
                this.onClearHistory = callback;
        }

        /**
         * Creates and configures the clear history button as a recycle bin icon.
         *
         * @return the configured button
         */

        /**
         * Updates the view with data from the given session history.
         *
         * @param history the session history to display
         * @throws NullPointerException if history is null
         */
        public void update(SessionHistory history) {
                Objects.requireNonNull(history, "history must not be null");

                // Update table
                List<TimerSession> sessions = history.getSessions();
                sessionTable.getItems().setAll(sessions);

                // Update stats
                int todaySessions = history.countTodaysCompletedWorkSessions();
                int todayMinutes = history.getTodaysTotalWorkMinutes();
                int weekSessions = history.countThisWeeksCompletedWorkSessions();
                int weekMinutes = history.getThisWeeksTotalWorkMinutes();

                updateStats(todaySessions, todayMinutes, weekSessions, weekMinutes);

                // Disable clear button if history is empty
                clearButton.setDisable(history.isEmpty());

                // Update chart
                updateChart(history);

        }

        private void updateChart(SessionHistory history) {
                // Clear previous data
                barChart.getData().clear();

                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Work Minutes");

                // Get sessions for the last N days
                LocalDate today = LocalDate.now();
                LocalDate sevenDaysAgo = today.minusDays(historyChartDays - 1); // inclusive of today

                try {
                        List<TimerSession> recentSessions = history.getSessionsInRange(sevenDaysAgo, today);

                        // Group by date and sum duration, ensure all 7 days are represented
                        Map<LocalDate, Integer> dailyMinutes = new TreeMap<>();

                        // Initialize last N days with 0
                        for (int i = 0; i < historyChartDays; i++) {
                                dailyMinutes.put(sevenDaysAgo.plusDays(i), 0);
                        }

                        // Fill with actual data (only completed work sessions)
                        Map<LocalDate, Integer> actualData = recentSessions.stream()
                                        .filter(TimerSession::isWorkSession)
                                        .filter(TimerSession::completed)
                                        .collect(Collectors.groupingBy(
                                                        session -> session.startTime().toLocalDate(),
                                                        Collectors.summingInt(TimerSession::durationMinutes)));

                        dailyMinutes.putAll(actualData);

                        DateTimeFormatter axisFormatter = DateTimeFormatter.ofPattern("EEE dd");

                        // Update axis label and rotation
                        CategoryAxis xAxis = (CategoryAxis) barChart.getXAxis();
                        xAxis.setLabel("Date (Last " + historyChartDays + " Days)");
                        if (historyChartDays > 14) {
                                xAxis.setTickLabelRotation(-45);
                        } else {
                                xAxis.setTickLabelRotation(0);
                        }

                        // Update title
                        barChart.setTitle("Last " + historyChartDays + " Days Activity");

                        for (Map.Entry<LocalDate, Integer> entry : dailyMinutes.entrySet()) {
                                String label = entry.getKey().format(axisFormatter);
                                series.getData().add(new XYChart.Data<>(label, entry.getValue()));
                        }

                        barChart.getData().add(series);

                } catch (Exception e) {
                        // Graceful fallback if history range fails or is empty
                        e.printStackTrace();
                }
        }

        private void toggleViewMode() {
                HistoryViewMode newMode = (currentMode == HistoryViewMode.TABLE)
                                ? HistoryViewMode.CHART
                                : HistoryViewMode.TABLE;
                setHistoryViewMode(newMode);

                // Notify listener to save preference
                if (onViewModeChanged != null) {
                        onViewModeChanged.accept(newMode);
                }
        }

        private void updateViewVisibility() {
                if (currentMode == HistoryViewMode.TABLE) {
                        if (viewContainer.getChildren().get(0) instanceof StackPane) {
                                viewContainer.getChildren().get(0).setVisible(true);
                        } else {
                                sessionTable.setVisible(true);
                        }
                        barChart.setVisible(false);
                        // Update icon to show "Chart" potential? No, usually button shows current view
                        // or switch action.
                        // Let's keep the generic switch icon.
                } else {
                        if (viewContainer.getChildren().get(0) instanceof StackPane) {
                                viewContainer.getChildren().get(0).setVisible(false);
                        } else {
                                sessionTable.setVisible(false);
                        }
                        barChart.setVisible(true);
                }
        }

        /**
         * Sets the view mode (Table or Chart).
         * 
         * @param mode the mode to set
         */
        public void setHistoryViewMode(HistoryViewMode mode) {
                if (mode != null) {
                        this.currentMode = mode;
                        updateViewVisibility();
                        updateToggleButtonState();
                }
        }

        private void updateToggleButtonState() {
                SVGPath icon = new SVGPath();
                String tooltipText;

                if (currentMode == HistoryViewMode.TABLE) {
                        // Current is Table, button should switch to Chart -> Show Chart Icon
                        // Bar Chart Icon
                        icon.setContent("M5 9.2h3V19H5zM10.6 5h2.8v14h-2.8zm5.6 8H19v6h-2.8z");
                        tooltipText = "Switch to Chart View";
                } else {
                        // Current is Chart, button should switch to Table -> Show Table/List Icon
                        // List View Icon (Material Design 'view_list')
                        icon.setContent("M4 14h4v-4H4v4zm0 5h4v-4H4v4zM4 9h4V5H4v4zm5 5h12v-4H9v4zm0 5h12v-4H9v4zM9 5v4h12V5H9z");
                        tooltipText = "Switch to Table View";
                }

                icon.setFill(javafx.scene.paint.Color.web(AppConstants.COLOR_PROGRESS_ACTIVE));
                icon.setScaleX(0.85);
                icon.setScaleY(0.85);

                viewToggleButton.setGraphic(icon);
                if (viewToggleButton.getTooltip() != null) {
                        viewToggleButton.getTooltip().setText(tooltipText);
                }
        }

        /**
         * Sets the callback for when the view mode is changed by the user.
         * 
         * @param callback the consumer to accept the new mode
         */
        public void setOnViewModeChanged(Consumer<HistoryViewMode> callback) {
                this.onViewModeChanged = callback;
        }

        /**
         * Applies the specified theme to this view.
         *
         * @param darkMode true to apply dark theme, false for light theme
         */
        public void applyTheme(boolean darkMode) {
                this.darkModeEnabled = darkMode;

                // Update stats label colors
                String textColor = darkMode ? AppConstants.COLOR_TEXT_SECONDARY_DARK : AppConstants.COLOR_TEXT_SECONDARY;
                todayStatsLabel.setStyle(String.format(STYLE_STATS_LABEL, textColor));
                weekStatsLabel.setStyle(String.format(STYLE_STATS_LABEL, textColor));

                updateTableBorderColor();
        }

        private void updateTableBorderColor() {
                if (tableBorderOverlay != null) {
                        String borderColor = darkModeEnabled ? "#5D4037" : "#C19A6B";
                        tableBorderOverlay.setStyle(String.format("""
                                -fx-border-color: %s;
                                -fx-border-radius: 20;
                                -fx-border-width: 1;
                                """, borderColor));
                }
        }

        /**
         * Updates the statistics labels.
         *
         * @param todaySessions number of sessions today
         * @param todayMinutes  minutes worked today
         * @param weekSessions  number of sessions this week
         * @param weekMinutes   minutes worked this week
         */
        private void updateStats(int todaySessions, int todayMinutes, int weekSessions, int weekMinutes) {
                todayStatsLabel.setText(String.format(
                                "Today: %d session%s (%s)",
                                todaySessions,
                                todaySessions == 1 ? "" : "s",
                                TimeFormatter.formatMinutes(todayMinutes)));

                weekStatsLabel.setText(String.format(
                                "This week: %d session%s (%s)",
                                weekSessions,
                                weekSessions == 1 ? "" : "s",
                                TimeFormatter.formatMinutes(weekMinutes)));
        }

        /**
         * Sets up the table columns with cell value factories.
         */
        private void setupTableColumns() {
                TableColumn<TimerSession, String> dateColumn = new TableColumn<>("Date");
                dateColumn.setPrefWidth(TABLE_COLUMN_DATE_WIDTH);
                dateColumn.setResizable(false);
                dateColumn.setCellValueFactory(
                                cellData -> new SimpleStringProperty(
                                                cellData.getValue().startTime().format(DATE_FORMATTER)));
                dateColumn.setCellFactory(column -> createCenteredCell());

                TableColumn<TimerSession, String> timeColumn = new TableColumn<>("Time");
                timeColumn.setPrefWidth(TABLE_COLUMN_TIME_WIDTH);
                timeColumn.setResizable(false);
                timeColumn.setCellValueFactory(
                                cellData -> new SimpleStringProperty(
                                                cellData.getValue().startTime().format(TIME_FORMATTER)));
                timeColumn.setCellFactory(column -> createCenteredCell());

                TableColumn<TimerSession, String> typeColumn = new TableColumn<>("Type");
                typeColumn.setPrefWidth(TABLE_COLUMN_TYPE_WIDTH);
                typeColumn.setResizable(false);
                typeColumn
                                .setCellValueFactory(cellData -> new SimpleStringProperty(
                                                cellData.getValue().type().getDisplayName()));
                typeColumn.setCellFactory(column -> createCenteredCell());

                TableColumn<TimerSession, String> durationColumn = new TableColumn<>("Duration");
                durationColumn.setPrefWidth(TABLE_COLUMN_DURATION_WIDTH);
                durationColumn.setResizable(false);
                durationColumn.setCellValueFactory(
                                cellData -> new SimpleStringProperty(cellData.getValue().durationMinutes() + " min"));
                durationColumn.setCellFactory(column -> createCenteredCell());

                TableColumn<TimerSession, String> statusColumn = new TableColumn<>("Status");
                statusColumn.setPrefWidth(TABLE_COLUMN_STATUS_WIDTH);
                statusColumn.setResizable(false);
                statusColumn.setCellValueFactory(
                                cellData -> new SimpleStringProperty(
                                                cellData.getValue().completed() ? "Completed" : "Skipped"));
                statusColumn.setCellFactory(column -> createCenteredCell());

                sessionTable.getColumns().add(dateColumn);
                sessionTable.getColumns().add(timeColumn);
                sessionTable.getColumns().add(typeColumn);
                sessionTable.getColumns().add(durationColumn);
                sessionTable.getColumns().add(statusColumn);

                // Placeholder for empty table
                sessionTable.setPlaceholder(new Label("No sessions recorded yet"));
        }

        /**
         * Creates a table cell with centered text alignment.
         *
         * @return a new TableCell with centered content
         */
        private TableCell<TimerSession, String> createCenteredCell() {
                return new TableCell<>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty || item == null) {
                                        setText(null);
                                } else {
                                        setText(item);
                                        setAlignment(Pos.CENTER);
                                }
                        }
                };
        }

        /**
         * Returns the session table for external configuration.
         *
         * @return the session table
         */
        public TableView<TimerSession> getSessionTable() {
                return sessionTable;
        }

        /**
         * Returns the clear history button for testing purposes.
         *
         * @return the clear history button
         */
        public Button getClearHistoryButton() {
                return clearButton;
        }

        public void setHistoryChartDays(int days) {
                this.historyChartDays = days;
        }

        /**
         * Sets the callback to be invoked when the settings button is clicked.
         * 
         * @param onSettingsClicked the callback handler
         */
        public void setOnSettingsClicked(Runnable onSettingsClicked) {
                this.onSettingsClicked = onSettingsClicked;
        }

}
