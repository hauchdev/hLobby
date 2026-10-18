package dev.hauch.hlobby.metrics;

import dev.faststats.ErrorTracker;
import dev.faststats.Metrics;
import dev.faststats.data.Metric;
import dev.faststats.velocity.VelocityContext;
import dev.hauch.hlobby.HLobby;

/**
 * Thin wrapper around FastStats that keeps the rest of the plugin decoupled
 * from the metrics library.
 *
 * <p>Replace {@link #FASTSTATS_TOKEN} with the token assigned by
 * <a href="https://faststats.dev/">FastStats</a> before publishing a
 * release. Server owners can disable metrics with {@code metrics.enabled}
 * in {@code config.yml}.</p>
 */
public final class MetricsWrapper {

    /** FastStats project token. Replace before releasing. */
    public static final String FASTSTATS_TOKEN = "";

    private static final ErrorTracker ERROR_TRACKER = ErrorTracker.contextAware();

    private final HLobby plugin;
    private VelocityContext context;

    public MetricsWrapper(final HLobby plugin, final VelocityContext.Builder contextBuilder) {
        this.plugin = plugin;
        if (!this.plugin.config().metrics().enabled()) {
            this.plugin.logger().debug("FastStats metrics are disabled by configuration.");
            return;
        }
        if (FASTSTATS_TOKEN == null || FASTSTATS_TOKEN.isBlank()) {
            this.plugin.logger().warn("FastStats metrics are enabled but no token is configured. "
                + "Set MetricsWrapper.FASTSTATS_TOKEN before releasing.");
            return;
        }
        try {
            this.context = contextBuilder
                .token(FASTSTATS_TOKEN)
                .errorTrackerService(ERROR_TRACKER)
                .metrics(factory -> factory
                    .addMetric(Metric.string("teleport_mode",
                        () -> this.plugin.lobbyManager().getTeleportMode().name()))
                    .addMetric(Metric.number("lobby_count",
                        () -> this.plugin.lobbyManager().getLobbyNames().size()))
                    .create())
                .create();
            this.plugin.logger().debug("FastStats metrics initialized.");
        } catch (final Exception exception) {
            this.plugin.logger().warn("Failed to initialize FastStats metrics: {}",
                exception.getMessage());
        }
    }

    /** Signals FastStats that the plugin is ready. */
    public void ready() {
        if (this.context != null) {
            try {
                this.context.ready();
            } catch (final Exception exception) {
                this.plugin.logger().warn("Failed to start FastStats metrics: {}",
                    exception.getMessage());
            }
        }
    }

    /** Shuts FastStats down. */
    public void shutdown() {
        if (this.context != null) {
            try {
                this.context.shutdown();
            } catch (final Exception exception) {
                this.plugin.logger().debug("FastStats shutdown error: {}", exception.getMessage());
            }
        }
    }

    /** Returns whether metrics are active. */
    public boolean isEnabled() {
        return this.context != null;
    }
}
