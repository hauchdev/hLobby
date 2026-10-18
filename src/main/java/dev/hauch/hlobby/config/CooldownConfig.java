package dev.hauch.hlobby.config;

/**
 * Settings for the per-player {@code /lobby} cooldown.
 *
 * @param enabled whether the cooldown is active
 * @param seconds the cooldown length in seconds
 */
public record CooldownConfig(boolean enabled, int seconds) {
}
