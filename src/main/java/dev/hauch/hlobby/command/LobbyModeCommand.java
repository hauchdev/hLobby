package dev.hauch.hlobby.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import dev.hauch.hlobby.HLobby;
import dev.hauch.hlobby.config.CommandDefinition;
import dev.hauch.hlobby.lobby.TeleportMode;
import dev.hauch.hlobby.message.MessageKey;
import dev.hauch.hlobby.util.Placeholders;

import java.io.IOException;

/**
 * {@code /lobbymode <mode>} changes the lobby selection strategy.
 */
public final class LobbyModeCommand extends BaseCommand {

    public LobbyModeCommand(final HLobby plugin, final CommandDefinition definition) {
        super(plugin, definition);
    }

    /** {@inheritDoc} */
    @Override
    public LiteralArgumentBuilder<CommandSource> builder() {
        return this.literal(this.name())
            .executes(context -> {
                this.plugin.messages().send(context.getSource(), MessageKey.MODE_ARGUMENTS,
                    Placeholders.usage("/" + this.name() + " <mode>"));
                return 1;
            })
            .then(this.argument("mode", StringArgumentType.word())
                .suggests(Suggestions.teleportModes())
                .executes(context -> {
                    if (!this.checkPermission(context.getSource())) {
                        return 0;
                    }
                    final CommandSource source = context.getSource();
                    final String value = context.getArgument("mode", String.class);

                    TeleportMode mode = null;
                    for (final TeleportMode candidate : TeleportMode.values()) {
                        if (candidate.name().equalsIgnoreCase(value)) {
                            mode = candidate;
                            break;
                        }
                    }

                    if (mode == null) {
                        this.plugin.messages().send(source, MessageKey.MODE_INVALID,
                            Placeholders.mode(value),
                            Placeholders.modes(Suggestions.modeList()));
                        return 1;
                    }

                    try {
                        this.plugin.lobbyManager().setTeleportMode(mode);
                    } catch (final IOException exception) {
                        this.plugin.logger().error("Failed to save the teleport mode.", exception);
                        this.plugin.messages().send(source, MessageKey.RELOAD_ERROR);
                        return 1;
                    }

                    this.plugin.messages().send(source, MessageKey.MODE_SUCCESS,
                        Placeholders.mode(mode.name()));
                    return 1;
                }));
    }
}
