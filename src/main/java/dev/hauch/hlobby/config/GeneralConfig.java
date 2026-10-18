package dev.hauch.hlobby.config;

/**
 * Global plugin settings.
 *
 * @param language the language file used for messages, without extension
 * @param debug whether verbose debug logging is enabled
 * @param checkUpdates whether the plugin should check for updates on startup
 */
public record GeneralConfig(String language, boolean debug, boolean checkUpdates) {
}
