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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link UserSettings}.
 */
class UserSettingsTest {

    /**
     * Verifies that the default constructor sets standard Pomodoro values.
     */
    @Test
    @DisplayName("Default constructor should set standard Pomodoro values")
    void defaultConstructorSetsDefaults() {
        // Act
        UserSettings settings = new UserSettings();

        // Assert
        assertEquals(25, settings.getWorkDurationMinutes());
        assertEquals(5, settings.getBreakDurationMinutes());
        assertEquals(7, settings.getHistoryChartDays());
    }

    /**
     * Verifies that the constructor with parameters sets the specified values.
     */
    @Test
    @DisplayName("Constructor with parameters should set specified values")
    void constructorWithParameters() {
        // Act
        UserSettings settings = new UserSettings(30, 10);

        // Assert
        assertEquals(30, settings.getWorkDurationMinutes());
        assertEquals(10, settings.getBreakDurationMinutes());
    }

    /**
     * Verifies that durations are correctly converted to seconds.
     */
    @Test
    @DisplayName("Should convert durations to seconds")
    void convertToSeconds() {
        // Arrange
        UserSettings settings = new UserSettings(25, 5);

        // Act & Assert
        assertEquals(1500, settings.getWorkDurationSeconds());
        assertEquals(300, settings.getBreakDurationSeconds());
    }

