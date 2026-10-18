package dev.hauch.hlobby.metrics;

import dev.hauch.hlobby.HLobby;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bstats.velocity.Metrics;

/**
 * Thin wrapper around bStats that keeps the rest of the plugin decoupled from
 * the metrics library.
 *
 * <p>Replace {@link #SERVICE_ID} with the id assigned by
 * <a href="https://bstats.org/what-is-my-plugin-id">bStats</a> before
 * publishing a release.</p>
 */
public final class MetricsWrapper {

    /** The bStats service id for this plugin. */
    public static final int SERVICE_ID = 0;

    private final HLobby plugin;
    private Metrics metrics;

    public MetricsWrapper(final HLobby plugin) {
        this.plugin = plugin;
    }

    /** Starts the metrics collector. */
    public void start(final Metrics.Factory factory) {
        if (SERVICE_ID <= 0) {
            this.plugin.logger().debug("bStats service id is not configured, metrics disabled.");
            return;
        }
        try {
            this.metrics = factory.make(this.plugin, SERVICE_ID);
            this.metrics.addCustomChart(new SimplePie("teleport_mode",
                () -> this.plugin.lobbyManager().getTeleportMode().name()));
            this.metrics.addCustomChart(new SingleLineChart("lobby_servers",
                () -> this.plugin.lobbyManager().getLobbyNames().size()));
            this.plugin.logger().debug("bStats metrics enabled.");
        } catch (final Exception exception) {
            this.plugin.logger().warn("Failed to start bStats metrics: {}", exception.getMessage());
        }
    }

    /** Shuts the metrics collector down. */
    public void shutdown() {
        if (this.metrics != null) {
            this.metrics.shutdown();
            this.metrics = null;
        }
    }

    /** Returns whether metrics are running. */
    public boolean isEnabled() {
        return this.metrics != null;
    }
}
