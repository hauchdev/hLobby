package dev.hauch.hlobby.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.hauch.hlobby.HLobby;
import dev.hauch.hlobby.config.CommandDefinition;
import dev.hauch.hlobby.message.MessageKey;
import dev.hauch.hlobby.util.Placeholders;

import java.util.Optional;

/**
 * {@code /lobbytp <player>} sends another player to the best lobby.
 */
public final class LobbyTpCommand extends BaseCommand {

    public LobbyTpCommand(final HLobby plugin, final CommandDefinition definition) {
        super(plugin, definition);
    }

    /** {@inheritDoc} */
    @Override
    public LiteralArgumentBuilder<CommandSource> builder() {
        return this.literal(this.name())
            .executes(context -> {
                this.plugin.messages().send(context.getSource(), MessageKey.TP_ARGUMENTS,
                    Placeholders.usage("/" + this.name() + " <player>"));
                return 1;
            })
            .then(this.argument("player", StringArgumentType.word())
                .suggests(Suggestions.players(this.plugin))
                .executes(context -> {
                    if (!this.checkPermission(context.getSource())) {
                        return 0;
                    }
                    final CommandSource source = context.getSource();
                    final String playerName = context.getArgument("player", String.class);

                    final Optional<Player> target = this.plugin.proxy().getPlayer(playerName);
                    if (target.isEmpty()) {
                        this.plugin.messages().send(source, MessageKey.PLAYER_NOT_FOUND,
                            Placeholders.player(playerName));
                        return 1;
                    }

                    final Optional<RegisteredServer> lobby =
                        this.plugin.lobbyManager().selectLobby(target.get());
                    if (lobby.isEmpty()) {
                        this.plugin.messages().send(source, MessageKey.LOBBY_NO_LOBBIES);
                        return 1;
                    }

                    this.plugin.teleportService().teleport(target.get(), lobby.get());
                    final String lobbyName = lobby.get().getServerInfo().getName();
                    this.plugin.messages().send(source, MessageKey.TP_SUCCESS,
                        Placeholders.player(playerName),
                        Placeholders.server(lobbyName));
                    this.plugin.messages().send(target.get(), MessageKey.TP_TARGET_NOTIFIED,
                        Placeholders.server(lobbyName));
                    return 1;
                }));
    }
}
