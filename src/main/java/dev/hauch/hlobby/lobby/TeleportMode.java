package dev.hauch.hlobby.lobby;

/**
 * The strategy used to pick the target lobby when a player is teleported.
 *
 * <p>Each mode maps to a {@link dev.hauch.hlobby.lobby.selector.LobbySelector}
 * implementation.</p>
 */
public enum TeleportMode {

    /** Pick a lobby at random. */
    RANDOM,

    /** Pick the lobby with the fewest players. */
    LEAST_PLAYERS,

    /** Pick the lobby with the most players. */
    MOST_PLAYERS,

    /** Pick the first lobby in the configured list. */
    FIRST_AVAILABLE,

    /** Pick the lobby with the highest configured priority. */
    PRIORITY,

    /** Rotate through the lobbies in order. */
    ROUND_ROBIN;

    /**
     * Parses a mode from its configuration name, ignoring case.
     *
     * @param value the configured value
     * @return the matching mode, or {@link #LEAST_PLAYERS} when unknown
     */
    public static TeleportMode parse(final String value) {
        if (value == null || value.isBlank()) {
            return LEAST_PLAYERS;
        }
        for (final TeleportMode mode : values()) {
            if (mode.name().equalsIgnoreCase(value.trim())) {
                return mode;
            }
        }
        return LEAST_PLAYERS;
    }
}
