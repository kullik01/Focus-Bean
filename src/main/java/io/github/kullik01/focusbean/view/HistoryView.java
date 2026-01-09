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

        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
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

        private static final String STYLE_CLEAR_BUTTON = """
                        -fx-background-color: transparent;
                        -fx-text-fill: #e74c3c;
                        -fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;
                        -fx-font-size: 12px;
                        -fx-cursor: hand;
                        -fx-padding: 4 8;
                        -fx-border-color: #e74c3c;
                        -fx-border-radius: 4;
                        -fx-background-radius: 4;
                        """;

        private static final String STYLE_CLEAR_BUTTON_HOVER = """
                        -fx-background-color: #e74c3c;
                        -fx-text-fill: white;
                        -fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;
                        -fx-font-size: 12px;
                        -fx-cursor: hand;
                        -fx-padding: 4 8;
                        -fx-border-color: #e74c3c;
                        -fx-border-radius: 4;
                        -fx-background-radius: 4;
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
         * Creates and configures the clear history button.
         *
         * @return the configured button
         */
        private Button createClearHistoryButton() {
                Button button = new Button(AppConstants.LABEL_CLEAR_HISTORY);
                button.setStyle(STYLE_CLEAR_BUTTON);

                button.setOnMouseEntered(e -> button.setStyle(STYLE_CLEAR_BUTTON_HOVER));
                button.setOnMouseExited(e -> button.setStyle(STYLE_CLEAR_BUTTON));

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
                confirmDialog.setContentText("This cannot be undone.");
                confirmDialog.setResizable(true);
                confirmDialog.getDialogPane().setMinWidth(300);

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
