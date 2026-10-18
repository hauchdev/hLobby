package dev.hauch.hlobby.message;

import dev.hauch.hlobby.HLobby;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

/**
 * Loads localized messages and turns them into Adventure components.
 *
 * <p>Messages are MiniMessage formatted and support the placeholders built by
 * {@link dev.hauch.hlobby.util.Placeholders}. Language files live in the
 * {@code lang} directory of the plugin data folder and are extracted from the
 * jar on first use.</p>
 */
public final class MessageManager {

    private static final String DIRECTORY = "lang";
    private static final String FALLBACK_LANGUAGE = "en";

    private final HLobby plugin;
    private final Logger logger;
    private final Path languageDirectory;
    private final Map<MessageKey, String> messages = new EnumMap<>(MessageKey.class);

    private String language = FALLBACK_LANGUAGE;
    private ConfigurationLoader<?> loader;
    private ConfigurationNode root;

    public MessageManager(final HLobby plugin) {
        this.plugin = plugin;
        this.logger = plugin.logger();
        this.languageDirectory = plugin.dataDirectory().resolve(DIRECTORY);
    }

    /** Returns the currently active language code. */
    public String language() {
        return this.language;
    }

    /** Returns the directory containing the language files. */
    public Path languageDirectory() {
        return this.languageDirectory;
    }

    /** Loads the configured language file, falling back to English. */
    public void load() throws IOException {
        this.language = this.plugin.config().general().language();
        this.ensureExtracted(this.language);

        Path file = this.languageDirectory.resolve(this.language + ".yml");
        if (!Files.exists(file)) {
            this.logger.warn("Language file '{}' not found, falling back to '{}'.",
                this.language, FALLBACK_LANGUAGE);
            this.language = FALLBACK_LANGUAGE;
            this.ensureExtracted(this.language);
            file = this.languageDirectory.resolve(this.language + ".yml");
        }

        this.loader = YamlConfigurationLoader.builder().path(file).build();
        this.root = this.loader.load();
        this.messages.clear();

        for (final MessageKey key : MessageKey.values()) {
            final String value = this.readNode(key);
            this.messages.put(key, value == null ? key.key() : value);
        }

        this.logger.debug("Loaded {} messages for language '{}'.", this.messages.size(), this.language);
    }

    /** Reloads the language file from disk. */
    public void reload() throws IOException {
        this.load();
        this.logger.info("Messages reloaded.");
    }

    /** Returns the raw MiniMessage string for a key. */
    public String raw(final MessageKey key) {
        return this.messages.getOrDefault(key, key.key());
    }

    /** Returns a deserialized component for a key, without the prefix. */
    public Component component(final MessageKey key, final TagResolver... resolvers) {
        return MiniMessage.miniMessage().deserialize(this.raw(key), resolvers);
    }

    /** Returns a deserialized component for a key, including the prefix. */
    public Component prefixed(final MessageKey key, final TagResolver... resolvers) {
        return MiniMessage.miniMessage().deserialize(this.raw(MessageKey.PREFIX) + this.raw(key), resolvers);
    }

    /** Sends a prefixed message, ignoring blank messages. */
    public void send(final Audience audience, final MessageKey key, final TagResolver... resolvers) {
        final String text = this.raw(key);
        if (text == null || text.isBlank()) {
            return;
        }
        audience.sendMessage(MiniMessage.miniMessage()
            .deserialize(this.raw(MessageKey.PREFIX) + text, resolvers));
    }

    /** Sends an arbitrary MiniMessage string, ignoring blank messages. */
    public void sendRaw(final Audience audience, final String text, final TagResolver... resolvers) {
        if (text == null || text.isBlank()) {
            return;
        }
        audience.sendMessage(MiniMessage.miniMessage().deserialize(text, resolvers));
    }

    /** Sends an action bar message, ignoring blank messages. */
    public void actionbar(final Audience audience, final MessageKey key, final TagResolver... resolvers) {
        final String text = this.raw(key);
        if (text == null || text.isBlank()) {
            return;
        }
        audience.sendActionBar(MiniMessage.miniMessage().deserialize(text, resolvers));
    }

    /** Sends a title and subtitle with the given timings. */
    public void title(final Audience audience, final MessageKey titleKey, final MessageKey subtitleKey,
        final int fadeInTicks, final int stayTicks, final int fadeOutTicks,
        final TagResolver... resolvers) {
        final Component title = this.component(titleKey, resolvers);
        final Component subtitle = this.component(subtitleKey, resolvers);
        final Title.Times times = Title.Times.times(
            Duration.ofMillis(fadeInTicks * 50L),
            Duration.ofMillis(stayTicks * 50L),
            Duration.ofMillis(fadeOutTicks * 50L));
        audience.showTitle(Title.title(title, subtitle, times));
    }

    private String readNode(final MessageKey key) {
        ConfigurationNode node = this.root;
        for (final String part : key.key().split("\\.")) {
            node = node.node(part);
        }
        return node.getString();
    }

    private void ensureExtracted(final String language) throws IOException {
        final Path target = this.languageDirectory.resolve(language + ".yml");
        if (Files.exists(target)) {
            return;
        }
        Files.createDirectories(this.languageDirectory);
        try (InputStream in = this.getClass().getClassLoader()
            .getResourceAsStream(DIRECTORY + "/" + language + ".yml")) {
            if (in == null) {
                throw new IOException("Bundled language file not found: " + DIRECTORY + "/" + language + ".yml");
            }
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
