package dev.hauch.hlobby.config;

import dev.hauch.hlobby.HLobby;
import dev.hauch.hlobby.lobby.TeleportMode;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Loads, validates and persists the plugin configuration.
 *
 * <p>The configuration is a YAML file named {@code config.yml} inside the
 * plugin data directory. A bundled default is extracted on the first run and
 * is never overwritten, so administrator changes are preserved across
 * updates.</p>
 */
public final class ConfigManager {

    /** Current configuration schema version. */
    public static final int CONFIG_VERSION = 1;

    private static final String RESOURCE = "config.yml";
    private static final String FILE_NAME = "config.yml";

    private final HLobby plugin;
    private final Logger logger;
    private final Path file;
    private final Path dataDirectory;
    private ConfigurationLoader<?> loader;

    private GeneralConfig general = new GeneralConfig("en", false, true);
    private LobbyConfig lobby = defaultLobby();
    private CommandsConfig commands = defaultCommands();
    private JoinMessageConfig joinMessage =
        new JoinMessageConfig(true, true, true, 10, 70, 20);
    private MiscConfig misc = new MiscConfig(true);

    public ConfigManager(final HLobby plugin) {
        this.plugin = plugin;
        this.logger = plugin.logger();
        this.dataDirectory = plugin.dataDirectory();
        this.file = this.dataDirectory.resolve(FILE_NAME);
    }

    /** Returns the path of the configuration file. */
    public Path file() {
        return this.file;
    }

    /** Reloads the configuration from disk. */
    public void load() throws IOException {
        this.ensureExtracted();
        this.loader = YamlConfigurationLoader.builder().path(this.file).build();
        final ConfigurationNode root = this.loader.load();

        final int version = root.node("config-version").getInt(CONFIG_VERSION);
        if (version < CONFIG_VERSION) {
            this.logger.warn("Your config.yml is using an older schema ({}). "
                + "New options will use their defaults. Consider regenerating it.", version);
        }

        this.general = this.readGeneral(root);
        this.lobby = this.readLobby(root);
        this.commands = this.readCommands(root);
        this.joinMessage = this.readJoinMessage(root);
        this.misc = new MiscConfig(root.node("misc", "show-no-permission").getBoolean(true));

        this.validate();
    }

    /** Writes the current configuration back to disk. */
    public void save() throws IOException {
        if (this.loader == null) {
            this.loader = YamlConfigurationLoader.builder().path(this.file).build();
        }
        final ConfigurationNode root = this.loader.load();
        root.node("config-version").set(CONFIG_VERSION);
        this.loader.save(root);
    }

    /** Reloads the configuration from disk, replacing the in-memory state. */
    public void reload() throws IOException {
        this.load();
        this.logger.info("Configuration reloaded.");
    }

    private GeneralConfig readGeneral(final ConfigurationNode root) {
        final ConfigurationNode node = root.node("general");
        return new GeneralConfig(
            node.node("language").getString("en").toLowerCase(Locale.ROOT),
            node.node("debug").getBoolean(false),
            node.node("check-updates").getBoolean(true)
        );
    }

    private LobbyConfig readLobby(final ConfigurationNode root) {
        final ConfigurationNode node = root.node("lobby");

        final Map<String, Integer> priorities = new LinkedHashMap<>();
        final ConfigurationNode priorityNode = node.node("priorities");
        for (final Map.Entry<Object, ? extends ConfigurationNode> entry
            : priorityNode.childrenMap().entrySet()) {
            priorities.put(entry.getKey().toString().toLowerCase(Locale.ROOT),
                entry.getValue().getInt(0));
        }

        final String fallbackServer = node.node("fallback-server").getString("");
        final String fallback = fallbackServer == null ? "" : fallbackServer.trim();

        return new LobbyConfig(
            node.node("teleport-mode").getString("LEAST_PLAYERS"),
            this.readList(node.node("servers")),
            Map.copyOf(priorities),
            node.node("default-priority").getInt(0),
            this.readList(node.node("blocked-servers")),
            fallback,
            this.readCountdown(node.node("countdown")),
            this.readCooldown(node.node("cooldown")),
            this.readOnJoin(node.node("on-join")),
            this.readOnKick(node.node("on-kick"))
        );
    }

    private CountdownConfig readCountdown(final ConfigurationNode node) {
        return new CountdownConfig(
            node.node("enabled").getBoolean(true),
            Math.max(0, node.node("seconds").getInt(3)),
            node.node("cancel-on-command").getBoolean(true),
            node.node("cancel-on-chat").getBoolean(true),
            node.node("sound").getBoolean(true)
        );
    }

    private CooldownConfig readCooldown(final ConfigurationNode node) {
        return new CooldownConfig(
            node.node("enabled").getBoolean(true),
            Math.max(0, node.node("seconds").getInt(5))
        );
    }

    private OnJoinConfig readOnJoin(final ConfigurationNode node) {
        return new OnJoinConfig(
            node.node("enabled").getBoolean(true),
            node.node("only-first-join").getBoolean(false)
        );
    }

    private OnKickConfig readOnKick(final ConfigurationNode node) {
        return new OnKickConfig(
            node.node("enabled").getBoolean(true),
            node.node("exclude-lobbies").getBoolean(false),
            node.node("notify").getBoolean(true)
        );
    }

