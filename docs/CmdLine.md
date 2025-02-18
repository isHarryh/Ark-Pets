ArkPets附加说明文档
# 命令行启动

ArkPets 的命令行启动方法。

### 前提
必须将命令行工作目录设置为程序文件所在的目录，否则可能发生各种奇怪的错误。

### 用法
`ArkPets.exe`：
```
ArkPets [--direct-start [--load-lib <path>] [--enable-snapshot]]
        [--quiet|--warn|--info|--debug]
```

| 选项                           | 描述                                                                                    |
|:-----------------------------|:--------------------------------------------------------------------------------------|
| `--direct-start`             | 直接启动桌宠而不打开启动器。(v1.5.1+)                                                               |
| `--quiet`                    | 以`ERROR`日志等级运行，只记录错误信息。(v2.0.0+)                                                      |
| `--warn`                     | 以`WARN`日志等级运行，记录日志和警告信息。(v2.0.0+)                                                     |
| `--info`                     | 以`INFO`日志等级运行，此为默认日志等级。(v2.0.0+)                                                      |
| `--debug`                    | 以`DEBUG`日志等级运行，记录完整的调试信息。(v2.0.0+)                                                    |
| `--ui-style <win\|mac\|...>` | *调试时使用* 切换启动器的 UI 样式。（v4.0.0+）                                                        |
| * `--load-lib <path>`        | *调试时使用* 加载外部库文件（例如 [RenderDoc](https://renderdoc.org)），其中 `<path>` 是文件的绝对路径。(v3.5.0+) |
| * `--enable-snapshot`        | *调试时使用* 自动保存预处理阶段的截图用于调试，并启用手动截图功能（按<kbd>Shift</kbd>+<kbd>b</kbd>保存截图）。(v3.5.0+)      |

上表中，标有 * 的选项需要与`--direct-start` 同时使用。

### 用例
```shell
cd /d D:\MyArkPets
ArkPets --direct-start --debug
```

### 提示
如果您使用的程序文件是 `.jar` 版本 ，命令行应该以形如 `java -jar ArkPets.jar` 或者 `ArkPets.jar` 开头。注意写完整文件名。
