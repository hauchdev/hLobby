package dev.hauch.hlobby.lobby.selector;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.hauch.hlobby.lobby.LobbyStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link LobbySelector} implementations.
 */
class LobbySelectorTest {

    private RegisteredServer server(final String name) {
        final RegisteredServer server = mock(RegisteredServer.class);
        final ServerInfo info = mock(ServerInfo.class);
        when(server.getServerInfo()).thenReturn(info);
        when(info.getName()).thenReturn(name);
        return server;
    }

    private LobbyStats stats(final RegisteredServer server, final int online) {
        final LobbyStats stats = mock(LobbyStats.class);
        when(stats.online(server)).thenReturn(online);
        when(stats.max(server)).thenReturn(-1);
        return stats;
    }

    private SelectionContext context(final LobbyStats stats) {
        return new SelectionContext(Map.of(), 0, stats);
    }

    @Test
    @DisplayName("least players selector picks the emptiest lobby")
    void leastPlayersPicksEmptiest() {
        final RegisteredServer a = server("a");
        final RegisteredServer b = server("b");
        final RegisteredServer c = server("c");

        final LobbyStats stats = mock(LobbyStats.class);
        when(stats.online(a)).thenReturn(5);
        when(stats.online(b)).thenReturn(1);
        when(stats.online(c)).thenReturn(3);

        final RegisteredServer selected = new LeastPlayersSelector()
            .select(null, List.of(a, b, c), context(stats));
        assertSame(b, selected);
    }

    @Test
    @DisplayName("most players selector picks the busiest lobby")
    void mostPlayersPicksBusiest() {
        final RegisteredServer a = server("a");
        final RegisteredServer b = server("b");

        final LobbyStats stats = mock(LobbyStats.class);
        when(stats.online(a)).thenReturn(2);
        when(stats.online(b)).thenReturn(9);

        final RegisteredServer selected = new MostPlayersSelector()
            .select(null, List.of(a, b), context(stats));
        assertSame(b, selected);
    }

    @Test
    @DisplayName("first available selector picks the first non-full lobby")
    void firstAvailablePicksFirst() {
        final RegisteredServer a = server("a");
        final RegisteredServer b = server("b");

        final LobbyStats stats = mock(LobbyStats.class);
        when(stats.isFull(a)).thenReturn(true);
        when(stats.isFull(b)).thenReturn(false);

        final RegisteredServer selected = new FirstAvailableSelector()
            .select(null, List.of(a, b), context(stats));
        assertSame(b, selected);
    }

    @Test
    @DisplayName("priority selector honours configured priorities")
    void priorityHonoursConfiguration() {
        final RegisteredServer a = server("a");
        final RegisteredServer b = server("b");
        final RegisteredServer c = server("c");

        final SelectionContext context = new SelectionContext(Map.of("b", 100), 0, mock(LobbyStats.class));
        final RegisteredServer selected = new PrioritySelector()
            .select(null, List.of(a, b, c), context);
        assertSame(b, selected);
    }

    @Test
    @DisplayName("round robin selector rotates through lobbies")
    void roundRobinRotates() {
        final RegisteredServer a = server("a");
        final RegisteredServer b = server("b");
        final RoundRobinSelector selector = new RoundRobinSelector();

        final RegisteredServer first = selector.select(null, List.of(a, b), context(mock(LobbyStats.class)));
        final RegisteredServer second = selector.select(null, List.of(a, b), context(mock(LobbyStats.class)));
        final RegisteredServer third = selector.select(null, List.of(a, b), context(mock(LobbyStats.class)));

        assertSame(a, first);
        assertSame(b, second);
        assertSame(a, third);
    }

    @Test
    @DisplayName("random selector returns a configured lobby")
    void randomReturnsCandidate() {
        final RegisteredServer a = server("a");
        final RegisteredServer b = server("b");
        final RegisteredServer selected = new RandomSelector()
            .select(null, List.of(a, b), context(mock(LobbyStats.class)));
        assertNotNull(selected);
    }

    @Test
    @DisplayName("selectors return null for empty candidate lists")
    void emptyCandidatesReturnNull() {
        final SelectionContext context = context(mock(LobbyStats.class));
        assertNull(new RandomSelector().select(null, List.of(), context));
        assertNull(new LeastPlayersSelector().select(null, List.of(), context));
        assertNull(new MostPlayersSelector().select(null, List.of(), context));
        assertNull(new FirstAvailableSelector().select(null, List.of(), context));
        assertNull(new PrioritySelector().select(null, List.of(), context));
        assertNull(new RoundRobinSelector().select(null, List.of(), context));
    }

    @Test
    @DisplayName("selector factory maps every mode")
    void factoryMapsEveryMode() {
        final SelectorFactory factory = new SelectorFactory();
        for (final dev.hauch.hlobby.lobby.TeleportMode mode : dev.hauch.hlobby.lobby.TeleportMode.values()) {
            assertNotNull(factory.selector(mode));
        }
        assertEquals(factory.selector(dev.hauch.hlobby.lobby.TeleportMode.LEAST_PLAYERS),
            factory.selectorFor("least_players"));
    }
}
