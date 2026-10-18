package dev.hauch.hlobby.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.hauch.hlobby.HLobby;
import dev.hauch.hlobby.message.MessageKey;
import net.kyori.adventure.text.Component;

import java.util.Optional;

/**
 * Redirects kicked players to a lobby instead of disconnecting them.
 */
public final class KickListener {

    private final HLobby plugin;

    public KickListener(final HLobby plugin) {
        this.plugin = plugin;
    }

    /** {@inheritDoc} */
    @Subscribe
    public void onKickedFromServer(final KickedFromServerEvent event) {
        if (!this.plugin.config().lobby().onKick().enabled()) {
            return;
        }
        if (event.kickedDuringServerConnect()) {
            // Let Velocity handle the kick during the connection process; redirecting here
            // can cause the player to end up in an inconsistent state.
            return;
        }
        if (this.plugin.config().lobby().onKick().excludeLobbies()
            && this.plugin.lobbyManager().isLobby(event.getServer())) {
            return;
        }

        final Optional<RegisteredServer> lobby =
            this.plugin.lobbyManager().selectLobby(event.getPlayer());
        if (lobby.isEmpty()) {
            event.setResult(KickedFromServerEvent.DisconnectPlayer.create(
                this.plugin.messages().component(MessageKey.LOBBY_NO_LOBBIES)));
            return;
        }

        final Component message = this.plugin.config().lobby().onKick().notifyPlayer()
            ? this.plugin.messages().component(MessageKey.KICK_REDIRECT)
            : Component.empty();
        event.setResult(KickedFromServerEvent.RedirectPlayer.create(lobby.get(), message));
    }
}
