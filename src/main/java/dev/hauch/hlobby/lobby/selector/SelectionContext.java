package dev.hauch.hlobby.lobby.selector;

import dev.hauch.hlobby.lobby.LobbyStats;

import java.util.Map;

/**
 * Inputs available to a {@link LobbySelector} when choosing a lobby.
 *
 * @param priorities per-server priority used by the priority strategy
 * @param defaultPriority fallback priority for servers without an entry
 * @param stats cached player statistics
 */
public record SelectionContext(
    Map<String, Integer> priorities,
    int defaultPriority,
    LobbyStats stats
) {
}
