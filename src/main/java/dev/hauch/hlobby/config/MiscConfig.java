package dev.hauch.hlobby.config;

/**
 * Miscellaneous behaviour settings.
 *
 * @param showNoPermission whether a missing-permission message is sent
 */
public record MiscConfig(boolean showNoPermission) {
}
