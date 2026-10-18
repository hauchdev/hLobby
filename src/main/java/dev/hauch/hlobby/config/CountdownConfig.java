package dev.hauch.hlobby.config;

/**
 * Settings for the teleport countdown.
 *
 * @param enabled whether a countdown is shown before teleporting
 * @param seconds the countdown length in seconds
 * @param cancelOnCommand whether running a command cancels the countdown
 * @param cancelOnChat whether sending a chat message cancels the countdown
 * @param sound whether a sound is played on every tick
 */
public record CountdownConfig(
    boolean enabled,
    int seconds,
    boolean cancelOnCommand,
    boolean cancelOnChat,
    boolean sound
) {
}
