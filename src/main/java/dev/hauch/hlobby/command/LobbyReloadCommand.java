package dev.hauch.hlobby.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import dev.hauch.hlobby.HLobby;
import dev.hauch.hlobby.config.CommandDefinition;
import dev.hauch.hlobby.message.MessageKey;

import java.io.IOException;

/**
 * {@code /lobbyreload} reloads the configuration and messages.
 */
public final class LobbyReloadCommand extends BaseCommand {

    public LobbyReloadCommand(final HLobby plugin, final CommandDefinition definition) {
        super(plugin, definition);
    }

    /** {@inheritDoc} */
    @Override
    public LiteralArgumentBuilder<CommandSource> builder() {
        return this.literal(this.name())
            .executes(context -> {
                if (!this.checkPermission(context.getSource())) {
                    return 0;
                }
                try {
                    this.plugin.config().reload();
                    this.plugin.messages().reload();
                    this.plugin.lobbyManager().reload();
                    this.plugin.messages().send(context.getSource(), MessageKey.RELOAD_SUCCESS);
                } catch (final IOException exception) {
                    this.plugin.logger().error("Failed to reload the configuration.", exception);
                    this.plugin.messages().send(context.getSource(), MessageKey.RELOAD_ERROR);
                }
                return 1;
            });
    }
}
