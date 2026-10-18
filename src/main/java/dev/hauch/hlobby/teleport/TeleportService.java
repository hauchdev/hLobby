package dev.hauch.hlobby.teleport;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.hauch.hlobby.HLobby;
import dev.hauch.hlobby.lobby.LobbyManager;
import dev.hauch.hlobby.message.MessageKey;
import dev.hauch.hlobby.permission.Permission;
import dev.hauch.hlobby.util.Placeholders;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles teleportation to lobbies, including cooldowns, blocked servers,
 * countdowns and fallback servers.
 */
public final class TeleportService {

    private final HLobby plugin;
    private final Logger logger;
    private final ProxyServer proxy;
    private final LobbyManager lobbyManager;
    private final Map<UUID, Countdown> countdowns = new ConcurrentHashMap<>();
    private final Map<UUID, Instant> cooldowns = new ConcurrentHashMap<>();

    public TeleportService(final HLobby plugin) {
        this.plugin = plugin;
        this.logger = plugin.logger();
        this.proxy = plugin.proxy();
        this.lobbyManager = plugin.lobbyManager();
    }

    /**
     * Sends a player to the best lobby, applying cooldown, blocked-server and
     * fallback rules.
     *
     * @param player the player
     * @return {@code true} when a teleport was started
     */
    public boolean sendToLobby(final Player player) {
        if (this.isCoolingDown(player)) {
            this.plugin.messages().send(player, MessageKey.LOBBY_COOLDOWN,
                Placeholders.seconds((int) this.remainingSeconds(player)));
            return false;
        }

        if (this.isOnBlockedServer(player)) {
            this.plugin.messages().send(player, MessageKey.LOBBY_BLOCKED_SERVER);
            return false;
        }

        final Optional<RegisteredServer> selected = this.lobbyManager.selectLobby(player);
        if (selected.isEmpty()) {
            return this.useFallback(player);
        }

        this.start(player, selected.get());
        return true;
    }

    /**
     * Sends a player to a specific lobby by name.
     *
     * @param player the player
     * @param serverName the lobby name
     * @return {@code true} when a teleport was started
     */
    public boolean sendToLobby(final Player player, final String serverName) {
        final Optional<RegisteredServer> lobby = this.lobbyManager.getLobby(serverName);
        if (lobby.isEmpty()) {
            this.plugin.messages().send(player, MessageKey.SERVER_NOT_LOBBY,
                Placeholders.server(serverName));
            return false;
        }
        this.start(player, lobby.get());
        return true;
    }

    /**
     * Sends a player to a lobby, bypassing the cooldown and blocked-server
     * checks. Used by administrative commands.
     *
     * @param player the player
     * @param server the target server
     */
    public void teleport(final Player player, final RegisteredServer server) {
        if (server == null) {
            return;
        }
        if (this.isAlreadyConnected(player, server)) {
            this.plugin.messages().send(player, MessageKey.LOBBY_SAME_SERVER);
            return;
        }
        this.applyCountdown(player, server);
    }

    /** Returns whether the player is already connected to the server. */
    public boolean isAlreadyConnected(final Player player, final RegisteredServer server) {
        return player.getCurrentServer()
            .map(ServerConnection::getServer)
            .map(server::equals)
            .orElse(false);
    }

    /** Returns whether the player is currently on a blocked server. */
    public boolean isOnBlockedServer(final Player player) {
        if (player.hasPermission(Permission.BYPASS_BLOCKED.getNode())) {
            return false;
        }
        return player.getCurrentServer()
            .map(ServerConnection::getServer)
            .map(this.lobbyManager::isBlocked)
            .orElse(false);
    }

    /** Returns whether the player has a bypass permission for the cooldown. */
    public boolean canBypassCooldown(final Player player) {
        return player.hasPermission(Permission.BYPASS_COOLDOWN.getNode());
    }

    /** Returns whether the player has a bypass permission for the countdown. */
    public boolean canBypassCountdown(final Player player) {
        return player.hasPermission(Permission.BYPASS_COUNTDOWN.getNode());
    }

