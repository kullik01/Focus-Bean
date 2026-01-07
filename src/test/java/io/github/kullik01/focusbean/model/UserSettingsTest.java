package io.github.kullik01.focusbean.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UserSettings}.
 */
class UserSettingsTest {

    @Test
    @DisplayName("Default constructor should set standard Pomodoro values")
    void defaultConstructorSetsDefaults() {
        UserSettings settings = new UserSettings();

        assertEquals(25, settings.getWorkDurationMinutes());
        assertEquals(5, settings.getBreakDurationMinutes());
    }

    @Test
    @DisplayName("Constructor with parameters should set specified values")
    void constructorWithParameters() {
        UserSettings settings = new UserSettings(30, 10);

        assertEquals(30, settings.getWorkDurationMinutes());
        assertEquals(10, settings.getBreakDurationMinutes());
    }

    @Test
    @DisplayName("Should convert durations to seconds")
    void convertToSeconds() {
        UserSettings settings = new UserSettings(25, 5);

        assertEquals(1500, settings.getWorkDurationSeconds());
        assertEquals(300, settings.getBreakDurationSeconds());
    }

    @Test
    @DisplayName("Should reject work duration below minimum")
    void rejectWorkDurationBelowMin() {
        assertThrows(IllegalArgumentException.class, () -> new UserSettings(0, 5));
    }

    @Test
    @DisplayName("Should reject work duration above maximum")
    void rejectWorkDurationAboveMax() {
        assertThrows(IllegalArgumentException.class, () -> new UserSettings(121, 5));
    }

    @Test
    @DisplayName("Should reject break duration below minimum")
    void rejectBreakDurationBelowMin() {
        assertThrows(IllegalArgumentException.class, () -> new UserSettings(25, 0));
    }

    @Test
    @DisplayName("Should reject break duration above maximum")
    void rejectBreakDurationAboveMax() {
        assertThrows(IllegalArgumentException.class, () -> new UserSettings(25, 61));
    }

    @Test
    @DisplayName("Should accept boundary values")
    void acceptBoundaryValues() {
        UserSettings minSettings = new UserSettings(1, 1);
        assertEquals(1, minSettings.getWorkDurationMinutes());
        assertEquals(1, minSettings.getBreakDurationMinutes());

        UserSettings maxSettings = new UserSettings(120, 60);
        assertEquals(120, maxSettings.getWorkDurationMinutes());
        assertEquals(60, maxSettings.getBreakDurationMinutes());
    }

    @Test
    @DisplayName("copyOf should create independent copy")
    void copyOfCreatesIndependentCopy() {
        UserSettings original = new UserSettings(30, 10);
        UserSettings copy = UserSettings.copyOf(original);

        assertEquals(original, copy);

        copy.setWorkDurationMinutes(45);
        assertNotEquals(original.getWorkDurationMinutes(), copy.getWorkDurationMinutes());
    }

    @Test
    @DisplayName("equals and hashCode should work correctly")
    void equalsAndHashCode() {
        UserSettings settings1 = new UserSettings(25, 5);
        UserSettings settings2 = new UserSettings(25, 5);
        UserSettings settings3 = new UserSettings(30, 5);

        assertEquals(settings1, settings2);
        assertEquals(settings1.hashCode(), settings2.hashCode());
        assertNotEquals(settings1, settings3);
    }
}
