package io.github.kullik01.focusbean.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Manages the collection of completed timer sessions.
 *
 * <p>
 * This class provides methods to add sessions, query session history,
 * and compute statistics. The session list is maintained in chronological
 * order.
 * </p>
 *
 * <p>
 * Thread safety: This class is not thread-safe. External synchronization
 * is required if accessed from multiple threads.
 * </p>
 */
public final class SessionHistory {

    private final List<TimerSession> sessions;

    /**
     * Creates an empty session history.
     */
    public SessionHistory() {
        this.sessions = new ArrayList<>();
    }

    /**
     * Creates a session history with the given sessions.
     *
     * @param sessions the initial list of sessions, will be copied
     * @throws NullPointerException if sessions is null
     */
    public SessionHistory(List<TimerSession> sessions) {
        Objects.requireNonNull(sessions, "sessions must not be null");
        this.sessions = new ArrayList<>(sessions);
    }

    /**
     * Adds a session to the history.
     *
     * @param session the session to add
     * @throws NullPointerException if session is null
     */
    public void addSession(TimerSession session) {
        Objects.requireNonNull(session, "session must not be null");
        sessions.add(session);
    }

    /**
     * Returns an unmodifiable view of all sessions.
     *
     * @return unmodifiable list of sessions in chronological order
     */
    public List<TimerSession> getSessions() {
        return Collections.unmodifiableList(sessions);
    }

    /**
     * Returns sessions that occurred on the specified date.
     *
     * @param date the date to filter by
     * @return list of sessions from that date
     * @throws NullPointerException if date is null
     */
    public List<TimerSession> getSessionsForDate(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return sessions.stream()
                .filter(session -> session.startTime().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    /**
     * Returns all sessions from today.
     *
     * @return list of today's sessions
     */
    public List<TimerSession> getTodaysSessions() {
        return getSessionsForDate(LocalDate.now());
    }

    /**
     * Returns sessions from the current week (last 7 days including today).
     *
     * @return list of sessions from the past week
     */
    public List<TimerSession> getThisWeeksSessions() {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6);

        return sessions.stream()
                .filter(session -> {
                    LocalDate sessionDate = session.startTime().toLocalDate();
                    return !sessionDate.isBefore(weekAgo) && !sessionDate.isAfter(today);
                })
                .collect(Collectors.toList());
    }

    /**
     * Counts completed work sessions for today.
     *
     * @return number of completed work sessions today
     */
    public int countTodaysCompletedWorkSessions() {
        return (int) getTodaysSessions().stream()
                .filter(session -> session.isWorkSession() && session.completed())
                .count();
    }

    /**
     * Counts completed work sessions for this week.
     *
     * @return number of completed work sessions this week
     */
    public int countThisWeeksCompletedWorkSessions() {
        return (int) getThisWeeksSessions().stream()
                .filter(session -> session.isWorkSession() && session.completed())
                .count();
    }

    /**
     * Calculates total work minutes completed today.
     *
     * @return total minutes of completed work sessions today
     */
    public int getTodaysTotalWorkMinutes() {
        return getTodaysSessions().stream()
                .filter(session -> session.isWorkSession() && session.completed())
                .mapToInt(TimerSession::durationMinutes)
                .sum();
    }

    /**
     * Calculates total work minutes completed this week.
     *
     * @return total minutes of completed work sessions this week
     */
    public int getThisWeeksTotalWorkMinutes() {
        return getThisWeeksSessions().stream()
                .filter(session -> session.isWorkSession() && session.completed())
                .mapToInt(TimerSession::durationMinutes)
                .sum();
    }

    /**
     * Calculates total work minutes completed yesterday.
     *
     * @return total minutes of completed work sessions yesterday
     */
    public int getYesterdaysTotalWorkMinutes() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return getSessionsForDate(yesterday).stream()
                .filter(session -> session.isWorkSession() && session.completed())
                .mapToInt(TimerSession::durationMinutes)
                .sum();
    }

    /**
     * Calculates the current streak of consecutive days with completed work
     * sessions.
     *
     * <p>
     * A streak counts backwards from yesterday. Today is not included because
     * it is still in progress. Days must have at least one completed work session
     * to count toward the streak.
     * </p>
     *
     * @return the number of consecutive days with completed work sessions
     */
    public int getCurrentStreak() {
        LocalDate checkDate = LocalDate.now().minusDays(1);
        int streak = 0;

        while (hasCompletedWorkOnDate(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }

        return streak;
    }

    /**
     * Checks if there is at least one completed work session on the given date.
     *
     * @param date the date to check
     * @return {@code true} if at least one completed work session exists
     */
    private boolean hasCompletedWorkOnDate(LocalDate date) {
        return getSessionsForDate(date).stream()
                .anyMatch(session -> session.isWorkSession() && session.completed());
    }

    /**
     * Returns the total number of sessions in history.
     *
     * @return the session count
     */
    public int size() {
        return sessions.size();
    }

    /**
     * Checks if the history is empty.
     *
     * @return {@code true} if no sessions have been recorded
     */
    public boolean isEmpty() {
        return sessions.isEmpty();
    }

    /**
     * Clears all sessions from history.
     */
    public void clear() {
        sessions.clear();
    }

    /**
     * Returns sessions within a date range (inclusive).
     *
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     * @return list of sessions within the date range
     * @throws NullPointerException     if startDate or endDate is null
     * @throws IllegalArgumentException if endDate is before startDate
     */
    public List<TimerSession> getSessionsInRange(LocalDate startDate, LocalDate endDate) {
        Objects.requireNonNull(startDate, "startDate must not be null");
        Objects.requireNonNull(endDate, "endDate must not be null");

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }

        return sessions.stream()
                .filter(session -> {
                    LocalDate sessionDate = session.startTime().toLocalDate();
                    return !sessionDate.isBefore(startDate) && !sessionDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return String.format("SessionHistory[sessions=%d]", sessions.size());
    }
}
