package io.github.kullik01.focusbean.view;

import io.github.kullik01.focusbean.model.SessionHistory;
import io.github.kullik01.focusbean.model.TimerSession;
import io.github.kullik01.focusbean.util.AppConstants;
import io.github.kullik01.focusbean.util.TimeFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");
        private static final double TABLE_COLUMN_DATE_WIDTH = 140;
        private static final double TABLE_COLUMN_TYPE_WIDTH = 80;
        private static final double TABLE_COLUMN_DURATION_WIDTH = 80;
        private static final double TABLE_COLUMN_STATUS_WIDTH = 80;

        private static final String STYLE_STATS_LABEL = """
                        -fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;
                        -fx-font-size: 14px;
                        -fx-text-fill: %s;
                        """;

        private final TableView<TimerSession> sessionTable;
        private final Label todayStatsLabel;
        private final Label weekStatsLabel;
        private final Button clearHistoryButton;

        private Runnable onClearHistory;

        /**
         * Creates a new HistoryView with empty data.
         */
        public HistoryView() {

                todayStatsLabel = new Label();
                todayStatsLabel.setStyle(String.format(STYLE_STATS_LABEL, AppConstants.COLOR_TEXT_SECONDARY));

                weekStatsLabel = new Label();
                weekStatsLabel.setStyle(String.format(STYLE_STATS_LABEL, AppConstants.COLOR_TEXT_SECONDARY));

                clearHistoryButton = createClearHistoryButton();

                sessionTable = new TableView<>();
                setupTableColumns();

                VBox statsBox = new VBox(5, todayStatsLabel, weekStatsLabel);
                statsBox.setAlignment(Pos.CENTER_LEFT);

                // Header row with stats and clear button
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                HBox headerRow = new HBox(10, statsBox, spacer, clearHistoryButton);
                headerRow.setAlignment(Pos.TOP_LEFT);

                setSpacing(15);
                setPadding(new Insets(20));
                setAlignment(Pos.TOP_CENTER);
                getChildren().addAll(headerRow, sessionTable);

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
         * Handles the clear history button click by showing a confirmation dialog.
         */
        private void handleClearHistoryClick() {
                // Create a manual Stage instead of Alert to have full control over the Scene
                // and transparency
                javafx.stage.Stage dialogStage = new javafx.stage.Stage();
                dialogStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
                dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL); // Block interaction with main window
                dialogStage.setTitle("Clear History");

                // Load custom logo if available (for taskbar/icon, though transparent stage
                // might not show it)
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

                // --- 1. Title Bar ---
                HBox titleBar = new HBox(10);
                titleBar.setAlignment(Pos.CENTER_LEFT);
                titleBar.setPadding(new Insets(10, 15, 10, 15));
                titleBar.setStyle("-fx-background-color: transparent;");

                // Logo/Icon
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

                // Title Text
                Label titleLabel = new Label("Clear History");
                titleLabel.setStyle(
                                "-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 14px; -fx-text-fill: #333333;");
                titleBar.getChildren().add(titleLabel);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                titleBar.getChildren().add(spacer);

                Button closeBtn = new Button("✕");
                closeBtn.setStyle(
                                "-fx-background-color: transparent; -fx-text-fill: #5D4037; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0 5 0 5;");
                closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(
                                "-fx-background-color: rgba(93, 64, 55, 0.1); -fx-text-fill: #5D4037; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0 5 0 5; -fx-background-radius: 4;"));
                closeBtn.setOnMouseExited(e -> closeBtn.setStyle(
                                "-fx-background-color: transparent; -fx-text-fill: #5D4037; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0 5 0 5;"));
                closeBtn.setOnAction(e -> dialogStage.close());
                titleBar.getChildren().add(closeBtn);

                // Drag support
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

                // --- 2. Content ---
                HBox legacyHeaderBox = new HBox(15);
                legacyHeaderBox.setAlignment(Pos.CENTER_LEFT);
                legacyHeaderBox.setPadding(new Insets(10, 0, 15, 0));

                Label legacyHeaderText = new Label("Clear all session history?");
                legacyHeaderText.setStyle("-fx-font-size: 16px; -fx-text-fill: #333333;");

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
                warningLabel.setStyle("-fx-text-fill: red; -fx-font-size: 13px;");

                VBox contentBody = new VBox(0);
                contentBody.setPadding(new Insets(0, 20, 20, 20)); // Padding for content
                contentBody.getChildren().addAll(legacyHeaderBox, warningLabel);

                // --- 3. Button Bar ---
                HBox buttonBar = new HBox(10);
                buttonBar.setAlignment(Pos.CENTER_RIGHT);
                buttonBar.setPadding(new Insets(20, 20, 20, 20));

                Button okButton = new Button("OK");
                okButton.setDefaultButton(true);
                String okButtonStyle = """
                                -fx-background-color: #E0E0E0;
                                -fx-text-fill: #333333;
                                -fx-background-radius: 20;
                                -fx-cursor: hand;
                                -fx-padding: 6 16 6 16;
                                -fx-font-size: 13px;
                                -fx-min-width: 70;
                                """;
                String okButtonHoverStyle = """
                                -fx-background-color: #D0D0D0;
                                -fx-text-fill: #333333;
                                -fx-background-radius: 20;
                                -fx-cursor: hand;
                                -fx-padding: 6 16 6 16;
                                -fx-font-size: 13px;
                                -fx-min-width: 70;
                                """;
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

                // --- 4. Main Window Structure ---
                VBox dialogLayout = new VBox(0);
                dialogLayout.getChildren().addAll(titleBar, contentBody, buttonBar);

                // --- Styling the Visual Box ---
                // Nested background for perfect border
                dialogLayout.setStyle(String.format("""
                                -fx-background-color: #D7B49E, %s;
                                -fx-background-insets: 0, 1.5;
                                -fx-background-radius: 12, 10.5;
                                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 4);
                                """, AppConstants.COLOR_WINDOW_BACKGROUND));

                dialogLayout.setMinWidth(320);

                // Root Container (Transparent with Padding) to prevent clipping
                javafx.scene.layout.StackPane root = new javafx.scene.layout.StackPane(dialogLayout);
                root.setPadding(new Insets(20)); // Safe zone for shadow/borders
                root.setStyle("-fx-background-color: transparent;");

                javafx.scene.Scene scene = new javafx.scene.Scene(root);
                scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
                scene.getStylesheets().add(getClass().getResource("/io/github/kullik01/focusbean/view/styles.css")
                                .toExternalForm());

                dialogStage.setScene(scene);

                // Ensure on top
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
                clearHistoryButton.setDisable(history.isEmpty());

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
                dateColumn.setCellValueFactory(
                                cellData -> new SimpleStringProperty(
                                                cellData.getValue().startTime().format(DATE_FORMATTER)));

                TableColumn<TimerSession, String> typeColumn = new TableColumn<>("Type");
                typeColumn.setPrefWidth(TABLE_COLUMN_TYPE_WIDTH);
                typeColumn
                                .setCellValueFactory(cellData -> new SimpleStringProperty(
                                                cellData.getValue().type().getDisplayName()));

                TableColumn<TimerSession, String> durationColumn = new TableColumn<>("Duration");
                durationColumn.setPrefWidth(TABLE_COLUMN_DURATION_WIDTH);
                durationColumn.setCellValueFactory(
                                cellData -> new SimpleStringProperty(cellData.getValue().durationMinutes() + " min"));

                TableColumn<TimerSession, String> statusColumn = new TableColumn<>("Status");
                statusColumn.setPrefWidth(TABLE_COLUMN_STATUS_WIDTH);
                statusColumn.setCellValueFactory(
                                cellData -> new SimpleStringProperty(
                                                cellData.getValue().completed() ? "Completed" : "Skipped"));

                sessionTable.getColumns().add(dateColumn);
                sessionTable.getColumns().add(typeColumn);
                sessionTable.getColumns().add(durationColumn);
                sessionTable.getColumns().add(statusColumn);

                // Placeholder for empty table
                sessionTable.setPlaceholder(new Label("No sessions recorded yet"));
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
                return clearHistoryButton;
        }

}
