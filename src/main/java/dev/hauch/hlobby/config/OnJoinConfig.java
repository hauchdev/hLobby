package dev.hauch.hlobby.config;

/**
 * Settings controlling automatic lobby assignment on proxy join.
 *
 * @param enabled whether players are sent to a lobby when they join
 * @param onlyFirstJoin whether only the first join of a player is redirected
 */
public record OnJoinConfig(boolean enabled, boolean onlyFirstJoin) {
}
