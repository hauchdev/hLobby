package dev.hauch.hlobby.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.hauch.hlobby.HLobby;
import dev.hauch.hlobby.config.CommandDefinition;
import dev.hauch.hlobby.message.MessageKey;
import dev.hauch.hlobby.util.Placeholders;

import java.util.List;

/**
 * {@code /lobbylist} prints every configured lobby and its player count.
 */
public final class LobbyListCommand extends BaseCommand {

    public LobbyListCommand(final HLobby plugin, final CommandDefinition definition) {
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

                final CommandSource source = context.getSource();
                final List<RegisteredServer> lobbies = this.plugin.lobbyManager().getLobbies();

                if (lobbies.isEmpty()) {
                    this.plugin.messages().send(source, MessageKey.LIST_EMPTY);
                    return 1;
                }

                this.plugin.messages().sendRaw(source, this.plugin.messages().raw(MessageKey.LIST_HEADER));

                int index = 1;
                for (final RegisteredServer server : lobbies) {
                    final String name = server.getServerInfo().getName();
                    final int online = this.plugin.serverStats().online(server);
                    final int max = this.plugin.serverStats().max(server);
                    this.plugin.messages().sendRaw(source,
                        this.plugin.messages().raw(MessageKey.LIST_ENTRY),
                        Placeholders.index(index++),
                        Placeholders.server(name),
                        Placeholders.online(online),
                        Placeholders.max(max));
                }

                this.plugin.messages().sendRaw(source, this.plugin.messages().raw(MessageKey.LIST_FOOTER));
                this.plugin.messages().sendRaw(source,
                    this.plugin.messages().raw(MessageKey.LIST_SUMMARY),
                    Placeholders.amount(lobbies.size()));
                return 1;
            });
    }
}
