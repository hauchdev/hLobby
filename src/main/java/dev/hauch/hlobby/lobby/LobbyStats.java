package dev.hauch.hlobby.lobby;

import com.velocitypowered.api.proxy.server.RegisteredServer;

/**
 * Read-only view of a lobby server's current player count.
 */
public interface LobbyStats {

    /**
     * Returns the number of players online on the server.
     *
     * @param server the server
     * @return the online count, or {@code -1} when unknown
     */
    int online(RegisteredServer server);

    /**
     * Returns the maximum number of players the server accepts.
     *
     * @param server the server
     * @return the maximum, or {@code -1} when unknown
     */
    int max(RegisteredServer server);

    /**
     * Returns whether the server has been pinged at least once.
     *
     * @param server the server
     * @return {@code true} when the server is known
     */
    boolean isKnown(RegisteredServer server);

    /**
     * Returns whether the server is full according to the cached maximum.
     *
     * @param server the server
     * @return {@code true} when the server is full, {@code false} when unknown
     */
    default boolean isFull(final RegisteredServer server) {
        final int max = this.max(server);
        return max >= 0 && this.online(server) >= max;
    }
}
