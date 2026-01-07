package io.github.kullik01.focusbean;

import io.github.kullik01.focusbean.controller.TimerController;
import io.github.kullik01.focusbean.model.SessionHistory;
import io.github.kullik01.focusbean.model.UserSettings;
import io.github.kullik01.focusbean.service.PersistenceService;
import io.github.kullik01.focusbean.service.TimerService;
import io.github.kullik01.focusbean.util.AppConstants;
import io.github.kullik01.focusbean.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

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

        // Initialize controller
        controller = new TimerController(timerService, persistenceService, settings, history);

        // Initialize view
        MainView mainView = new MainView(controller);

        // Create scene
        Scene scene = new Scene(
                mainView,
                AppConstants.DEFAULT_WINDOW_WIDTH,
                AppConstants.DEFAULT_WINDOW_HEIGHT);

        // Set up keyboard shortcuts
        scene.setOnKeyPressed(mainView::handleKeyPress);

        // Configure stage
        primaryStage.setTitle(AppConstants.APP_NAME);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(AppConstants.MIN_WINDOW_WIDTH);
        primaryStage.setMinHeight(AppConstants.MIN_WINDOW_HEIGHT);

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
