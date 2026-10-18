package dev.hauch.hlobby.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.hauch.hlobby.HLobby;
import dev.hauch.hlobby.config.JoinMessageConfig;
import dev.hauch.hlobby.message.MessageKey;
import dev.hauch.hlobby.permission.Permission;
import dev.hauch.hlobby.update.UpdateChecker;
import dev.hauch.hlobby.util.Placeholders;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Optional;

/**
 * Sends players to a lobby when they join and shows the welcome message when
 * they enter a lobby server.
 */
public final class JoinListener {

    private final HLobby plugin;
    private final FirstJoinTracker firstJoin;

    public JoinListener(final HLobby plugin) {
        this.plugin = plugin;
        this.firstJoin = new FirstJoinTracker(plugin);
    }

    /**
     * Sends a freshly connected player to the best lobby.
     */
    @Subscribe
    public void onChooseInitialServer(final PlayerChooseInitialServerEvent event) {
        if (!this.plugin.config().lobby().onJoin().enabled()) {
            return;
        }

        final Player player = event.getPlayer();
        if (this.plugin.config().lobby().onJoin().onlyFirstJoin()
            && !this.firstJoin.markJoined(player.getUniqueId())) {
            return;
        }

        final Optional<RegisteredServer> lobby = this.plugin.lobbyManager().selectLobby(player);
        if (lobby.isEmpty()) {
            return;
        }
        if (event.getInitialServer().map(server -> server.equals(lobby.get())).orElse(false)) {
            return;
        }

        event.setInitialServer(lobby.get());
        this.plugin.config().debug("Sending {} to initial lobby {}.",
            player.getUsername(), lobby.get().getServerInfo().getName());
    }

    /**
     * Notifies operators when an update is available.
     */
    @Subscribe
    public void onPostLogin(final PostLoginEvent event) {
        final UpdateChecker checker = this.plugin.updateChecker();
        if (checker == null || !checker.isUpdateAvailable()) {
            return;
        }
        final Player player = event.getPlayer();
        if (!player.hasPermission(Permission.UPDATE_NOTIFY.getNode())) {
            return;
        }
        this.plugin.messages().send(player, MessageKey.UPDATE_AVAILABLE,
            Placeholders.version(checker.latestVersion().orElse("")),
            Placeholders.url(checker.downloadUrl().orElse("")));
    }

    /**
     * Shows the welcome message when a player enters a lobby server.
     */
    @Subscribe
    public void onServerPostConnect(final ServerPostConnectEvent event) {
        final Player player = event.getPlayer();
        final RegisteredServer server = player.getCurrentServer()
            .map(ServerConnection::getServer)
            .orElse(null);
        if (server == null || !this.plugin.lobbyManager().isLobby(server)) {
            return;
        }

        final JoinMessageConfig config = this.plugin.config().joinMessage();
        final TagResolver resolver = Placeholders.player(player.getUsername());

        if (config.title()) {
            this.plugin.messages().title(player, MessageKey.JOIN_TITLE, MessageKey.JOIN_SUBTITLE,
                config.fadeIn(), config.stay(), config.fadeOut(), resolver);
        }
        if (config.actionbar()) {
            this.plugin.messages().actionbar(player, MessageKey.JOIN_ACTIONBAR, resolver);
        }
        if (config.chat()) {
            this.plugin.messages().send(player, MessageKey.JOIN_CHAT, resolver);
        }
    }
}
