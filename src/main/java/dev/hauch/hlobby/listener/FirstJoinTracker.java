package dev.hauch.hlobby.listener;

import dev.hauch.hlobby.HLobby;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

/**
 * Persists which players have already joined the proxy, so that the
 * {@code only-first-join} option can be honoured across restarts.
 */
public final class FirstJoinTracker {

    private static final String FILE_NAME = "first-join.yml";
    private static final int MAX_ENTRIES = 5000;

    private final HLobby plugin;
    private final Path file;
    private final Set<UUID> known = ConcurrentHashMap.newKeySet();

    public FirstJoinTracker(final HLobby plugin) {
        this.plugin = plugin;
        this.file = plugin.dataDirectory().resolve(FILE_NAME);
        this.load();
    }

    /**
     * Marks a player as having joined.
     *
     * @param uniqueId the player id
     * @return {@code true} when this is their first recorded join
     */
    public boolean markJoined(final UUID uniqueId) {
        if (this.known.contains(uniqueId)) {
            return false;
        }
        this.known.add(uniqueId);
        this.save();
        return true;
    }

    /** Returns the number of recorded players. */
    public int size() {
        return this.known.size();
    }

    private void load() {
        if (!Files.exists(this.file)) {
            return;
        }
        try {
            final ConfigurationLoader<?> loader = YamlConfigurationLoader.builder()
                .path(this.file).build();
            final ConfigurationNode root = loader.load();
            final List<String> ids = root.node("players").getList(String.class, List.of());
            for (final String id : ids) {
                try {
                    this.known.add(UUID.fromString(id));
                } catch (final IllegalArgumentException ignored) {
                    // Ignore malformed entries.
                }
            }
        } catch (final IOException exception) {
            this.plugin.logger().warn("Failed to load first-join data.", exception);
        }
    }

    private void save() {
        try {
            Files.createDirectories(this.file.getParent());
            final ConfigurationLoader<?> loader = YamlConfigurationLoader.builder()
                .path(this.file).build();
            final ConfigurationNode root = loader.load();
            final List<String> ids = this.known.stream()
                .map(UUID::toString)
                .limit(MAX_ENTRIES)
                .toList();
            root.node("players").set(List.class, ids);
            loader.save(root);
        } catch (final IOException exception) {
            this.plugin.logger().warn("Failed to save first-join data.", exception);
        }
    }
}
