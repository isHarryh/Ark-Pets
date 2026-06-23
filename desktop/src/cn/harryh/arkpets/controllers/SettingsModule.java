/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.concurrent.SocketData;
import cn.harryh.arkpets.guitasks.AppInstallTask;
import cn.harryh.arkpets.guitasks.GuiTask;
import cn.harryh.arkpets.guitasks.requests.CheckAppUpdateTask;
import cn.harryh.arkpets.guitasks.requests.DownloadAppTask;
import cn.harryh.arkpets.guitasks.requests.McCheckAppUpdateTask;
import cn.harryh.arkpets.network.SourceStrategy;
import cn.harryh.arkpets.network.api.McQueryVersion;
import cn.harryh.arkpets.platform.StartupConfig;
import cn.harryh.arkpets.tray.HostTray;
import cn.harryh.arkpets.utils.*;
import cn.harryh.arkpets.utils.GuiComponents.*;
import com.alibaba.fastjson2.JSONObject;
import com.badlogic.gdx.graphics.Color;
import com.jfoenix.controls.JFXDialog;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.apache.log4j.Level;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static cn.harryh.arkpets.Const.*;


public final class SettingsModule implements Controller<ArkHomeFX> {
    @FXML
    private ScrollPane moduleScroll;
    @FXML
    private Pane noticeBox;

    @FXML
    private ComboBox<NamedItem<Float>> configDisplayScale;
    @FXML
    private Button configDisplayScaleHelp;
    @FXML
    private ComboBox<NamedItem<Integer>> configDisplayFps;
    @FXML
    private Button configDisplayFpsHelp;

    @FXML
    private TabPane configRenderTabPane;
    @FXML
    private ComboBox<NamedItem<Integer>> configCanvasColor;
    @FXML
    private ComboBox<NamedItem<Float>> configCanvasCoverage;
    @FXML
    private Button configCanvasCoverageHelp;
    @FXML
    private ComboBox<NamedItem<Integer>> configCanvasSamplingInterval;
    @FXML
    private ComboBox<NamedItem<Integer>> configRenderOutline;
    @FXML
    private ComboBox<NamedItem<Integer>> configRenderOutlineEmphasis;
    @FXML
    private ComboBox<NamedItem<Integer>> configRenderOutlineColor;
    @FXML
    private ComboBox<NamedItem<Integer>> configRenderOutlineColorEmphasis;
    @FXML
    private ComboBox<NamedItem<Float>> configRenderOutlineWidth;
    @FXML
    private Slider configRenderOpacityNormal;
    @FXML
    private Label configRenderOpacityNormalValue;
    @FXML
    private Slider configRenderOpacityDim;
    @FXML
    private Label configRenderOpacityDimValue;
    @FXML
    private ComboBox<NamedItem<Integer>> configRenderShadowColor;
    @FXML
    private CheckBox configEnableAngle;
    @FXML
    private Button configEnableAngleHelp;
    @FXML
    private CheckBox configEnableMipMap;
    @FXML
    private Button configEnableMipMapHelp;

    @FXML
    private CheckBox configWindowTopmost;
    @FXML
    private ComboBox<String> configLoggingLevel;
    @FXML
    private Button exportLog;
    @FXML
    private CheckBox configAutoStartup;
    @FXML
    private CheckBox configSolidExit;
    @FXML
    private CheckBox configWindowToolwindow;
    @FXML
    private Button configWindowToolwindowHelp;
    @FXML
    private CheckBox configEcoMode;

