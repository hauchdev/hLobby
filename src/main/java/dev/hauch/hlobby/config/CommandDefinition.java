package dev.hauch.hlobby.config;

import java.util.List;

/**
 * A single command definition loaded from the {@code commands} section of the
 * configuration.
 *
 * @param enabled whether the command should be registered
 * @param name the primary command name
 * @param aliases additional aliases for the command
 * @param permission the permission required to use the command
 */
public record CommandDefinition(
    boolean enabled,
    String name,
    List<String> aliases,
    String permission
) {

    /** Returns a definition with no aliases. */
    public CommandDefinition(final boolean enabled, final String name, final String permission) {
        this(enabled, name, List.of(), permission);
    }
}
