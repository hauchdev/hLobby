package dev.hauch.hlobby.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import dev.hauch.hlobby.HLobby;
import dev.hauch.hlobby.config.CommandDefinition;
import dev.hauch.hlobby.message.MessageKey;
import dev.hauch.hlobby.util.Placeholders;

import java.io.IOException;

/**
 * Root administrative command for hLobby.
 *
 * <p>Subcommands:</p>
 * <ul>
 *   <li>{@code /hlobby reload} – reloads configuration, messages and lobbies</li>
 *   <li>{@code /hlobby update} – checks GitHub for a newer release</li>
 *   <li>{@code /hlobby about} – shows version, metrics and website</li>
 *   <li>{@code /hlobby help} – shows this help</li>
 * </ul>
 */
public final class HlobbyCommand extends BaseCommand {

    public HlobbyCommand(final HLobby plugin, final CommandDefinition definition) {
        super(plugin, definition);
    }

    /** {@inheritDoc} */
    @Override
    public LiteralArgumentBuilder<CommandSource> builder() {
        return this.literal(this.name())
            .executes(context -> {
                this.sendHelp(context.getSource());
                return 1;
            })
            .then(this.literal("help").executes(context -> {
                this.sendHelp(context.getSource());
                return 1;
            }))
            .then(this.literal("reload").executes(context -> {
                if (!this.checkPermission(context.getSource())) {
                    return 0;
                }
                this.reload(context.getSource());
                return 1;
            }))
            .then(this.literal("update").executes(context -> {
                if (!this.checkPermission(context.getSource())) {
                    return 0;
                }
                if (this.plugin.updateChecker() == null) {
                    this.plugin.messages().send(context.getSource(), MessageKey.UPDATE_ERROR);
                    return 1;
                }
                this.plugin.updateChecker().checkNow(context.getSource());
                return 1;
            }))
            .then(this.literal("about").executes(context -> {
                this.sendAbout(context.getSource());
                return 1;
            }));
    }

    private void sendHelp(final CommandSource source) {
        this.plugin.messages().send(source, MessageKey.HLOBBY_HELP_HEADER);
        this.plugin.messages().send(source, MessageKey.HLOBBY_HELP_RELOAD);
        this.plugin.messages().send(source, MessageKey.HLOBBY_HELP_UPDATE);
        this.plugin.messages().send(source, MessageKey.HLOBBY_HELP_ABOUT);
    }

    private void reload(final CommandSource source) {
        try {
            this.plugin.config().reload();
            this.plugin.messages().reload();
            this.plugin.lobbyManager().reload();
            this.plugin.messages().send(source, MessageKey.RELOAD_SUCCESS);
        } catch (final IOException exception) {
            this.plugin.logger().error("Failed to reload the configuration.", exception);
            this.plugin.messages().send(source, MessageKey.RELOAD_ERROR);
        }
    }

    private void sendAbout(final CommandSource source) {
        this.plugin.messages().send(source, MessageKey.HLOBBY_ABOUT_VERSION,
            Placeholders.version(HLobby.PLUGIN_VERSION));
        if (this.plugin.metrics() != null && this.plugin.metrics().isEnabled()) {
            this.plugin.messages().send(source, MessageKey.HLOBBY_ABOUT_METRICS);
        } else {
            this.plugin.messages().send(source, MessageKey.HLOBBY_ABOUT_METRICS_DISABLED);
        }
        this.plugin.messages().send(source, MessageKey.HLOBBY_ABOUT_WEBSITE,
            Placeholders.url(HLobby.PLUGIN_WEBSITE));
    }
}
