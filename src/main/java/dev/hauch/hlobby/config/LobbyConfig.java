package dev.hauch.hlobby.config;

import java.util.List;
import java.util.Map;

/**
 * All lobby related settings.
 *
 * @param teleportMode the strategy used to select a lobby
 * @param servers the server names registered as lobbies
 * @param priorities per-server priority used by the priority strategy
 * @param defaultPriority fallback priority for servers not listed explicitly
 * @param blockedServers servers on which the {@code /lobby} command is blocked
 * @param fallbackServer optional server used when no lobby is available
 * @param countdown countdown settings
 * @param cooldown cooldown settings
 * @param onJoin automatic assignment settings for proxy joins
 * @param onKick redirect settings for kicked players
 */
public record LobbyConfig(
    String teleportMode,
    List<String> servers,
    Map<String, Integer> priorities,
    int defaultPriority,
    List<String> blockedServers,
    String fallbackServer,
    CountdownConfig countdown,
    CooldownConfig cooldown,
    OnJoinConfig onJoin,
    OnKickConfig onKick
) {
}
