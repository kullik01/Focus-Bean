package io.github.kullik01.focusbean;

import io.github.kullik01.focusbean.controller.TimerController;
import io.github.kullik01.focusbean.model.SessionHistory;
import io.github.kullik01.focusbean.model.UserSettings;
import io.github.kullik01.focusbean.service.NotificationService;
import io.github.kullik01.focusbean.service.PersistenceService;
import io.github.kullik01.focusbean.service.TimerService;
import io.github.kullik01.focusbean.util.AppConstants;
import io.github.kullik01.focusbean.view.CustomTitleBar;
import io.github.kullik01.focusbean.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main JavaFX application class for Focus Bean.
 *
 * <p>
 * This class initializes the application, sets up the MVC components,
 * loads persisted data, and configures the primary stage. It also handles
 * saving data on application shutdown.
 * </p>
 */
public final class FocusBeanApplication extends Application {

    private static final Logger LOGGER = Logger.getLogger(FocusBeanApplication.class.getName());

    private TimerController controller;
    private PersistenceService persistenceService;

    /**
     * Application entry point called by JavaFX runtime.
     *
     * @param primaryStage the primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        LOGGER.info("Starting Focus Bean application");

        try {
            initializeApplication(primaryStage);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start application", e);
            throw e;
        }
    }

    /**
     * Initializes all application components and displays the main window.
     *
     * @param primaryStage the primary stage to configure
     */
    private void initializeApplication(Stage primaryStage) {
        // Initialize persistence
        persistenceService = new PersistenceService();
        PersistenceService.LoadedData loadedData = persistenceService.load();

        UserSettings settings = loadedData.settings();
        SessionHistory history = loadedData.history();

        LOGGER.log(Level.INFO, "Loaded settings: {0}", settings);
        LOGGER.log(Level.INFO, "Loaded {0} sessions from history", history.size());

        // Initialize services
        TimerService timerService = new TimerService();
        NotificationService notificationService = new NotificationService();

        // Initialize controller
        controller = new TimerController(
                timerService,
                persistenceService,
                notificationService,
                settings,
                history);

        // Initialize view
        MainView mainView = new MainView(controller);

        // Create custom title bar for undecorated window
        CustomTitleBar titleBar = new CustomTitleBar(primaryStage);

        // Create content container with title bar at top
        VBox contentBox = new VBox();
        contentBox.getChildren().addAll(titleBar, mainView);
        javafx.scene.layout.VBox.setVgrow(mainView, javafx.scene.layout.Priority.ALWAYS);

        // Style contentBox with background ONLY (no border here, as it gets clipped)
        contentBox.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 16;
                """, AppConstants.COLOR_WINDOW_BACKGROUND));

        // Clip content to rounded corners (bound to container size for precision)
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.setArcWidth(32);
        clip.setArcHeight(32);
        clip.widthProperty().bind(contentBox.widthProperty());
        clip.heightProperty().bind(contentBox.heightProperty());
        contentBox.setClip(clip);

        // Create a dedicated border overlay that won't be clipped to ensure the line is
        // visible
        Region borderOverlay = new Region();
        borderOverlay.setMouseTransparent(true);
        borderOverlay.setStyle(String.format("""
                -fx-background-color: transparent;
                -fx-border-color: %s;
                -fx-border-width: 1;
                -fx-border-radius: 16;
                """, AppConstants.COLOR_CARD_BORDER));

        // Create outer wrapper with content and border overlay, plus shadow
        javafx.scene.layout.StackPane root = new javafx.scene.layout.StackPane(contentBox, borderOverlay);
        root.setStyle("-fx-background-color: transparent;");

        // Create scene with transparent background for rounded corners
        Scene scene = new Scene(
                root,
                AppConstants.DEFAULT_WINDOW_WIDTH,
                AppConstants.DEFAULT_WINDOW_HEIGHT + 32);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

        // Set up keyboard shortcuts
        scene.setOnKeyPressed(mainView::handleKeyPress);

        // Configure stage as transparent (allows rounded corners)
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setTitle(AppConstants.APP_NAME);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setAlwaysOnTop(true);

        // Prevent closing unless explicitly requested via the close button
        primaryStage.setOnCloseRequest(event -> {
            Object closeRequested = primaryStage.getProperties().get("close_requested");
            if (closeRequested == null || !Boolean.TRUE.equals(closeRequested)) {
                LOGGER.info("Preventing close request (not explicit)");
                event.consume();
            }
        });

        primaryStage.show();
        LOGGER.info("Application window displayed");
    }

    /**
     * Called when the application is stopping.
     *
     * <p>
     * Saves current data to disk before exiting.
     * </p>
     */
    @Override
    public void stop() {
        LOGGER.info("Shutting down Focus Bean application");

        if (controller != null) {
            controller.saveData();
            LOGGER.info("Data saved successfully");
        }
    }

    /**
     * Main entry point for the application.
     *
     * <p>
     * This method is only used when running without the Launcher class.
     * The preferred entry point is {@link Launcher#main(String[])}.
     * </p>
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        LOGGER.info("Launching Focus Bean");
        launch(args);
    }
}
