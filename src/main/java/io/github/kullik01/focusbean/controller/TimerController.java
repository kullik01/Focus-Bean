package io.github.kullik01.focusbean.controller;

import io.github.kullik01.focusbean.model.SessionHistory;
import io.github.kullik01.focusbean.model.TimerSession;
import io.github.kullik01.focusbean.model.TimerState;
import io.github.kullik01.focusbean.model.UserSettings;
import io.github.kullik01.focusbean.service.PersistenceService;
import io.github.kullik01.focusbean.service.TimerService;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Orchestrates the Pomodoro timer workflow and coordinates between model, view,
 * and services.
 *
 * <p>
 * This controller manages the timer state machine, handles transitions between
 * work and break sessions, records completed sessions to history, and persists
 * data to disk.
 * </p>
 *
 * <p>
 * The controller exposes observable properties that views can bind to for
 * automatic UI updates.
 * </p>
 *
 * <p>
 * Thread safety: This class must only be used from the JavaFX Application
 * Thread.
 * </p>
 */
public final class TimerController {

    private static final Logger LOGGER = Logger.getLogger(TimerController.class.getName());

    private final TimerService timerService;
    private final PersistenceService persistenceService;
    private final UserSettings settings;
    private final SessionHistory history;

    private LocalDateTime currentSessionStartTime;
    private int currentSessionDuration;
    private TimerState currentSessionType;
    private TimerState pendingSessionType;

    /**
     * Creates a new TimerController with the specified services and models.
     *
     * @param timerService       the timer service for countdown functionality
     * @param persistenceService the persistence service for saving data
     * @param settings           the user settings
     * @param history            the session history
     * @throws NullPointerException if any parameter is null
     */
    public TimerController(
            TimerService timerService,
            PersistenceService persistenceService,
            UserSettings settings,
            SessionHistory history) {
        this.timerService = Objects.requireNonNull(timerService, "timerService must not be null");
        this.persistenceService = Objects.requireNonNull(persistenceService, "persistenceService must not be null");
        this.settings = Objects.requireNonNull(settings, "settings must not be null");
        this.history = Objects.requireNonNull(history, "history must not be null");

        timerService.setOnTimerComplete(this::onTimerComplete);

        LOGGER.fine("TimerController initialized");
    }

    /**
     * Starts a new work session.
     *
     * <p>
     * If a session is already running, it will be stopped and a new
     * work session will begin.
     * </p>
     */
    public void startWork() {
        int durationSeconds = settings.getWorkDurationSeconds();
        startSession(TimerState.WORK, durationSeconds, settings.getWorkDurationMinutes());
    }

    /**
     * Starts a new break session.
     *
     * <p>
     * If a session is already running, it will be stopped and a new
     * break session will begin.
     * </p>
     */
    public void startBreak() {
        int durationSeconds = settings.getBreakDurationSeconds();
        startSession(TimerState.BREAK, durationSeconds, settings.getBreakDurationMinutes());
    }

    /**
     * Starts the timer based on the current state.
     *
     * <p>
     * If IDLE, starts a work or break session based on the pending session type.
     * If PAUSED, resumes the current session.
     * </p>
     */
    public void startOrResume() {
        TimerState state = timerService.getCurrentState();

        if (state == TimerState.IDLE) {
            if (pendingSessionType == TimerState.BREAK) {
                startBreak();
            } else {
                startWork();
            }
        } else if (state == TimerState.PAUSED) {
            resume();
        }
    }

    /**
     * Pauses the currently running timer.
     *
     * <p>
     * If no session is running, this method has no effect.
     * </p>
     */
    public void pause() {
        timerService.pause();
    }

    /**
     * Resumes a paused timer.
     *
     * <p>
     * If the timer is not paused, this method has no effect.
     * </p>
     */
    public void resume() {
        timerService.resume();
    }

    /**
     * Stops and resets the timer to IDLE state.
     *
     * <p>
     * Any current session will be discarded without being recorded.
     * The pending session type is reset to WORK.
     * </p>
     */
    public void reset() {
        currentSessionStartTime = null;
        currentSessionDuration = 0;
        currentSessionType = null;
        pendingSessionType = null;
        timerService.reset();

        LOGGER.info("Timer reset, current session discarded");
    }

    /**
     * Skips the current session and transitions to the next.
     *
     * <p>
     * The skipped session will be recorded as incomplete.
     * </p>
     */
    public void skip() {
        if (currentSessionStartTime != null && currentSessionType != null) {
            recordSession(false);
        }
        timerService.skip();
    }

