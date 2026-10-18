package dev.hauch.hlobby.lobby.selector;

import dev.hauch.hlobby.lobby.TeleportMode;

import java.util.EnumMap;
import java.util.Map;

/**
 * Creates and caches the {@link LobbySelector} for each {@link TeleportMode}.
 */
public final class SelectorFactory {

    private final Map<TeleportMode, LobbySelector> selectors = new EnumMap<>(TeleportMode.class);

    public SelectorFactory() {
        this.selectors.put(TeleportMode.RANDOM, new RandomSelector());
        this.selectors.put(TeleportMode.LEAST_PLAYERS, new LeastPlayersSelector());
        this.selectors.put(TeleportMode.MOST_PLAYERS, new MostPlayersSelector());
        this.selectors.put(TeleportMode.FIRST_AVAILABLE, new FirstAvailableSelector());
        this.selectors.put(TeleportMode.PRIORITY, new PrioritySelector());
        // Round-robin keeps mutable state, so it must be a singleton.
        this.selectors.put(TeleportMode.ROUND_ROBIN, new RoundRobinSelector());
    }

    /**
     * Returns the selector for a teleport mode.
     *
     * @param mode the mode
     * @return the selector, never {@code null}
     */
    public LobbySelector selector(final TeleportMode mode) {
        return this.selectors.getOrDefault(mode, this.selectors.get(TeleportMode.LEAST_PLAYERS));
    }

    /**
     * Returns the selector for a mode parsed from configuration.
     *
     * @param configured the configured mode name
     * @return the selector, never {@code null}
     */
    public LobbySelector selectorFor(final String configured) {
        return this.selector(TeleportMode.parse(configured));
    }
}
