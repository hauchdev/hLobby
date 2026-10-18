package dev.hauch.hlobby.lobby.selector;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rotates through the lobbies in order, distributing players evenly.
 */
public final class RoundRobinSelector implements LobbySelector {

    private final AtomicInteger cursor = new AtomicInteger();

    /** {@inheritDoc} */
    @Override
    public RegisteredServer select(final Player player, final List<RegisteredServer> candidates,
        final SelectionContext context) {
        if (candidates.isEmpty()) {
            return null;
        }
        final int index = Math.floorMod(this.cursor.getAndIncrement(), candidates.size());
        return candidates.get(index);
    }

    /** Returns the current rotation cursor, for diagnostics. */
    public int cursor() {
        return this.cursor.get();
    }
}
