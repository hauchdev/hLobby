package dev.hauch.hlobby.update;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocitypowered.api.proxy.Player;
import dev.hauch.hlobby.HLobby;
import dev.hauch.hlobby.message.MessageKey;
import dev.hauch.hlobby.permission.Permission;
import dev.hauch.hlobby.util.Placeholders;
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
                    return;
                }
                this.parse(response.body());
            })
            .exceptionally(throwable -> {
                this.logger.debug("Update check failed: {}", throwable.getMessage());
                return null;
            });
    }

    private void parse(final String body) {
        try {
            final JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            final String tag = json.get("tag_name").getAsString();
            final String version = tag.startsWith("v") ? tag.substring(1) : tag;
            final String url = json.has("html_url") ? json.get("html_url").getAsString() : "";

            this.latestVersion.set(version);
            this.downloadUrl.set(url);

            if (this.isNewer(version, HLobby.PLUGIN_VERSION)) {
                this.updateAvailable.set(true);
                this.logger.info("A new version of hLobby is available: {} (current: {}). Download: {}",
                    version, HLobby.PLUGIN_VERSION, url);
                this.notifyOperators();
            } else {
                this.logger.debug("hLobby is up to date (v{}).", HLobby.PLUGIN_VERSION);
            }
        } catch (final RuntimeException exception) {
            this.logger.debug("Failed to parse the update response: {}", exception.getMessage());
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

    private boolean isNewer(final String candidate, final String current) {
        final String[] candidateParts = candidate.split("[.\\-]");
        final String[] currentParts = current.split("[.\\-]");
        final int length = Math.max(candidateParts.length, currentParts.length);
        for (int i = 0; i < length; i++) {
            final int candidateValue = i < candidateParts.length ? numeric(candidateParts[i]) : 0;
            final int currentValue = i < currentParts.length ? numeric(currentParts[i]) : 0;
            if (candidateValue != currentValue) {
                return candidateValue > currentValue;
            }
        }
        return false;
    }

    private int numeric(final String value) {
        final String digits = value.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(digits);
        } catch (final NumberFormatException exception) {
            return 0;
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
}
