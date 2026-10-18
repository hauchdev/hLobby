package dev.hauch.hlobby;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.hauch.hlobby.api.HLobbyProvider;
import dev.hauch.hlobby.api.HLobbyProvider.ApiHolder;
import dev.hauch.hlobby.command.CommandRegistrar;
import dev.hauch.hlobby.config.ConfigManager;
import dev.hauch.hlobby.listener.ListenerRegistrar;
import dev.hauch.hlobby.lobby.LobbyManager;
import dev.hauch.hlobby.lobby.ServerStatsCache;
import dev.hauch.hlobby.message.MessageManager;
import dev.hauch.hlobby.metrics.MetricsWrapper;
import dev.hauch.hlobby.teleport.TeleportService;
import dev.hauch.hlobby.update.UpdateChecker;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Entry point of the hLobby proxy plugin.
 *
 * <p>hLobby is a modern, highly configurable lobby management system for the
 * Velocity proxy. It manages lobby servers, balances players between them with
 * pluggable selection strategies, teleports players back to a lobby with an
 * optional countdown and exposes a small public API for third-party plugins.</p>
 *
 * <p>The Velocity annotation processor writes {@code velocity-plugin.json}
 * from this class at compile time.</p>
 */
@Plugin(
    id = "hlobby",
    name = "hLobby",
    version = "1.0.0",
    description = "A modern, highly configurable lobby management system for Velocity.",
    url = "https://github.com/hauchdev/hlobby",
    authors = {"hauchdev"}
)
public final class HLobby implements HLobbyProvider {

    /** The plugin id, used everywhere a stable identifier is required. */
    public static final String PLUGIN_ID = "hlobby";

    /** The plugin version. Keep in sync with {@code build.gradle}. */
    public static final String PLUGIN_VERSION = "1.0.0";

    private static volatile HLobby instance;

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;
    private final PluginContainer container;

    private ConfigManager config;
    private MessageManager messages;
    private LobbyManager lobbyManager;
    private TeleportService teleportService;
    private ServerStatsCache serverStats;
    private MetricsWrapper metrics;
    private Metrics.Factory metricsFactory;
    private UpdateChecker updateChecker;

    @Inject
    public HLobby(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        instance = this;
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.container = proxy.getPluginManager().getPlugin(PLUGIN_ID)
            .orElseThrow(() -> new IllegalStateException("Plugin container for " + PLUGIN_ID + " not found"));
    }

    /**
     * Injects the bStats metrics factory when it is available.
     *
     * <p>The injection is optional so that the plugin keeps working even when
     * the metrics library cannot be bound by the proxy's injector.</p>
     *
     * @param metricsFactory the bStats metrics factory
     */
    @Inject(optional = true)
    public void setMetricsFactory(final Metrics.Factory metricsFactory) {
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        final long start = System.currentTimeMillis();
        this.logger.info("Enabling hLobby v{} ...", PLUGIN_VERSION);
        ApiHolder.set(this);

        try {
            this.config = new ConfigManager(this);
            this.config.load();

            this.messages = new MessageManager(this);
            this.messages.load();

            this.lobbyManager = new LobbyManager(this);
            this.teleportService = new TeleportService(this);

            if (this.config.general().checkUpdates()) {
                this.updateChecker = new UpdateChecker(this);
            }

            new CommandRegistrar(this).register();
            new ListenerRegistrar(this).register();

            this.serverStats = new ServerStatsCache(this);
            this.serverStats.start();

            if (this.metricsFactory != null) {
                this.metrics = new MetricsWrapper(this);
                this.metrics.start(this.metricsFactory);
            }

            if (this.updateChecker != null) {
                this.updateChecker.checkAsync();
            }

            this.logger.info("hLobby v{} enabled in {} ms.", PLUGIN_VERSION,
                System.currentTimeMillis() - start);
        } catch (Exception exception) {
            this.logger.error("Failed to enable hLobby. The plugin will not function correctly.", exception);
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.logger.info("Disabling hLobby v{} ...", PLUGIN_VERSION);

        if (this.metrics != null) {
            this.metrics.shutdown();
        }
        if (this.serverStats != null) {
            this.serverStats.shutdown();
        }
        if (this.teleportService != null) {
            this.teleportService.shutdown();
        }
        if (this.updateChecker != null) {
            this.updateChecker.shutdown();
        }

        instance = null;
        ApiHolder.clear();
        this.logger.info("hLobby disabled.");
    }

    /**
     * Returns the active plugin instance.
     *
     * @return the running instance, or {@code null} when not enabled
     */
    public static HLobby instance() {
        return instance;
    }

    /**
     * Returns the active plugin instance, throwing when the plugin is disabled.
     *
     * @return the running instance
     * @throws IllegalStateException if the plugin is not enabled
     */
    public static HLobby requireInstance() {
        final HLobby running = instance;
        if (running == null) {
            throw new IllegalStateException("hLobby is not enabled");
        }
        return running;
    }

    @Override
    public HLobbyProvider api() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ProxyServer proxy() {
        return this.proxy;
    }

    /** {@inheritDoc} */
    @Override
    public Logger logger() {
        return this.logger;
    }

    /** {@inheritDoc} */
    @Override
    public Path dataDirectory() {
        return this.dataDirectory;
    }

    /** {@inheritDoc} */
    @Override
    public PluginContainer container() {
        return this.container;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigManager config() {
        return this.config;
    }

    /** {@inheritDoc} */
    @Override
    public MessageManager messages() {
        return this.messages;
    }

    /** {@inheritDoc} */
    @Override
    public LobbyManager lobbyManager() {
        return this.lobbyManager;
    }

    /** {@inheritDoc} */
    @Override
    public TeleportService teleportService() {
        return this.teleportService;
    }

    /**
     * Returns the server statistics cache.
     *
     * @return the cache, or {@code null} before the plugin has enabled
     */
    public ServerStatsCache serverStats() {
        return this.serverStats;
    }

    /**
     * Returns the update checker.
     *
     * @return the update checker, or {@code null} when update checks are disabled
     */
    public UpdateChecker updateChecker() {
        return this.updateChecker;
    }
}
