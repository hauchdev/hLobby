package dev.hauch.hlobby.lobby.selector;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Selects a lobby uniformly at random.
 */
public final class RandomSelector implements LobbySelector {

    /** {@inheritDoc} */
    @Override
    public RegisteredServer select(final Player player, final List<RegisteredServer> candidates,
        final SelectionContext context) {
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }
}
