package dev.hauch.hlobby.lobby;

/**
 * Immutable snapshot of a server's player counts.
 *
 * @param online the number of online players
 * @param max the maximum number of players, or {@code -1} when unknown
 */
public record ServerStats(int online, int max) {

    /** Returns a snapshot with an unknown maximum. */
    public static ServerStats of(final int online) {
        return new ServerStats(online, -1);
    }
}
