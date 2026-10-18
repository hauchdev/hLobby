package dev.hauch.hlobby.message;

/**
 * Every user-facing message exposed by the plugin.
 *
 * <p>Keys are dot separated and map directly to the nested structure of the
 * language files (for example {@code lobby.teleport} resolves to
 * {@code lobby: teleport:} in {@code lang/en.yml}).</p>
 */
public enum MessageKey {

    PREFIX("prefix"),
    NO_PERMISSION("no-permission"),
    ONLY_PLAYERS("only-players"),
    PLAYER_NOT_FOUND("player-not-found"),
    USAGE("usage"),
    UNKNOWN_SERVER("unknown-server"),
    COMMAND_DISABLED("command.disabled"),
    RELOAD_SUCCESS("reload.success"),
    RELOAD_ERROR("reload.error"),

    // Lobby command
    LOBBY_TELEPORT("lobby.teleport"),
    LOBBY_NO_LOBBIES("lobby.no-lobbies"),
    LOBBY_BLOCKED_SERVER("lobby.blocked-server"),
    LOBBY_SAME_SERVER("lobby.same-server"),
    LOBBY_WAITING("lobby.waiting"),
    LOBBY_CANCELLED("lobby.cancelled"),
    LOBBY_COOLDOWN("lobby.cooldown"),
    LOBBY_FALLBACK("lobby.fallback"),
    LOBBY_USING_FALLBACK("lobby.using-fallback"),

    // Create
    CREATE_SUCCESS("create.success"),
    CREATE_ALREADY("create.already"),
    CREATE_NOT_FOUND("create.not-found"),
    CREATE_ARGUMENTS("create.arguments"),
    CREATE_LIST("create.list"),

    // Remove
    REMOVE_SUCCESS("remove.success"),
    REMOVE_NOT_LOBBY("remove.not-lobby"),
    REMOVE_ARGUMENTS("remove.arguments"),
    REMOVE_EMPTY("remove.empty"),

    // List
    LIST_HEADER("list.header"),
    LIST_ENTRY("list.entry"),
    LIST_EMPTY("list.empty"),
    LIST_FOOTER("list.footer"),
    LIST_SUMMARY("list.summary"),

    // Transport
    TRANSPORT_SUCCESS("transport.success"),
    TRANSPORT_SAME("transport.same"),
    TRANSPORT_NOT_LOBBY("transport.not-lobby"),
    TRANSPORT_ARGUMENTS("transport.arguments"),
    TRANSPORT_NOTHING("transport.nothing"),

    // Mode
    MODE_SUCCESS("mode.success"),
    MODE_INVALID("mode.invalid"),
    MODE_ARGUMENTS("mode.arguments"),
    MODE_LIST("mode.list"),

    // Tp
    TP_SUCCESS("tp.success"),
    TP_TARGET_NOTIFIED("tp.target-notified"),
    TP_ARGUMENTS("tp.arguments"),

    // Server
    SERVER_SUCCESS("server.success"),
    SERVER_NOT_LOBBY("server.not-lobby"),
    SERVER_ARGUMENTS("server.arguments"),

    // Join messages
    JOIN_TITLE("join.title"),
    JOIN_SUBTITLE("join.subtitle"),
    JOIN_ACTIONBAR("join.actionbar"),
    JOIN_CHAT("join.chat"),

    // Kick
    KICK_REDIRECT("kick.redirect"),

    // Update
    UPDATE_AVAILABLE("update.available"),
    UPDATE_CHECKING("update.checking"),
    UPDATE_UP_TO_DATE("update.up-to-date"),
    UPDATE_AVAILABLE_NOW("update.available-now"),
    UPDATE_ERROR("update.error"),

    // Admin command
    HLOBBY_HELP_HEADER("hlobby.help.header"),
    HLOBBY_HELP_RELOAD("hlobby.help.reload"),
    HLOBBY_HELP_UPDATE("hlobby.help.update"),
    HLOBBY_HELP_ABOUT("hlobby.help.about"),
    HLOBBY_ABOUT_VERSION("hlobby.about.version"),
    HLOBBY_ABOUT_METRICS("hlobby.about.metrics"),
    HLOBBY_ABOUT_METRICS_DISABLED("hlobby.about.metrics-disabled"),
    HLOBBY_ABOUT_WEBSITE("hlobby.about.website"),
    HLOBBY_UNKNOWN_SUBCOMMAND("hlobby.unknown-subcommand");

    private final String key;

    MessageKey(final String key) {
        this.key = key;
    }

    /** Returns the dot separated message key. */
    public String key() {
        return this.key;
    }
}
