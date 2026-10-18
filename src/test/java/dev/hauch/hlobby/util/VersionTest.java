package dev.hauch.hlobby.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link Version}.
 */
class VersionTest {

    @Test
    @DisplayName("newer patch versions are detected")
    void detectsNewerPatch() {
        assertTrue(Version.isNewer("1.0.1", "1.0.0"));
        assertTrue(Version.isNewer("1.1.0", "1.0.9"));
        assertTrue(Version.isNewer("2.0.0", "1.9.9"));
    }

    @Test
    @DisplayName("older versions are rejected")
    void rejectsOlderVersions() {
        assertFalse(Version.isNewer("1.0.0", "1.0.0"));
        assertFalse(Version.isNewer("1.0.0", "1.0.1"));
        assertFalse(Version.isNewer("0.9.0", "1.0.0"));
    }

    @Test
    @DisplayName("snapshot suffixes are ignored")
    void ignoresSuffixes() {
        assertTrue(Version.isNewer("1.0.1-SNAPSHOT", "1.0.0"));
        assertFalse(Version.isNewer("1.0.0-SNAPSHOT", "1.0.0"));
    }

    @Test
    @DisplayName("empty and null values are handled")
    void handlesEmptyValues() {
        assertFalse(Version.isNewer(null, "1.0.0"));
        assertFalse(Version.isNewer("", "1.0.0"));
        assertTrue(Version.isNewer("1.0.1", null));
    }

    @Test
    @DisplayName("numeric segments are parsed")
    void parsesNumericSegments() {
        assertEquals(12, Version.numeric("12-SNAPSHOT"));
        assertEquals(0, Version.numeric("SNAPSHOT"));
        assertEquals(0, Version.numeric(null));
    }
}
