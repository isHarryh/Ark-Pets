/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.guitasks.*;
import cn.harryh.arkpets.utils.*;
import com.jfoenix.controls.*;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static cn.harryh.arkpets.Const.*;


public final class SettingsModule implements Controller<ArkHomeFX> {
    @FXML
    private Pane noticeBox;
    @FXML
    private JFXComboBox<Float> configDisplayScale;
    @FXML
    private JFXComboBox<Integer> configDisplayFps;
    @FXML
    private JFXButton manageModelCheck;
    @FXML
    private JFXButton manageModelFetch;
    @FXML
    private JFXButton manageModelVerify;
    @FXML
    private JFXButton manageModelImport;
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
    private Label aboutQueryUpdate;
    @FXML
    private Label aboutVisitWebsite;
    @FXML
    private Label aboutReadme;
    @FXML
    private Label aboutGitHub;

    private GuiComponents.NoticeBar appVersionNotice;
    private GuiComponents.NoticeBar diskFreeSpaceNotice;
    private GuiComponents.NoticeBar datasetIncompatibleNotice;

    private ArkHomeFX app;

    @Override
    public void initializeWith(ArkHomeFX app) {
        this.app = app;
        initConfigDisplay();
        initModelManage();
        initConfigAdvanced();
        initAbout();
        initNoticeBox();
        initScheduledListener();
    }

    private void initConfigDisplay() {
        configDisplayScale.getItems().setAll(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f);
        configDisplayScale.getSelectionModel().select(app.config.display_scale);
        configDisplayScale.valueProperty().addListener(observable -> {
            if (configDisplayScale.getValue() != null) {
                app.config.display_scale = configDisplayScale.getValue();
                app.config.saveConfig();
            }
        });
        configDisplayFps.getItems().setAll(25, 30, 45, 60);
        configDisplayFps.getSelectionModel().select(Integer.valueOf(app.config.display_fps));
        configDisplayFps.valueProperty().addListener(observable -> {
            if (configDisplayFps.getValue() != null) {
                app.config.display_fps = configDisplayFps.getValue();
                app.config.saveConfig();
            }
        });
    }

    private void initModelManage() {
        manageModelCheck.setOnAction(e -> {
            if (!app.initModelsDataset(true))
                return;
            new CheckModelUpdateTask(app.root, GuiTask.GuiTaskStyle.COMMON).start();
        });
        manageModelFetch.setOnAction(e -> {
            /* Foreground fetch models */
            // Go to [Step 1/3]:
            new DownloadModelsTask(app.root, GuiTask.GuiTaskStyle.COMMON) {
                @Override
                protected void onSucceeded(boolean result){
                    // Go to [Step 2/3]:
                    new UnzipModelsTask(root, GuiTaskStyle.STRICT, PathConfig.tempModelsZipCachePath) {
                        @Override
                        protected void onSucceeded(boolean result) {
                            // Go to [Step 3/3]:
                            new PostUnzipModelTask(root, GuiTaskStyle.STRICT) {
                                @Override
                                protected void onSucceeded(boolean result) {
                                    try {
                                        IOUtils.FileUtil.delete(new File(PathConfig.tempModelsZipCachePath).toPath(), false);
                                    } catch (IOException ex) {
                                        Logger.warn("Task", "The zip file cannot be deleted, because " + ex.getMessage());
                                    }
                                    app.modelReload(true);
                                    app.switchToModelsPane();
                                }
                            }.start();
                        }
                    }.start();
                }
            }.start();
        });
        manageModelVerify.setOnAction(e -> {
            /* Foreground verify models */
            if (!app.initModelsDataset(true))
                return;
            new VerifyModelsTask(app.root, GuiTask.GuiTaskStyle.COMMON, app.modelsDataset).start();
        });
        manageModelImport.setOnAction(e -> {
            // Initialize the file chooser
            Logger.info("ModelManager", "Opening file chooser to import zip file");
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extensionFilter1 = new FileChooser.ExtensionFilter("All Files", "*.*");
            FileChooser.ExtensionFilter extensionFilter2 = new FileChooser.ExtensionFilter("Archives", "*.zip");
            fileChooser.getExtensionFilters().addAll(extensionFilter1, extensionFilter2);
            fileChooser.setSelectedExtensionFilter(extensionFilter2);
            // Handle the chosen file
            File zipFile = fileChooser.showOpenDialog(app.root.getScene().getWindow());
            if (zipFile != null && zipFile.isFile()) {
                Logger.info("ModelManager", "Importing zip file: " + zipFile);
                // Go to [Step 1/2]:
                new UnzipModelsTask(app.root, GuiTask.GuiTaskStyle.STRICT, zipFile.getPath()) {
                    @Override
                    protected void onSucceeded(boolean result) {
                        // Go to [Step 2/2]:
                        new PostUnzipModelTask(root, GuiTaskStyle.STRICT) {
                            @Override
                            protected void onSucceeded(boolean result) {
                                app.modelReload(true);
                                app.switchToModelsPane();
                            }
                        }.start();
                    }
                }.start();
            }
        });
    }

