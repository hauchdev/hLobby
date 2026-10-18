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
 * {@code /lobbycreate <server>} registers a server as a lobby.
 */
public final class LobbyCreateCommand extends BaseCommand {

    public LobbyCreateCommand(final HLobby plugin, final CommandDefinition definition) {
        super(plugin, definition);
    }

    /** {@inheritDoc} */
    @Override
    public LiteralArgumentBuilder<CommandSource> builder() {
        return this.literal(this.name())
            .executes(context -> {
                this.plugin.messages().send(context.getSource(), MessageKey.CREATE_ARGUMENTS,
                    Placeholders.usage("/" + this.name() + " <server>"));
                return 1;
            })
            .then(this.argument("server", StringArgumentType.word())
                .suggests(Suggestions.allServers(this.plugin))
                .executes(context -> {
                    if (!this.checkPermission(context.getSource())) {
                        return 0;
                    }
                    final String server = context.getArgument("server", String.class);
                    try {
                        final LobbyResult result = this.plugin.lobbyManager().addLobby(server);
                        switch (result) {
                            case ADDED -> this.plugin.messages().send(context.getSource(),
                                MessageKey.CREATE_SUCCESS, Placeholders.server(server));
                            case ALREADY_EXISTS -> this.plugin.messages().send(context.getSource(),
                                MessageKey.CREATE_ALREADY, Placeholders.server(server));
                            case SERVER_NOT_FOUND -> this.plugin.messages().send(context.getSource(),
                                MessageKey.CREATE_NOT_FOUND, Placeholders.server(server));
                            default -> {
                            }
                        }
                    } catch (final IOException exception) {
                        this.plugin.logger().error("Failed to save the lobby list.", exception);
                        this.plugin.messages().send(context.getSource(), MessageKey.RELOAD_ERROR);
                    }
                    return 1;
                }));
    }
}
