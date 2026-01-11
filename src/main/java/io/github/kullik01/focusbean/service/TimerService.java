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
package io.github.kullik01.focusbean.service;

import io.github.kullik01.focusbean.model.TimerState;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Duration;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the Pomodoro timer countdown using JavaFX Timeline.
 *
 * <p>
 * This service handles the core timing functionality, providing observable
 * properties for remaining time and current state. The UI can bind to these
 * properties for automatic updates.
 * </p>
 *
 * <p>
 * The timer operates on one-second intervals and notifies listeners when
 * the countdown reaches zero via the {@link #setOnTimerComplete(Runnable)}
 * callback.
 * </p>
 *
 * <p>
 * Thread safety: This class must only be used from the JavaFX Application
 * Thread.
 * </p>
 */
public final class TimerService {

    private static final Logger LOGGER = Logger.getLogger(TimerService.class.getName());
    private static final int ONE_SECOND_MS = 1000;

    private final Timeline timeline;
    private final IntegerProperty remainingSeconds;
    private final ObjectProperty<TimerState> currentState;

    private Runnable onTimerComplete;
    private TimerState stateBeforePause;

    /**
     * Creates a new TimerService in the IDLE state.
     */
    public TimerService() {
        this.remainingSeconds = new SimpleIntegerProperty(0);
        this.currentState = new SimpleObjectProperty<>(TimerState.IDLE);
        this.stateBeforePause = null;

        this.timeline = new Timeline(new KeyFrame(
                Duration.millis(ONE_SECOND_MS),
                event -> tick()));
        this.timeline.setCycleCount(Timeline.INDEFINITE);

        LOGGER.fine("TimerService initialized");
    }

    /**
     * Starts the timer for the specified duration.
     *
     * <p>
     * This will stop any currently running timer and start fresh
     * with the given duration and state.
     * </p>
     *
     * @param durationSeconds the countdown duration in seconds
     * @param state           the state to set (must be WORK or BREAK)
     * @throws IllegalArgumentException if durationSeconds is not positive
     *                                  or state is not WORK or BREAK
     */
    public void start(int durationSeconds, TimerState state) {
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("durationSeconds must be positive, was: " + durationSeconds);
        }
        if (state != TimerState.WORK && state != TimerState.BREAK) {
            throw new IllegalArgumentException("state must be WORK or BREAK, was: " + state);
        }

        timeline.stop();
        remainingSeconds.set(durationSeconds);
        currentState.set(state);
        stateBeforePause = null;
        timeline.play();

        LOGGER.log(Level.INFO, "Timer started: {0} seconds in {1} state",
                new Object[] { durationSeconds, state });
    }

    /**
     * Pauses the currently running timer.
     *
     * <p>
     * If the timer is not running, this method has no effect.
     * </p>
     */
    public void pause() {
        if (!currentState.get().isRunning()) {
            LOGGER.fine("Pause called but timer is not running");
            return;
        }

        stateBeforePause = currentState.get();
        timeline.pause();
        currentState.set(TimerState.PAUSED);

        LOGGER.log(Level.INFO, "Timer paused with {0} seconds remaining",
                remainingSeconds.get());
    }

    /**
     * Resumes a paused timer.
     *
     * <p>
     * If the timer is not paused, this method has no effect.
     * </p>
     */
    public void resume() {
        if (currentState.get() != TimerState.PAUSED || stateBeforePause == null) {
            LOGGER.fine("Resume called but timer is not paused");
            return;
        }

        currentState.set(stateBeforePause);
        stateBeforePause = null;
        timeline.play();

        LOGGER.log(Level.INFO, "Timer resumed with {0} seconds remaining",
                remainingSeconds.get());
    }

    /**
     * Stops and resets the timer to IDLE state.
     *
     * <p>
     * The remaining seconds will be set to zero.
     * </p>
     */
    public void reset() {
        timeline.stop();
        remainingSeconds.set(0);
        currentState.set(TimerState.IDLE);
        stateBeforePause = null;

        LOGGER.info("Timer reset to IDLE");
    }

    /**
     * Skips the current session and transitions to the next state.
     *
     * <p>
     * If in WORK state, transitions to BREAK. If in BREAK state,
     * transitions to WORK. Triggers the completion callback.
     * </p>
     */
    public void skip() {
        if (currentState.get() == TimerState.IDLE) {
            LOGGER.fine("Skip called but timer is IDLE");
            return;
        }

        LOGGER.info("Skipping current session");
        timeline.stop();
        remainingSeconds.set(0);

        if (onTimerComplete != null) {
            onTimerComplete.run();
        }
    }

    /**
     * Sets the callback to invoke when the timer reaches zero.
     *
     * @param callback the callback to invoke, may be {@code null}
     */
    public void setOnTimerComplete(Runnable callback) {
        this.onTimerComplete = callback;
    }

    /**
     * Returns the remaining seconds property for binding.
     *
     * @return the read-only remaining seconds property
     */
    public ReadOnlyIntegerProperty remainingSecondsProperty() {
        return remainingSeconds;
    }

    /**
     * Returns the current remaining seconds.
     *
     * @return the remaining seconds
     */
    public int getRemainingSeconds() {
        return remainingSeconds.get();
    }

    /**
     * Returns the current state property for binding.
     *
     * @return the read-only current state property
     */
    public ReadOnlyObjectProperty<TimerState> currentStateProperty() {
        return currentState;
    }

    /**
     * Returns the current timer state.
     *
     * @return the current state
     */
    public TimerState getCurrentState() {
        return currentState.get();
    }

    /**
     * Checks if the timer is currently running (not paused or idle).
     *
     * @return {@code true} if the timer is actively counting down
     */
    public boolean isRunning() {
        return currentState.get().isRunning();
    }

    /**
     * Checks if the timer is paused.
     *
     * @return {@code true} if the timer is paused
     */
    public boolean isPaused() {
        return currentState.get() == TimerState.PAUSED;
    }

    /**
     * Returns the state the timer was in before being paused.
     *
     * @return the state before pause, or {@code null} if not paused
     */
    public TimerState getStateBeforePause() {
        return stateBeforePause;
    }

    /**
     * Called every second by the Timeline to decrement the counter.
     *
     * <p>
     * When the counter reaches zero, the timer stops and invokes
     * the completion callback if one is set.
     * </p>
     */
    private void tick() {
        int current = remainingSeconds.get();

        if (current > 0) {
            remainingSeconds.set(current - 1);
        }

        if (remainingSeconds.get() == 0) {
            timeline.stop();
            LOGGER.info("Timer completed");

            if (onTimerComplete != null) {
                onTimerComplete.run();
            }
        }
    }
}
