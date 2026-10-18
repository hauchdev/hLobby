package dev.hauch.hlobby.lobby.selector;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.List;
import java.util.Locale;

/**
 * Selects the lobby with the highest configured priority.
 *
 * <p>Priorities are read from the {@code lobby.priorities} map and fall back to
 * {@code lobby.default-priority}. Ties are broken by the configured order.</p>
 */
public final class PrioritySelector implements LobbySelector {

    /** {@inheritDoc} */
    @Override
    public RegisteredServer select(final Player player, final List<RegisteredServer> candidates,
        final SelectionContext context) {
        RegisteredServer best = null;
        int bestPriority = Integer.MIN_VALUE;

        for (final RegisteredServer candidate : candidates) {
            final String name = candidate.getServerInfo().getName().toLowerCase(Locale.ROOT);
            final int priority = context.priorities()
                .getOrDefault(name, context.defaultPriority());
            if (priority > bestPriority) {
                bestPriority = priority;
                best = candidate;
            }
        }
        return best;
    }
}
