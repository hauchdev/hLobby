package dev.hauch.hlobby.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import dev.hauch.hlobby.HLobby;
import dev.hauch.hlobby.config.CommandDefinition;
import dev.hauch.hlobby.message.MessageKey;
import dev.hauch.hlobby.util.Placeholders;

/**
 * The main {@code /lobby} command. Sends the player to the best lobby.
 */
public final class LobbyCommand extends BaseCommand {

    public LobbyCommand(final HLobby plugin, final CommandDefinition definition) {
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
                final Player player = this.player(context.getSource());
                if (player == null) {
                    return 0;
                }
                this.plugin.teleportService().sendToLobby(player);
                return 1;
            })
            .then(this.literal("help").executes(context -> {
                if (!this.checkPermission(context.getSource())) {
                    return 0;
                }
                this.plugin.messages().send(context.getSource(), MessageKey.USAGE,
                    Placeholders.usage("/" + this.name()));
                return 1;
            }));
    }
}
