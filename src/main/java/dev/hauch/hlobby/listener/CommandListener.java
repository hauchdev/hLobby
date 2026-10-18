package dev.hauch.hlobby.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import dev.hauch.hlobby.HLobby;
import dev.hauch.hlobby.config.CommandDefinition;
import dev.hauch.hlobby.message.MessageKey;

/**
 * Cancels an active teleport countdown when the player runs a command or sends
 * a chat message.
 */
public final class CommandListener {

    private final HLobby plugin;

    public CommandListener(final HLobby plugin) {
        this.plugin = plugin;
    }

    /** {@inheritDoc} */
    @Subscribe
    public void onCommandExecute(final CommandExecuteEvent event) {
        if (!this.plugin.config().lobby().countdown().cancelOnCommand()) {
            return;
        }
        if (!(event.getCommandSource() instanceof Player player)) {
            return;
        }
        if (this.isLobbyCommand(event.getCommand())) {
            return;
        }
        this.cancel(player);
    }

    /** {@inheritDoc} */
    @Subscribe
    public void onPlayerChat(final PlayerChatEvent event) {
        if (!this.plugin.config().lobby().countdown().cancelOnChat()) {
            return;
        }
        this.cancel(event.getPlayer());
    }

    private void cancel(final Player player) {
        if (this.plugin.teleportService().cancelCountdown(player.getUniqueId())) {
            this.plugin.messages().send(player, MessageKey.LOBBY_CANCELLED);
        }
    }

    private boolean isLobbyCommand(final String command) {
        final CommandDefinition definition = this.plugin.config().commands().lobby();
        if (definition.name().equalsIgnoreCase(command)) {
            return true;
        }
        return definition.aliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(command));
    }
}
