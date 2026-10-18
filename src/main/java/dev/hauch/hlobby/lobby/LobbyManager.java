package dev.hauch.hlobby.lobby;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.hauch.hlobby.HLobby;
import dev.hauch.hlobby.lobby.selector.SelectionContext;
import dev.hauch.hlobby.lobby.selector.SelectorFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which servers are lobbies and selects a target lobby for a player.
 *
 * <p>The configured lobby names are kept in a case-insensitive set. Changes are
 * persisted to {@code config.yml} immediately.</p>
 */
public final class LobbyManager {

    private final HLobby plugin;
    private final Logger logger;
    private final ProxyServer proxy;
    private final Set<String> lobbyNames = ConcurrentHashMap.newKeySet();
    private final SelectorFactory selectors = new SelectorFactory();

    public LobbyManager(final HLobby plugin) {
        this.plugin = plugin;
        this.logger = plugin.logger();
        this.proxy = plugin.proxy();
        this.lobbyNames.addAll(plugin.config().lobby().servers().stream()
            .map(name -> name.toLowerCase(Locale.ROOT))
            .toList());
    }

    /**
     * Returns whether the named server is registered as a lobby.
     *
     * @param name the server name
     * @return {@code true} when the server is a lobby
     */
    public boolean isLobby(final String name) {
        return name != null && this.lobbyNames.contains(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns whether the given server is registered as a lobby.
     *
     * @param server the server
     * @return {@code true} when the server is a lobby
     */
    public boolean isLobby(final RegisteredServer server) {
        return server != null && this.isLobby(server.getServerInfo().getName());
    }

    /**
     * Returns whether the given server is blocked for the {@code /lobby} command.
     *
     * @param server the server
     * @return {@code true} when blocked
     */
    public boolean isBlocked(final RegisteredServer server) {
        if (server == null) {
            return false;
        }
        final String name = server.getServerInfo().getName().toLowerCase(Locale.ROOT);
        return this.plugin.config().lobby().blockedServers().stream()
            .map(blocked -> blocked.toLowerCase(Locale.ROOT))
            .anyMatch(name::equals);
    }

    /**
     * Returns the currently configured lobbies that are registered on the proxy.
     *
     * @return an immutable list of lobby servers
     */
    public List<RegisteredServer> getLobbies() {
        final List<RegisteredServer> servers = new ArrayList<>();
        for (final String name : this.lobbyNames) {
            this.proxy.getServer(name).ifPresent(servers::add);
        }
        return List.copyOf(servers);
    }

    /**
     * Returns a registered lobby by name.
     *
     * @param name the server name
     * @return the lobby, if it exists and is registered
     */
    public Optional<RegisteredServer> getLobby(final String name) {
        return this.proxy.getServer(name).filter(this::isLobby);
    }

    /** Returns the configured lobby names. */
    public Set<String> getLobbyNames() {
        return Set.copyOf(this.lobbyNames);
    }

    /** Returns whether at least one lobby is configured. */
    public boolean hasLobbies() {
        return !this.getLobbies().isEmpty();
    }

    /** Returns the configured teleport mode. */
    public TeleportMode getTeleportMode() {
        return TeleportMode.parse(this.plugin.config().lobby().teleportMode());
    }

    /** Returns the selection inputs for the configured mode. */
    public SelectionContext context() {
        return new SelectionContext(
            this.plugin.config().lobby().priorities(),
            this.plugin.config().lobby().defaultPriority(),
            this.plugin.serverStats());
    }

    /**
     * Selects a target lobby for a player using the configured mode.
     *
     * @param player the player, may be {@code null}
     * @return the selected lobby, if any
     */
    public Optional<RegisteredServer> selectLobby(final Player player) {
        final List<RegisteredServer> candidates = this.getLobbies();
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        final RegisteredServer selected = this.selectors
            .selector(this.getTeleportMode())
            .select(player, candidates, this.context());
        return Optional.ofNullable(selected);
    }

    /**
     * Adds a server to the lobby list.
     *
     * @param name the server name
     * @return the outcome of the operation
     * @throws IOException if the configuration cannot be saved
     */
    public LobbyResult addLobby(final String name) throws IOException {
        final Optional<RegisteredServer> server = this.proxy.getServer(name);
        if (server.isEmpty()) {
            return LobbyResult.SERVER_NOT_FOUND;
        }
        final String key = server.get().getServerInfo().getName().toLowerCase(Locale.ROOT);
        if (!this.lobbyNames.add(key)) {
            return LobbyResult.ALREADY_EXISTS;
        }
        this.plugin.config().setLobbyServers(this.lobbyNames);
        this.logger.info("Added '{}' to the lobby list.", key);
        return LobbyResult.ADDED;
    }

    /**
     * Removes a server from the lobby list.
     *
     * @param name the server name
     * @return the outcome of the operation
     * @throws IOException if the configuration cannot be saved
     */
    public LobbyResult removeLobby(final String name) throws IOException {
        final String key = name == null ? "" : name.toLowerCase(Locale.ROOT);
        if (!this.lobbyNames.remove(key)) {
            return LobbyResult.NOT_LOBBY;
        }
        this.plugin.config().setLobbyServers(this.lobbyNames);
        this.logger.info("Removed '{}' from the lobby list.", key);
        return LobbyResult.REMOVED;
    }

    /**
     * Changes the configured teleport mode.
     *
     * @param mode the new mode
     * @throws IOException if the configuration cannot be saved
     */
    public void setTeleportMode(final TeleportMode mode) throws IOException {
        this.plugin.config().setTeleportMode(mode);
        this.logger.info("Teleport mode set to {}.", mode);
    }

    /** Rebuilds the in-memory lobby set from the configuration. */
    public void reload() {
        this.lobbyNames.clear();
        this.lobbyNames.addAll(this.plugin.config().lobby().servers().stream()
            .map(name -> name.toLowerCase(Locale.ROOT))
            .toList());
    }
}
