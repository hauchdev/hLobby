# hLobby — Modern lobby management for Velocity

<p align="center">
  A highly configurable, proxy-side lobby system for Velocity with smart routing,
  teleport countdowns, kick redirection, multi-language messages and more.
</p>

<p align="center">
  <a href="https://github.com/hauchdev/hlobby/actions/workflows/build.yml"><img src="https://github.com/hauchdev/hlobby/actions/workflows/build.yml/badge.svg" alt="Build"></a>
  <a href="https://github.com/hauchdev/hlobby/releases/latest"><img src="https://img.shields.io/github/v/release/hauchdev/hlobby?logo=github&color=blue" alt="Release"></a>
  <a href="LICENSE"><img src="https://img.shields.io/github/license/hauchdev/hlobby?color=green" alt="License"></a>
  <img src="https://img.shields.io/badge/Velocity-3.4.0+-00A3E0?logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxIDEiPjwvc3ZnPg==" alt="Velocity">
  <img src="https://img.shields.io/badge/Java-21+-orange?logo=openjdk" alt="Java">
</p>

<p align="center">
  <a href="#-features">Features</a> •
  <a href="#%EF%B8%8F-quick-start">Quick Start</a> •
  <a href="#-commands">Commands</a> •
  <a href="#-permissions">Permissions</a> •
  <a href="#%EF%B8%8F-configuration">Configuration</a> •
  <a href="#-metrics">Metrics</a> •
  <a href="#-update-checker">Updates</a> •
  <a href="#-api">API</a>
</p>

---

## 📋 Table of Contents

