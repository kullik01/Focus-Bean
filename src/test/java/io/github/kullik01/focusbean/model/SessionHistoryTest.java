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
package io.github.kullik01.focusbean.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SessionHistory}.
 */
class SessionHistoryTest {

    private SessionHistory history;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        history = new SessionHistory();
        now = LocalDateTime.now();
    }

    @Test
    @DisplayName("New history should be empty")
    void newHistoryIsEmpty() {
        assertTrue(history.isEmpty());
        assertEquals(0, history.size());
    }

    @Test
    @DisplayName("Should add session to history")
    void addSession() {
        TimerSession session = TimerSession.completedWork(now.minusMinutes(25), now, 25);
        history.addSession(session);

        assertFalse(history.isEmpty());
        assertEquals(1, history.size());
        assertEquals(session, history.getSessions().get(0));
    }

    @Test
    @DisplayName("getSessions should return unmodifiable list")
    void getSessionsReturnsUnmodifiableList() {
        TimerSession session = TimerSession.completedWork(now.minusMinutes(25), now, 25);
        history.addSession(session);

        List<TimerSession> sessions = history.getSessions();
        assertThrows(UnsupportedOperationException.class, () -> sessions.add(session));
    }

    @Test
    @DisplayName("Should filter sessions by date")
    void getSessionsForDate() {
        LocalDateTime today = now;
        LocalDateTime yesterday = now.minusDays(1);

        TimerSession todaySession = TimerSession.completedWork(
                today.minusMinutes(25), today, 25);
        TimerSession yesterdaySession = TimerSession.completedWork(
                yesterday.minusMinutes(25), yesterday, 25);

        history.addSession(todaySession);
        history.addSession(yesterdaySession);

        List<TimerSession> todaysSessions = history.getSessionsForDate(LocalDate.now());
        assertEquals(1, todaysSessions.size());
        assertEquals(todaySession, todaysSessions.get(0));
    }

    @Test
    @DisplayName("Should count today's completed work sessions")
    void countTodaysCompletedWorkSessions() {
        // Add completed work session today
        history.addSession(TimerSession.completedWork(now.minusMinutes(25), now, 25));

        // Add incomplete work session today
        history.addSession(TimerSession.interrupted(now.minusMinutes(10), now, TimerState.WORK, 25));

        // Add completed break session today
        history.addSession(TimerSession.completedBreak(now.minusMinutes(5), now, 5));

        assertEquals(1, history.countTodaysCompletedWorkSessions());
    }

    @Test
    @DisplayName("Should calculate today's total work minutes")
    void getTodaysTotalWorkMinutes() {
        history.addSession(TimerSession.completedWork(now.minusMinutes(50), now.minusMinutes(25), 25));
        history.addSession(TimerSession.completedWork(now.minusMinutes(25), now, 25));

        assertEquals(50, history.getTodaysTotalWorkMinutes());
    }

    @Test
    @DisplayName("Should get sessions in date range")
    void getSessionsInRange() {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysAgo = today.minusDays(3);

        LocalDateTime sessionTime1 = today.atTime(10, 0);
        LocalDateTime sessionTime2 = today.minusDays(2).atTime(10, 0);
        LocalDateTime sessionTime3 = today.minusDays(5).atTime(10, 0);

        history.addSession(TimerSession.completedWork(sessionTime1.minusMinutes(25), sessionTime1, 25));
        history.addSession(TimerSession.completedWork(sessionTime2.minusMinutes(25), sessionTime2, 25));
        history.addSession(TimerSession.completedWork(sessionTime3.minusMinutes(25), sessionTime3, 25));

        List<TimerSession> inRange = history.getSessionsInRange(threeDaysAgo, today);
        assertEquals(2, inRange.size());
    }

    @Test
    @DisplayName("clear should remove all sessions")
    void clearRemovesAllSessions() {
        history.addSession(TimerSession.completedWork(now.minusMinutes(25), now, 25));
        history.clear();

        assertTrue(history.isEmpty());
    }

    @Test
    @DisplayName("Should reject null session")
    void rejectNullSession() {
        assertThrows(NullPointerException.class, () -> history.addSession(null));
    }

    @Test
    @DisplayName("getSessionsInRange should reject invalid range")
    void rejectInvalidRange() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        assertThrows(IllegalArgumentException.class, () -> history.getSessionsInRange(today, yesterday));
    }
}