    /** Returns whether the player is currently cooling down. */
    public boolean isCoolingDown(final Player player) {
        return !this.canBypassCooldown(player) && this.remainingSeconds(player) > 0;
    }

    /** Returns the remaining cooldown in whole seconds. */
    public long remainingSeconds(final Player player) {
        final Instant end = this.cooldowns.get(player.getUniqueId());
        if (end == null) {
            return 0;
        }
        return Math.max(0, Duration.between(Instant.now(), end).getSeconds());
    }

    /** Starts or refreshes the cooldown for a player. */
    public void setCooldown(final Player player) {
        final int seconds = this.plugin.config().lobby().cooldown().seconds();
        if (seconds <= 0 || this.canBypassCooldown(player)) {
            return;
        }
        this.cooldowns.put(player.getUniqueId(), Instant.now().plusSeconds(seconds));
    }

    /** Returns whether the player has an active countdown. */
    public boolean isCountingDown(final UUID uniqueId) {
        return this.countdowns.containsKey(uniqueId);
    }

    /** Cancels an active countdown, returning whether one existed. */
    public boolean cancelCountdown(final UUID uniqueId) {
        final Countdown countdown = this.countdowns.remove(uniqueId);
        if (countdown == null) {
            return false;
        }
        countdown.cancel();
        return true;
    }

    /** Returns the number of active countdowns. */
    public int activeCountdowns() {
        return this.countdowns.size();
    }

    /** Cancels every active countdown and clears cooldowns. */
    public void shutdown() {
        for (final Countdown countdown : this.countdowns.values()) {
            countdown.cancel();
        }
        this.countdowns.clear();
        this.cooldowns.clear();
    }

    private boolean useFallback(final Player player) {
        final String fallback = this.plugin.config().lobby().fallbackServer();
        if (fallback == null || fallback.isBlank()) {
            this.plugin.messages().send(player, MessageKey.LOBBY_FALLBACK);
            return false;
        }
        final Optional<RegisteredServer> server = this.proxy.getServer(fallback);
        if (server.isEmpty()) {
            this.plugin.messages().send(player, MessageKey.LOBBY_NO_LOBBIES);
            return false;
        }
        this.plugin.messages().send(player, MessageKey.LOBBY_USING_FALLBACK,
            Placeholders.server(server.get().getServerInfo().getName()));
        this.applyCountdown(player, server.get());
        return true;
    }

    private void start(final Player player, final RegisteredServer server) {
        this.applyCountdown(player, server);
        this.setCooldown(player);
    }

    private void applyCountdown(final Player player, final RegisteredServer server) {
        if (this.isAlreadyConnected(player, server)) {
            this.plugin.messages().send(player, MessageKey.LOBBY_SAME_SERVER);
            return;
        }

        final boolean countdownEnabled = this.plugin.config().lobby().countdown().enabled();
        if (countdownEnabled && !this.canBypassCountdown(player)) {
            final Countdown existing = this.countdowns.remove(player.getUniqueId());
            if (existing != null) {
                existing.cancel();
            }
            final int seconds = this.plugin.config().lobby().countdown().seconds();
            final boolean sound = this.plugin.config().lobby().countdown().sound();
            final Countdown countdown = new Countdown(this.plugin, player, server, seconds, sound,
                () -> this.connect(player, server));
            this.countdowns.put(player.getUniqueId(), countdown);
        } else {
            this.connect(player, server);
        }
    }

    private void connect(final Player player, final RegisteredServer server) {
        this.countdowns.remove(player.getUniqueId());
        player.createConnectionRequest(server).connectWithIndication().thenAccept(success -> {
            if (Boolean.TRUE.equals(success)) {
                this.plugin.messages().send(player, MessageKey.LOBBY_TELEPORT,
                    Placeholders.server(server.getServerInfo().getName()));
                this.logger.debug("Sent {} to lobby {}.", player.getUsername(),
                    server.getServerInfo().getName());
            }
        });
    }
}
