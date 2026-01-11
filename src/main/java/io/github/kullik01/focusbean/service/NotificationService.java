package io.github.kullik01.focusbean.service;

import io.github.kullik01.focusbean.model.NotificationSound;
import io.github.kullik01.focusbean.model.TimerState;
import io.github.kullik01.focusbean.model.UserSettings;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles notifications when timer sessions complete.
 *
 * <p>
 * This service provides audio and visual feedback to the user when a focus
 * or break session ends. Notifications are configurable via
 * {@link UserSettings}.
 * </p>
 *
 * <p>
 * The service supports:
 * </p>
 * <ul>
 * <li>Sound notification: Plays a selected audio sound</li>
 * <li>Popup notification: Shows a Windows system tray notification</li>
 * <li>Custom sounds: Users can select their own sound file</li>
 * </ul>
 *
 * <p>
 * Thread safety: This class must only be used from the JavaFX Application
 * Thread.
 * </p>
 */
public final class NotificationService {

    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());

    private TrayIcon trayIcon;
    private MediaPlayer currentSound;
    private NotificationSound loadedSoundType;
    private String loadedCustomPath;

    /**
     * Creates a new NotificationService and initializes system tray.
     */
    public NotificationService() {
        initializeSystemTray();
        LOGGER.fine("NotificationService initialized");
    }

    /**
     * Notifies the user that a session has completed.
     *
     * <p>
     * This method checks the user settings and triggers the appropriate
     * notifications (sound and/or popup) based on the configuration.
     * </p>
     *
     * @param completedSessionType the type of session that completed (WORK or
     *                             BREAK)
     * @param settings             the user settings containing notification
     *                             preferences
     * @throws NullPointerException if completedSessionType or settings is null
     */
    public void notifySessionComplete(TimerState completedSessionType, UserSettings settings) {
        Objects.requireNonNull(completedSessionType, "completedSessionType must not be null");
        Objects.requireNonNull(settings, "settings must not be null");

        LOGGER.log(Level.INFO, "Session complete notification triggered for: {0}", completedSessionType);

        if (settings.isSoundNotificationEnabled()) {
            playSound(settings);
        }

        if (settings.isPopupNotificationEnabled()) {
            showSystemTrayNotification(completedSessionType);
        }
    }

    /**
     * Plays the notification sound based on user settings.
     *
     * @param settings the user settings containing sound preferences
     */
    public void playSound(UserSettings settings) {
        NotificationSound sound = settings.getNotificationSound();

        if (sound.isSilent()) {
            LOGGER.fine("Sound is set to NONE, skipping playback");
            return;
        }

        if (sound.isSystemBeep()) {
            playSystemBeep();
            return;
        }

        try {
            // Load sound if needed (either different type or different custom path)
            if (needsReload(sound, settings.getCustomSoundPath())) {
                loadSound(sound, settings.getCustomSoundPath());
            }

            if (currentSound != null) {
                currentSound.play();
                LOGGER.fine("Played notification sound");
            } else {
                LOGGER.warning("No sound loaded, falling back to system beep");
                playSystemBeep();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to play sound, using fallback beep", e);
            playSystemBeep();
        }
    }

    /**
     * Previews a notification sound for settings dialog.
     *
     * @param sound        the sound to preview
     * @param customPath   the custom sound path (only used if sound is CUSTOM)
     * @param onCompletion callback to run when playback finishes (can be null)
     */
    public void previewSound(NotificationSound sound, String customPath, Runnable onCompletion) {
        if (sound.isSilent()) {
            if (onCompletion != null) {
                // Run on FX thread just in case
                javafx.application.Platform.runLater(onCompletion);
            }
            return;
        }

        if (sound.isSystemBeep()) {
            playSystemBeep();
            if (onCompletion != null) {
                javafx.application.Platform.runLater(onCompletion);
            }
            return;
        }

        try {
            // Check if we are already playing this exact sound
            if (currentSound != null && currentSound.getStatus() == MediaPlayer.Status.PLAYING) {
                // If the same sound is requested, maybe we just want to let it play?
                // But usually preview button acting as stop means we should have stopped it
                // before calling this.
                // However, if we need to load a NEW sound, we should plain load it.
                // The logical flow in UI: if playing -> stop; if not playing -> preview.
                // So this method is called when we want to START preview.
                currentSound.stop();
            }

            loadSound(sound, customPath);
            if (currentSound != null) {
                if (onCompletion != null) {
                    currentSound.setOnEndOfMedia(onCompletion);
                }
                currentSound.play();
            } else {
                if (onCompletion != null) {
                    javafx.application.Platform.runLater(onCompletion);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to preview sound", e);
            playSystemBeep();
            if (onCompletion != null) {
                javafx.application.Platform.runLater(onCompletion);
            }
        }
    }

    /**
     * Shows a Windows system tray notification indicating session completion.
     *
     * @param completedSessionType the type of session that completed
     */
    public void showSystemTrayNotification(TimerState completedSessionType) {
        if (!SystemTray.isSupported()) {
            LOGGER.warning("System tray not supported, cannot show notification");
            return;
        }

        if (trayIcon == null) {
            LOGGER.warning("Tray icon not initialized, cannot show notification");
            return;
        }

        String title;
        String message;

        if (completedSessionType == TimerState.WORK) {
            title = "Focus Session Complete!";
            message = "Great work! It's time to take a break.";
        } else if (completedSessionType == TimerState.BREAK) {
            title = "Break Time Over!";
            message = "Ready to get back to work?";
        } else {
            title = "Session Complete!";
            message = "Your timer has finished.";
        }

        try {
            // Use custom toast notification instead of system tray
            io.github.kullik01.focusbean.view.ToastNotification.show(title, message);
            LOGGER.log(Level.FINE, "Displayed toast notification: {0}", title);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to show toast notification", e);
        }
    }

    /**
     * Checks if the sound needs to be reloaded.
     */
    private boolean needsReload(NotificationSound sound, String customPath) {
        if (currentSound == null || loadedSoundType != sound) {
            return true;
        }
        if (sound.isCustom() && !Objects.equals(loadedCustomPath, customPath)) {
            return true;
        }
        return false;
    }

    /**
     * Loads a notification sound.
     *
     * @param sound      the sound to load
     * @param customPath the custom sound path (for CUSTOM sound type)
     */
    private void loadSound(NotificationSound sound, String customPath) {
        try {
            String urlString;

            if (sound.isCustom()) {
                if (customPath == null || customPath.isBlank()) {
                    LOGGER.warning("Custom sound selected but no path provided");
                    currentSound = null;
                    return;
                }
                File file = new File(customPath);
                if (!file.exists()) {
                    LOGGER.log(Level.WARNING, "Custom sound file not found: {0}", customPath);
                    currentSound = null;
                    return;
                }
                urlString = file.toURI().toString();
            } else {
                URL resourceUrl = getClass().getResource(sound.getResourcePath());
                if (resourceUrl == null) {
                    LOGGER.log(Level.WARNING, "Sound resource not found: {0}", sound.getResourcePath());
                    currentSound = null;
                    return;
                }
                urlString = resourceUrl.toExternalForm();
            }

            // Dispose previous player if exists
            if (currentSound != null) {
                currentSound.dispose();
            }

            Media media = new Media(urlString);
            currentSound = new MediaPlayer(media);
            currentSound.setVolume(0.8);
            loadedSoundType = sound;
            loadedCustomPath = customPath;

            LOGGER.log(Level.FINE, "Loaded sound: {0}", sound);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load sound", e);
            currentSound = null;
        }
    }

    /**
     * Stops the currently playing sound.
     */
    public void stopSound() {
        if (currentSound != null) {
            currentSound.stop();
        }
    }

    /**
     * Initializes the system tray icon for notifications.
     */
    private void initializeSystemTray() {
        if (!SystemTray.isSupported()) {
            LOGGER.warning("System tray is not supported on this platform");
            return;
        }

        try {
            SystemTray systemTray = SystemTray.getSystemTray();

            // Create a larger icon for better quality in notifications
            // Windows will downscale it for the tray but use the resolution for toasts
            int size = 256;
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g2d = image.createGraphics();

            // Enable anti-aliasing for smooth edges
            g2d.setRenderingHint(
                    java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(
                    java.awt.RenderingHints.KEY_RENDERING,
                    java.awt.RenderingHints.VALUE_RENDER_QUALITY);

            g2d.setColor(new java.awt.Color(93, 64, 55)); // Dark Coffee Brown (#5D4037)

            // Draw circle with slight padding
            int padding = size / 8;
            g2d.fillOval(padding, padding, size - (2 * padding), size - (2 * padding));
            g2d.dispose();

            trayIcon = new TrayIcon(image, "Focus Bean");
            trayIcon.setImageAutoSize(true);

            // Add to system tray to enable notifications
            systemTray.add(trayIcon);

            LOGGER.fine("System tray icon added for notification");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to initialize system tray", e);
            trayIcon = null;
        }
    }

    /**
     * Plays a system beep as a fallback notification.
     */
    private void playSystemBeep() {
        Toolkit.getDefaultToolkit().beep();
        LOGGER.fine("Played system beep");
    }
}
