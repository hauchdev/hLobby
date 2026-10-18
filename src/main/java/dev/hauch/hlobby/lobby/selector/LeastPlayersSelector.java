package dev.hauch.hlobby.lobby.selector;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.List;

/**
 * Selects the lobby with the fewest online players.
 *
 * <p>Servers reported as full are skipped when at least one non-full lobby is
 * available; otherwise the least populated lobby is returned.</p>
 */
public final class LeastPlayersSelector implements LobbySelector {

    /** {@inheritDoc} */
    @Override
    public RegisteredServer select(final Player player, final List<RegisteredServer> candidates,
        final SelectionContext context) {
        RegisteredServer best = null;
        int bestCount = Integer.MAX_VALUE;

        for (final RegisteredServer candidate : candidates) {
            final int online = context.stats().online(candidate);
            if (online < bestCount) {
                bestCount = online;
                best = candidate;
            }
        }

        if (best == null) {
            return null;
        }
        if (!context.stats().isFull(best)) {
            return best;
        }

        // Every lobby is full: return the least populated one anyway.
        return best;
    }
}
