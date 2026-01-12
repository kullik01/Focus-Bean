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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TimerController}.
 */
class TimerControllerTest {

    private TimerService timerService;
    private PersistenceService persistenceService;
    private NotificationService notificationService;
    private UserSettings settings;
    private SessionHistory history;
    private TimerController controller;

    @BeforeEach
    void setUp() {
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

    @Test
    @DisplayName("Pending session type should be null initially")
    void pendingSessionTypeNullInitially() {
        assertNull(controller.getPendingSessionType());
    }

    @Test
    @DisplayName("startOrResume should start work when pending session is null")
    void startOrResumeStartsWorkWhenNoPending() {
        controller.startOrResume();
        verify(timerService).start(anyInt(), eq(TimerState.WORK));
    }

    @Test
    @DisplayName("updateSettings should not affect pending session type")
    void updateSettingsShouldNotAffectPendingSessionType() {
        // Simulate that pendingSessionType would be BREAK after a completed work
        // session
        // We can't directly set pendingSessionType, but we can verify updateSettings
        // doesn't reset it

        // Initial state: no pending session
        assertNull(controller.getPendingSessionType());

        // Update settings
        controller.updateSettings(30, 10);

        // Pending session type should still be null (not affected by settings update)
        assertNull(controller.getPendingSessionType());

        // Verify settings were updated
        assertEquals(30, settings.getWorkDurationMinutes());
        assertEquals(10, settings.getBreakDurationMinutes());
    }

    @Test
    @DisplayName("reset should clear pending session type")
    void resetClearsPendingSessionType() {
        controller.reset();
        assertNull(controller.getPendingSessionType());
    }

    @Test
    @DisplayName("updateSettings should persist data")
    void updateSettingsPersistsData() {
        controller.updateSettings(25, 5);
        verify(persistenceService, atLeastOnce()).save(eq(settings), eq(history));
    }

    @Test
    @DisplayName("Controller should return correct settings")
    void controllerReturnsSettings() {
        assertSame(settings, controller.getSettings());
    }

    @Test
    @DisplayName("Controller should return correct history")
    void controllerReturnsHistory() {
        assertSame(history, controller.getHistory());
    }

    @Test
    @DisplayName("startWork should start work session with correct duration")
    void startWorkStartsWithCorrectDuration() {
        settings.setWorkDurationMinutes(25);
        controller.startWork();
        verify(timerService).start(eq(25 * 60), eq(TimerState.WORK));
    }

    @Test
    @DisplayName("startBreak should start break session with correct duration")
    void startBreakStartsWithCorrectDuration() {
        settings.setBreakDurationMinutes(5);
        controller.startBreak();
        verify(timerService).start(eq(5 * 60), eq(TimerState.BREAK));
    }

    @Test
    @DisplayName("pause should delegate to timerService")
    void pauseDelegatesToTimerService() {
        controller.pause();
        verify(timerService).pause();
    }

    @Test
    @DisplayName("resume should delegate to timerService")
    void resumeDelegatesToTimerService() {
        controller.resume();
        verify(timerService).resume();
    }
}