    /**
     * Updates the user settings.
     *
     * <p>
     * Changes take effect on the next session start. The settings
     * are persisted to disk.
     * </p>
     *
     * @param workMinutes  the new work duration in minutes
     * @param breakMinutes the new break duration in minutes
     */
    public void updateSettings(int workMinutes, int breakMinutes) {
        settings.setWorkDurationMinutes(workMinutes);
        settings.setBreakDurationMinutes(breakMinutes);
        saveData();

        LOGGER.log(Level.INFO, "Settings updated: work={0}min, break={1}min",
                new Object[] { workMinutes, breakMinutes });
    }

    /**
     * Saves current data to disk.
     */
    public void saveData() {
        persistenceService.save(settings, history);
    }

    /**
     * Returns the remaining seconds property for UI binding.
     *
     * @return the read-only remaining seconds property
     */
    public ReadOnlyIntegerProperty remainingSecondsProperty() {
        return timerService.remainingSecondsProperty();
    }

    /**
     * Returns the current state property for UI binding.
     *
     * @return the read-only current state property
     */
    public ReadOnlyObjectProperty<TimerState> currentStateProperty() {
        return timerService.currentStateProperty();
    }

    /**
     * Returns the current timer state.
     *
     * @return the current state
     */
    public TimerState getCurrentState() {
        return timerService.getCurrentState();
    }

    /**
     * Returns the remaining seconds.
     *
     * @return the remaining seconds
     */
    public int getRemainingSeconds() {
        return timerService.getRemainingSeconds();
    }

    /**
     * Returns the user settings.
     *
     * @return the user settings
     */
    public UserSettings getSettings() {
        return settings;
    }

    /**
     * Returns the session history.
     *
     * @return the session history
     */
    public SessionHistory getHistory() {
        return history;
    }

    /**
     * Returns the state the timer was in before being paused.
     *
     * @return the state before pause, or {@code null} if not paused
     */
    public TimerState getStateBeforePause() {
        return timerService.getStateBeforePause();
    }

    /**
     * Returns the pending session type that will start when the user presses play.
     *
     * <p>
     * This is set after a session completes to indicate what type of session
     * should start next. Returns {@code null} if a work session should start
     * (the default behavior).
     * </p>
     *
     * @return the pending session type, or {@code null} if work is pending
     */
    public TimerState getPendingSessionType() {
        return pendingSessionType;
    }

    /**
     * Starts a timer session with the given parameters.
     *
     * @param state           the session type (WORK or BREAK)
     * @param durationSeconds the countdown duration
     * @param durationMinutes the configured duration for recording
     */
    private void startSession(TimerState state, int durationSeconds, int durationMinutes) {
        currentSessionStartTime = LocalDateTime.now();
        currentSessionDuration = durationMinutes;
        currentSessionType = state;

        timerService.start(durationSeconds, state);

        LOGGER.log(Level.INFO, "Started {0} session: {1} minutes",
                new Object[] { state, durationMinutes });
    }

    /**
     * Called when the timer completes (reaches zero).
     *
     * <p>
     * Records the completed session and sets the pending session type
     * for the user to start manually. The timer returns to IDLE state.
     * </p>
     */
    private void onTimerComplete() {
        TimerState completedSessionType = currentSessionType;

        if (currentSessionStartTime != null && currentSessionType != null) {
            recordSession(true);
        }

        // Set pending session type for next manual start
        if (completedSessionType == TimerState.WORK) {
            LOGGER.info("Work session complete, break is now pending");
            pendingSessionType = TimerState.BREAK;
        } else if (completedSessionType == TimerState.BREAK) {
            LOGGER.info("Break session complete, work is now pending");
            pendingSessionType = TimerState.WORK;
        }

        // Return to IDLE state - user must press play to start next session
        timerService.reset();
    }

    /**
     * Records a session to history and saves to disk.
     *
     * @param completed {@code true} if the session completed normally
     */
    private void recordSession(boolean completed) {
        LocalDateTime endTime = LocalDateTime.now();

        TimerSession session;
        if (completed) {
            if (currentSessionType == TimerState.WORK) {
                session = TimerSession.completedWork(currentSessionStartTime, endTime, currentSessionDuration);
            } else {
                session = TimerSession.completedBreak(currentSessionStartTime, endTime, currentSessionDuration);
            }
        } else {
            session = TimerSession.interrupted(currentSessionStartTime, endTime, currentSessionType,
                    currentSessionDuration);
        }

        history.addSession(session);
        saveData();

        LOGGER.log(Level.INFO, "Recorded {0} session: completed={1}",
                new Object[] { currentSessionType, completed });

        // Reset tracking variables
        currentSessionStartTime = null;
        currentSessionDuration = 0;
        currentSessionType = null;
    }
}
