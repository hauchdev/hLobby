package dev.hauch.hlobby.config;

/**
 * Command definitions for every command exposed by the plugin.
 *
 * @param lobby the main {@code /lobby} command
 * @param create the {@code /lobbycreate} command
 * @param remove the {@code /lobbyremove} command
 * @param list the {@code /lobbylist} command
 * @param transport the {@code /lobbytransport} command
 * @param mode the {@code /lobbymode} command
 * @param tp the {@code /lobbytp} command
 * @param server the {@code /lobbyserver} command
 * @param reload the {@code /lobbyreload} command
 * @param hlobby the root {@code /hlobby} administrative command
 */
public record CommandsConfig(
    CommandDefinition lobby,
    CommandDefinition create,
    CommandDefinition remove,
    CommandDefinition list,
    CommandDefinition transport,
    CommandDefinition mode,
    CommandDefinition tp,
    CommandDefinition server,
    CommandDefinition reload,
    CommandDefinition hlobby
) {
}
