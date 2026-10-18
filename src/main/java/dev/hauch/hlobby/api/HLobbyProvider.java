package dev.hauch.hlobby.api;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.hauch.hlobby.config.ConfigManager;
import dev.hauch.hlobby.lobby.LobbyManager;
import dev.hauch.hlobby.lobby.TeleportMode;
import dev.hauch.hlobby.message.MessageManager;
import dev.hauch.hlobby.teleport.TeleportService;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Stable public API surface of hLobby.
 *
 * <p>Third-party plugins should depend on this interface rather than on the
 * concrete {@code HLobby} class. Obtain it with {@link #instance()}.</p>
 */
public interface HLobbyProvider {

    /**
     * Returns the API instance of the running plugin.
     *
     * @return the API instance, or {@code null} when hLobby is disabled
     */
    static HLobbyProvider instance() {
        return ApiHolder.instance;
    }

    /** Returns the underlying plugin instance. */
    HLobbyProvider api();

    /** Returns the Velocity proxy server. */
    ProxyServer proxy();

    /** Returns the plugin logger. */
    Logger logger();

    /** Returns the plugin data directory. */
    Path dataDirectory();

    /** Returns the plugin container. */
    PluginContainer container();

    /** Returns the configuration manager. */
    ConfigManager config();

    /** Returns the localized message manager. */
    MessageManager messages();

    /** Returns the lobby manager. */
    LobbyManager lobbyManager();

    /** Returns the teleport service. */
    TeleportService teleportService();

    /** Returns the currently configured teleport mode. */
    default TeleportMode teleportMode() {
        return config().lobby().teleportMode();
    }

    /**
     * Holder that keeps the live API instance without exposing mutable state.
     */
    final class ApiHolder {

        private static volatile HLobbyProvider instance;

        private ApiHolder() {
        }

        /** Publishes the live API instance. Called when the plugin enables. */
        public static void set(final HLobbyProvider provider) {
            instance = provider;
        }

        /** Clears the live API instance. Called when the plugin disables. */
        public static void clear() {
            instance = null;
        }
    }
}