    private void initConfigAdvanced() {
        configLoggingLevel.getItems().setAll(Const.LogConfig.debug, Const.LogConfig.info, Const.LogConfig.warn, Const.LogConfig.error);
        configLoggingLevel.valueProperty().addListener(observable -> {
            if (configLoggingLevel.getValue() != null) {
                Logger.setLevel(Level.toLevel(configLoggingLevel.getValue(), Level.INFO));
                app.config.logging_level = Logger.getLevel().toString();
                app.config.saveConfig();
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
            // Only available in Windows OS
            try {
                Logger.debug("Config", "Request to explore the log dir");
                Runtime.getRuntime().exec("explorer logs");
            } catch (IOException ex) {
                Logger.warn("Config", "Exploring log dir failed");
            }
        });

        configNetworkAgent.setPromptText("示例：0.0.0.0:0");
        configNetworkAgent.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                configNetworkAgentStatus.setText("未使用代理");
                configNetworkAgentStatus.setStyle("-fx-text-fill:" + GuiPrefabs.COLOR_LIGHT_GRAY);
                Logger.info("Network", "Set proxy to none");
                System.setProperty("http.proxyHost", "");
                System.setProperty("http.proxyPort", "");
                System.setProperty("https.proxyHost", "");
                System.setProperty("https.proxyPort", "");
            } else {
                if (newValue.matches(ipPortRegex)) {
                    String[] ipPort = newValue.split(":");
                    System.setProperty("http.proxyHost", ipPort[0]);
                    System.setProperty("http.proxyPort", ipPort[1]);
                    System.setProperty("https.proxyHost", ipPort[0]);
                    System.setProperty("https.proxyPort", ipPort[1]);
                    configNetworkAgentStatus.setText("代理生效中");
                    configNetworkAgentStatus.setStyle("-fx-text-fill:" + GuiPrefabs.COLOR_SUCCESS);
                    Logger.info("Network", "Set proxy to host " + ipPort[0] + ", port " + ipPort[1]);
                } else {
                    configNetworkAgentStatus.setText("输入不合法");
                    configNetworkAgentStatus.setStyle("-fx-text-fill:" + GuiPrefabs.COLOR_DANGER);
                }
            }
        });
        configNetworkAgentStatus.setText("未使用代理");
        configNetworkAgentStatus.setStyle("-fx-text-fill:" + GuiPrefabs.COLOR_LIGHT_GRAY);

        configAutoStartup.setSelected(ArkConfig.StartupConfig.isSetStartup());
        configAutoStartup.setOnAction(e -> {
            if (configAutoStartup.isSelected()) {
                if (ArkConfig.StartupConfig.addStartup()) {
                    GuiPrefabs.DialogUtil.createCommonDialog(app.root, GuiPrefabs.IconUtil.getIcon(GuiPrefabs.IconUtil.ICON_SUCCESS_ALT, GuiPrefabs.COLOR_SUCCESS), "开机自启动", "开机自启动设置成功。",
                            "下次开机时将会自动生成您最后一次启动的桌宠。", null).show();
                } else {
                    if (ArkConfig.StartupConfig.generateScript() == null)
                        GuiPrefabs.DialogUtil.createCommonDialog(app.root, GuiPrefabs.IconUtil.getIcon(GuiPrefabs.IconUtil.ICON_WARNING_ALT, GuiPrefabs.COLOR_WARNING), "开机自启动", "开机自启动设置失败。",
                                "无法确认目标程序的位置，其原因和相关解决方案如下：", "为确保自启动服务的稳定性，直接打开的ArkPets的\".jar\"版启动器，是不支持配置自启动的。请使用exe版的安装包安装ArkPets后运行，或使用zip版的压缩包解压程序文件后运行。另外，当您使用错误的工作目录运行启动器时也可能出现此情况。").show();
                    else
                        GuiPrefabs.DialogUtil.createCommonDialog(app.root, GuiPrefabs.IconUtil.getIcon(GuiPrefabs.IconUtil.ICON_WARNING_ALT, GuiPrefabs.COLOR_WARNING), "开机自启动", "开机自启动设置失败。",
                                "无法写入系统的启动目录，其原因可参见日志文件。", "这有可能是由于权限不足导致的。请尝试关闭反病毒软件，并以管理员权限运行启动器。").show();
                    configAutoStartup.setSelected(false);
                }
            } else {
                ArkConfig.StartupConfig.removeStartup();
            }
        });
    }

    private void initAbout() {
        aboutQueryUpdate.setOnMouseClicked  (e -> {
            /* Foreground check app update */
            new CheckAppUpdateTask(app.root, GuiTask.GuiTaskStyle.COMMON, "manual").start();
        });
        aboutVisitWebsite.setOnMouseClicked (e -> NetUtils.browseWebpage(Const.PathConfig.urlOfficial));
        aboutReadme.setOnMouseClicked       (e -> NetUtils.browseWebpage(Const.PathConfig.urlReadme));
        aboutGitHub.setOnMouseClicked       (e -> NetUtils.browseWebpage(Const.PathConfig.urlLicense));
    }

    private void initNoticeBox() {
        appVersionNotice = new GuiComponents.NoticeBar(noticeBox) {
            @Override
            protected boolean getActivated() {
                return isUpdateAvailable;
            }

            @Override
            protected String getColorString() {
                return GuiPrefabs.COLOR_INFO;
            }

            @Override
            protected String getIconSVGPath() {
                return GuiPrefabs.IconUtil.ICON_UPDATE;
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
        diskFreeSpaceNotice = new GuiComponents.NoticeBar(noticeBox) {
            @Override
            protected boolean getActivated() {
                long freeSpace = new File(".").getFreeSpace();
                return freeSpace < diskFreeSpaceRecommended && freeSpace > 0;
            }

            @Override
            protected String getColorString() {
                return GuiPrefabs.COLOR_WARNING;
            }

            @Override
            protected String getIconSVGPath() {
                return GuiPrefabs.IconUtil.ICON_WARNING_ALT;
            }

            @Override
            protected String getText() {
                return "当前磁盘存储空间不足，可能影响使用体验。";
            }
        };
        datasetIncompatibleNotice = new GuiComponents.NoticeBar(noticeBox) {
            @Override
            protected boolean getActivated() {
                return isDatasetIncompatible;
            }

            @Override
            protected String getColorString() {
                return GuiPrefabs.COLOR_WARNING;
            }

            @Override
            protected String getIconSVGPath() {
                return GuiPrefabs.IconUtil.ICON_WARNING_ALT;
            }

            @Override
            protected String getText() {
                return "模型库可能不兼容当前的 ArkPets 版本，请更新软件。";
            }

            @Override
            protected void onClick(MouseEvent event) {
                NetUtils.browseWebpage(PathConfig.urlDownload);
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
                    datasetIncompatibleNotice.refresh();
                });
                return task;
            }
        };
        ss.setDelay(new Duration(2000));
        ss.setPeriod(new Duration(2000));
        ss.setRestartOnFailure(true);
        ss.start();
    }
}
