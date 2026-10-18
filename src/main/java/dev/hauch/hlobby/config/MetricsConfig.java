package dev.hauch.hlobby.config;

/**
 * Settings for FastStats anonymous metrics.
 *
 * @param enabled whether usage metrics are submitted to faststats.dev
 */
public record MetricsConfig(boolean enabled) {
}
