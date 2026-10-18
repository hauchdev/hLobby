package dev.hauch.hlobby.command;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.hauch.hlobby.HLobby;
import dev.hauch.hlobby.lobby.TeleportMode;

import java.util.Arrays;

/**
 * Reusable Brigadier suggestion providers for hLobby commands.
 */
public final class Suggestions {

    private Suggestions() {
    }

    /** Suggests the configured lobby servers. */
    public static SuggestionProvider<CommandSource> lobbyServers(final HLobby plugin) {
        return (context, builder) -> {
            for (final String name : plugin.lobbyManager().getLobbyNames()) {
                builder.suggest(name);
            }
            return builder.buildFuture();
        };
    }

    /** Suggests every server registered on the proxy. */
    public static SuggestionProvider<CommandSource> allServers(final HLobby plugin) {
        return (context, builder) -> {
            for (final RegisteredServer server : plugin.proxy().getAllServers()) {
                builder.suggest(server.getServerInfo().getName());
            }
            return builder.buildFuture();
        };
    }

    /** Suggests the online player names. */
    public static SuggestionProvider<CommandSource> players(final HLobby plugin) {
        return (context, builder) -> {
            for (final Player player : plugin.proxy().getAllPlayers()) {
                builder.suggest(player.getUsername());
            }
            return builder.buildFuture();
        };
    }

    /** Suggests the available teleport modes. */
    public static SuggestionProvider<CommandSource> teleportModes() {
        return (context, builder) -> {
            for (final TeleportMode mode : TeleportMode.values()) {
                builder.suggest(mode.name());
            }
            return builder.buildFuture();
        };
    }

    /** Suggests the available languages. */
    public static SuggestionProvider<CommandSource> languages(final HLobby plugin) {
        return (context, builder) -> {
            builder.suggest("en");
            builder.suggest("es");
            return builder.buildFuture();
        };
    }

    /** Returns the comma separated list of teleport mode names. */
    public static String modeList() {
        return String.join(", ", Arrays.stream(TeleportMode.values())
            .map(TeleportMode::name)
            .toList());
    }
}
