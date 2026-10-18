package dev.hauch.hlobby.config;

/**
 * Settings controlling the behaviour when a player is kicked from a server.
 *
 * @param enabled whether kicked players are redirected to a lobby
 * @param excludeLobbies whether players kicked from a lobby are left alone
 * @param notifyPlayer whether the player is notified about the redirect
 */
public record OnKickConfig(boolean enabled, boolean excludeLobbies, boolean notifyPlayer) {
}
