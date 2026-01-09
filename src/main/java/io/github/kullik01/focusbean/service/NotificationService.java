package io.github.kullik01.focusbean.service;

import io.github.kullik01.focusbean.model.NotificationSound;
import io.github.kullik01.focusbean.model.TimerState;
import io.github.kullik01.focusbean.model.UserSettings;
import javafx.scene.media.AudioClip;

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
    private AudioClip currentSound;
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
     * @param sound      the sound to preview
     * @param customPath the custom sound path (only used if sound is CUSTOM)
     */
    public void previewSound(NotificationSound sound, String customPath) {
        if (sound.isSilent()) {
            return;
        }

        if (sound.isSystemBeep()) {
            playSystemBeep();
            return;
        }

        try {
            loadSound(sound, customPath);
            if (currentSound != null) {
                currentSound.play();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to preview sound", e);
            playSystemBeep();
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
            trayIcon.displayMessage(title, message, MessageType.INFO);
            LOGGER.log(Level.FINE, "Displayed system tray notification: {0}", title);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to show system tray notification", e);
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

            currentSound = new AudioClip(urlString);
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
     * Initializes the system tray icon for notifications.
     */
    private void initializeSystemTray() {
        if (!SystemTray.isSupported()) {
            LOGGER.warning("System tray is not supported on this platform");
            return;
        }

        try {
            SystemTray systemTray = SystemTray.getSystemTray();

            // Create a small icon (16x16 is standard for tray)
            BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g2d = image.createGraphics();
            g2d.setColor(new java.awt.Color(0, 120, 212)); // Windows blue
            g2d.fillOval(2, 2, 12, 12);
            g2d.dispose();

            trayIcon = new TrayIcon(image, "Focus Bean");
            trayIcon.setImageAutoSize(true);

            // Add to system tray to enable notifications
            systemTray.add(trayIcon);

            LOGGER.fine("System tray icon added for notifications");
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
