package dev.hauch.hlobby.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import dev.hauch.hlobby.HLobby;
import dev.hauch.hlobby.config.CommandDefinition;
import dev.hauch.hlobby.config.CommandsConfig;

/**
 * Registers every command declared in the configuration.
 *
 * <p>Commands can be renamed, given aliases or disabled entirely from
 * {@code config.yml}. Disabled commands are simply not registered.</p>
 */
public final class CommandRegistrar {

    private final HLobby plugin;

    public CommandRegistrar(final HLobby plugin) {
        this.plugin = plugin;
    }

    /** Registers all configured commands. */
    public void register() {
        final CommandsConfig commands = this.plugin.config().commands();

        this.register(commands.lobby(), new LobbyCommand(this.plugin, commands.lobby()));
        this.register(commands.create(), new LobbyCreateCommand(this.plugin, commands.create()));
        this.register(commands.remove(), new LobbyRemoveCommand(this.plugin, commands.remove()));
        this.register(commands.list(), new LobbyListCommand(this.plugin, commands.list()));
        this.register(commands.transport(), new LobbyTransportCommand(this.plugin, commands.transport()));
        this.register(commands.mode(), new LobbyModeCommand(this.plugin, commands.mode()));
        this.register(commands.tp(), new LobbyTpCommand(this.plugin, commands.tp()));
        this.register(commands.server(), new LobbyServerCommand(this.plugin, commands.server()));
        this.register(commands.reload(), new LobbyReloadCommand(this.plugin, commands.reload()));
        this.register(commands.hlobby(), new HlobbyCommand(this.plugin, commands.hlobby()));
    }

    private void register(final CommandDefinition definition, final PluginCommand command) {
        if (!definition.enabled()) {
            this.plugin.logger().debug("Command '{}' is disabled, skipping registration.",
                definition.name());
            return;
        }

        try {
            final LiteralCommandNode<CommandSource> node = command.builder().build();
            final CommandManager manager = this.plugin.proxy().getCommandManager();
            final CommandMeta meta = manager.metaBuilder(definition.name())
                .aliases(definition.aliases().toArray(new String[0]))
                .plugin(this.plugin.container())
                .build();
            manager.register(meta, new BrigadierCommand(node));
            this.plugin.logger().debug("Registered command '{}' with aliases {}.",
                definition.name(), definition.aliases());
        } catch (final IllegalArgumentException exception) {
            this.plugin.logger().error("Failed to register command '{}': {}",
                definition.name(), exception.getMessage());
        }
    }
}
