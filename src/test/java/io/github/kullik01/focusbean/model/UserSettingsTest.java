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

    @Test
    @DisplayName("Default constructor should set default notification values")
    void defaultConstructorSetsNotificationDefaults() {
        UserSettings settings = new UserSettings();

        assertTrue(settings.isSoundNotificationEnabled(), "Sound notification should be enabled by default");
        assertFalse(settings.isPopupNotificationEnabled(), "Popup notification should be disabled by default");
    }

    @Test
    @DisplayName("Full constructor should set notification values")
    void fullConstructorSetsNotificationValues() {
        UserSettings settings = new UserSettings(25, 5, 60, false, true);

        assertFalse(settings.isSoundNotificationEnabled());
        assertTrue(settings.isPopupNotificationEnabled());
    }

    @Test
    @DisplayName("Notification setters should update values")
    void notificationSettersWork() {
        UserSettings settings = new UserSettings();

        settings.setSoundNotificationEnabled(false);
        settings.setPopupNotificationEnabled(true);

        assertFalse(settings.isSoundNotificationEnabled());
        assertTrue(settings.isPopupNotificationEnabled());
    }

    @Test
    @DisplayName("copyOf should preserve notification settings")
    void copyOfPreservesNotificationSettings() {
        UserSettings original = new UserSettings(25, 5, 60, false, true);
        UserSettings copy = UserSettings.copyOf(original);

        assertEquals(original.isSoundNotificationEnabled(), copy.isSoundNotificationEnabled());
        assertEquals(original.isPopupNotificationEnabled(), copy.isPopupNotificationEnabled());
        assertEquals(original, copy);
    }

    @Test
    @DisplayName("equals should consider notification settings")
    void equalsConsidersNotificationSettings() {
        UserSettings settings1 = new UserSettings(25, 5, 60, true, false);
        UserSettings settings2 = new UserSettings(25, 5, 60, true, false);
        UserSettings settings3 = new UserSettings(25, 5, 60, false, false);
        UserSettings settings4 = new UserSettings(25, 5, 60, true, true);

        assertEquals(settings1, settings2);
        assertNotEquals(settings1, settings3, "Different sound setting should not be equal");
        assertNotEquals(settings1, settings4, "Different popup setting should not be equal");
    }

    @Test
    @DisplayName("hashCode should consider notification settings")
    void hashCodeConsidersNotificationSettings() {
        UserSettings settings1 = new UserSettings(25, 5, 60, true, false);
        UserSettings settings2 = new UserSettings(25, 5, 60, true, false);
        UserSettings settings3 = new UserSettings(25, 5, 60, false, false);

        assertEquals(settings1.hashCode(), settings2.hashCode());
        assertNotEquals(settings1.hashCode(), settings3.hashCode());
    }

    @Test
    @DisplayName("toString should include notification settings")
    void toStringIncludesNotificationSettings() {
        UserSettings settings = new UserSettings(25, 5, 60, true, false);
        String str = settings.toString();

        assertTrue(str.contains("sound=true"), "toString should include sound setting");
        assertTrue(str.contains("popup=false"), "toString should include popup setting");
    }
}
