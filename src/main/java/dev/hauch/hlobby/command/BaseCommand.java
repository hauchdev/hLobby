package dev.hauch.hlobby.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import dev.hauch.hlobby.HLobby;
import dev.hauch.hlobby.config.CommandDefinition;
import dev.hauch.hlobby.message.MessageKey;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

/**
 * Shared behaviour for every hLobby command.
 */
public abstract class BaseCommand implements PluginCommand {

    protected final HLobby plugin;
    private final CommandDefinition definition;

    protected BaseCommand(final HLobby plugin, final CommandDefinition definition) {
        this.plugin = plugin;
        this.definition = definition;
    }

    /** Returns the configured command name. */
    protected String name() {
        return this.definition.name();
    }

    /** Returns the configured permission node. */
    protected String permission() {
        return this.definition.permission();
    }

    /** Returns the configured aliases. */
    protected java.util.List<String> aliases() {
        return this.definition.aliases();
    }

    /**
     * Verifies the source has the required permission, sending a message when
     * the configuration asks for one.
     *
     * @param source the command source
     * @return {@code true} when the command may run
     */
    protected boolean checkPermission(final CommandSource source) {
        if (source.hasPermission(this.permission())) {
            return true;
        }
        if (this.plugin.config().misc().showNoPermission()) {
            this.plugin.messages().send(source, MessageKey.NO_PERMISSION);
        }
        return false;
    }

    /**
     * Casts the source to a player, sending the only-players message otherwise.
     *
     * @param source the command source
     * @return the player, or {@code null}
     */
    protected Player player(final CommandSource source) {
        if (source instanceof Player player) {
            return player;
        }
        this.plugin.messages().send(source, MessageKey.ONLY_PLAYERS);
        return null;
    }

    /** Creates a literal node with the given name. */
    protected LiteralArgumentBuilder<CommandSource> literal(final String name) {
        return BrigadierCommand.literalArgumentBuilder(name);
    }

    /** Creates a required argument node. */
    protected <T> RequiredArgumentBuilder<CommandSource, T> argument(final String name,
        final ArgumentType<T> type) {
        return BrigadierCommand.requiredArgumentBuilder(name, type);
    }
}
