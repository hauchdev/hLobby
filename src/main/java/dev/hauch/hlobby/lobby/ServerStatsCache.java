package dev.hauch.hlobby.lobby;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.TaskStatus;
import dev.hauch.hlobby.HLobby;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Periodically pings lobby servers and caches their player counts.
 *
 * <p>The cache is refreshed by a repeating task so that selection strategies
 * such as {@code LEAST_PLAYERS} can make decisions without blocking on a
 * network round-trip.</p>
 */
public final class ServerStatsCache implements LobbyStats {

    /** Refresh interval, in seconds. */
    public static final long REFRESH_SECONDS = 15;

    private final HLobby plugin;
    private final Logger logger;
    private final PluginContainer container;
    private final Map<String, ServerStats> cache = new ConcurrentHashMap<>();
    private final AtomicReference<ScheduledTask> task = new AtomicReference<>();

    public ServerStatsCache(final HLobby plugin) {
        this.plugin = plugin;
        this.logger = plugin.logger();
        this.container = plugin.container();
    }

    /** Starts the repeating refresh task. */
    public void start() {
        if (this.task.get() != null) {
            return;
        }
        final ScheduledTask scheduled = this.plugin.proxy().getScheduler()
            .buildTask(this.container, this::refresh)
            .delay(Duration.ofSeconds(1))
            .repeat(Duration.ofSeconds(REFRESH_SECONDS))
            .schedule();
        this.task.set(scheduled);
        this.logger.debug("Server statistics cache started (interval {}s).", REFRESH_SECONDS);
    }

    /** Stops the refresh task and clears the cache. */
    public void shutdown() {
        final ScheduledTask scheduled = this.task.getAndSet(null);
        if (scheduled != null) {
            scheduled.cancel();
        }
        this.cache.clear();
    }

    /** Returns a copy of the cached statistics. */
    public Map<String, ServerStats> snapshot() {
        return Map.copyOf(this.cache);
    }

    /** {@inheritDoc} */
    @Override
    public int online(final RegisteredServer server) {
        final ServerStats stats = this.cache.get(server.getServerInfo().getName().toLowerCase());
        if (stats != null && stats.online() >= 0) {
            return stats.online();
        }
        return server.getPlayersConnected().size();
    }

    /** {@inheritDoc} */
    @Override
    public int max(final RegisteredServer server) {
        final ServerStats stats = this.cache.get(server.getServerInfo().getName().toLowerCase());
        return stats == null ? -1 : stats.max();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isKnown(final RegisteredServer server) {
        return this.cache.containsKey(server.getServerInfo().getName().toLowerCase());
    }

    private void refresh() {
        for (final RegisteredServer server : this.plugin.lobbyManager().getLobbies()) {
            final String name = server.getServerInfo().getName().toLowerCase();
            server.ping().whenComplete((ping, throwable) -> {
                if (throwable != null || ping == null) {
                    this.cache.put(name, ServerStats.of(server.getPlayersConnected().size()));
                    return;
                }
                final int online = ping.getPlayers().map(players -> players.getOnline()).orElse(-1);
                final int max = ping.getPlayers().map(players -> players.getMax()).orElse(-1);
                this.cache.put(name, new ServerStats(online, max));
            });
        }
    }
}
