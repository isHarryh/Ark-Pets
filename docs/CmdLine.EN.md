ArkPets Supplementary Documentation
# Command Line Interface

Launching ArkPets via the command line interface (CLI).

### Prerequisites
The active shell directory **must be set to the folder containing the executable**, otherwise runtime exceptions may occur.

### Usage Syntax
`ArkPets.exe`：
```
ArkPets [--direct-start [--config <path>] [--load-lib <path>]]
        [--quiet|--warn|--info|--debug]
```

| Option                | Description                                                                                                                                    |
|:----------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------|
| `--direct-start`      | Launch the desktop pet immediately without launching the graphical UI. (v1.5.1+)                                                               |
| `--quiet`             | Set log level to `ERROR`, outputting error events only. (v2.0.0+)                                                                              |
| `--warn`              | Set log level to `WARN`, outputting warnings and errors. (v2.0.0+)                                                                             |
| `--info`              | Set log level to `INFO` (Default). (v2.0.0+)                                                                                                   |
| `--debug`             | Set log level to `DEBUG`, enabling detailed diagnostic logging and [Debug Features](Debug.EN.md). (v2.0.0+)                                    |
| * `--config <path>`   | Load a specific configuration file on startup. (v3.11.0+)                                                                                      |
| * `--load-lib <path>` | *Debugging utility:* Load external libraries (such as [RenderDoc](https://renderdoc.org)), where `<path>` is the absolute file path. (v3.5.0+) |

*Options marked with an asterisk (`*`) require `--direct-start` to be set.*

### Example Usage
```shell
cd /d D:\MyArkPets
ArkPets --direct-start --debug
```

### Notes
If using the `.jar` distribution, prefix command executions with `java -jar ArkPets.jar` or target `ArkPets.jar` directly. Ensure full filenames and paths are explicitly declared.
