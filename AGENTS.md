# AGENTS.md

Guidance for AI coding agents working on the **ArkPets** repository.

## Project Overview

**ArkPets** is an Arknights desktop pets application.
It renders Spine models as interactive desktop pets, managed by a JavaFX GUI launcher.

- **Language**: Java 17, built with Gradle (use the bundled wrapper `gradlew`)
- **Operating System**: Windows only (macOS/Linux support is experimental)
- **Localization**: user-facing strings and docs are written in Simplified Chinese

### Tech Stack

| Concern         | Library                                          |
|-----------------|--------------------------------------------------|
| Rendering       | libGDX 1.11 (LWJGL3 backend) + Spine runtime 3.8 |
| Launcher GUI    | JavaFX 17 + JFoenix 9                            |
| Window control  | JNA 5.12 (User32)                                |
| JSON            | fastjson2                                        |
| Logging         | reload4j via custom `Logger`                     |
| Error reporting | Sentry                                           |

## Module Layout

Two Gradle modules (see `settings.gradle`), root package `cn.harryh.arkpets`:

```
core/     Pet runtime (libGDX app) and shared code, depended on by desktop
desktop/  Launcher (JavaFX app), main entry classes
assets/   Shared resources (desktop's resources directory points here)
docs/     User-facing docs (Debug.md, CmdLine.md, CustomModel.md, FAQ.md, Telemetry.md)
```

> **Non-standard source layout**:
> sources live directly in `src/` (i.e. `core/src/cn/harryh/...`), NOT in `src/main/java`.
> This is configured via `sourceSets` in each `build.gradle`. Tests (if any) are in `core/src/test/java/`.

### Key Packages

> Here we use "P" to show packages under `cn.harryh.arkpets`, "C" to show classes under `cn.harryh.arkpets`.

**`/core`**:

| Package         | Responsibility                                                       |
|-----------------|----------------------------------------------------------------------|
| P `animations`  | Behavior state machine, animation clips, stochastic animation matrix |
| P `render`      | Custom shaders, dynamic orthographic camera                          |
| P `platform`    | Win32 window control via JNA, startup config                         |
| P `concurrent`  | Launcher & pet IPC: `SocketServer`, `SocketClient`, `ProcessPool`    |
| P `telemetry`   | Opt-in telemetry with a WAL design, see `docs/Telemetry.md`          |
| P `tray`        | System tray integration (`HostTray`, `MemberTray*`)                  |
| P `transitions` | Easing/transition helpers for smooth movement                        |
| C `ArkPets`     | The libGDX `ApplicationListener` for a single pet                    |
| C `ArkConfig`   | Config model persisted to `ArkPetsConfig.json`                       |
| C `Const`       | Global constants, paths, config file names                           |

**`/desktop`**:

| Package          | Responsibility                                         |
|------------------|--------------------------------------------------------|
| `controllers`    | JavaFX UI page modules and dialogs                     |
| `guitasks`       | Background GUI tasks (downloads, unzip, update checks) |
| `network`        | HTTP layer, update APIs, CDN integration               |
| `utils/markdown` | CommonMark to JavaFX FXML rendering for announcements  |

### Process Architecture

`DesktopLauncher` (JavaFX launcher) starts pets either embedded
or as separate processes via the socket-based IPC in `core/.../concurrent`.
`--direct-start` CLI flag skips the launcher and boots `EmbeddedLauncher` (libGDX) directly.
CLI options are documented in `docs/CmdLine.md`.

## Building

```shell
# Run the launcher GUI
gradlew desktop:run
# Run with debugger attached
gradlew desktop:debug
# Compile check
# This is the fastest way to verify changes
gradlew desktop:classes
# Distribution artifacts
# This will generate jar, zip and exe distributables into desktop/build/dist/
gradlew desktop:distAll
```

The build CI is available at `.github/workflows/build.yml`.

## Conventions

### Practices

- **Encoding**: UTF-8 is enforced on all Java compilation tasks. Do not rely on platform default charset.
- **Logging**: use `cn.harryh.arkpets.utils.Logger` (`Logger.debug/info/warn/error`), never `System.out`.
- **Config**: new settings need a field in `ArkConfig` (with `@JSONField(defaultValue = ...)` and a `@since` tag), a default in `assets/ArkPetsConfigDefault.json`, and a UI control in `desktop/.../controllers/SettingsModule` if user-facing.

### Comments

- **Copyright header**: every `.java` file starts with a custom copyright block; keep it when creating files.
- **Comments and Javadoc**: written in English. Javadoc sentences use third-person singular verbs (e.g. "Gets the..."), matching existing style.

### VCS

- **Commit messages**: conventional commits style written in English.
- **Changelog**: `CHANGELOG.md` uses a custom table format. When asked to update the changelog, follow the skill at `.agents/skills/update-changelog/SKILL.md`.
