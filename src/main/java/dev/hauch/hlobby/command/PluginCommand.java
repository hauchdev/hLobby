package dev.hauch.hlobby.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;

/**
 * A registrable plugin command.
 */
public interface PluginCommand {

    /**
     * Builds the Brigadier command tree for this command.
     *
     * @return the root node builder
     */
    LiteralArgumentBuilder<CommandSource> builder();
}
