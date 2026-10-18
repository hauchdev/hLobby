package dev.hauch.hlobby.lobby.selector;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.List;

/**
 * Selects the lobby with the most online players.
 */
public final class MostPlayersSelector implements LobbySelector {

    /** {@inheritDoc} */
    @Override
    public RegisteredServer select(final Player player, final List<RegisteredServer> candidates,
        final SelectionContext context) {
        RegisteredServer best = null;
        int bestCount = -1;

        for (final RegisteredServer candidate : candidates) {
            final int online = context.stats().online(candidate);
            if (online > bestCount) {
                bestCount = online;
                best = candidate;
            }
        }
        return best;
    }
}
