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
package io.github.kullik01.focusbean.controller;

import io.github.kullik01.focusbean.model.SessionHistory;
import io.github.kullik01.focusbean.model.TimerState;
import io.github.kullik01.focusbean.model.UserSettings;
import io.github.kullik01.focusbean.service.NotificationService;
import io.github.kullik01.focusbean.service.PersistenceService;
import io.github.kullik01.focusbean.service.TimerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TimerController}.
 */
class TimerControllerTest {

    /** Mocked timer service. */
    private TimerService timerService;
    /** Mocked persistence service. */
    private PersistenceService persistenceService;
    /** Mocked notification service. */
    private NotificationService notificationService;
    /** User settings for testing. */
    private UserSettings settings;
    /** Session history for testing. */
    private SessionHistory history;
    /** The controller under test. */
    private TimerController controller;

    /**
     * Initializes the test environment before each test.
     */
    @BeforeEach
    void setUp() {
        // Arrange
        timerService = mock(TimerService.class);
        persistenceService = mock(PersistenceService.class);
        notificationService = mock(NotificationService.class);
        settings = new UserSettings();
        history = new SessionHistory();

        // Mock default idle state
        when(timerService.getCurrentState()).thenReturn(TimerState.IDLE);

        controller = new TimerController(
                timerService,
                persistenceService,
                notificationService,
                settings,
                history);
    }

    /**
     * Verifies that the pending session type is null upon initialization.
     */
    @Test
    @DisplayName("Pending session type should be null initially")
    void pendingSessionTypeNullInitially() {
        // Act & Assert
        assertNull(controller.getPendingSessionType());
    }

    /**
     * Verifies that startOrResume starts a work session when no session is pending.
     */
    @Test
    @DisplayName("startOrResume should start work when pending session is null")
    void startOrResumeStartsWorkWhenNoPending() {
        // Act
        controller.startOrResume();

        // Assert
        verify(timerService).start(anyInt(), eq(TimerState.WORK));
    }

    /**
     * Verifies that updating settings does not affect the pending session type.
     */
    @Test
    @DisplayName("updateSettings should not affect pending session type")
    void updateSettingsShouldNotAffectPendingSessionType() {
        // Arrange
        // Initial state: no pending session
        assertNull(controller.getPendingSessionType());

        // Act
        controller.updateSettings(30, 10);

        // Assert
        // Pending session type should still be null (not affected by settings update)
        assertNull(controller.getPendingSessionType());

        // Verify settings were updated
        assertEquals(30, settings.getWorkDurationMinutes());
        assertEquals(10, settings.getBreakDurationMinutes());
    }

    /**
     * Verifies that reset preserves the null pending session type when no session was active.
     */
    @Test
    @DisplayName("reset should preserve null pending session type when no session was active")
    void resetPreservesNullPendingSessionType() {
        // Act
        controller.reset();

        // Assert
        assertNull(controller.getPendingSessionType());
    }

    /**
     * Verifies that updateSettings triggers data persistence.
     */
    @Test
    @DisplayName("updateSettings should persist data")
    void updateSettingsPersistsData() {
        // Act
        controller.updateSettings(25, 5);

        // Assert
        verify(persistenceService, atLeastOnce()).save(eq(settings), eq(history));
    }

    /**
     * Verifies that the controller returns the correct settings object.
     */
    @Test
    @DisplayName("Controller should return correct settings")
    void controllerReturnsSettings() {
        // Act & Assert
        assertSame(settings, controller.getSettings());
    }

    /**
     * Verifies that the controller returns the correct history object.
     */
    @Test
    @DisplayName("Controller should return correct history")
    void controllerReturnsHistory() {
        // Act & Assert
        assertSame(history, controller.getHistory());
    }

    /**
     * Verifies that startWork initiates a work session with the configured duration.
     */
    @Test
    @DisplayName("startWork should start work session with correct duration")
    void startWorkStartsWithCorrectDuration() {
        // Arrange
        settings.setWorkDurationMinutes(25);

        // Act
        controller.startWork();

        // Assert
        verify(timerService).start(eq(25 * 60), eq(TimerState.WORK));
    }

    /**
     * Verifies that startBreak initiates a break session with the configured duration.
     */
    @Test
    @DisplayName("startBreak should start break session with correct duration")
    void startBreakStartsWithCorrectDuration() {
        // Arrange
        settings.setBreakDurationMinutes(5);

        // Act
        controller.startBreak();

        // Assert
        verify(timerService).start(eq(5 * 60), eq(TimerState.BREAK));
    }

    /**
     * Verifies that pause delegates the call to the timer service.
     */
    @Test
    @DisplayName("pause should delegate to timerService")
    void pauseDelegatesToTimerService() {
        // Act
        controller.pause();

        // Assert
        verify(timerService).pause();
    }

    /**
     * Verifies that resume delegates the call to the timer service.
     */
    @Test
    @DisplayName("resume should delegate to timerService")
    void resumeDelegatesToTimerService() {
        // Act
        controller.resume();

        // Assert
        verify(timerService).resume();
    }
}
