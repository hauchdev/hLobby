# hLobby

A modern, highly configurable lobby management system for [Velocity](https://papermc.io/software/velocity)
proxies.

hLobby handles the routing between your game servers and lobby servers. It can send players to
the best lobby with pluggable selection strategies, show a teleport countdown, redirect kicked
players back to a lobby and much more.

## Features

- **Multiple selection strategies**: random, least players, most players, first available,
  priority and round-robin.
- **Configurable countdown** before teleporting to a lobby, cancellable by commands or chat.
- **Per-player cooldown** for the `/lobby` command with bypass permissions.
- **Automatic join redirection** to the best lobby.
- **Kick redirection** to a lobby instead of disconnecting the player.
- **Blocked servers** where `/lobby` cannot be used.
- **Fallback server** for when no lobby is available.
- **Full command customization**: rename commands, add aliases or disable them entirely.
- **Internationalization**: bundled English and Spanish translations, easy to add more.
- **Server statistics cache** for accurate player counts without blocking the proxy thread.
- **bStats metrics** support (requires a service id).
- **Update checker** that notifies operators on join.
- **Small public API** for other plugins.

## Requirements

- Velocity **3.4.0+** (hLobby is developed against the 3.4.0-SNAPSHOT API and runs on Velocity 4.x).
- Java **21** or newer.

## Installation

1. Download the latest `hLobby-<version>.jar` from the [releases page](https://github.com/hauchdev/hlobby/releases).
2. Drop the jar into your Velocity `plugins/` directory.
3. Start the proxy. Default configuration and language files will be extracted to `plugins/hLobby/`.
4. Edit `plugins/hLobby/config.yml` and add your lobby servers.

## Building from source

```bash
./gradlew build
```

The distributable jar is `build/libs/hLobby-<version>.jar`.

## Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/lobby` | `lobby.use` | Sends you to the best lobby. |
| `/lobbycreate <server>` | `lobby.admin.create` | Registers a server as a lobby. |
| `/lobbyremove <server>` | `lobby.admin.remove` | Removes a server from the lobby list. |
| `/lobbylist` | `lobby.admin.list` | Lists configured lobbies and their player counts. |
| `/lobbytransport <from> <to>` | `lobby.admin.transport` | Moves every player from one lobby to another. |
| `/lobbymode <mode>` | `lobby.admin.mode` | Changes the teleport strategy. |
| `/lobbytp <player>` | `lobby.admin.tp` | Sends another player to the best lobby. |
| `/lobbyserver <server>` | `lobby.admin.server` | Join a specific lobby. |
| `/lobbyreload` | `lobby.admin.reload` | Reloads the configuration and messages. |

All command names, aliases and permissions can be changed in `config.yml`.

## Permissions

| Permission | Description |
| --- | --- |
| `lobby.use` | Use `/lobby`. |
| `lobby.admin.create` | Add servers to the lobby list. |
| `lobby.admin.remove` | Remove servers from the lobby list. |
| `lobby.admin.list` | List lobbies. |
| `lobby.admin.transport` | Transport players between lobbies. |
| `lobby.admin.mode` | Change the teleport mode. |
| `lobby.admin.tp` | Teleport another player to a lobby. |
| `lobby.admin.server` | Join a specific lobby. |
| `lobby.admin.reload` | Reload configuration and messages. |
| `lobby.admin.*` | All administrative permissions. |
| `lobby.*` | All permissions. |
| `lobby.bypass.cooldown` | Ignore the `/lobby` cooldown. |
| `lobby.bypass.countdown` | Skip the teleport countdown. |
| `lobby.bypass.blocked` | Use `/lobby` on blocked servers. |
| `lobby.update.notify` | Be notified when an update is available. |

## Configuration

A full reference of every option is available in [`docs/CONFIGURATION.md`](docs/CONFIGURATION.md).

Key sections:

- `general`: language, debug mode, update checks.
- `lobby`: teleport mode, server list, priorities, countdown, cooldown, on-join/on-kick behaviour.
- `join-messages`: title, action bar and chat shown when entering a lobby.
- `commands`: rename, alias or disable every command.

## Languages

hLobby uses [MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatted messages.
Language files live in `plugins/hLobby/lang/` and are extracted from the jar on first use.

- `en` — English (default).
- `es` — Spanish.

Set `general.language` in `config.yml` to the file name without extension.

## Metrics

hLobby supports anonymous usage metrics through [bStats](https://bstats.org/). To enable it,
replace `MetricsWrapper.SERVICE_ID` with the id assigned by bStats and rebuild the jar.
Players can opt-out through `plugins/bStats/config.txt`.

## Update checker

On startup hLobby queries GitHub for the latest release. Operators with the `lobby.update.notify`
permission are notified when they join. Disable this with `general.check-updates: false`.

## API

Other plugins can interact with hLobby through the `HLobbyProvider` interface:

```java
HLobbyProvider api = HLobbyProvider.instance();
if (api != null) {
    api.lobbyManager().selectLobby(player).ifPresent(lobby -> {
        api.teleportService().teleport(player, lobby);
    });
}
```

## Contributing

Contributions are welcome. Please read [`CONTRIBUTING.md`](CONTRIBUTING.md) before opening a pull
request.

## License

hLobby is released under the [MIT License](LICENSE).

## Support

For questions or bug reports, please open an issue on
[GitHub](https://github.com/hauchdev/hlobby/issues).
