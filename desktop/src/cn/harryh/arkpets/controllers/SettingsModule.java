/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.guitasks.CheckAppUpdateTask;
import cn.harryh.arkpets.guitasks.GuiTask;
import cn.harryh.arkpets.platform.StartupConfig;
import cn.harryh.arkpets.platform.WindowSystem;
import cn.harryh.arkpets.utils.*;
import cn.harryh.arkpets.utils.GuiComponents.*;
import com.badlogic.gdx.graphics.Color;
import com.jfoenix.controls.*;
import com.sun.jna.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.apache.log4j.Level;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cn.harryh.arkpets.Const.*;


public final class SettingsModule implements Controller<ArkHomeFX> {
    @FXML
    private Pane noticeBox;

    @FXML
    private JFXComboBox<NamedItem<Float>> configDisplayScale;
    @FXML
    private JFXButton configDisplayScaleHelp;
    @FXML
    private JFXComboBox<NamedItem<Integer>> configDisplayFps;
    @FXML
    private JFXButton configDisplayFpsHelp;
    @FXML
    private JFXComboBox<NamedItem<Integer>> configCanvasSize;
    @FXML
    private JFXButton configCanvasSizeHelp;

    @FXML
    private JFXComboBox<NamedItem<Integer>> configCanvasColor;
    @FXML
    private JFXButton toggleConfigRenderOutline;
    @FXML
    private HBox wrapperConfigRenderOutline;
    @FXML
    private JFXComboBox<NamedItem<Integer>> configRenderOutline;
    @FXML
    private JFXComboBox<NamedItem<Integer>> configRenderOutlineColor;
    @FXML
    private JFXComboBox<NamedItem<Float>> configRenderOutlineWidth;
    @FXML
    private JFXButton toggleConfigRenderOpacity;
    @FXML
    private HBox wrapperConfigRenderOpacity;
    @FXML
    private JFXSlider configRenderOpacityNormal;
    @FXML
    private Label configRenderOpacityNormalValue;
    @FXML
    private JFXSlider configRenderOpacityDim;
    @FXML
    private Label configRenderOpacityDimValue;
    @FXML
    private JFXButton toggleConfigRenderShadow;
    @FXML
    private HBox wrapperConfigRenderShadow;
    @FXML
    private JFXComboBox<NamedItem<Integer>> configRenderShadowColor;

    @FXML
    private JFXCheckBox configWindowTopmost;
    @FXML
    private JFXComboBox<String> configLoggingLevel;
    @FXML
    private Label exploreLogDir;
    @FXML
    private JFXTextField configNetworkAgent;
    @FXML
    private Label configNetworkAgentStatus;
    @FXML
    private JFXCheckBox configAutoStartup;
    @FXML
    private JFXCheckBox configSolidExit;
    @FXML
    private JFXCheckBox configWindowToolwindow;
    @FXML
    private JFXButton configWindowToolwindowHelp;

    @FXML
    private JFXComboBox<NamedItem<String>> configWindowSystem;
    @FXML
    private JFXButton configWindowSystemHelp;
    @FXML
    private Label aboutQueryUpdate;
    @FXML
    private Label aboutVisitWebsite;
    @FXML
    private Label aboutReadme;
    @FXML
    private Label aboutGitHub;

    private NoticeBar appVersionNotice;
    private NoticeBar diskFreeSpaceNotice;
    private WarningHandbookEntrance displayScaleHelpEntrance;
    private DangerHandbookEntrance displayFpsHelpEntrance;

    private ArkHomeFX app;

    @Override
    public void initializeWith(ArkHomeFX app) {
        this.app = app;
        initNoticeBox();
        initHandbookEntrance();
        initConfigDisplay();
        initConfigAdvanced();
        initAbout();
        initScheduledListener();
    }

