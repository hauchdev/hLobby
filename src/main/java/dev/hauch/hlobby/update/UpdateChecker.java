package dev.hauch.hlobby.update;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import dev.hauch.hlobby.HLobby;
import dev.hauch.hlobby.message.MessageKey;
import dev.hauch.hlobby.permission.Permission;
import dev.hauch.hlobby.util.Placeholders;
import dev.hauch.hlobby.util.Version;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Checks GitHub for a newer release and notifies operators.
 *
 * <p>The check is performed off the main thread and its result is cached for
 * the lifetime of the proxy.</p>
 */
public final class UpdateChecker {

    private static final String REPOSITORY = "hauchdev/hlobby";
    private static final String LATEST_RELEASE_URL =
        "https://api.github.com/repos/" + REPOSITORY + "/releases/latest";

    private final HLobby plugin;
    private final Logger logger;
    private final HttpClient client;

    private final AtomicReference<String> latestVersion = new AtomicReference<>();
    private final AtomicReference<String> downloadUrl = new AtomicReference<>();
    private final AtomicBoolean updateAvailable = new AtomicBoolean(false);

    public UpdateChecker(final HLobby plugin) {
        this.plugin = plugin;
        this.logger = plugin.logger();
        this.client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    /** Performs the update check asynchronously. */
    public void checkAsync() {
        this.performRequest((version, url, available) -> {
            if (available) {
                this.logger.info("A new version of hLobby is available: {} (current: {}). Download: {}",
                    version, HLobby.PLUGIN_VERSION, url);
                this.notifyOperators();
            } else {
                this.logger.debug("hLobby is up to date (v{}).", HLobby.PLUGIN_VERSION);
            }
        });
    }

    /**
     * Performs an update check now and messages the source with the result.
     *
     * @param source the command source to notify
     */
    public void checkNow(final CommandSource source) {
        this.plugin.messages().send(source, MessageKey.UPDATE_CHECKING);
        this.performRequest((version, url, available) -> this.plugin.proxy().getScheduler()
            .buildTask(this.plugin, () -> {
                if (version == null) {
                    this.plugin.messages().send(source, MessageKey.UPDATE_ERROR);
                    return;
                }
                if (available) {
                    this.plugin.messages().send(source, MessageKey.UPDATE_AVAILABLE_NOW,
                        Placeholders.version(version), Placeholders.url(url));
                } else {
                    this.plugin.messages().send(source, MessageKey.UPDATE_UP_TO_DATE,
                        Placeholders.version(HLobby.PLUGIN_VERSION));
                }
            }).schedule());
    }

    private void performRequest(final UpdateResultCallback callback) {
        final HttpRequest request = HttpRequest.newBuilder(URI.create(LATEST_RELEASE_URL))
            .timeout(Duration.ofSeconds(10))
            .header("User-Agent", "hLobby-update-checker")
            .header("Accept", "application/vnd.github+json")
            .GET()
            .build();

        this.client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenAccept(response -> {
                if (response.statusCode() != 200) {
                    this.logger.debug("Update check returned HTTP {}.", response.statusCode());
                    callback.onResult(null, null, false);
                    return;
                }
                this.parse(response.body(), callback);
            })
            .exceptionally(throwable -> {
                this.logger.debug("Update check failed: {}", throwable.getMessage());
                callback.onResult(null, null, false);
                return null;
            });
    }

    private void parse(final String body, final UpdateResultCallback callback) {
        try {
            final JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            final String tag = json.get("tag_name").getAsString();
            final String version = tag.startsWith("v") ? tag.substring(1) : tag;
            final String url = json.has("html_url") ? json.get("html_url").getAsString() : "";

            this.latestVersion.set(version);
            this.downloadUrl.set(url);

            final boolean available = Version.isNewer(version, HLobby.PLUGIN_VERSION);
            this.updateAvailable.set(available);
            callback.onResult(version, url, available);
        } catch (final RuntimeException exception) {
            this.logger.debug("Failed to parse the update response: {}", exception.getMessage());
            callback.onResult(null, null, false);
        }
    }

    private void notifyOperators() {
        for (final Player player : this.plugin.proxy().getAllPlayers()) {
            if (player.hasPermission(Permission.UPDATE_NOTIFY.getNode())) {
                this.plugin.messages().send(player, MessageKey.UPDATE_AVAILABLE,
                    Placeholders.version(this.latestVersion.get()),
                    Placeholders.url(this.downloadUrl.get()));
            }
        }
    }

    /** Returns whether a newer release was found. */
    public boolean isUpdateAvailable() {
        return this.updateAvailable.get();
    }

    /** Returns the latest published version. */
    public Optional<String> latestVersion() {
        return Optional.ofNullable(this.latestVersion.get());
    }

    /** Returns the download URL of the latest release. */
    public Optional<String> downloadUrl() {
        return Optional.ofNullable(this.downloadUrl.get());
    }

    /** No-op shutdown, retained for API symmetry. */
    public void shutdown() {
        // The HTTP client uses daemon threads and needs no explicit shutdown.
    }

    private interface UpdateResultCallback {
        void onResult(String version, String url, boolean available);
    }
}
