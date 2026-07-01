# BGA → BGG Converter

A small Spring Boot web application that converts a **boardgamearena.com** statistics table
(copied straight from the game overview) into ready-to-paste **boardgamegeek.com** forum markup.

Paste the statistic into the textarea on the left and the matching BGG markup appears live in the
textarea on the right — no button press needed. A large **Copy** button puts the result on the
clipboard, and a large **Clear** button empties the input.

## Features

- **Live conversion** — the preview updates on every keystroke (debounced).
- **Copy & Clear** — one large button each, with icons.
- **Player name mapping** — known BGA usernames are replaced with real names; unknown ones become
  `BGA User <username>`. Configured in `application.properties`.
- **Per-player filtering** — a statistic is printed for a player only when that player has a real
  value for it (a blank, `-` or `0` counts as "not set" and is skipped). Rows that are empty for
  *everyone* disappear completely.
- **Ordering by placement** — players are printed best-placed first, read from the result row.
- **Multiple themes** — visually and structurally different output styles (see below).
- **Internationalized UI** — English (default) and German, switchable in the header.
- **Dark theme** — Bootstrap 5.3 dark theme, delivered via WebJars.

## Requirements

- Java 25
- Maven (the bundled `./mvnw` wrapper is used in all examples)

## Running

```bash
./mvnw spring-boot:run
```

Then open <http://localhost:8080/>. Switch language with the buttons in the header or via
`?lang=de` / `?lang=en`.

## Docker & deployment

Every push to `main`/`master` builds a Docker image and publishes it to the GitHub
Container Registry via GitHub Actions. Run it locally with:

```bash
docker run --rm -p 8080:8080 ghcr.io/jensgiehl/bga2bgg:latest
```

- [`docs/releases.md`](docs/releases.md) — how versioning works: conventional commits drive
  automated version bumps, changelog, GitHub Releases (with the JAR) and versioned Docker images.
- [`docs/docker-build.md`](docs/docker-build.md) — the build & publish pipeline and how to
  use the image.
- [`docs/server-setup.md`](docs/server-setup.md) — running it on a Hetzner Linux server with
  automatic HTTPS per subdomain and automatic rollout on every push (Traefik + Watchtower).
  Ready-to-use files live in [`deploy/`](deploy).

## How the conversion works

Given a pasted BGA table such as:

```
User-A    USer-B    User-C    User-D
Spielergebnis    1. (36)    2. (34)    3. (33)    4. (27)
Bedenkzeit    9h16    8h29    19h55    14h56
Punkte für Abenteuertafel A    36    34    33    27
...
```

- The **first line** holds the BGA usernames (columns are separated by tabs; runs of two or more
  spaces are accepted as well, so multi-word labels and values like `1. (36)` stay intact).
- Every following line is a statistic: a label followed by one value per player.
- Usernames are mapped to display names, players are ordered by their finishing position, and each
  player keeps only the statistics that actually carry a value.

### Input validation

The input must be consistent: every statistic row has to carry exactly one value per player
(i.e. the number of values must match the number of usernames in the header). If it does not —
for example three usernames but four values in a row — no markup is produced and a localized
error message is shown above the output instead:

```
User-B    User-C    User-D                 <- 3 players
Spielergebnis    1. (36)    2. (34)    3. (33)    4. (27)   <- 4 values -> error
```

> Inconsistent input: row "Spielergebnis" has 4 value(s) but there are 3 player(s). ...

The classic theme produces:

```
[i]This game was played on boardgamearea.com[/i]

[b][u]g{BGA User MrX}g[/u][/b]
Spielergebnis: [b]1. (36)[/b]
...
```

## Configuration

### Player name mapping

Add mappings to `src/main/resources/application.properties` using bracket notation (this preserves
the exact, case-sensitive username):

```properties
bga.player-mapping[MrX]=Jens
bga.player-mapping[Dummy]=Sven
```

Any username without a mapping is printed as `BGA User <username>`.

## Themes

Themes are `BggTheme` implementations; each is a Spring bean and is picked up automatically.

| Id         | UI label (en / de)        | Style                                                              |
|------------|---------------------------|-------------------------------------------------------------------|
| `colorful` | Colorful / Farbenfroh     | **Default.** Gold/silver/bronze colored names via `[color]` (no emoji). |
| `default`  | Classic / Klassisch       | The reference format: intro line + one block per player.          |
| `compact`  | Compact / Kompakt         | One condensed line per player, statistics separated by `\|`.       |

Add your own theme by implementing
[`BggTheme`](src/main/java/de/agiehl/boardgame/b2btest/theme/BggTheme.java) and adding a
`theme.<id>` message key in both `messages.properties` files.

## Internationalization

UI texts live in `src/main/resources/messages.properties` (English, default) and
`messages_de.properties` (German). The language is selected with the `lang` request parameter and
remembered in the session. The generated BGG content itself is intentionally not translated — it is
game content, not UI chrome.

## Project structure

```
parser/      BgaStatisticsParser  — text  → RawStatistics (structure only)
conversion/  PlayerNameResolver   — username → display name
             GameResultAssembler  — RawStatistics → GameResult (resolve, filter, order)
             StatisticConversionService — orchestrates parse → assemble → render
domain/      GameResult, Player, StatEntry (immutable records)
theme/       BggTheme + implementations + ThemeRegistry
web/         PageController (UI), ConversionApiController (POST /api/convert)
config/      PlayerMappingProperties, WebConfig (i18n)
```

## Tests

Unit tests cover the parser, the assembler (name mapping, per-player filtering, ordering), the
themes and the conversion service; a `@SpringBootTest` covers the web endpoints and i18n.

```bash
./mvnw test
```

### Playwright UI test

An end-to-end browser test ([`ConverterUiPlaywrightTest`](src/test/java/de/agiehl/boardgame/b2btest/web/ConverterUiPlaywrightTest.java))
drives the real page (live conversion, Clear, Copy). It is tagged `playwright` and **skipped by the
default build**. Run it explicitly with the `ui-tests` profile (the browser is downloaded
automatically on first run):

```bash
./mvnw test -Pui-tests
```

## Tech stack

Spring Boot 4.1 · Spring MVC · Thymeleaf · Bootstrap 5.3 & Bootstrap Icons (WebJars) · Playwright.
