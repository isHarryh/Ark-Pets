ArkPets Supplementary Documentation
# Telemetry Specifications

## Overview

ArkPets incorporates an opt-out telemetry service designed to record real-world runtime diagnostics. Collected metrics help developers evaluate:

- **Stability & Crash Events**: Frequency and stack traces of runtime exceptions;
- **Performance Metrics**: Rendering efficiency across distinct hardware configurations;
- **User Preferences**: Feature usage and settings customisation trends.

Telemetry is enabled by default. Users may opt out at any time by toggling **Upload Anonymous Usage Data** under launcher Options.

## Privacy Statement

### Scope of Data Collection

We enforce strict data minimisation practices:

- **No Personal Data Collected**: Personal files, system clipboards, screen captures, and browsing history are never accessed;
- **No Sensitive Configuration Data**: Credentials (such as Mirrorchyan CDK access keys) are strictly excluded;
- **No Automatic Log Uploads**: Application log files are only transmitted when manually dispatched via explicit user action (**Upload Logs**).

### Data Security

We implement standard industry protocols to protect system transmissions:

- **Anonymised Analytics**: Collected metrics cannot be mapped to individual user identities. Transient IP metadata is used solely during network transmission;
- **Encrypted In-Transit**: All telemetry events are transmitted over TLS-encrypted endpoints;
- **Sentry Platform Infrastructure**: Telemetry processing is managed via [Sentry](https://sentry.io), a secure error-monitoring service provider.

### Data Sharing Policy

Recorded data is accessible exclusively to the ArkPets development team and Sentry server instances. **Data is never sold or shared with third parties.**

### Voluntary Opt-Out

You can opt out anytime via application settings. Disabling telemetry immediately terminates event dispatching and clears local pending queues.

If you identify data collection outside the declared scope, open an Issue ticket for inspection.

## Telemetry Metrics

Automatically submitted events fall into the following categories:

| Event Type | Trigger | Payloads |
|:---|:---|:---|
| Model Session<br>(`MODEL_SESSION`) | Desktop pet process termination | Active model identifier, session duration, exit state (clean shutdown, exception, crash) |
| Desktop Session<br>(`DESKTOP_SESSION`) | Launcher UI process termination | Launcher runtime session duration |
| Config Snapshot<br>(`CONFIG`) | Desktop pet startup | Non-sensitive runtime settings and flags |
| System Info<br>(`SYSTEM_INFO`) | Post GL context creation | GPU vendor, driver release, operating system architecture |
| Performance Metrics | Periodic intervals during runtime | Render time, resolution, FPS, RAM utilisation, CPU load |
| Crash Reports | Unhandled exceptions | Exception class names and frame stack traces |

Users can also dispatch application diagnostic logs manually via **Upload Logs** when error dialogues occur.

## Technical Details

Technical details intended for developers and system maintainers.

### Architecture

ArkPets operates across two isolated JVM processes: **Desktop** (JavaFX UI launcher) and **Core** (libGDX pet renderer). Core can execute independently without Desktop. Architectural constraint: **Desktop manages all Sentry network transmissions; Core contains zero network communication drivers.**

Inter-process telemetry transfer utilises Write-Ahead Logging (**WAL**) over file system IPC:

- WAL files reside within log directories, named using PID identifiers (`.wal` extension), using append-only writes;
- Frame format: `magic("WAL1") | version | type | seq | timestamp | payloadLen | payload | crc32(payload)`. Corrupted frames or trailing checksum failures are discarded on read;
- Desktop scans WAL directories at startup and shutdown, consuming records only when targeted PIDs have terminated. Uploaded files are deleted upon commit.

Session lifecycles use **heartbeat events** to prevent data loss during power outages or process terminations: startup appends an initial heartbeat, followed by background thread pulses at regular intervals. Consumers determine session states as follows:

| Exit State | Sentry Severity | WAL Pattern |
|:---:|:---:|:---|
| Clean Exit | `INFO` | Final heartbeat contains `stopped=true` |
| Caught Exception | `ERROR` | Contains `exception` record and stack trace |
| Process Crash / Power Off | `WARN` | Contains active `running` heartbeats only |

Additional design considerations:

- **Timestamp Semantics**: WAL frame timestamps map directly into Sentry events, ensuring metric time reflects event generation rather than upload dispatch times;
- **Configuration Snapshots**: Core reflects public scalars in `ArkConfig` on boot to capture static configuration states per session;
- **Non-blocking Metrics**: Render threads perform memory-only aggregations; snapshots piggyback on heartbeat WAL writes without I/O performance penalties.

### Configuration Parameters

#### Sentry Data Source Name (DSN)

- **Gradle Pipeline**: Gradle property `SENTRY_DSN` serves as the primary source, defined in `desktop/build.gradle` (falls back to placeholder DSN if missing):
  - `run` and `debug` tasks inject `-Dsentry.dsn=...` via `jvmArgs`;
  - `dist` tasks embed DSN values into distributions via jpackage `--java-options`.

- **CI Builds**: Injected by `.github/workflows/build.yml` via `ORG_GRADLE_PROJECT_SENTRY_DSN: ${{ secrets.SENTRY_DSN }}`.

- **Manual Configuration**: When running directly outside Gradle pipelines (e.g. IDE execution), configure via (in order of precedence):
  1. Java System property `-Dsentry.dsn`
  2. Environment variable `SENTRY_DSN`
  3. Properties file `sentry.properties`

#### Sentry Environment Flag

Defaults to `"dev"`. Release builds override this value to `"production"` using `-Dsentry.environment=production`.

#### Configuration Reference Table

| Configuration Target | File Path / Reference |
|:---|:---|
| SDK Options Initialisation | `desktop/src/cn/harryh/arkpets/utils/SentryHelper.java` |
| DSN Resolution & Task Injections | `desktop/build.gradle` |
| CI Secret Definition | `.github/workflows/build.yml` |