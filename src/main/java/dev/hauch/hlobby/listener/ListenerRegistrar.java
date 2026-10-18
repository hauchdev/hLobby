package dev.hauch.hlobby.listener;

import com.velocitypowered.api.event.EventManager;
import dev.hauch.hlobby.HLobby;

/**
 * Registers the plugin event listeners.
 */
public final class ListenerRegistrar {

    private final HLobby plugin;

    public ListenerRegistrar(final HLobby plugin) {
        this.plugin = plugin;
    }

    /** Registers every listener with the Velocity event manager. */
    public void register() {
        final EventManager eventManager = this.plugin.proxy().getEventManager();
        eventManager.register(this.plugin.container(), new JoinListener(this.plugin));
        eventManager.register(this.plugin.container(), new KickListener(this.plugin));
        eventManager.register(this.plugin.container(), new CommandListener(this.plugin));
    }
}
