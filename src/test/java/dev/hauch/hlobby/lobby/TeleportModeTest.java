package dev.hauch.hlobby.lobby;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link TeleportMode}.
 */
class TeleportModeTest {

    @Test
    @DisplayName("modes are parsed case-insensitively")
    void parsesCaseInsensitively() {
        assertEquals(TeleportMode.RANDOM, TeleportMode.parse("random"));
        assertEquals(TeleportMode.LEAST_PLAYERS, TeleportMode.parse("Least_Players"));
        assertEquals(TeleportMode.ROUND_ROBIN, TeleportMode.parse("ROUND_ROBIN"));
    }

    @Test
    @DisplayName("unknown values fall back to LEAST_PLAYERS")
    void fallsBackForUnknownValues() {
        assertEquals(TeleportMode.LEAST_PLAYERS, TeleportMode.parse("does-not-exist"));
        assertEquals(TeleportMode.LEAST_PLAYERS, TeleportMode.parse(null));
        assertEquals(TeleportMode.LEAST_PLAYERS, TeleportMode.parse("  "));
    }

    @Test
    @DisplayName("all modes are defined")
    void definesAllModes() {
        assertEquals(6, TeleportMode.values().length);
    }
}
