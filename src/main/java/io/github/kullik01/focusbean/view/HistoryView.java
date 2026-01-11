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

        private static final String STYLE_HEADER_LABEL = """
                        -fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;
                        -fx-font-size: 16px;
                        -fx-font-weight: 600;
                        -fx-text-fill: %s;
                        """;

        private final TableView<TimerSession> sessionTable;
        private final Label todayStatsLabel;
        private final Label weekStatsLabel;
        private final Label headerLabel;
        private final Button clearHistoryButton;

        private Runnable onClearHistory;

        /**
         * Creates a new HistoryView with empty data.
         */
        public HistoryView() {
                headerLabel = new Label(AppConstants.LABEL_HISTORY);
                headerLabel.setStyle(String.format(STYLE_HEADER_LABEL, AppConstants.COLOR_TEXT_PRIMARY));

                clearHistoryButton = createClearHistoryButton();

                todayStatsLabel = new Label();
                todayStatsLabel.setStyle(String.format(STYLE_STATS_LABEL, AppConstants.COLOR_TEXT_SECONDARY));

                weekStatsLabel = new Label();
                weekStatsLabel.setStyle(String.format(STYLE_STATS_LABEL, AppConstants.COLOR_TEXT_SECONDARY));

                sessionTable = new TableView<>();
                setupTableColumns();

                // Header row with title and clear button
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                HBox headerRow = new HBox(10, headerLabel, spacer, clearHistoryButton);
                headerRow.setAlignment(Pos.CENTER_LEFT);

                VBox statsBox = new VBox(5, todayStatsLabel, weekStatsLabel);
                statsBox.setAlignment(Pos.CENTER_LEFT);

                setSpacing(15);
                setPadding(new Insets(20));
                setAlignment(Pos.TOP_CENTER);
                getChildren().addAll(headerRow, statsBox, sessionTable);

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
                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Clear History");
                confirmDialog.setHeaderText("Clear all session history?");

                // Use a Label for content to easily style it red
                Label contentLabel = new Label("This cannot be undone!");
                contentLabel.setStyle("-fx-text-fill: red; -fx-font-size: 13px;");
                confirmDialog.getDialogPane().setContent(contentLabel);

                // Configure dialog window behavior
                // Dialos are modal by default. The main window has setAlwaysOnTop(true),
                // so we need to ensure dialog shows with higher priority
                confirmDialog.setResizable(false);
                confirmDialog.getDialogPane().setMinWidth(300);

                // Ensure dialog appears on top of always-on-top main window
                confirmDialog.setOnShown(event -> {
                        try {
                                javafx.stage.Stage dialogStage = (javafx.stage.Stage) confirmDialog.getDialogPane()
                                                .getScene().getWindow();
                                if (dialogStage != null) {
                                        dialogStage.setAlwaysOnTop(true);
                                        dialogStage.toFront();
                                }
                        } catch (Exception e) {
                                // Ignore if we can't access the stage
                        }
                });

                // Apply brown coffee theme styling to match GUI
                String dialogStyle = String.format("""
                                -fx-background-color: %s;
                                """, AppConstants.COLOR_WINDOW_BACKGROUND);

                // OK button: default grey styling
                String okButtonStyle = """
                                -fx-background-color: #E0E0E0;
                                -fx-text-fill: #333333;
                                -fx-background-radius: 4;
                                -fx-border-radius: 4;
                                -fx-cursor: hand;
                                -fx-padding: 8 16 8 16;
                                -fx-font-size: 13px;
                                """;

                String okButtonHoverStyle = """
                                -fx-background-color: #D0D0D0;
                                -fx-text-fill: #333333;
                                -fx-background-radius: 4;
                                -fx-border-radius: 4;
                                -fx-cursor: hand;
                                -fx-padding: 8 16 8 16;
                                -fx-font-size: 13px;
                                """;

                // Cancel button: brown theme styling
                String cancelButtonStyle = """
                                -fx-background-color: #A0522D;
                                -fx-text-fill: white;
                                -fx-background-radius: 4;
                                -fx-border-radius: 4;
                                -fx-cursor: hand;
                                -fx-padding: 8 16 8 16;
                                -fx-font-size: 13px;
                                """;

                String cancelButtonHoverStyle = """
                                -fx-background-color: #8B4513;
                                -fx-text-fill: white;
                                -fx-background-radius: 4;
                                -fx-border-radius: 4;
                                -fx-cursor: hand;
                                -fx-padding: 8 16 8 16;
                                -fx-font-size: 13px;
                                """;

                confirmDialog.getDialogPane().setStyle(dialogStyle);

                // Style buttons using reliable lookupButton method
                Button okButton = (Button) confirmDialog.getDialogPane().lookupButton(ButtonType.OK);
                if (okButton != null) {
                        okButton.setStyle(okButtonStyle);
                        okButton.setOnMouseEntered(e -> okButton.setStyle(okButtonHoverStyle));
                        okButton.setOnMouseExited(e -> okButton.setStyle(okButtonStyle));
                }

                Button cancelButton = (Button) confirmDialog.getDialogPane().lookupButton(ButtonType.CANCEL);
                if (cancelButton != null) {
                        cancelButton.setStyle(cancelButtonStyle);
                        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle(cancelButtonHoverStyle));
                        cancelButton.setOnMouseExited(e -> cancelButton.setStyle(cancelButtonStyle));
                }

                // Ensure dialog appears in front of main window
                Optional<ButtonType> result = confirmDialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                        if (onClearHistory != null) {
                                onClearHistory.run();
                        }
                }
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
