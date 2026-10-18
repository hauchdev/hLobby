package dev.hauch.hlobby.config;

/**
 * Settings for the message shown when a player enters a lobby server.
 *
 * @param title whether a title is displayed
 * @param actionbar whether an action bar message is displayed
 * @param chat whether a chat message is displayed
 * @param fadeIn title fade in, in ticks
 * @param stay title stay, in ticks
 * @param fadeOut title fade out, in ticks
 */
public record JoinMessageConfig(
    boolean title,
    boolean actionbar,
    boolean chat,
    int fadeIn,
    int stay,
    int fadeOut
) {
}