- [✨ Features](#-features)
- [⚡ Quick Start](#%EF%B8%8F-quick-start)
- [🎮 Commands](#-commands)
- [🔒 Permissions](#-permissions)
- [⚙️ Configuration](#%EF%B8%8F-configuration)
- [🌐 Localization](#-localization)
- [📊 Metrics](#-metrics)
- [🔄 Update Checker](#-update-checker)
- [🔌 Developer API](#-developer-api)
- [🛠️ Building from Source](#%EF%B8%8F-building-from-source)
- [🤝 Contributing](#-contributing)
- [📄 License](#-license)

## ✨ Features

| | | |
|---|---|---|
| 🎯 **Smart lobby selection** | Random, least/most players, first available, priority and round-robin strategies | |
| ⏱️ **Teleport countdown** | Configurable delay with cancel-on-move, cancel-on-chat and bypass permission | |
| 🔄 **Per-player cooldown** | Stop spam with a configurable `/lobby` cooldown | |
| 🚪 **Join redirection** | Send players straight to the best lobby on connect | |
| ⚡ **Kick redirection** | Redirect kicked players to a lobby instead of disconnecting them | |
| 🚫 **Blocked servers** | Disable `/lobby` on specific downstream servers | |
| 🆘 **Fallback server** | Join a backup server when no lobby is available | |
| 🏷️ **Join messages** | Title, action bar and chat greeting on lobby join | |
| ⚙️ **Command customization** | Rename, alias or disable every command from `config.yml` | |
| 🌐 **Multi-language** | English and Spanish included, easy to add more | |
| 📊 **FastStats metrics** | Anonymous usage statistics via faststats.dev, opt-out in config | |
| 🔄 **GitHub update checker** | Notifies operators when a new release is available | |
| 🔌 **Public API** | Small, stable API for third-party plugins | |

## ⚡ Quick Start

### 📥 Installation

```bash
# 1. Download the latest JAR from Releases
wget https://github.com/hauchdev/hlobby/releases/latest/download/hLobby-1.0.0.jar

# 2. Drop it into your Velocity plugins folder
mv hLobby-1.0.0.jar /your-velocity-proxy/plugins/

# 3. Start the proxy
# hLobby generates plugins/hLobby/config.yml and plugins/hLobby/lang/*.yml on first start.

# 4. Edit config.yml, add your lobby servers, then run /hlobby reload
```

## 🎮 Commands

All lobby management commands are configurable in `config.yml` (names, aliases and permissions).

| Command | Aliases | Description | Permission |
|---|---|---|---|
| `/lobby` | `hub`, `leave` | Send yourself to the best lobby | `lobby.use` |
| `/lobbycreate <server>` | `create` | Register a backend server as a lobby | `lobby.admin.create` |
| `/lobbyremove <server>` | `remove` | Remove a server from the lobby list | `lobby.admin.remove` |
| `/lobbylist` | `list` | List configured lobbies and player counts | `lobby.admin.list` |
| `/lobbytransport <from> <to>` | `transport` | Move every player from one lobby to another | `lobby.admin.transport` |
| `/lobbymode <mode>` | `mode` | Change the teleport strategy | `lobby.admin.mode` |
| `/lobbytp <player>` | `tp` | Teleport another player to a lobby | `lobby.admin.tp` |
| `/lobbyserver <server>` | `server`, `joins` | Join a specific lobby | `lobby.admin.server` |
| `/lobbyreload` | `hlobbyreload` | Reload config, messages and lobbies | `lobby.admin.reload` |
| `/hlobby reload` | — | Reload config, messages and lobbies | `lobby.admin.main` |
| `/hlobby update` | — | Check GitHub for updates manually | `lobby.admin.main` |
| `/hlobby about` | — | Show version, metrics status and website | `lobby.admin.main` |
| `/hlobby help` | — | Show the admin help menu | `lobby.admin.main` |

## 🔒 Permissions

| Permission | Default | Description |
|---|---|---|
| `lobby.use` | true | Use `/lobby` |
| `lobby.admin.create` | op | Add servers to the lobby list |
| `lobby.admin.remove` | op | Remove servers from the lobby list |
| `lobby.admin.list` | op | List configured lobbies |
| `lobby.admin.transport` | op | Move players between lobbies |
| `lobby.admin.mode` | op | Change the teleport mode |
| `lobby.admin.tp` | op | Teleport another player to a lobby |
| `lobby.admin.server` | op | Join a specific lobby |
| `lobby.admin.reload` | op | Reload configuration and messages |
| `lobby.admin.main` | op | Use `/hlobby` admin commands |
| `lobby.admin.*` | op | All administrative permissions |
| `lobby.bypass.cooldown` | op | Ignore the `/lobby` cooldown |
| `lobby.bypass.countdown` | op | Skip the teleport countdown |
| `lobby.bypass.blocked` | op | Use `/lobby` on blocked servers |
| `lobby.update.notify` | op | Be notified when an update is available |
| `lobby.*` | op | All permissions |

## ⚙️ Configuration

`config.yml` is generated automatically on first start. A full reference is available in [`docs/CONFIGURATION.md`](docs/CONFIGURATION.md).

Key sections:

- `general` — language, debug mode, update checks.
- `lobby` — teleport mode, server list, priorities, countdown, cooldown, on-join/on-kick behaviour.
- `join-messages` — title, action bar and chat shown when entering a lobby.
- `commands` — rename, alias or disable every command.
- `metrics` — enable/disable FastStats anonymous metrics.

Example:

```yaml
general:
  language: en
  debug: false
  check-updates: true

lobby:
  teleport-mode: LEAST_PLAYERS
  servers:
    - lobby-1
    - lobby-2
  fallback-server: fallback

metrics:
  enabled: true
```

## 🌐 Localization

Messages use [MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatting. Language files live in `plugins/hLobby/lang/` and are extracted from the jar on first use.

| Language | File |
|---|---|
| English | `en.yml` (default) |
| Spanish | `es.yml` |

Set `general.language` in `config.yml` to the file name without extension. Adding a new language is as simple as dropping a new `<code>.yml` file into `plugins/hLobby/lang/`.

## 📊 Metrics

hLobby bundles [FastStats](https://faststats.dev/) for anonymous plugin statistics — no extra download needed. Only aggregated usage counters are submitted; no player names, IP addresses or chat data is ever collected.

Server owners can opt-out at any time:

```yaml
metrics:
  enabled: false
```

Plugin authors: replace `MetricsWrapper.FASTSTATS_TOKEN` with your project token before publishing a release.

## 🔄 Update Checker

On startup hLobby queries the [GitHub Releases API](https://api.github.com/repos/hauchdev/hlobby/releases/latest) for a newer version. If one is found, every online operator with `lobby.update.notify` receives a chat message with the new version and a direct download link.

You can also trigger a manual check with `/hlobby update`. Disable automatic checks with:

```yaml
general:
  check-updates: false
```

## 🔌 Developer API

Other plugins can interact with hLobby through the `HLobbyProvider` interface:

```java
import dev.hauch.hlobby.api.HLobbyProvider;

public class MyPlugin {

    public void demo() {
        HLobbyProvider api = HLobbyProvider.instance();
        if (api == null) {
            return;
        }

        // Select the best lobby for a player
        api.lobbyManager().selectLobby(player).ifPresent(lobby -> {
            api.teleportService().teleport(player, lobby);
        });
    }
}
```

## 🛠️ Building from Source

Requirements: JDK 21+ and Gradle 8.5+.

```bash
# Clone the repository
git clone https://github.com/hauchdev/hlobby.git
cd hlobby

# Build the shaded jar
./gradlew build

# The distributable jar is: build/libs/hLobby-<version>.jar
```

CI is wired through `.github/workflows/build.yml`. Every push runs `./gradlew build` on Ubuntu and Windows. Tagged `v*` builds additionally publish a GitHub Release with the packaged JAR.

## 🤝 Contributing

Contributions are welcome. Please read [`CONTRIBUTING.md`](CONTRIBUTING.md) before opening a pull request.

When adding a command, permission or message, update:

- `src/main/resources/config.yml`
- `src/main/resources/lang/en.yml` and `es.yml`
- `README.md` and `docs/CONFIGURATION.md`
- Unit tests when applicable

## 📄 License

hLobby is released under the [MIT License](LICENSE).

---

<p align="center">Made with ❤️ by <a href="https://github.com/hauchdev">Hauchdev</a></p>
