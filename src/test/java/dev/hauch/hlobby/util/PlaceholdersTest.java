package dev.hauch.hlobby.util;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link Placeholders}.
 */
class PlaceholdersTest {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private String plain(final String text, final TagResolver... resolvers) {
        final net.kyori.adventure.text.Component component;
        if (resolvers.length == 0) {
            component = this.miniMessage.deserialize(text);
        } else {
            component = this.miniMessage.deserialize(text, resolvers);
        }
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Test
    @DisplayName("player placeholder is replaced")
    void playerPlaceholder() {
        assertEquals("Hello Steve!", this.plain("Hello <player>!", Placeholders.player("Steve")));
    }

    @Test
    @DisplayName("server placeholder is replaced")
    void serverPlaceholder() {
        assertEquals("Lobby: lobby1", this.plain("Lobby: <server>", Placeholders.server("lobby1")));
    }

    @Test
    @DisplayName("amount placeholder is replaced")
    void amountPlaceholder() {
        assertEquals("Moved 12 players", this.plain("Moved <amount> players", Placeholders.amount(12)));
    }

    @Test
    @DisplayName("seconds placeholder is replaced")
    void secondsPlaceholder() {
        assertEquals("Wait 5 seconds", this.plain("Wait <seconds> seconds", Placeholders.seconds(5)));
    }

    @Test
    @DisplayName("multiple placeholders are replaced")
    void multiplePlaceholders() {
        assertEquals("Steve -> lobby1",
            this.plain("<player> -> <server>", Placeholders.player("Steve"), Placeholders.server("lobby1")));
    }
}