    /**
     * Verifies that work duration below the minimum is rejected.
     */
    @Test
    @DisplayName("Should reject work duration below minimum")
    void rejectWorkDurationBelowMin() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new UserSettings(0, 5));
    }

    /**
     * Verifies that work duration above the maximum is rejected.
     */
    @Test
    @DisplayName("Should reject work duration above maximum")
    void rejectWorkDurationAboveMax() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new UserSettings(901, 5));
    }

    /**
     * Verifies that break duration below the minimum is rejected.
     */
    @Test
    @DisplayName("Should reject break duration below minimum")
    void rejectBreakDurationBelowMin() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new UserSettings(25, 0));
    }

    /**
     * Verifies that break duration above the maximum is rejected.
     */
    @Test
    @DisplayName("Should reject break duration above maximum")
    void rejectBreakDurationAboveMax() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new UserSettings(25, 901));
    }

    /**
     * Verifies that chart days below the minimum are rejected.
     */
    @Test
    @DisplayName("Should reject chart days below minimum")
    void rejectChartDaysBelowMin() {
        // Arrange
        UserSettings settings = new UserSettings();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> settings.setHistoryChartDays(0));
    }

    /**
     * Verifies that chart days above the maximum are rejected.
     */
    @Test
    @DisplayName("Should reject chart days above maximum")
    void rejectChartDaysAboveMax() {
        // Arrange
        UserSettings settings = new UserSettings();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> settings.setHistoryChartDays(31));
    }

    /**
     * Verifies that boundary values for durations and chart days are accepted.
     */
    @Test
    @DisplayName("Should accept boundary values")
    void acceptBoundaryValues() {
        // Arrange, Act & Assert
        UserSettings minSettings = new UserSettings(1, 1);
        assertEquals(1, minSettings.getWorkDurationMinutes());
        assertEquals(1, minSettings.getBreakDurationMinutes());

        UserSettings maxSettings = new UserSettings(120, 60);
        assertEquals(120, maxSettings.getWorkDurationMinutes());
        assertEquals(60, maxSettings.getBreakDurationMinutes());

        UserSettings chartSettings = new UserSettings();
        chartSettings.setHistoryChartDays(1);
        assertEquals(1, chartSettings.getHistoryChartDays());

        chartSettings.setHistoryChartDays(30);
        assertEquals(30, chartSettings.getHistoryChartDays());
    }

    /**
     * Verifies that copyOf creates an independent copy of the settings.
     */
    @Test
    @DisplayName("copyOf should create independent copy")
    void copyOfCreatesIndependentCopy() {
        // Arrange
        UserSettings original = new UserSettings(30, 10);
        UserSettings copy = UserSettings.copyOf(original);

        // Act & Assert
        assertEquals(original, copy);

        copy.setWorkDurationMinutes(45);
        assertNotEquals(original.getWorkDurationMinutes(), copy.getWorkDurationMinutes());
    }

    /**
     * Verifies that equals and hashCode work correctly.
     */
    @Test
    @DisplayName("equals and hashCode should work correctly")
    void equalsAndHashCode() {
        // Arrange
        UserSettings settings1 = new UserSettings(25, 5);
        UserSettings settings2 = new UserSettings(25, 5);
        UserSettings settings3 = new UserSettings(30, 5);

        // Act & Assert
        assertEquals(settings1, settings2);
        assertEquals(settings1.hashCode(), settings2.hashCode());
        assertNotEquals(settings1, settings3);
    }

    /**
     * Verifies that the default constructor sets default notification values.
     */
    @Test
    @DisplayName("Default constructor should set default notification values")
    void defaultConstructorSetsNotificationDefaults() {
        // Act
        UserSettings settings = new UserSettings();

        // Assert
        assertTrue(settings.isSoundNotificationEnabled(), "Sound notification should be enabled by default");
        assertTrue(settings.isPopupNotificationEnabled(), "Popup notification should be enabled by default");
    }

    /**
     * Verifies that the full constructor sets notification values correctly.
     */
    @Test
    @DisplayName("Full constructor should set notification values")
    void fullConstructorSetsNotificationValues() {
        // Act
        UserSettings settings = new UserSettings(25, 5, 60, false, true, NotificationSound.SYSTEM_BEEP, null, 7);

        // Assert
        assertFalse(settings.isSoundNotificationEnabled());
        assertTrue(settings.isPopupNotificationEnabled());
    }

    /**
     * Verifies that notification setters correctly update values.
     */
    @Test
    @DisplayName("Notification setters should update values")
    void notificationSettersWork() {
        // Arrange
        UserSettings settings = new UserSettings();

        // Act
        settings.setSoundNotificationEnabled(false);
        settings.setPopupNotificationEnabled(true);

        // Assert
        assertFalse(settings.isSoundNotificationEnabled());
        assertTrue(settings.isPopupNotificationEnabled());
    }

    /**
     * Verifies that copyOf preserves notification settings.
     */
    @Test
    @DisplayName("copyOf should preserve notification settings")
    void copyOfPreservesNotificationSettings() {
        // Arrange
        UserSettings original = new UserSettings(25, 5, 60, false, true, NotificationSound.SYSTEM_BEEP, null, 7);
        UserSettings copy = UserSettings.copyOf(original);

        // Act & Assert
        assertEquals(original.isSoundNotificationEnabled(), copy.isSoundNotificationEnabled());
        assertEquals(original.isPopupNotificationEnabled(), copy.isPopupNotificationEnabled());
        assertEquals(original, copy);
    }

    /**
     * Verifies that equals considers notification settings.
     */
    @Test
    @DisplayName("equals should consider notification settings")
    void equalsConsidersNotificationSettings() {
        // Arrange
        UserSettings settings1 = new UserSettings(25, 5, 60, true, false, NotificationSound.SYSTEM_BEEP, null, 7);
        UserSettings settings2 = new UserSettings(25, 5, 60, true, false, NotificationSound.SYSTEM_BEEP, null, 7);
        UserSettings settings3 = new UserSettings(25, 5, 60, false, false, NotificationSound.SYSTEM_BEEP, null, 7);
        UserSettings settings4 = new UserSettings(25, 5, 60, true, true, NotificationSound.SYSTEM_BEEP, null, 7);

        // Act & Assert
        assertEquals(settings1, settings2);
        assertNotEquals(settings1, settings3, "Different sound setting should not be equal");
        assertNotEquals(settings1, settings4, "Different popup setting should not be equal");
    }

    /**
     * Verifies that hashCode considers notification settings.
     */
    @Test
    @DisplayName("hashCode should consider notification settings")
    void hashCodeConsidersNotificationSettings() {
        // Arrange
        UserSettings settings1 = new UserSettings(25, 5, 60, true, false, NotificationSound.SYSTEM_BEEP, null, 7);
        UserSettings settings2 = new UserSettings(25, 5, 60, true, false, NotificationSound.SYSTEM_BEEP, null, 7);
        UserSettings settings3 = new UserSettings(25, 5, 60, false, false, NotificationSound.SYSTEM_BEEP, null, 7);

        // Act & Assert
        assertEquals(settings1.hashCode(), settings2.hashCode());
        assertNotEquals(settings1.hashCode(), settings3.hashCode());
    }

    /**
     * Verifies that toString includes notification settings.
     */
    @Test
    @DisplayName("toString should include notification settings")
    void toStringIncludesNotificationSettings() {
        // Arrange
        UserSettings settings = new UserSettings(25, 5, 60, true, false, NotificationSound.SYSTEM_BEEP, null, 7);

        // Act
        String str = settings.toString();

        // Assert
        assertTrue(str.contains("sound=true"), "toString should include sound setting");
        assertTrue(str.contains("popup=false"), "toString should include popup setting");
    }

    /**
     * Verifies that the default constructor sets the default history view mode.
     */
    @Test
    @DisplayName("Default constructor should set default history view mode")
    void defaultConstructorSetsHistoryViewMode() {
        // Act
        UserSettings settings = new UserSettings();

        // Assert
        assertEquals(HistoryViewMode.TABLE, settings.getHistoryViewMode(), "Default history view mode should be TABLE");
    }

    /**
     * Verifies that the history view mode setter correctly updates the value.
     */
    @Test
    @DisplayName("History view mode setter should update value")
    void historyViewModeSetterWorks() {
        // Arrange
        UserSettings settings = new UserSettings();

        // Act
        settings.setHistoryViewMode(HistoryViewMode.CHART);

        // Assert
        assertEquals(HistoryViewMode.CHART, settings.getHistoryViewMode());
    }

    /**
     * Verifies that copyOf preserves the history view mode.
     */
    @Test
    @DisplayName("copyOf should preserve history view mode")
    void copyOfPreservesHistoryViewMode() {
        // Arrange
        UserSettings original = new UserSettings();
        original.setHistoryViewMode(HistoryViewMode.CHART);

        // Act
        UserSettings copy = UserSettings.copyOf(original);

        // Assert
        assertEquals(HistoryViewMode.CHART, copy.getHistoryViewMode());
        assertEquals(original, copy);
    }

    /**
     * Verifies that equals considers the history view mode.
     */
    @Test
    @DisplayName("equals should consider history view mode")
    void equalsConsidersHistoryViewMode() {
        // Arrange
        UserSettings settings1 = new UserSettings();
        settings1.setHistoryViewMode(HistoryViewMode.TABLE);

        UserSettings settings2 = new UserSettings();
        settings2.setHistoryViewMode(HistoryViewMode.CHART);

        // Act & Assert
        assertNotEquals(settings1, settings2, "Different history view mode should not be equal");

        settings2.setHistoryViewMode(HistoryViewMode.TABLE);
        assertEquals(settings1, settings2);
    }

    /**
     * Verifies that toString includes the history view mode.
     */
    @Test
    @DisplayName("toString should include history view mode")
    void toStringIncludesHistoryViewMode() {
        // Arrange
        UserSettings settings = new UserSettings();
        settings.setHistoryViewMode(HistoryViewMode.CHART);

        // Act
        String str = settings.toString();

        // Assert
        assertTrue(str.contains("historyViewMode=CHART"), "toString should include history view mode");
    }

    /**
     * Verifies that the chart days setter correctly updates the value.
     */
    @Test
    @DisplayName("Chart days setter should update value")
    void chartDaysSetterWorks() {
        // Arrange
        UserSettings settings = new UserSettings();

        // Act
        settings.setHistoryChartDays(14);

        // Assert
        assertEquals(14, settings.getHistoryChartDays());
    }

    /**
     * Verifies that equals considers chart days.
     */
    @Test
    @DisplayName("equals should consider chart days")
    void equalsConsidersChartDays() {
        // Arrange
        UserSettings settings1 = new UserSettings();
        settings1.setHistoryChartDays(7);

        UserSettings settings2 = new UserSettings();
        settings2.setHistoryChartDays(14);

        // Act & Assert
        assertNotEquals(settings1, settings2);

        settings2.setHistoryChartDays(7);
        assertEquals(settings1, settings2);
    }
}
