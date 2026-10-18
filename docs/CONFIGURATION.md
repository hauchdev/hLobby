# Configuration reference

hLobby stores its settings in `plugins/hLobby/config.yml` and its messages in
`plugins/hLobby/lang/<language>.yml`.

## General

```yaml
general:
  language: "en"
  debug: false
  check-updates: true
```

- `language` — language file to load, without extension. Files live in `plugins/hLobby/lang/`.
- `debug` — enables verbose debug logging to the console.
- `check-updates` — checks GitHub for a newer release on startup.

## Lobby settings

```yaml
lobby:
  teleport-mode: "LEAST_PLAYERS"
  servers:
    - "lobby"
    - "lobby2"
  priorities:
    lobby: 10
    lobby2: 5
  default-priority: 0
  blocked-servers: []
  fallback-server: ""
```

- `teleport-mode` — how a target lobby is chosen.
  - `RANDOM` — pick a random lobby.
  - `LEAST_PLAYERS` — pick the lobby with the fewest players.
  - `MOST_PLAYERS` — pick the lobby with the most players.
  - `FIRST_AVAILABLE` — pick the first lobby in `servers`.
  - `PRIORITY` — pick the lobby with the highest priority.
  - `ROUND_ROBIN` — rotate through the lobbies in order.
- `servers` — list of Velocity server names that are considered lobbies.
- `priorities` — per-server priority used by the `PRIORITY` mode. Higher is preferred.
- `default-priority` — priority for servers not listed in `priorities`.
- `blocked-servers` — servers where the `/lobby` command cannot be used unless the player has
  `lobby.bypass.blocked`.
- `fallback-server` — optional server used when no lobby is available. Leave empty to disable.

### Countdown

```yaml
lobby:
  countdown:
    enabled: true
    seconds: 3
    cancel-on-command: true
    cancel-on-chat: true
    sound: true
```

- `enabled` — show a countdown before teleporting.
- `seconds` — countdown length.
- `cancel-on-command` — cancel the countdown when the player runs a command.
- `cancel-on-chat` — cancel the countdown when the player sends a chat message.
- `sound` — play a sound on every tick.

### Cooldown

```yaml
lobby:
  cooldown:
    enabled: true
    seconds: 5
```

- `enabled` — restrict how often `/lobby` can be used.
- `seconds` — cooldown length. Players with `lobby.bypass.cooldown` ignore this.

### On join

```yaml
lobby:
  on-join:
    enabled: true
    only-first-join: false
```

- `enabled` — send players to a lobby when they join the proxy.
- `only-first-join` — when `true`, only redirect the player's first ever join to the proxy. The
  list of known players is persisted in `plugins/hLobby/first-join.yml`.

### On kick

```yaml
lobby:
  on-kick:
    enabled: true
    exclude-lobbies: false
    notify: true
```

- `enabled` — redirect kicked players to a lobby.
- `exclude-lobbies` — when `true`, players kicked from a lobby are not redirected.
- `notify` — send a message explaining the redirect.

## Join messages

```yaml
join-messages:
  title: true
  actionbar: true
  chat: true
  title-fade-in: 10
  title-stay: 70
  title-fade-out: 20
```

Controls the welcome message shown when a player enters a lobby server. Timings are in ticks.

## Commands

Every command can be renamed, aliased or disabled:

```yaml
commands:
  lobby:
    enabled: true
    name: "lobby"
    aliases: ["hub", "leave"]
    permission: "lobby.use"
  create:
    enabled: true
    name: "lobbycreate"
    aliases: ["lobbyadd", "addlobby"]
    permission: "lobby.admin.create"
  hlobby:
    enabled: true
    name: "hlobby"
    aliases: ["hlobbyadmin", "hla"]
    permission: "lobby.admin.main"
```

## Metrics

```yaml
metrics:
  enabled: true
```

- `enabled` — submit anonymous usage statistics to [FastStats](https://faststats.dev/).
  No player data is collected. Plugin authors must replace
  `MetricsWrapper.FASTSTATS_TOKEN` before releasing a build.

## Miscellaneous

```yaml
misc:
  show-no-permission: true
```

- `show-no-permission` — send a message when a player lacks the required permission.

## Messages

Messages use [MiniMessage](https://docs.advntr.dev/minimessage/format.html). Placeholders are
written with angle brackets, for example `<player>`, `<server>` and `<amount>`. The available
placeholders are listed at the top of each language file.

To add a new language, create a file in `plugins/hLobby/lang/` (for example `fr.yml`) and set
`general.language` to `fr`.