    private JoinMessageConfig readJoinMessage(final ConfigurationNode root) {
        final ConfigurationNode node = root.node("join-messages");
        return new JoinMessageConfig(
            node.node("title").getBoolean(true),
            node.node("actionbar").getBoolean(true),
            node.node("chat").getBoolean(true),
            Math.max(0, node.node("title-fade-in").getInt(10)),
            Math.max(0, node.node("title-stay").getInt(70)),
            Math.max(0, node.node("title-fade-out").getInt(20))
        );
    }

    private CommandsConfig readCommands(final ConfigurationNode root) {
        final ConfigurationNode node = root.node("commands");
        return new CommandsConfig(
            this.readCommand(node.node("lobby"), "lobby", "lobby.use"),
            this.readCommand(node.node("create"), "lobbycreate", "lobby.admin.create"),
            this.readCommand(node.node("remove"), "lobbyremove", "lobby.admin.remove"),
            this.readCommand(node.node("list"), "lobbylist", "lobby.admin.list"),
            this.readCommand(node.node("transport"), "lobbytransport", "lobby.admin.transport"),
            this.readCommand(node.node("mode"), "lobbymode", "lobby.admin.mode"),
            this.readCommand(node.node("tp"), "lobbytp", "lobby.admin.tp"),
            this.readCommand(node.node("server"), "lobbyserver", "lobby.admin.server"),
            this.readCommand(node.node("reload"), "lobbyreload", "lobby.admin.reload")
        );
    }

    private CommandDefinition readCommand(final ConfigurationNode node, final String defaultName,
        final String defaultPermission) {
        final String name = node.node("name").getString(defaultName);
        final List<String> aliases = this.readList(node.node("aliases"));
        return new CommandDefinition(
            node.node("enabled").getBoolean(true),
            name == null || name.isBlank() ? defaultName : name,
            aliases,
            node.node("permission").getString(defaultPermission)
        );
    }

    private List<String> readList(final ConfigurationNode node) {
        final List<String> values = node.getList(String.class, new ArrayList<>());
        if (values == null) {
            return List.of();
        }
        return List.copyOf(values.stream().filter(value -> value != null && !value.isBlank()).toList());
    }

    private void validate() {
        final List<String> servers = this.lobby.servers();
        if (servers.isEmpty() && this.general.debug()) {
            this.logger.debug("No lobby servers configured yet. Use /lobbycreate to add one.");
        }
    }

    /** Logs a message when debug mode is enabled. */
    public void debug(final String message, final Object... args) {
        if (this.general.debug()) {
            this.logger.debug(message, args);
        }
    }

    private void ensureExtracted() throws IOException {
        if (Files.exists(this.file)) {
            return;
        }
        Files.createDirectories(this.dataDirectory);
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IOException("Bundled resource not found: " + RESOURCE);
            }
            Files.copy(in, this.file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static LobbyConfig defaultLobby() {
        return new LobbyConfig(
            "LEAST_PLAYERS",
            List.of(),
            Map.of(),
            0,
            List.of(),
            "",
            new CountdownConfig(true, 3, true, true, true),
            new CooldownConfig(true, 5),
            new OnJoinConfig(true, false),
            new OnKickConfig(true, false, true)
        );
    }

    private static CommandsConfig defaultCommands() {
        return new CommandsConfig(
            new CommandDefinition(true, "lobby", "lobby.use"),
            new CommandDefinition(true, "lobbycreate", "lobby.admin.create"),
            new CommandDefinition(true, "lobbyremove", "lobby.admin.remove"),
            new CommandDefinition(true, "lobbylist", "lobby.admin.list"),
            new CommandDefinition(true, "lobbytransport", "lobby.admin.transport"),
            new CommandDefinition(true, "lobbymode", "lobby.admin.mode"),
            new CommandDefinition(true, "lobbytp", "lobby.admin.tp"),
            new CommandDefinition(true, "lobbyserver", "lobby.admin.server"),
            new CommandDefinition(true, "lobbyreload", "lobby.admin.reload")
        );
    }

    /** Persists a new set of lobby servers and refreshes the in-memory state. */
    public void setLobbyServers(final java.util.Collection<String> servers) throws IOException {
        final ConfigurationNode root = this.loader.load();
        root.node("lobby", "servers").set(List.class, new ArrayList<>(servers));
        this.loader.save(root);
        this.lobby = this.readLobby(root);
    }

    /** Persists a new teleport mode and refreshes the in-memory state. */
    public void setTeleportMode(final TeleportMode mode) throws IOException {
        final ConfigurationNode root = this.loader.load();
        root.node("lobby", "teleport-mode").set(mode.name());
        this.loader.save(root);
        this.lobby = this.readLobby(root);
    }

    public GeneralConfig general() {
        return this.general;
    }

    public LobbyConfig lobby() {
        return this.lobby;
    }

    public CommandsConfig commands() {
        return this.commands;
    }

    public JoinMessageConfig joinMessage() {
        return this.joinMessage;
    }

    public MiscConfig misc() {
        return this.misc;
    }

    /** Returns the owning plugin instance. */
    public HLobby plugin() {
        return this.plugin;
    }
}
