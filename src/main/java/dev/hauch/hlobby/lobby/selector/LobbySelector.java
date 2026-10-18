package dev.hauch.hlobby.lobby.selector;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.List;

/**
 * Chooses a target lobby for a player.
 *
 * <p>Implementations must be deterministic with respect to the supplied
 * candidates and must not perform blocking I/O.</p>
 */
@FunctionalInterface
public interface LobbySelector {

    /**
     * Selects a lobby from the supplied candidates.
     *
     * @param player the player being teleported, may be {@code null}
     * @param candidates the available lobbies
     * @param context the selection inputs
     * @return the chosen lobby, or {@code null} when none is suitable
     */
    RegisteredServer select(Player player, List<RegisteredServer> candidates, SelectionContext context);
}
