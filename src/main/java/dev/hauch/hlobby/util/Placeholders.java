package dev.hauch.hlobby.util;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * Small helpers to build MiniMessage tag resolvers for the placeholders used
 * across the plugin messages.
 */
public final class Placeholders {

    private Placeholders() {
    }

    /** Resolves {@code %player%}. */
    public static TagResolver player(final String value) {
        return Placeholder.parsed("player", value);
    }

    /** Resolves {@code %target%}. */
    public static TagResolver target(final String value) {
        return Placeholder.parsed("target", value);
    }

    /** Resolves {@code %server%}. */
    public static TagResolver server(final String value) {
        return Placeholder.parsed("server", value);
    }

    /** Resolves {@code %from%}. */
    public static TagResolver from(final String value) {
        return Placeholder.parsed("from", value);
    }

    /** Resolves {@code %to%}. */
    public static TagResolver to(final String value) {
        return Placeholder.parsed("to", value);
    }

    /** Resolves {@code %amount%}. */
    public static TagResolver amount(final int value) {
        return Placeholder.parsed("amount", String.valueOf(value));
    }

    /** Resolves {@code %seconds%}. */
    public static TagResolver seconds(final int value) {
        return Placeholder.parsed("seconds", String.valueOf(value));
    }

    /** Resolves {@code %time%}. */
    public static TagResolver time(final int value) {
        return Placeholder.parsed("time", String.valueOf(value));
    }

    /** Resolves {@code %mode%}. */
    public static TagResolver mode(final String value) {
        return Placeholder.parsed("mode", value);
    }

    /** Resolves {@code %version%}. */
    public static TagResolver version(final String value) {
        return Placeholder.parsed("version", value);
    }

    /** Resolves {@code %url%}. */
    public static TagResolver url(final String value) {
        return Placeholder.parsed("url", value);
    }

    /** Resolves {@code %online%}. */
    public static TagResolver online(final int value) {
        return Placeholder.parsed("online", String.valueOf(value));
    }

    /** Resolves {@code %max%}. */
    public static TagResolver max(final int value) {
        return Placeholder.parsed("max", String.valueOf(value));
    }

    /** Resolves {@code %index%}. */
    public static TagResolver index(final int value) {
        return Placeholder.parsed("index", String.valueOf(value));
    }

    /** Resolves {@code %remaining%}. */
    public static TagResolver remaining(final int value) {
        return Placeholder.parsed("remaining", String.valueOf(value));
    }

    /** Resolves {@code %usage%}. */
    public static TagResolver usage(final String value) {
        return Placeholder.parsed("usage", value);
    }

    /** Resolves {@code %modes%}. */
    public static TagResolver modes(final String value) {
        return Placeholder.parsed("modes", value);
    }

    /** Resolves {@code %servers%}. */
    public static TagResolver servers(final String value) {
        return Placeholder.parsed("servers", value);
    }

    /** Resolves {@code %command%}. */
    public static TagResolver command(final String value) {
        return Placeholder.parsed("command", value);
    }
}
