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
import io.github.kullik01.focusbean.view.MiniTimerView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
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

    /** Logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(FocusBeanApplication.class.getName());

    /** The controller managing the application state and logic. */
    private TimerController controller;
    /** The service responsible for persisting and loading data. */
    private PersistenceService persistenceService;
    /** The primary stage for the main application window. */
    private Stage primaryStage;
    /** The secondary stage for the mini floating timer. */
    private Stage miniStage;
    /** The main view containing all application tabs. */
    private MainView mainView;
    /** The compact view for the mini floating timer. */
    private MiniTimerView miniTimerView;

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
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Failed to start application", exception);
            throw exception;
        }
    }

    /**
     * Initializes all application components and displays the main window.
     *
     * @param stage the primary stage to configure
     */
    private void initializeApplication(Stage stage) {
        this.primaryStage = stage;
        // Set AppUserModelID for Windows notifications
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            try {
                com.sun.jna.platform.win32.Shell32.INSTANCE.SetCurrentProcessExplicitAppUserModelID(
                        new com.sun.jna.WString("Focus Bean"));
                LOGGER.info("Set AppUserModelID to io.github.kullik01.focusbean");
            } catch (Throwable throwable) {
                // Use Throwable to catch potential linkage errors if JNA fails to load
                LOGGER.log(Level.WARNING, "Failed to set AppUserModelID", throwable);
            }
        }

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
        mainView = new MainView(controller);

        // Create custom title bar for undecorated window
        CustomTitleBar titleBar = new CustomTitleBar(primaryStage);

        // Create content container with title bar at top
        VBox contentBox = new VBox();
        contentBox.getChildren().addAll(titleBar, mainView);
        javafx.scene.layout.VBox.setVgrow(mainView, javafx.scene.layout.Priority.ALWAYS);

        // Style contentBox with background ONLY (no border here, as it gets clipped)
        // Apply theme-appropriate colors
        String windowBgColor = settings.isDarkModeEnabled() ? "#1A1512" : AppConstants.COLOR_WINDOW_BACKGROUND;
        String borderColor = settings.isDarkModeEnabled() ? "#3D332B" : AppConstants.COLOR_CARD_BORDER;
        contentBox.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 16;
                """, windowBgColor));

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
                """, borderColor));

        // Pass the border overlay to MainView so it can update the color on theme changes
        mainView.setWindowBorderOverlay(borderOverlay);

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

        // Load application icon
        try {
            String logoPath = "/io/github/kullik01/focusbean/view/logo.png";
            if (getClass().getResource(logoPath) != null) {
                primaryStage.getIcons().add(new Image(getClass().getResourceAsStream(logoPath)));
            } else {
                // Fallback to searching in root if not found in package
                if (getClass().getResource("/logo.png") != null) {
                    primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/logo.png")));
                }
            }
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, "Failed to load application icon", exception);
        }

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

        // Wire mini mode callbacks from MainView
        mainView.setOnMiniModeRequested(this::showMiniMode);

        // Wire HostServices for opening URLs from the About tab
        mainView.getAboutView().setUrlOpener(url -> getHostServices().showDocument(url));
    }

    /**
     * Shows the mini floating timer and hides the main window.
     *
     * <p>The mini stage is a compact always-on-top window showing only the
     * circular timer, time display, and a start/pause button. It shares
     * the same {@link TimerController} as the main window.</p>
     */
    private void showMiniMode() {
        if (this.miniStage != null && this.miniStage.isShowing()) {
            LOGGER.fine("Mini mode already showing");
            return;
        }

        double mainX = this.primaryStage.getX();
        double mainY = this.primaryStage.getY();

        this.primaryStage.hide();

        boolean isDark = this.controller.getSettings().isDarkModeEnabled();
        this.miniTimerView = new MiniTimerView(this.controller, isDark);

        this.miniTimerView.setOnShowFullWindow(this::hideMiniMode);
        this.miniTimerView.setOnCloseMiniMode(this::hideMiniMode);
        this.miniTimerView.setOnMinimize(() -> {
            if (this.miniStage != null) {
                this.miniStage.setIconified(true);
            }
        });

        javafx.scene.shape.Rectangle clip =
                new javafx.scene.shape.Rectangle();
        clip.setArcWidth(40);
        clip.setArcHeight(40);
        clip.widthProperty().bind(this.miniTimerView.widthProperty());
        clip.heightProperty().bind(this.miniTimerView.heightProperty());
        this.miniTimerView.setClip(clip);

        String borderColor = isDark
                ? AppConstants.COLOR_CARD_BORDER_DARK
                : AppConstants.COLOR_CARD_BORDER;
        Region miniBorderOverlay = new Region();
        miniBorderOverlay.setMouseTransparent(true);
        miniBorderOverlay.setStyle(String.format("""
                -fx-background-color: transparent;
                -fx-border-color: %s;
                -fx-border-width: 1;
                -fx-border-radius: 20;
                """, borderColor));

        StackPane miniRoot =
                new StackPane(this.miniTimerView, miniBorderOverlay);
        miniRoot.setStyle("-fx-background-color: transparent;");

        Scene miniScene = new Scene(miniRoot);
        miniScene.setFill(javafx.scene.paint.Color.TRANSPARENT);

        miniScene.setOnKeyPressed(this.miniTimerView::handleKeyPress);

        this.miniStage = new Stage();
        this.miniStage.initStyle(StageStyle.TRANSPARENT);
        this.miniStage.setTitle(AppConstants.APP_NAME + " - Mini");
        this.miniStage.setScene(miniScene);
        this.miniStage.setResizable(false);
        this.miniStage.setAlwaysOnTop(true);

        this.miniStage.setX(mainX + 50);
        this.miniStage.setY(mainY + 50);

        try {
            String logoPath =
                    "/io/github/kullik01/focusbean/view/logo.png";
            if (this.getClass().getResource(logoPath) != null) {
                this.miniStage.getIcons().add(new Image(
                        this.getClass().getResourceAsStream(logoPath)));
            } else if (this.getClass().getResource("/logo.png") != null) {
                this.miniStage.getIcons().add(new Image(
                        this.getClass().getResourceAsStream("/logo.png")));
            }
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING,
                    "Failed to load mini window icon", exception);
        }

        final double[] dragOffset = new double[2];
        miniRoot.setOnMousePressed(event -> {
            if (event.getButton()
                    == javafx.scene.input.MouseButton.PRIMARY) {
                dragOffset[0] = event.getSceneX();
                dragOffset[1] = event.getSceneY();
            }
        });
        miniRoot.setOnMouseDragged(event -> {
            if (event.getButton()
                    == javafx.scene.input.MouseButton.PRIMARY) {
                this.miniStage.setX(
                        event.getScreenX() - dragOffset[0]);
                this.miniStage.setY(
                        event.getScreenY() - dragOffset[1]);
            }
        });

        this.miniStage.setOnCloseRequest(event -> {
            event.consume();
            this.hideMiniMode();
        });

        this.miniStage.show();
        LOGGER.info("Mini mode activated");
    }

    /**
     * Hides the mini timer and restores the main application window.
     */
    private void hideMiniMode() {
        if (this.miniStage != null) {
            this.miniStage.close();
            this.miniStage = null;
            this.miniTimerView = null;
        }

        this.primaryStage.show();
        LOGGER.info("Full window restored from mini mode");
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

        // Close mini stage if open
        if (miniStage != null) {
            miniStage.close();
            miniStage = null;
        }

        if (controller != null) {
            controller.shutdown();
            LOGGER.info("Controller shutdown complete");
        }

        // Force JVM exit to ensure all threads terminate
        System.exit(0);
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