    @FXML
    private TextField configNetworkSource;
    @FXML
    private TextField configNetworkProxy;
    @FXML
    private Label configNetworkProxyStatus;

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
        initConfigRendering();
        initConfigAdvanced();
        initNetwork();
        initAbout();
        initScheduledListener();
        ScrollUtils.addSmoothScrolling(moduleScroll);
        Platform.runLater(() -> GuiPrefabs.disableScrollPaneCache(moduleScroll));
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
    }

    private void initConfigRendering() {
        new TabPaneSetup(configRenderTabPane, durationFast).makeResponsive();

        new ComboBoxSetup<>(configCanvasColor).setItems(new NamedItem<>("透明", 0x00000000),
                        new NamedItem<>("绿色", 0x00FF00FF),
                        new NamedItem<>("蓝色", 0x0000FFFF),
                        new NamedItem<>("品红色", 0xFF00FFFF))
                .selectValue(Color.rgba8888(ArkConfig.getGdxColorFrom(app.config.canvas_color)), app.config.canvas_color + "（自定义）")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.canvas_color = String.format("#%08X", newValue.value());
                    app.config.save();
                });

        new ComboBoxSetup<>(configCanvasCoverage).setItems(new NamedItem<>("最宽", 0.45f),
                        new NamedItem<>("较宽", 0.65f),
                        new NamedItem<>("标准", 0.8f),
                        new NamedItem<>("较窄", 0.9f),
                        new NamedItem<>("最窄", 0.95f))
                .selectValue(app.config.canvas_coverage, app.config.canvas_coverage * 100f + "% 覆盖率（自定义）")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.canvas_coverage = newValue.value();
                    app.config.save();
                });
        new HelpHandbookEntrance(app.body, configCanvasCoverageHelp) {
            @Override
            public Handbook getHandbook() {
                return new ControlHelpHandbook((Labeled) configCanvasCoverage.getParent().getChildrenUnmodifiable().get(0)) {
                    @Override
                    public String getContent() {
                        return "设置桌宠窗口边界的相对大小。更宽的边界能够防止动画溢出；更窄的边界能够防止鼠标误触。";
                    }
                };
            }
        };
        new ComboBoxSetup<>(configCanvasSamplingInterval).setItems(new NamedItem<>("极精确", 1),
                        new NamedItem<>("精确", 4),
                        new NamedItem<>("粗略", 16),
                        new NamedItem<>("极粗略", 64))
                .selectValue(app.config.canvas_sampling_interval, "间隔" + app.config.canvas_sampling_interval + "帧（自定义）")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.canvas_sampling_interval = newValue.value();
                    app.config.save();
                });

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
        new ComboBoxSetup<>(configRenderOutlineEmphasis).setItems(new NamedItem<>("始终开启", ArkConfig.RenderOutline.ALWAYS.ordinal()),
                        new NamedItem<>("处于前台时", ArkConfig.RenderOutline.FOCUSED.ordinal()),
                        new NamedItem<>("点击时", ArkConfig.RenderOutline.PRESSING.ordinal()),
                        new NamedItem<>("拖拽时", ArkConfig.RenderOutline.DRAGGING.ordinal()),
                        new NamedItem<>("关闭", ArkConfig.RenderOutline.NEVER.ordinal()))
                .selectValue(app.config.render_outline_emphasis, "未知")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.render_outline_emphasis = newValue.value();
                    app.config.save();
                });
        new ComboBoxSetup<>(configRenderOutlineColor).setItems(new NamedItem<>("黄色", 0xFFFF00FF),
                        new NamedItem<>("橙色", 0xFFBB00FF),
                        new NamedItem<>("白色", 0xFFFFFFFF),
                        new NamedItem<>("青色", 0x00FFFFFF))
                .selectValue(Color.rgba8888(ArkConfig.getGdxColorFrom(app.config.render_outline_color)), app.config.render_outline_color + "（自定义）")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.render_outline_color = String.format("#%08X", newValue.value());
                    app.config.save();
                });
        new ComboBoxSetup<>(configRenderOutlineColorEmphasis).setItems(new NamedItem<>("黄色", 0xFFFF00FF),
                        new NamedItem<>("橙色", 0xFFBB00FF),
                        new NamedItem<>("白色", 0xFFFFFFFF),
                        new NamedItem<>("青色", 0x00FFFFFF))
                .selectValue(Color.rgba8888(ArkConfig.getGdxColorFrom(app.config.render_outline_emphasis_color)), app.config.render_outline_emphasis_color + "（自定义）")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.render_outline_emphasis_color = String.format("#%08X", newValue.value());
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

        new ComboBoxSetup<>(configRenderShadowColor).setItems(new NamedItem<>("禁用", 0x00000000),
                        new NamedItem<>("轻微", 0x00000077),
                        new NamedItem<>("标准", 0x000000BB),
                        new NamedItem<>("重墨", 0x000000FF))
                .selectValue(Color.rgba8888(ArkConfig.getGdxColorFrom(app.config.render_shadow_color)), app.config.render_shadow_color + "（自定义）")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.render_shadow_color = String.format("#%08X", newValue.value());
                    app.config.save();
                });

        configEnableMipMap.setSelected(app.config.render_enable_mipmap);
        configEnableMipMap.setOnAction(e -> {
            app.config.render_enable_mipmap = configEnableMipMap.isSelected();
            app.config.save();
        });
        new HelpHandbookEntrance(app.body, configEnableMipMapHelp) {
            @Override
            protected Handbook getHandbook() {
                return new ControlHelpHandbook((Labeled) configEnableMipMap.getParent().getChildrenUnmodifiable().get(0)) {
                    @Override
                    public String getContent() {
                        return "启用时，将会使用 MipMap 技术来消除由于纹理缩放导致的锯齿，但是会略微增加显存占用。\n" +
                                "禁用时，桌宠在小分辨率渲染时可能会产生锯齿。";
                    }
                };
            }
        };

        configEnableAngle.setSelected(app.config.render_enable_angle);
        configEnableAngle.setOnAction(e -> {
            app.config.render_enable_angle = configEnableAngle.isSelected();
            app.config.save();
        });
        new HelpHandbookEntrance(app.body, configEnableAngleHelp) {
            @Override
            protected Handbook getHandbook() {
                return new ControlHelpHandbook((Labeled) configEnableAngle.getParent().getChildrenUnmodifiable().get(0)) {
                    @Override
                    public String getContent() {
                        String apiText;
                        if (com.sun.jna.Platform.isWindows()) apiText = "DirectX 11";
                        else if (com.sun.jna.Platform.isMac()) apiText = "Metal";
                        else apiText = "";
                        return "启用时，桌宠将使用实验性 " + apiText + " 进行渲染，这可能会在一定程度上提高性能，并解决某些渲染问题（如背景黑色等）。\n" +
                                "禁用时，桌宠将使用 OpenGL 进行渲染，在某些情况下可能会遇到兼容问题。";
                    }
                };
            }
        };
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

        exportLog.setOnMouseClicked(e -> app.dialogs.popDialog("logDialog"));

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

        configEcoMode.setSelected(app.config.eco_mode);
        configEcoMode.setOnAction(e -> {
            app.config.eco_mode = configEcoMode.isSelected();
            app.config.save();
        });
    }

    private void initNetwork() {
        BooleanProperty isMc = ((DownloadDialog) app.dialogs.getDialogController("downloadDialog")).getIsMcProperty();
        isMc.addListener(observable -> configNetworkSource.setText(isMc.get() ? "Mirror 酱" : "公共源"));
        configNetworkSource.setText(isMc.get() ? "Mirror 酱" : "公共源");
        configNetworkSource.setEditable(false);
        configNetworkSource.setCursor(Cursor.HAND);
        configNetworkSource.setOnMouseClicked(e -> app.dialogs.popDialog("downloadDialog"));
        configNetworkSource.setOnKeyPressed(e -> app.dialogs.popDialog("downloadDialog"));

        configNetworkProxy.setPromptText("示例：0.0.0.0:0");
        configNetworkProxy.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                configNetworkProxyStatus.setText("未使用代理");
                configNetworkProxyStatus.setTextFill(GuiPrefabs.COLOR_LIGHT_GRAY);
                Logger.info("Network", "Set proxy to none");
                setProxy("", "");
            } else {
                if (ipPortRegex.matcher(newValue).matches()) {
                    String[] ipPort = newValue.split(":");
                    setProxy(ipPort[0], ipPort[1]);
                    configNetworkProxyStatus.setText("代理生效中");
                    configNetworkProxyStatus.setTextFill(GuiPrefabs.COLOR_SUCCESS);
                    Logger.info("Network", "Set proxy to host " + ipPort[0] + ", port " + ipPort[1]);
                } else {
                    configNetworkProxyStatus.setText("输入不合法");
                    configNetworkProxyStatus.setTextFill(GuiPrefabs.COLOR_DANGER);
                }
            }
        });
        configNetworkProxyStatus.setText("未使用代理");
        configNetworkProxyStatus.setTextFill(GuiPrefabs.COLOR_LIGHT_GRAY);
    }

    private void initAbout() {
        aboutQueryUpdate.setOnMouseClicked(e -> {
            /* Foreground check app update */
            new CheckAppUpdateTask(app.body, GuiTask.GuiTaskStyle.COMMON, "manual") {
                @Override
                protected void onHasNewStableVersion(Version stableVersion) {
                    if (style == GuiTaskStyle.HIDDEN)
                        return;
                    JFXDialog dialog = GuiPrefabs.Dialogs.createConfirmDialog(app.body,
                            GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_INFO_ALT, GuiPrefabs.COLOR_INFO),
                            "检查软件更新",
                            "检测到软件有新的版本！",
                            "当前版本 " + appVersion + " 可更新到 " + stableVersion + "\n是否要现在进行更新？",
                            () -> executeAppUpdate());
                    Button gotoButton = new GuiPrefabs.ButtonBuilder()
                            .setText("访问官网")
                            .setOnAction(e -> app.popBrowser(PathConfig.urlDownload))
                            .build();
                    GuiPrefabs.Dialogs.attachAction(dialog, gotoButton, 0);
                    dialog.show();
                }

                @Override
                protected void onUpToDated(Version stableVersion) {
                    if (style == GuiTaskStyle.HIDDEN)
                        return;
                    JFXDialog dialog = GuiPrefabs.Dialogs.createCommonDialog(app.body,
                            GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_SUCCESS_ALT, GuiPrefabs.COLOR_SUCCESS),
                            "检查软件更新",
                            "尚未发现新的正式版本。",
                            "当前版本 " + appVersion + " 已是最新",
                            null);
                    Button forceButton = new GuiPrefabs.ButtonBuilder()
                            .setText("强制重装")
                            .setOnAction(e -> {
                                executeAppUpdate();
                                GuiPrefabs.Dialogs.disposeDialog(dialog);
                            })
                            .build();
                    GuiPrefabs.Dialogs.attachAction(dialog, forceButton, 0);
                    dialog.show();
                }

                @Override
                protected void onAPIFailed() {
                    if (style == GuiTaskStyle.HIDDEN)
                        return;
                    GuiPrefabs.Dialogs.createCommonDialog(parent,
                            GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_DANGER, GuiPrefabs.COLOR_DANGER),
                            "检查软件更新",
                            "服务器返回了无效的消息。",
                            "可能是兼容性问题或服务器不可用。\n您可以访问ArkPets官网或GitHub仓库以查看是否有新版本。",
                            null).show();
                }
            }.start();
        });
        aboutVisitWebsite.setOnMouseClicked(e -> app.popBrowser(Const.PathConfig.urlOfficial));
        aboutReadme.setOnMouseClicked(e -> app.popBrowser(Const.PathConfig.urlReadme));
        aboutGitHub.setOnMouseClicked(e -> app.popBrowser(Const.PathConfig.urlLicense));
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
                return "ArkPets 有新版本可用！点击此处进行更新~";
            }

            @Override
            protected void onClick(MouseEvent event) {
                executeAppUpdate();
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
                        int maxHz = Monitor.getMaxRefreshRate();
                        return "您设置的最大帧率超过了您的显示器的最大刷新率（" + maxHz + " Hz），因此实际帧率并不会得到提高。";
                    }
                };
            }

            @Override
            protected boolean getEntranceVisibleCondition() {
                int configHz = configDisplayFps.getValue() == null ?
                        app.config.display_fps : configDisplayFps.getValue().value();
                int maxHz = Monitor.getMaxRefreshRate();
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

    private void assertDownloadSource(boolean doDoubleCheck, Runnable onDone) {
        SourceStrategy.getStrategy("AppDownload").clearPrimarySource();
        String cdk;
        if ((cdk = app.config.getMcCdk()) != null) {
            Logger.debug("Updater", "Attempting using CDK to fetch resource");
            new McCheckAppUpdateTask(app.body, GuiTask.GuiTaskStyle.STRICT, cdk) {
                @Override
                protected void onReceivedData(JSONObject json) {
                    McQueryVersion value = json.toJavaObject(McQueryVersion.class);
                    try {
                        value.raiseForCode();
                        SourceStrategy.getStrategy("AppDownload").setPrimarySource("MirrorChyan", value.data.url);

                        Logger.info("Updater", "CDK assertion passed");
                        if (onDone != null)
                            onDone.run();
                    } catch (McQueryVersion.McException e) {
                        Logger.warn("Updater", "CDK assertion not passed, " + e.getMessage());
                        if (doDoubleCheck)
                            app.dialogs.popDialog("downloadDialog", (Runnable) () -> assertDownloadSource(false, onDone));
                        else if (onDone != null)
                            onDone.run();
                    }
                }
            }.start();
        } else {
            Logger.info("Updater", "CDK assertion not passed due to not set");
            if (doDoubleCheck)
                app.dialogs.popDialog("downloadDialog", (Runnable) () -> assertDownloadSource(false, onDone));
            else if (onDone != null)
                onDone.run();
        }
    }

    private void executeAppUpdate() {
        if (!StartupConfig.getInstance().isAutoUpdateAvailable()) {
            // Maybe the user is not an installer user
            Logger.warn("Updater", "Maybe auto update is not available");
            GuiPrefabs.Dialogs.createConfirmDialog(app.body,
                    GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_INFO_ALT, GuiPrefabs.COLOR_INFO),
                    "自动更新不可用",
                    "请您手动下载新版程序文件并进行安装",
                    "您使用的似乎不是安装包版本的 ArkPets，或者您的系统环境存在限制，所以无法自动更新哦~ 是否前往 ArkPets 官网下载？",
                    () -> app.popBrowser(PathConfig.urlDownload)).show();
        } else {
            assertDownloadSource(true, () -> new DownloadAppTask(app.body, GuiTask.GuiTaskStyle.COMMON) {
                @Override
                protected void onDownloadedFile(File file) {
                    Logger.info("Updater", "Closing all core instances");
                    HostTray.getInstance().forEachMemberTray(memberTray -> memberTray.sendOperation(SocketData.Operation.LOGOUT));

                    Logger.info("Updater", "Starting update task");
                    new AppInstallTask(app.body, GuiTaskStyle.COMMON, file) {
                        @Override
                        protected void onSucceeded(boolean result) {
                            Logger.info("Launcher", "Updater close request");
                            GuiPrefabs.fadeOutWindow(app.stage, durationNormal, e -> Platform.exit());
                        }
                    }.start();
                }
            }.start());
        }
    }

    private static void setProxy(String host, String port) {
        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", port);
        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", port);
    }
}
