package dev.hauch.hlobby.lobby.selector;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.List;

/**
 * Selects the first lobby in the configured list that is not full.
 */
public final class FirstAvailableSelector implements LobbySelector {

    /** {@inheritDoc} */
    @Override
    public RegisteredServer select(final Player player, final List<RegisteredServer> candidates,
        final SelectionContext context) {
        for (final RegisteredServer candidate : candidates) {
            if (!context.stats().isFull(candidate)) {
                return candidate;
            }
        }
        // Every lobby is full: fall back to the first entry.
        return candidates.isEmpty() ? null : candidates.get(0);
    }
}
