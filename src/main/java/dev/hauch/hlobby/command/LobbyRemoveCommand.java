package dev.hauch.hlobby.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import dev.hauch.hlobby.HLobby;
import dev.hauch.hlobby.config.CommandDefinition;
import dev.hauch.hlobby.lobby.LobbyResult;
import dev.hauch.hlobby.message.MessageKey;
import dev.hauch.hlobby.util.Placeholders;

import java.io.IOException;

/**
 * {@code /lobbyremove <server>} removes a server from the lobby list.
 */
public final class LobbyRemoveCommand extends BaseCommand {

    public LobbyRemoveCommand(final HLobby plugin, final CommandDefinition definition) {
        super(plugin, definition);
    }

    /** {@inheritDoc} */
    @Override
    public LiteralArgumentBuilder<CommandSource> builder() {
        return this.literal(this.name())
            .executes(context -> {
                this.plugin.messages().send(context.getSource(), MessageKey.REMOVE_ARGUMENTS,
                    Placeholders.usage("/" + this.name() + " <server>"));
                return 1;
            })
            .then(this.argument("server", StringArgumentType.word())
                .suggests(Suggestions.lobbyServers(this.plugin))
                .executes(context -> {
                    if (!this.checkPermission(context.getSource())) {
                        return 0;
                    }
                    final String server = context.getArgument("server", String.class);
                    try {
                        final LobbyResult result = this.plugin.lobbyManager().removeLobby(server);
                        if (result == LobbyResult.REMOVED) {
                            this.plugin.messages().send(context.getSource(), MessageKey.REMOVE_SUCCESS,
                                Placeholders.server(server));
                        } else {
                            this.plugin.messages().send(context.getSource(), MessageKey.REMOVE_NOT_LOBBY,
                                Placeholders.server(server));
                        }
                    } catch (final IOException exception) {
                        this.plugin.logger().error("Failed to save the lobby list.", exception);
                        this.plugin.messages().send(context.getSource(), MessageKey.RELOAD_ERROR);
                    }
                    return 1;
                }));
    }
}
