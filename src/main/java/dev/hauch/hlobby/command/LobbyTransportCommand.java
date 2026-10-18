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
 * {@code /lobbytransport <from> <to>} moves every player from one lobby to another.
 */
public final class LobbyTransportCommand extends BaseCommand {

    public LobbyTransportCommand(final HLobby plugin, final CommandDefinition definition) {
        super(plugin, definition);
    }

    /** {@inheritDoc} */
    @Override
    public LiteralArgumentBuilder<CommandSource> builder() {
        return this.literal(this.name())
            .executes(context -> {
                this.plugin.messages().send(context.getSource(), MessageKey.TRANSPORT_ARGUMENTS,
                    Placeholders.usage("/" + this.name() + " <from> <to>"));
                return 1;
            })
            .then(this.argument("from", StringArgumentType.word())
                .suggests(Suggestions.lobbyServers(this.plugin))
                .then(this.argument("to", StringArgumentType.word())
                    .suggests(Suggestions.lobbyServers(this.plugin))
                    .executes(context -> {
                        if (!this.checkPermission(context.getSource())) {
                            return 0;
                        }
                        final CommandSource source = context.getSource();
                        final String fromName = context.getArgument("from", String.class);
                        final String toName = context.getArgument("to", String.class);

                        final Optional<RegisteredServer> from = this.plugin.lobbyManager().getLobby(fromName);
                        if (from.isEmpty()) {
                            this.plugin.messages().send(source, MessageKey.TRANSPORT_NOT_LOBBY,
                                Placeholders.server(fromName));
                            return 1;
                        }
                        final Optional<RegisteredServer> to = this.plugin.lobbyManager().getLobby(toName);
                        if (to.isEmpty()) {
                            this.plugin.messages().send(source, MessageKey.TRANSPORT_NOT_LOBBY,
                                Placeholders.server(toName));
                            return 1;
                        }
                        if (from.get().equals(to.get())) {
                            this.plugin.messages().send(source, MessageKey.TRANSPORT_SAME);
                            return 1;
                        }

                        int moved = 0;
                        for (final Player player : from.get().getPlayersConnected()) {
                            this.plugin.teleportService().teleport(player, to.get());
                            moved++;
                        }

                        if (moved == 0) {
                            this.plugin.messages().send(source, MessageKey.TRANSPORT_NOTHING,
                                Placeholders.server(fromName));
                            return 1;
                        }

                        this.plugin.messages().send(source, MessageKey.TRANSPORT_SUCCESS,
                            Placeholders.from(fromName),
                            Placeholders.to(toName),
                            Placeholders.amount(moved));
                        return 1;
                    })));
    }
}
