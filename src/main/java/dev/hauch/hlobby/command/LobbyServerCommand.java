package dev.hauch.hlobby.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import dev.hauch.hlobby.HLobby;
import dev.hauch.hlobby.config.CommandDefinition;
import dev.hauch.hlobby.message.MessageKey;
import dev.hauch.hlobby.util.Placeholders;

/**
 * {@code /lobbyserver <server>} joins a specific lobby.
 */
public final class LobbyServerCommand extends BaseCommand {

    public LobbyServerCommand(final HLobby plugin, final CommandDefinition definition) {
        super(plugin, definition);
    }

    /** {@inheritDoc} */
    @Override
    public LiteralArgumentBuilder<CommandSource> builder() {
        return this.literal(this.name())
            .executes(context -> {
                this.plugin.messages().send(context.getSource(), MessageKey.SERVER_ARGUMENTS,
                    Placeholders.usage("/" + this.name() + " <server>"));
                return 1;
            })
            .then(this.argument("server", StringArgumentType.word())
                .suggests(Suggestions.lobbyServers(this.plugin))
                .executes(context -> {
                    if (!this.checkPermission(context.getSource())) {
                        return 0;
                    }
                    final Player player = this.player(context.getSource());
                    if (player == null) {
                        return 0;
                    }
                    final String server = context.getArgument("server", String.class);
                    this.plugin.teleportService().sendToLobby(player, server);
                    return 1;
                }));
    }
}
