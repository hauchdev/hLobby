package dev.hauch.hlobby.teleport;

import dev.hauch.hlobby.HLobby;
import dev.hauch.hlobby.message.MessageKey;
import dev.hauch.hlobby.util.Placeholders;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A per-player countdown that teleports the player when it reaches zero.
 *
 * <p>The countdown is shown through the waiting message and can be cancelled
 * by the player running a command or sending a chat message.</p>
 */
public final class Countdown {

    private final HLobby plugin;
    private final Player player;
    private final RegisteredServer target;
    private final int total;
    private final boolean sound;
    private final Runnable onComplete;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private final ScheduledTask task;
    private int remaining;

    public Countdown(final HLobby plugin, final Player player, final RegisteredServer target,
        final int seconds, final boolean sound, final Runnable onComplete) {
        this.plugin = plugin;
        this.player = player;
        this.target = target;
        this.total = Math.max(1, seconds);
        this.remaining = this.total;
        this.sound = sound;
        this.onComplete = onComplete;
        this.task = plugin.proxy().getScheduler()
            .buildTask(plugin.container(), this::tick)
            .repeat(Duration.ofSeconds(1))
            .schedule();
        this.announce();
    }

    /** Returns the player waiting for this countdown. */
    public Player player() {
        return this.player;
    }

    /** Returns the server the player will be sent to. */
    public RegisteredServer target() {
        return this.target;
    }

    /** Returns the number of seconds remaining. */
    public int remaining() {
        return this.remaining;
    }

    /** Returns whether this countdown was cancelled. */
    public boolean isCancelled() {
        return this.cancelled.get();
    }

    /** Cancels the countdown without teleporting. */
    public void cancel() {
        if (!this.cancelled.compareAndSet(false, true)) {
            return;
        }
        this.finished.set(true);
        this.task.cancel();
    }

    private void tick() {
        if (this.cancelled.get() || this.finished.get()) {
            return;
        }
        this.remaining--;
        if (this.remaining <= 0) {
            this.complete();
            return;
        }
        this.announce();
    }

    private void complete() {
        if (!this.finished.compareAndSet(false, true)) {
            return;
        }
        this.cancelled.set(true);
        this.task.cancel();
        this.onComplete.run();
    }

    private void announce() {
        this.plugin.messages().send(this.player, MessageKey.LOBBY_WAITING,
            Placeholders.seconds(this.remaining));
        this.plugin.messages().actionbar(this.player, MessageKey.LOBBY_WAITING,
            Placeholders.seconds(this.remaining));
        if (this.sound) {
            this.playSound();
        }
    }

    private void playSound() {
        this.player.playSound(Sound.sound(
            Key.key("minecraft:block.note_block.pling"),
            Sound.Source.MASTER, 1.0f, 1.0f));
    }
}
