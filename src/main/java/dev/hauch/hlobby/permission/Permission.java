package dev.hauch.hlobby.permission;

/**
 * Permission nodes used by the plugin.
 *
 * <p>The wildcard nodes {@code lobby.*} and {@code lobby.admin.*} are provided
 * for convenience, but every command also honours its own specific node.</p>
 */
public enum Permission {

    /** Use the main {@code /lobby} command. */
    USE("lobby.use"),

    /** Add a server to the lobby list. */
    ADMIN_CREATE("lobby.admin.create"),

    /** Remove a server from the lobby list. */
    ADMIN_REMOVE("lobby.admin.remove"),

    /** List the configured lobbies. */
    ADMIN_LIST("lobby.admin.list"),

    /** Transport all players from one lobby to another. */
    ADMIN_TRANSPORT("lobby.admin.transport"),

    /** Change the teleport mode. */
    ADMIN_MODE("lobby.admin.mode"),

    /** Teleport another player to a lobby. */
    ADMIN_TP("lobby.admin.tp"),

    /** Join a specific lobby. */
    ADMIN_SERVER("lobby.admin.server"),

    /** Reload the configuration. */
    ADMIN_RELOAD("lobby.admin.reload"),

    /** Use the root {@code /hlobby} administrative command. */
    ADMIN_MAIN("lobby.admin.main"),

    /** Ignore the {@code /lobby} cooldown. */
    BYPASS_COOLDOWN("lobby.bypass.cooldown"),

    /** Skip the teleport countdown. */
    BYPASS_COUNTDOWN("lobby.bypass.countdown"),

    /** Use {@code /lobby} on blocked servers. */
    BYPASS_BLOCKED("lobby.bypass.blocked"),

    /** Be notified when an update is available. */
    UPDATE_NOTIFY("lobby.update.notify"),

    /** All administrative permissions. */
    ADMIN_ALL("lobby.admin.*"),

    /** All permissions. */
    ALL("lobby.*");

    private final String node;

    Permission(final String node) {
        this.node = node;
    }

    /** Returns the permission node string. */
    public String getNode() {
        return this.node;
    }

    /** Returns the permission node for a command id. */
    public static String command(final String id) {
        return switch (id) {
            case "lobby" -> USE.getNode();
            case "create" -> ADMIN_CREATE.getNode();
            case "remove" -> ADMIN_REMOVE.getNode();
            case "list" -> ADMIN_LIST.getNode();
            case "transport" -> ADMIN_TRANSPORT.getNode();
            case "mode" -> ADMIN_MODE.getNode();
            case "tp" -> ADMIN_TP.getNode();
            case "server" -> ADMIN_SERVER.getNode();
            case "reload" -> ADMIN_RELOAD.getNode();
            case "hlobby" -> ADMIN_MAIN.getNode();
            default -> "lobby." + id;
        };
    }
}