    private void initConfigDisplay() {
        new ComboBoxSetup<>(configDisplayScale).setItems(new NamedItem<>("x0.5", 0.5f),
                new NamedItem<>("x0.75", 0.75f),
                new NamedItem<>("x1.0", 1f),
                new NamedItem<>("x1.25", 1.25f),
                new NamedItem<>("x1.5", 1.5f),
                new NamedItem<>("x2.0", 2f),
                new NamedItem<>("x2.5", 2.5f),
                new NamedItem<>("x3.0", 3.0f))
                .selectValue(app.config.display_scale, "x" + app.config.display_scale + "（自定义）")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.display_scale = newValue.value();
                    app.config.save();
                    displayScaleHelpEntrance.refreshAndEnsureDisplayed();
                });
        new ComboBoxSetup<>(configDisplayFps).setItems(new NamedItem<>("25", 25),
                new NamedItem<>("30", 30),
                new NamedItem<>("45", 45),
                new NamedItem<>("60", 60),
                new NamedItem<>("120", 120))
                .selectValue(app.config.display_fps, app.config.display_fps + "（自定义）")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.display_fps = newValue.value();
                    app.config.save();
                    displayFpsHelpEntrance.refreshAndEnsureDisplayed();
                });
        new ComboBoxSetup<>(configCanvasSize).setItems(new NamedItem<>("最宽", 4),
                new NamedItem<>("较宽", 8),
                new NamedItem<>("标准", 16),
                new NamedItem<>("较窄", 32),
                new NamedItem<>("最窄", 0))
                .selectValue(app.config.canvas_fitting_samples, "每" + app.config.canvas_fitting_samples + "帧采样（自定义）")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.canvas_fitting_samples = newValue.value();
                    app.config.save();
                });
        new HelpHandbookEntrance(app.body, configCanvasSizeHelp) {
            @Override
            public Handbook getHandbook() {
                return new ControlHelpHandbook((Labeled)configCanvasSize.getParent().getChildrenUnmodifiable().get(0)) {
                    @Override
                    public String getContent() {
                        return "设置桌宠窗口边界的相对大小。更宽的边界能够防止动画溢出；更窄的边界能够防止鼠标误触。";
                    }
                };
            }
        };

        new ComboBoxSetup<>(configCanvasColor).setItems(new NamedItem<>("透明", 0x00000000),
                        new NamedItem<>("绿色", 0x00FF00FF),
                        new NamedItem<>("蓝色", 0x0000FFFF),
                        new NamedItem<>("品红色", 0xFF00FFFF))
                .selectValue(Color.rgba8888(ArkConfig.getGdxColorFrom(app.config.canvas_color)), app.config.canvas_color + "（自定义）")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.canvas_color = String.format("#%08X", newValue.value());
                    app.config.save();
                });

        GuiPrefabs.bindToggleAndWrapper(toggleConfigRenderOutline, wrapperConfigRenderOutline, durationFast);
        new ComboBoxSetup<>(configRenderOutline).setItems(new NamedItem<>("始终开启", ArkConfig.RenderOutline.ALWAYS.ordinal()),
                        new NamedItem<>("处于前台时", ArkConfig.RenderOutline.FOCUSED.ordinal()),
                        new NamedItem<>("点击时", ArkConfig.RenderOutline.PRESSING.ordinal()),
                        new NamedItem<>("拖拽时", ArkConfig.RenderOutline.DRAGGING.ordinal()),
                        new NamedItem<>("关闭", ArkConfig.RenderOutline.NEVER.ordinal()))
                .selectValue(app.config.render_outline, "未知")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.render_outline = newValue.value();
                    app.config.save();
                });
        new ComboBoxSetup<>(configRenderOutlineColor).setItems(new NamedItem<>("黄色", 0xFFFF00FF),
                        new NamedItem<>("白色", 0xFFFFFFFF),
                        new NamedItem<>("青色", 0x00FFFFFF))
                .selectValue(Color.rgba8888(ArkConfig.getGdxColorFrom(app.config.render_outline_color)), app.config.render_outline_color + "（自定义）")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.render_outline_color = String.format("#%08X", newValue.value());
                    app.config.save();
                });
        new ComboBoxSetup<>(configRenderOutlineWidth).setItems(new NamedItem<>("极细", 1f),
                        new NamedItem<>("较细", 1.5f),
                        new NamedItem<>("标准", 2f),
                        new NamedItem<>("较粗", 3f),
                        new NamedItem<>("极粗", 5f))
                .selectValue(app.config.render_outline_width, app.config.render_outline_width + "个单位（自定义）")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.render_outline_width = newValue.value();
                    app.config.save();
                });

        GuiPrefabs.bindToggleAndWrapper(toggleConfigRenderOpacity, wrapperConfigRenderOpacity, durationFast);
        final int minOpacity = 10;
        GuiComponents.SliderSetup<Integer> setupRenderOpacityDim = new GuiComponents.SimpleIntegerSliderSetup(configRenderOpacityDim);
        setupRenderOpacityDim
                .setDisplay(configRenderOpacityDimValue, "%d%%", "不透明度 (Opacity)")
                .setRange(minOpacity, 100)
                .setTicks(minOpacity, 100 - minOpacity)
                .setSliderValue(app.config.opacity_dim * 100)
                .setOnChanged((observable, oldValue, newValue) -> {
                    app.config.opacity_dim = setupRenderOpacityDim.getValidatedValue() / 100f;
                    app.config.save();
                });
        GuiComponents.SliderSetup<Integer> setupRenderOpacityNormal = new GuiComponents.SimpleIntegerSliderSetup(configRenderOpacityNormal);
        setupRenderOpacityNormal
                .setDisplay(configRenderOpacityNormalValue, "%d%%", "不透明度 (Opacity)")
                .setRange(minOpacity, 100)
                .setTicks(minOpacity, 100 - minOpacity)
                .setSliderValue(app.config.opacity_normal * 100)
                .setOnChanged((observable, oldValue, newValue) -> {
                    setupRenderOpacityDim.setRange(minOpacity, setupRenderOpacityNormal.getValidatedValue());
                    setupRenderOpacityDim.setDisable(minOpacity >= setupRenderOpacityNormal.getValidatedValue());
                    app.config.opacity_normal = setupRenderOpacityNormal.getValidatedValue() / 100f;
                    app.config.save();
                });
        setupRenderOpacityDim.setRange(minOpacity, setupRenderOpacityNormal.getValidatedValue());
        setupRenderOpacityDim.setDisable(minOpacity >= setupRenderOpacityNormal.getValidatedValue());

        GuiPrefabs.bindToggleAndWrapper(toggleConfigRenderShadow, wrapperConfigRenderShadow, durationFast);
        new ComboBoxSetup<>(configRenderShadowColor).setItems(new NamedItem<>("禁用", 0x00000000),
                        new NamedItem<>("轻微", 0x00000077),
                        new NamedItem<>("标准", 0x000000BB),
                        new NamedItem<>("重墨", 0x000000FF))
                .selectValue(Color.rgba8888(ArkConfig.getGdxColorFrom(app.config.render_shadow_color)), app.config.render_shadow_color + "（自定义）")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.render_shadow_color = String.format("#%08X", newValue.value());
                    app.config.save();
                });
    }

    private void initConfigAdvanced() {
        configLoggingLevel.getItems().setAll(Const.LogConfig.debug, Const.LogConfig.info, Const.LogConfig.warn, Const.LogConfig.error);
        configLoggingLevel.valueProperty().addListener(observable -> {
            if (configLoggingLevel.getValue() != null) {
                Logger.setLevel(Level.toLevel(configLoggingLevel.getValue(), Level.INFO));
                app.config.logging_level = Logger.getLevel().toString();
                app.config.save();
            }
        });
        String level = app.config.logging_level;
        List<String> args = Arrays.asList(ArgPending.argCache);
        if (args.contains(Const.LogConfig.errorArg))
            level = Const.LogConfig.error;
        else if (args.contains(Const.LogConfig.warnArg))
            level = Const.LogConfig.warn;
        else if (args.contains(Const.LogConfig.infoArg))
            level = Const.LogConfig.info;
        else if (args.contains(Const.LogConfig.debugArg))
            level = Const.LogConfig.debug;
        configLoggingLevel.getSelectionModel().select(level);

        exploreLogDir.setOnMouseClicked(e -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    Logger.debug("Config", "Request to explore the log dir");
                    Desktop.getDesktop().open(new File("logs"));
                } catch (IOException ex) {
                    Logger.warn("Config", "Exploring log dir failed");
                }
            });
        });

        configNetworkAgent.setPromptText("示例：0.0.0.0:0");
        configNetworkAgent.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                configNetworkAgentStatus.setText("未使用代理");
                configNetworkAgentStatus.setTextFill(GuiPrefabs.COLOR_LIGHT_GRAY);
                Logger.info("Network", "Set proxy to none");
                NetUtils.setProxy("", "");
            } else {
                if (ipPortRegex.matcher(newValue).matches()) {
                    String[] ipPort = newValue.split(":");
                    NetUtils.setProxy(ipPort[0], ipPort[1]);
                    configNetworkAgentStatus.setText("代理生效中");
                    configNetworkAgentStatus.setTextFill(GuiPrefabs.COLOR_SUCCESS);
                    Logger.info("Network", "Set proxy to host " + ipPort[0] + ", port " + ipPort[1]);
                } else {
                    configNetworkAgentStatus.setText("输入不合法");
                    configNetworkAgentStatus.setTextFill(GuiPrefabs.COLOR_DANGER);
                }
            }
        });
        configNetworkAgentStatus.setText("未使用代理");
        configNetworkAgentStatus.setTextFill(GuiPrefabs.COLOR_LIGHT_GRAY);

        StartupConfig startup = StartupConfig.getInstance();
        configAutoStartup.setSelected(startup.isSetStartup());
        configAutoStartup.setOnAction(e -> {
            if (configAutoStartup.isSelected()) {
                if (startup.addStartup()) {
                    GuiPrefabs.Dialogs.createCommonDialog(app.body,
                            GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_SUCCESS_ALT, GuiPrefabs.COLOR_SUCCESS),
                            "开机自启动",
                            "开机自启动设置成功。",
                            "下次开机时将会自动生成您最后一次启动的桌宠。",
                            null).show();
                } else {
                    if (!startup.isStartupAvailable())
                        GuiPrefabs.Dialogs.createCommonDialog(app.body,
                                GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_WARNING_ALT, GuiPrefabs.COLOR_WARNING),
                                "开机自启动",
                                "开机自启动设置失败。",
                                "无法确认目标程序的位置，其原因和相关解决方案如下：",
                                "为确保自启动服务的稳定性，直接打开的ArkPets的\".jar\"版启动器，是不支持配置自启动的。请使用exe版的安装包安装ArkPets后运行，或使用zip版的压缩包解压程序文件后运行。另外，当您使用错误的工作目录运行启动器时也可能出现此情况。").show();
                    else
                        GuiPrefabs.Dialogs.createCommonDialog(app.body,
                                GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_WARNING_ALT, GuiPrefabs.COLOR_WARNING),
                                "开机自启动",
                                "开机自启动设置失败。",
                                "无法写入系统的启动目录，其原因可参见日志文件。",
                                "这有可能是由于权限不足导致的。请尝试关闭反病毒软件，并以管理员权限运行启动器。").show();
                    configAutoStartup.setSelected(false);
                }
            } else {
                startup.removeStartup();
            }
        });

        configSolidExit.setSelected(app.config.launcher_solid_exit);
        configSolidExit.setOnAction(e -> {
            app.config.launcher_solid_exit = configSolidExit.isSelected();
            app.config.save();
        });

        configWindowTopmost.setSelected(app.config.window_style_topmost);
        configWindowTopmost.setOnAction(e -> {
            app.config.window_style_topmost = configWindowTopmost.isSelected();
            app.config.save();
        });

        configWindowToolwindow.setSelected(app.config.window_style_toolwindow);
        configWindowToolwindow.setOnAction(e -> {
            app.config.window_style_toolwindow = configWindowToolwindow.isSelected();
            app.config.save();
        });
        new HelpHandbookEntrance(app.body, configWindowToolwindowHelp) {
            @Override
            public Handbook getHandbook() {
                return new ControlHelpHandbook(configWindowToolwindow) {
                    @Override
                    public String getContent() {
                        return "启用时，桌宠将以后台工具程序的样式启动，不会在任务栏中显示程序图标。禁用时，作为普通程序启动的桌宠可以被直播流软件捕获。";
                    }
                };
            }
        };

        NamedItem<String>[] items = getWindowSystemItems().toArray(new NamedItem[0]);
        new ComboBoxSetup<>(configWindowSystem).setItems(items)
                .selectValue(app.config.window_system, app.config.window_system)
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.window_system = newValue.value();
                    app.config.save();
                });
        new HelpHandbookEntrance(app.body, configWindowSystemHelp) {
            @Override
            public Handbook getHandbook() {
                return new ControlHelpHandbook((Labeled) configWindowSystem.getParent().getChildrenUnmodifiable().get(0)) {
                    @Override
                    public String getContent() {
                        return getWindowSystemInfo();
                    }
                };
            }
        };
    }

    private static ArrayList<NamedItem<String>> getWindowSystemItems() {
        ArrayList<NamedItem<String>> windowSystemItems = new ArrayList<>();
        windowSystemItems.add(new NamedItem<>("自动", WindowSystem.AUTO.name()));
        if (Platform.isWindows()) {
            windowSystemItems.add(new NamedItem<>("User32", WindowSystem.USER32.name()));
        }
        if (Platform.isLinux()) {
            windowSystemItems.add(new NamedItem<>("X11", WindowSystem.X11.name()));
            windowSystemItems.add(new NamedItem<>("Mutter", WindowSystem.MUTTER.name()));
            windowSystemItems.add(new NamedItem<>("KWin", WindowSystem.KWIN.name()));
        }
        if (Platform.isMac()) {
            windowSystemItems.add(new NamedItem<>("Quartz", WindowSystem.QUARTZ.name()));
        }
        windowSystemItems.add(new NamedItem<>("NULL", WindowSystem.NULL.name()));
        return windowSystemItems;
    }

    private static String getWindowSystemInfo() {
        String content = "不同平台对于窗口查询、操作有不同的 API，除非你遇到了桌宠窗口的问题，否则通常不需要更改。以下是对 API 的简单介绍：\n";
        if (Platform.isWindows()) {
            content += "User32 —— Windows 窗口系统。\n";
        }
        if (Platform.isLinux()) {
            content += """
                    Mutter —— GNOME 环境，需要安装集成扩展。
                    KWin —— KDE 环境，需要安装集成插件。
                    X11 —— 通用 X11 环境支持，适用于 Xfce,Mate,LXDE 等环境。
                    """;
        }
        if (Platform.isMac()) {
            content += "Quartz —— MacOS Quartz 窗口系统。\n";
        }
        content += "NULL —— 空实现，桌宠不会有任何窗口交互。";
        return content;
    }

    private void initAbout() {
        aboutQueryUpdate.setOnMouseClicked  (e -> {
            /* Foreground check app update */
            new CheckAppUpdateTask(app.body, GuiTask.GuiTaskStyle.COMMON, "manual").start();
        });
        aboutVisitWebsite.setOnMouseClicked (e -> NetUtils.browseWebpage(Const.PathConfig.urlOfficial));
        aboutReadme.setOnMouseClicked       (e -> NetUtils.browseWebpage(Const.PathConfig.urlReadme));
        aboutGitHub.setOnMouseClicked       (e -> NetUtils.browseWebpage(Const.PathConfig.urlLicense));
    }

    private void initNoticeBox() {
        appVersionNotice = new NoticeBar(noticeBox) {
            @Override
            protected boolean isToActivate() {
                return isUpdateAvailable;
            }

            @Override
            protected javafx.scene.paint.Color getColor() {
                return GuiPrefabs.COLOR_INFO;
            }

            @Override
            protected String getIconSVGPath() {
                return GuiPrefabs.Icons.SVG_UPDATE;
            }

            @Override
            protected String getText() {
                return "ArkPets 有新版本可用！点击此处前往下载~";
            }

            @Override
            protected void onClick(MouseEvent event) {
                NetUtils.browseWebpage(Const.PathConfig.urlDownload);
            }
        };
        diskFreeSpaceNotice = new NoticeBar(noticeBox) {
            @Override
            protected boolean isToActivate() {
                long freeSpace = new File(".").getFreeSpace();
                return freeSpace < diskFreeSpaceRecommended && freeSpace > 0;
            }

            @Override
            protected javafx.scene.paint.Color getColor() {
                return GuiPrefabs.COLOR_WARNING;
            }

            @Override
            protected String getIconSVGPath() {
                return GuiPrefabs.Icons.SVG_WARNING_ALT;
            }

            @Override
            protected String getText() {
                return "当前磁盘存储空间不足，可能影响使用体验。";
            }
        };
    }

    private void initHandbookEntrance() {
        displayScaleHelpEntrance = new WarningHandbookEntrance(app.body, configDisplayScaleHelp) {
            @Override
            protected Handbook getHandbook() {
                return new ControlWarningHandbook() {
                    @Override
                    protected String getHeader() {
                        return "当前设置的缩放倍率过高";
                    }

                    @Override
                    protected String getContent() {
                        return "过高的缩放倍率可能导致桌宠尺寸过大，从而阻碍您的正常使用，请您谨慎选择。";
                    }
                };
            }

            @Override
            protected boolean getEntranceVisibleCondition() {
                float configScale = configDisplayScale.getValue() == null ?
                        app.config.display_scale : configDisplayScale.getValue().value();
                return configScale > 2f;
            }
        };
        displayFpsHelpEntrance = new DangerHandbookEntrance(app.body, configDisplayFpsHelp) {
            @Override
            public Handbook getHandbook() {
                return new ControlDangerHandbook() {
                    @Override
                    protected String getHeader() {
                        return "当前设置的最大帧率过高";
                    }

                    @Override
                    protected String getContent() {
                        int maxHz = -1;
                        for (ArkConfig.Monitor i : ArkConfig.Monitor.getMonitors())
                            if (i.hz > maxHz)
                                maxHz = i.hz;
                        return "您设置的最大帧率超过了您的显示器的最大刷新率（" + maxHz + " Hz），因此实际帧率并不会得到提高。";
                    }
                };
            }

            @Override
            protected boolean getEntranceVisibleCondition() {
                int configHz = configDisplayFps.getValue() == null ?
                        app.config.display_fps : configDisplayFps.getValue().value();
                int maxHz = -1;
                for (ArkConfig.Monitor i : ArkConfig.Monitor.getMonitors())
                    if (i.hz > maxHz)
                        maxHz = i.hz;
                return configHz > maxHz;
            }
        };
    }

    private void initScheduledListener() {
        ScheduledService<Boolean> ss = new ScheduledService<>() {
            @Override
            protected Task<Boolean> createTask() {
                Task<Boolean> task = new Task<>() {
                    @Override
                    protected Boolean call() {
                        return true;
                    }
                };
                task.setOnSucceeded(e -> {
                    appVersionNotice.refresh();
                    diskFreeSpaceNotice.refresh();
                    displayScaleHelpEntrance.refresh();
                    displayFpsHelpEntrance.refresh();
                });
                return task;
            }
        };
        ss.setDelay(new Duration(5000));
        ss.setPeriod(new Duration(5000));
        ss.setRestartOnFailure(true);
        ss.start();
    }
}
