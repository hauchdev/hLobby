package dev.hauch.hlobby.lobby;

/**
 * Outcome of mutating the lobby server list.
 */
public enum LobbyResult {

    /** The server was added to the lobby list. */
    ADDED,

    /** The server was already a lobby. */
    ALREADY_EXISTS,

    /** The server was removed from the lobby list. */
    REMOVED,

    /** The server was not a lobby. */
    NOT_LOBBY,

    /** The server is not registered on the proxy. */
    SERVER_NOT_FOUND
}
