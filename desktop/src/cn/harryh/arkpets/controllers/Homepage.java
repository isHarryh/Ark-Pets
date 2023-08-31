/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.utils.*;
import com.alibaba.fastjson.JSONObject;
import com.jfoenix.controls.*;
import org.apache.log4j.Level;

import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.ZipException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;

import static cn.harryh.arkpets.Const.*;
import static cn.harryh.arkpets.utils.ControlUtils.*;
import static cn.harryh.arkpets.utils.PopupUtils.*;


public class Homepage {
    private boolean isNoFilter = true;
    private boolean isHttpsTrustAll = false;
    private boolean isUpdateAvailable = false;
    private boolean isDatasetIncompatible = false;
    public boolean isNewcomer = false;
    public PopupUtils.Handbook trayExitHandbook = new TrayExitHandBook();
    public JavaProcess.UnexpectedExitCodeException lastLaunchFailed = null;

    @FXML
    private AnchorPane root;
    @FXML
    private Pane sidebar;
    @FXML
    private JFXButton menuBtn1;
    @FXML
    private JFXButton menuBtn2;
    @FXML
    private JFXButton menuBtn3;
    @FXML
    private Pane wrapper1;
    @FXML
    private Pane wrapper2;
    @FXML
    private Pane wrapper3;
    @FXML
    private Pane wrapper0;
    @FXML
    private JFXButton startBtn;

    @FXML
    private Pane loadFailureTip;
    @FXML
    private JFXButton searchModelConfirm;
    @FXML
    private JFXButton searchModelReset;
    @FXML
    private JFXButton searchModelRandom;
    @FXML
    private JFXButton searchModelReload;
    @FXML
    private JFXTextField searchModelInput;
    @FXML
    private JFXListView<JFXListCell<AssetCtrl>> searchModelList;
    @FXML
    private JFXComboBox<String> searchModelFilter;
    @FXML
    private Label selectedModelName;
    @FXML
    private Label selectedModelAppellation;
    @FXML
    private Label selectedModelSkinGroupName;
    @FXML
    private Label selectedModelType;
    @FXML
    private ImageView selectedModelView;

    @FXML
    private JFXCheckBox configBehaviorAllowWalk;
    @FXML
    private JFXCheckBox configBehaviorAllowSit;
    @FXML
    private JFXSlider configBehaviorAiActivation;
    @FXML
    private Label configBehaviorAiActivationValue;
    @FXML
    private JFXCheckBox configBehaviorAllowInteract;
    @FXML
    private JFXCheckBox configBehaviorDoPeerRepulsion;
    @FXML
    private JFXCheckBox configDeployMultiMonitors;
    @FXML
    private Label configDeployMultiMonitorsStatus;
    @FXML
    private JFXSlider configDeployMarginBottom;
    @FXML
    private Label configDeployMarginBottomValue;
    @FXML
    private JFXSlider configPhysicGravity;
    @FXML
    private Label configPhysicGravityValue;
    @FXML
    private JFXSlider configPhysicAirFriction;
    @FXML
    private Label configPhysicAirFrictionValue;
    @FXML
    private JFXSlider configPhysicStaticFriction;
    @FXML
    private Label configPhysicStaticFrictionValue;
    @FXML
    private JFXSlider configPhysicSpeedLimitX;
    @FXML
    private Label configPhysicSpeedLimitXValue;
    @FXML
    private JFXSlider configPhysicSpeedLimitY;
    @FXML
    private Label configPhysicSpeedLimitYValue;
    @FXML
    private Label configPhysicRestore;

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
    private JFXComboBox<String> configLoggingLevel;
    @FXML
    private Label exploreLogDir;
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

    private ListCell<AssetCtrl> selectedModelItem;
    private ArrayList<AssetCtrl> foundModelAssets = new ArrayList<>();
    private ArrayList<JFXListCell<AssetCtrl>> foundModelItems = new ArrayList<>();

    public ArkConfig config;
    public JSONObject modelsDatasetFull;
    private NoticeBar appVersionNotice;
    private NoticeBar diskFreeSpaceNotice;
    private NoticeBar datasetIncompatibleNotice;
    private Logger.RealtimeInspector inspector = new Logger.RealtimeInspector(Logger.getCurrentLogFilePath());

    public Homepage() {
    }

    public void initialize() {
        Logger.info("Launcher", "Initializing Homepage");
        wrapper0.setVisible(true);
        popLoading(e -> {
            config = Objects.requireNonNull(ArkConfig.getConfig(), "ArkConfig returns a null instance, please check the config file.");
            if (config.isAllPhysicConfigZeroed())
                isNewcomer = config.isNewcomer();
            initMenuBtn(menuBtn1, 1);
            initMenuBtn(menuBtn2, 2);
            initMenuBtn(menuBtn3, 3);
            initWrapper(1);
            initModelManage();
            initConfigBehavior();
            initConfigDisplay();
            initConfigAdvanced();
            initAbout();
            initNoticeBox();
            initLaunchingStatusListener();
            initScheduledListener();

            dealModelReload(false);
            initModelSearch();
            config.saveConfig();
            menuBtn1.getStyleClass().add("menu-btn-active");
            foregroundCheckUpdate(false, "auto");
        });
    }

    private void initMenuBtn(Button $menuBtn, int $boundIdx) {
        $menuBtn.getStyleClass().setAll("menu-btn");
        $menuBtn.setOnAction(e -> {
            initWrapper($boundIdx);
            menuBtn1.getStyleClass().setAll("menu-btn");
            menuBtn2.getStyleClass().setAll("menu-btn");
            menuBtn3.getStyleClass().setAll("menu-btn");
            $menuBtn.getStyleClass().add("menu-btn-active");
        });
    }

    private void initWrapper(int $activeIdx) {
        List<Pane> wrappers = Arrays.asList(null, wrapper1, wrapper2, wrapper3);
        for (short i = 0; i < wrappers.size(); i++) {
            if (wrappers.get(i) != null) {
                if ($activeIdx == i) {
                    // Show
                    fadeInNode(wrappers.get(i), durationNormal, null);
                } else {
                    // Hide
                    wrappers.get(i).setVisible(false);
                }
            }
        }
    }

    private final ChangeListener<String> filterListener = (observable, oldValue, newValue) -> {
        if (searchModelFilter.getValue() != null) {
            popLoading(e -> {
                isNoFilter = searchModelFilter.getSelectionModel().getSelectedIndex() == 0;
                Logger.info("ModelManager", "Filter \"" + searchModelFilter.getValue() + "\"");
                dealModelSearch(searchModelInput.getText());
                searchModelFilter.getSelectionModel().clearAndSelect(searchModelFilter.getSelectionModel().getSelectedIndex());
            });
        }
    };

    private void initModelSearch() {
        searchModelInput.setPromptText("输入关键字");
        searchModelInput.setOnKeyPressed(e -> {
            if (e.getCode().getName().equals(KeyCode.ENTER.getName()))
                dealModelSearch(searchModelInput.getText());
        });
        searchModelConfirm.setOnAction(e -> dealModelSearch(searchModelInput.getText()));
        searchModelReset.setOnAction(e -> popLoading(ev -> {
            searchModelInput.setText("");
            searchModelInput.requestFocus();
            searchModelFilter.getSelectionModel().select(0);
            dealModelSearch("");
        }));
        searchModelRandom.setOnAction(e -> dealModelRandom());
        searchModelReload.setOnAction(e -> dealModelReload(true));

        searchModelFilter.valueProperty().removeListener(filterListener);
        searchModelFilter.getItems().setAll("全部");
        searchModelFilter.getSelectionModel().select(0);
        if (assertModelLoaded(false)) {
            Set<String> filterTags = modelsDatasetFull.getJSONObject("sortTags").keySet();
            for (String s : filterTags)
                searchModelFilter.getItems().add(modelsDatasetFull.getJSONObject("sortTags").getString(s));
        }
        searchModelFilter.valueProperty().addListener(filterListener);
    }

    private boolean initModelDataset(boolean $doPopNotice) {
        try {
            try {
                // Read dataset file
                modelsDatasetFull = Objects.requireNonNull(JSONObject.parseObject(IOUtils.FileUtil.readString(new File(PathConfig.fileModelsDataPath), charsetDefault)));
                // Assert keys existed
                if (!modelsDatasetFull.containsKey("data"))
                    throw new DatasetException("The key 'data' may not in the dataset.");
                if (!modelsDatasetFull.containsKey("storageDirectory"))
                    throw new DatasetException("The key 'storageDirectory' may not in the dataset.");
                if (!modelsDatasetFull.containsKey("sortTags"))
                    throw new DatasetException("The key 'sortTags' may not in the dataset.");
                try {
                    // Check dataset compatibility
                    if (!modelsDatasetFull.containsKey("arkPetsCompatibility"))
                        throw new DatasetException("The key 'arkPetsCompatibility' may not in the dataset.");
                    int[] acVersionResult = modelsDatasetFull.getObject("arkPetsCompatibility", int[].class);
                    Version acVersion = new Version(acVersionResult);
                    if (appVersion.lessThan(acVersion)) {
                        isDatasetIncompatible = true;
                        Logger.warn("ModelManager", "The model dataset may be incompatible (required " + acVersion + " or newer)");
                    } else {
                        isDatasetIncompatible = false;
                    }
                } catch (Exception ex) {
                    Logger.warn("ModelManager", "Failed to get the compatibility of the model database.");
                }
                Logger.debug("ModelManager", "Initialized model dataset successfully.");
                return true;
            } catch (Exception e) {
                modelsDatasetFull = null;
                throw e;
            }
        } catch (FileNotFoundException e) {
            Logger.warn("ModelManager", "Failed to initialize model dataset due to file not found. (" + e.getMessage() + ")");
            if ($doPopNotice)
                popNotice(IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING), "模型载入失败", "模型未成功载入：未找到数据集。",
                        "模型数据集文件 " + PathConfig.fileModelsDataPath + " 可能不在工作目录下。\n请先前往 [选项] 进行模型下载。", null).show();
        } catch (DatasetException e) {
            Logger.warn("ModelManager", "Failed to initialize model dataset due to dataset parsing error. (" + e.getMessage() + ")");
            if ($doPopNotice)
                popNotice(IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING), "模型载入失败", "模型未成功载入：数据集解析失败。",
                        "模型数据集可能不完整，或无法被启动器正确识别。请尝试更新模型或更新软件。", null).show();
        } catch (IOException e) {
            Logger.error("ModelManager", "Failed to initialize model dataset due to unknown reasons, details see below.", e);
            if ($doPopNotice)
                popNotice(IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING), "模型载入失败", "模型未成功载入：发生意外错误。",
                        "失败原因概要：" + e.getLocalizedMessage(), null).show();
        }
        return false;
    }

    private boolean initModelAssets(boolean $doPopNotice) {
        if (!initModelDataset($doPopNotice))
            return false;
        try {
            // Find every model assets.
            ArrayList<AssetCtrl> foundModelAssets = new ArrayList<>();
            JSONObject modelsDatasetStorageDirectory = modelsDatasetFull.getJSONObject("storageDirectory");
            JSONObject modelsDatasetData = modelsDatasetFull.getJSONObject("data");
            for (String key : modelsDatasetStorageDirectory.keySet())
                foundModelAssets.addAll(AssetCtrl.getAllAssetCtrls(new File(modelsDatasetStorageDirectory.getString(key)), modelsDatasetData));
            this.foundModelAssets = AssetCtrl.sortAssetCtrls(foundModelAssets);
            if (this.foundModelAssets.size() == 0)
                throw new IOException("Found no assets in the target directories.");
            // Write models to menu items.
            ArrayList<JFXListCell<AssetCtrl>> foundModelItems = new ArrayList<>();
            searchModelList.getSelectionModel().getSelectedItems().addListener((ListChangeListener<JFXListCell<AssetCtrl>>)
                    (observable -> observable.getList().forEach((Consumer<JFXListCell<AssetCtrl>>)cell -> selectModel(cell.getItem(), cell))));
            searchModelList.setFixedCellSize(30);
            for (AssetCtrl asset : this.foundModelAssets)
                foundModelItems.add(getMenuItem(asset, searchModelList));
            this.foundModelItems = foundModelItems;
            Logger.debug("ModelManager", "Initialized model assets successfully.");
            return true;
        } catch (IOException e) {
            foundModelAssets = new ArrayList<>();
            foundModelItems = new ArrayList<>();
            Logger.error("ModelManager", "Failed to initialize model assets due to unknown reasons, details see below.", e);
            if ($doPopNotice)
                popNotice(IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING), "模型载入失败", "模型未成功载入：读取模型列表失败。",
                        "失败原因概要：" + e.getLocalizedMessage(), null).show();
        }
        return false;
    }

    private void initConfigBehavior() {
        configBehaviorAllowWalk.setSelected(config.behavior_allow_walk);
        configBehaviorAllowWalk.setOnAction(e -> {
            config.behavior_allow_walk = configBehaviorAllowWalk.isSelected();
            config.saveConfig();
        });
        configBehaviorAllowSit.setSelected(config.behavior_allow_sit);
        configBehaviorAllowSit.setOnAction(e -> {
            config.behavior_allow_sit = configBehaviorAllowSit.isSelected();
            config.saveConfig();
        });

        SliderUtil.SliderSetup<Integer> setupBehaviorAiActivation = new SliderUtil.SimpleIntegerSliderSetup(configBehaviorAiActivation);
        setupBehaviorAiActivation
                .setDisplay(configBehaviorAiActivationValue, "%d 级", "活跃级别 (activation level)")
                .setRange(0, 8)
                .setTicks(1, 0)
                .setSliderValue(config.behavior_ai_activation)
                .setOnChanged((observable, oldValue, newValue) -> {
                    config.behavior_ai_activation = setupBehaviorAiActivation.getValidatedValue();
                    config.saveConfig();
                });

        configBehaviorAllowInteract.setSelected(config.behavior_allow_interact);
        configBehaviorAllowInteract.setOnAction(e -> {
            config.behavior_allow_interact = configBehaviorAllowInteract.isSelected();
            config.saveConfig();
        });
        configBehaviorDoPeerRepulsion.setSelected(config.behavior_do_peer_repulsion);
        configBehaviorDoPeerRepulsion.setOnAction(e -> {
            config.behavior_do_peer_repulsion = configBehaviorDoPeerRepulsion.isSelected();
            config.saveConfig();
        });

        configDeployMultiMonitors.setSelected(config.display_multi_monitors);
        configDeployMultiMonitors.setOnAction(e -> {
            config.display_multi_monitors = configDeployMultiMonitors.isSelected();
            config.saveConfig();
        });

        SliderUtil.SliderSetup<Integer> setupDeployMarginBottom = new SliderUtil.SimpleIntegerSliderSetup(configDeployMarginBottom);
        setupDeployMarginBottom
                .setDisplay(configDeployMarginBottomValue, "%d px", "像素 (pixel)")
                .setRange(0, 120)
                .setTicks(10, 10)
                .setSliderValue(config.display_margin_bottom)
                .setOnChanged((observable, oldValue, newValue) -> {
                    config.display_margin_bottom = setupDeployMarginBottom.getValidatedValue();
                    config.saveConfig();
                });
        SliderUtil.SliderSetup<Integer> setupPhysicGravity = new SliderUtil.SimpleMultipleIntegerSliderSetup(configPhysicGravity, 10);
        setupPhysicGravity
                .setDisplay(configPhysicGravityValue, "%d px/s²", "像素每平方秒 (pixel/s²)")
                .setRange(0, 1000)
                .setTicks(100, 10)
                .setSliderValue(config.physic_gravity_acc)
                .setOnChanged((observable, oldValue, newValue) -> {
                    config.physic_gravity_acc = setupPhysicGravity.getValidatedValue();
                    config.saveConfig();
                });
        SliderUtil.SliderSetup<Integer> setupPhysicAirFriction = new SliderUtil.SimpleMultipleIntegerSliderSetup(configPhysicAirFriction, 10);
        setupPhysicAirFriction
                .setDisplay(configPhysicAirFrictionValue, "%d px/s²", "像素每平方秒 (pixel/s²)")
                .setRange(0, 1000)
                .setTicks(100, 10)
                .setSliderValue(config.physic_air_friction_acc)
                .setOnChanged((observable, oldValue, newValue) -> {
                    config.physic_air_friction_acc = setupPhysicAirFriction.getValidatedValue();
                    config.saveConfig();
                });
        SliderUtil.SliderSetup<Integer> setupPhysicStaticFriction = new SliderUtil.SimpleMultipleIntegerSliderSetup(configPhysicStaticFriction, 10);
        setupPhysicStaticFriction
                .setDisplay(configPhysicStaticFrictionValue, "%d px/s²", "像素每平方秒 (pixel/s²)")
                .setRange(0, 1000)
                .setTicks(100, 10)
                .setSliderValue(config.physic_static_friction_acc)
                .setOnChanged((observable, oldValue, newValue) -> {
                    config.physic_static_friction_acc = setupPhysicStaticFriction.getValidatedValue();
                    config.saveConfig();
                });
        SliderUtil.SliderSetup<Integer> setupPhysicSpeedLimitX = new SliderUtil.SimpleMultipleIntegerSliderSetup(configPhysicSpeedLimitX, 10);
        setupPhysicSpeedLimitX
                .setDisplay(configPhysicSpeedLimitXValue, "%d px/s", "像素每秒 (pixel/s)")
                .setRange(0, 1000)
                .setTicks(100, 10)
                .setSliderValue(config.physic_speed_limit_x)
                .setOnChanged((observable, oldValue, newValue) -> {
                    config.physic_speed_limit_x = setupPhysicSpeedLimitX.getValidatedValue();
                    config.saveConfig();
                });
        SliderUtil.SliderSetup<Integer> setupPhysicSpeedLimitY = new SliderUtil.SimpleMultipleIntegerSliderSetup(configPhysicSpeedLimitY, 10);
        setupPhysicSpeedLimitY
                .setDisplay(configPhysicSpeedLimitYValue, "%d px/s", "像素每秒 (pixel/s)")
                .setRange(0, 1000)
                .setTicks(100, 10)
                .setSliderValue(config.physic_speed_limit_y)
                .setOnChanged((observable, oldValue, newValue) -> {
                    config.physic_speed_limit_y = setupPhysicSpeedLimitY.getValidatedValue();
                    config.saveConfig();
                });
        EventHandler<MouseEvent> configPhysicRestoreEvent = e -> {
            ArkConfig defaults = ArkConfig.defaultConfig;
            setupPhysicGravity.setSliderValue(defaults.physic_gravity_acc);
            setupPhysicAirFriction.setSliderValue(defaults.physic_air_friction_acc);
            setupPhysicStaticFriction.setSliderValue(defaults.physic_static_friction_acc);
            setupPhysicSpeedLimitX.setSliderValue(defaults.physic_speed_limit_x);
            setupPhysicSpeedLimitY.setSliderValue(defaults.physic_speed_limit_y);
            Logger.info("Config", "Physic params restored");
        };
        configPhysicRestore.setOnMouseClicked(e -> {
            configPhysicRestoreEvent.handle(e);
            initWrapper(2);
        });
        if (config.isAllPhysicConfigZeroed())
            configPhysicRestoreEvent.handle(null);
    }

    private void initConfigDisplay() {
        configDisplayScale.getItems().setAll(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f);
        configDisplayScale.getSelectionModel().select(config.display_scale);
        configDisplayScale.valueProperty().addListener(observable -> {
            if (configDisplayScale.getValue() != null) {
                config.display_scale = configDisplayScale.getValue();
                config.saveConfig();
            }
        });
        configDisplayFps.getItems().setAll(25, 30, 45, 60);
        configDisplayFps.getSelectionModel().select(Integer.valueOf(config.display_fps));
        configDisplayFps.valueProperty().addListener(observable -> {
            if (configDisplayFps.getValue() != null) {
                config.display_fps = configDisplayFps.getValue();
                config.saveConfig();
            }
        });
    }

    private void initModelManage() {
        manageModelCheck.setOnAction(e -> foregroundCheckModels());
        manageModelFetch.setOnAction(e -> foregroundFetchModels());
        manageModelVerify.setOnAction(e -> foregroundVerifyModels());
    }

    private void initConfigAdvanced() {
        configLoggingLevel.getItems().setAll(LogConfig.debug, LogConfig.info, LogConfig.warn, LogConfig.error);
        configLoggingLevel.valueProperty().addListener(observable -> {
            if (configLoggingLevel.getValue() != null) {
                Logger.setLevel(Level.toLevel(configLoggingLevel.getValue(), Level.INFO));
                config.logging_level = Logger.getLevel().toString();
                config.saveConfig();
            }
        });
        String level = config.logging_level;
        List<String> args = Arrays.asList(ArgPending.argCache);
        if (args.contains(LogConfig.errorArg))
            level = LogConfig.error;
        else if (args.contains(LogConfig.warnArg))
            level = LogConfig.warn;
        else if (args.contains(LogConfig.infoArg))
            level = LogConfig.info;
        else if (args.contains(LogConfig.debugArg))
            level = LogConfig.debug;
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

        configAutoStartup.setSelected(ArkConfig.StartupConfig.isSetStartup());
        configAutoStartup.setOnAction(e -> {
            if (configAutoStartup.isSelected()) {
                if (ArkConfig.StartupConfig.addStartup()) {
                    popNotice(IconUtil.getIcon(IconUtil.ICON_SUCCESS_ALT, COLOR_SUCCESS), "开机自启动", "开机自启动设置成功。",
                            "下次开机时将会自动生成您最后一次启动的桌宠。", null).show();
                } else {
                    if (ArkConfig.StartupConfig.generateScript() == null)
                        popNotice(IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING), "开机自启动", "开机自启动设置失败。",
                                "无法确认目标程序的位置，其原因和相关解决方案如下：", "为确保自启动服务的稳定性，直接打开的ArkPets的\".jar\"版启动器，是不支持配置自启动的。请使用exe版的安装包安装ArkPets后运行，或使用zip版的压缩包解压程序文件后运行。另外，当您使用错误的工作目录运行启动器时也可能出现此情况。").show();
                    else
                        popNotice(IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING), "开机自启动", "开机自启动设置失败。",
                                "无法写入系统的启动目录，其原因可参见日志文件。", "这有可能是由于权限不足导致的。请尝试关闭反病毒软件，并以管理员权限运行启动器。").show();
                    configAutoStartup.setSelected(false);
                }
            } else {
                ArkConfig.StartupConfig.removeStartup();
            }
        });
    }

    private void initAbout() {
        aboutQueryUpdate.setOnMouseClicked  (e -> foregroundCheckUpdate(true, "manual"));
        aboutVisitWebsite.setOnMouseClicked (e -> NetUtils.browseWebpage(PathConfig.urlOfficial));
        aboutReadme.setOnMouseClicked       (e -> NetUtils.browseWebpage(PathConfig.urlReadme));
        aboutGitHub.setOnMouseClicked       (e -> NetUtils.browseWebpage(PathConfig.urlLicense));
    }

    private void initNoticeBox() {
        appVersionNotice = new NoticeBar(noticeBox) {
            @Override
            protected boolean getActivated() {
                return isUpdateAvailable;
            }

            @Override
            protected String getColorString() {
                return COLOR_INFO;
            }

            @Override
            protected String getIconSVGPath() {
                return IconUtil.ICON_UPDATE;
            }

            @Override
            protected String getText() {
                return "ArkPets 有新版本可用！点击此处前往下载~";
            }

            @Override
            protected void onClick(MouseEvent event) {
                NetUtils.browseWebpage(PathConfig.urlDownload);
            }
        };
        diskFreeSpaceNotice = new NoticeBar(noticeBox) {
            @Override
            protected boolean getActivated() {
                long freeSpace = new File(".").getFreeSpace();
                return freeSpace < diskFreeSpaceRecommended && freeSpace > 0;
            }

            @Override
            protected String getColorString() {
                return COLOR_WARNING;
            }

            @Override
            protected String getIconSVGPath() {
                return IconUtil.ICON_WARNING_ALT;
            }

            @Override
            protected String getText() {
                return "当前磁盘存储空间不足，可能影响使用体验。";
            }
        };
        datasetIncompatibleNotice = new NoticeBar(noticeBox) {
            @Override
            protected boolean getActivated() {
                return isDatasetIncompatible;
            }

            @Override
            protected String getColorString() {
                return COLOR_WARNING;
            }

            @Override
            protected String getIconSVGPath() {
                return IconUtil.ICON_WARNING_ALT;
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

    private void initLaunchingStatusListener() {
        ScheduledService<Boolean> ss = new ScheduledService<>() {
            @Override
            protected Task<Boolean> createTask() {
                Task<Boolean> task = new Task<>() {
                    @Override
                    protected Boolean call() throws Exception {
                        if (lastLaunchFailed != null) {
                            Exception e = lastLaunchFailed;
                            lastLaunchFailed = null;
                            throw e;
                        }
                        return false;
                    }
                };
                task.setOnFailed(e -> popError(task.getException()).show());
                return task;
            }
        };
        ss.setDelay(new Duration(1000));
        ss.setPeriod(new Duration(500));
        ss.setRestartOnFailure(true);
        ss.start();
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
                    configDeployMultiMonitorsStatus.setText("检测到 " + ArkConfig.Monitor.getMonitors().length + " 个显示屏");
                });
                return task;
            }
        };
        ss.setDelay(new Duration(2000));
        ss.setPeriod(new Duration(1000));
        ss.setRestartOnFailure(true);
        ss.start();
    }

    public JFXDialog foregroundTask(Task<Boolean> $task, String $header, String $defaultContent, Boolean $cancelable) {
        JFXDialog dialog = DialogUtil.createCenteredDialog(root, false);
        ProgressBar bar = new ProgressBar(-1);
        bar.setPrefSize(root.getWidth() * 0.6, 10);

        VBox content = new VBox();
        Label h2 = (Label)DialogUtil.getPrefabsH2($header);
        Label h3 = (Label)DialogUtil.getPrefabsH3($defaultContent);
        content.setSpacing(5);
        content.getChildren().add(h2);
        content.getChildren().add(new Separator());
        content.getChildren().add(h3);

        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setHeading(bar);
        layout.setBody(content);
        layout.setActions(DialogUtil.getOkayButton(dialog, root));
        dialog.setContent(layout);

        if ($cancelable) {
            JFXButton cancel = DialogUtil.getCancelButton(dialog, root);
            cancel.setOnAction(e -> {
                $task.cancel();
                DialogUtil.disposeDialog(dialog, root);
            });
            layout.setActions(cancel);
        } else {
            layout.setActions(List.of());
        }
        dialog.show();

        final double[] cachedProgress = {-1};
        $task.progressProperty().addListener((observable, oldValue, newValue) -> {
            if (Math.abs((double)newValue - cachedProgress[0]) >= 0.001) {
                cachedProgress[0] = (double)newValue;
                bar.setProgress((double)newValue);
            }
        });
        $task.messageProperty().addListener(((observable, oldValue, newValue) -> h3.setText(newValue)));
        $task.setOnCancelled(e -> {
            Logger.info("Task", "Foreground dialog task was cancelled.");
            DialogUtil.disposeDialog(dialog, root);
        });
        $task.setOnFailed(e -> {
            Logger.error("Task", "Foreground dialog task failed, details see below.", $task.getException());
            popError($task.getException()).show();
            DialogUtil.disposeDialog(dialog, root);
        });
        $task.setOnSucceeded(e -> {
            Logger.info("Task", "Foreground dialog task completed.");
            DialogUtil.disposeDialog(dialog, root);
        });
        Thread thread = new Thread($task);
        thread.start();
        return dialog;
    }

    public void popLoading(EventHandler<ActionEvent> $onLoading) {
        fadeInNode(wrapper0, durationFast, e -> {
            try {
                $onLoading.handle(e);
            } catch (Exception ex) {
                Logger.error("Task", "Foreground loading task failed, details see below.", ex);
            }
            fadeOutNode(wrapper0, durationFast, null);
        });
    }

    public JFXDialog popError(Throwable $e) {
        JFXDialog dialog = DialogUtil.createCenteredDialog(root, false);

        VBox content = new VBox();
        Label h2 = (Label)DialogUtil.getPrefabsH2("啊哦~ ArkPets启动器抛出了一个异常。");
        Label h3 = (Label)DialogUtil.getPrefabsH3("请重试操作，或查看帮助文档与日志。如需联系开发者，请提供下述信息：");
        content.setSpacing(5);
        content.getChildren().add(h2);
        content.getChildren().add(new Separator());
        content.getChildren().add(h3);

        JFXTextArea textArea = new JFXTextArea();
        textArea.setEditable(false);
        textArea.setScrollTop(0);
        textArea.getStyleClass().add("popup-detail-field");
        textArea.appendText("[Exception] " + $e.getClass().getSimpleName() + "\n");
        textArea.appendText("[Message] " + ($e.getLocalizedMessage() != null ? $e.getLocalizedMessage() : "") + "\n");
        textArea.appendText("\n[StackTrace]\nCaused by " + $e.getClass().getCanonicalName() + ": " + $e.getMessage() + "\n");
        for (StackTraceElement ste : $e.getStackTrace())
            textArea.appendText("  at " + ste + "\n");
        content.getChildren().add(textArea);

        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setHeading(DialogUtil.getHeading(IconUtil.getIcon(IconUtil.ICON_DANGER, COLOR_DANGER), "发生异常", COLOR_DANGER));
        layout.setBody(content);
        layout.setActions(DialogUtil.getOkayButton(dialog, root));
        dialog.setContent(layout);

        if ($e instanceof JavaProcess.UnexpectedExitCodeException) {
            h2.setText("检测到桌宠异常退出");
            h3.setText("桌宠运行时异常退出。如果该现象是在启动后立即发生的，可能是因为暂不支持该模型。您可以稍后重试或查看日志文件。");
        }
        if ($e instanceof FileNotFoundException) {
            h3.setText("未找到某个文件或目录，请稍后重试。详细信息：");
        }
        if ($e instanceof NetUtils.HttpResponseCodeException) {
            h2.setText("神经递质接收异常");
            if (((NetUtils.HttpResponseCodeException)$e).isRedirection()) {
                h3.setText("请求的网络地址被重定向转移。详细信息：");
            }
            if (((NetUtils.HttpResponseCodeException)$e).isClientError()) {
                h3.setText("可能是客户端引发的网络错误，详细信息：");
                if (((NetUtils.HttpResponseCodeException)$e).getCode() == 403) {
                    h3.setText("(403)访问被拒绝。详细信息：");
                }
                if (((NetUtils.HttpResponseCodeException)$e).getCode() == 404) {
                    h3.setText("(404)找不到要访问的目标。详细信息：");
                }
            }
            if (((NetUtils.HttpResponseCodeException)$e).isServerError()) {
                h3.setText("可能是服务器引发的网络错误，详细信息：");
                if (((NetUtils.HttpResponseCodeException)$e).getCode() == 500) {
                    h3.setText("(500)服务器发生故障，请稍后重试。详细信息");
                }
            }
        }
        if ($e instanceof UnknownHostException) {
            h2.setText("无法建立神经连接");
            h3.setText("找不到服务器地址。可能是因为网络未连接或DNS解析失败，请尝试更换网络环境、检查防火墙和代理设置。");
        }
        if ($e instanceof ConnectException) {
            h2.setText("无法建立神经连接");
            h3.setText("在建立连接时发生了问题。请尝试更换网络环境、检查防火墙和代理设置。");
        }
        if ($e instanceof SocketException) {
            h2.setText("无法建立神经连接");
            h3.setText("在访问套接字时发生了问题。请尝试更换网络环境、检查防火墙和代理设置。");
        }
        if ($e instanceof SocketTimeoutException) {
            h2.setText("神经递质接收异常");
            h3.setText("接收数据超时。请尝试更换网络环境、检查防火墙和代理设置。");
        }
        if ($e instanceof SSLException) {
            h2.setText("无法建立安全的神经连接");
            h3.setText("SSL证书错误，请检查代理设置。您也可以尝试[信任]所有证书后重试刚才的操作。");
            JFXButton apply = DialogUtil.getTrustButton(dialog, root);
            apply.setOnAction(e -> {
                isHttpsTrustAll = true;
                DialogUtil.disposeDialog(dialog, root);
            });
            layout.setActions(DialogUtil.getOkayButton(dialog, root), apply);
        }
        if ($e instanceof ZipException) {
            h3.setText("压缩文件相关错误。推测可能是下载源问题，请再次重试。");
        }
        //dialog.show();
        return dialog;
    }

    public JFXDialog popNotice(Node $graphic, String $title, String $header, String $content, String $detail) {
        JFXDialog dialog = DialogUtil.createCenteredDialog(root, false);
        VBox content = new VBox();
        Label h2 = (Label)DialogUtil.getPrefabsH2($header);
        Label h3 = (Label)DialogUtil.getPrefabsH3($content);
        content.setSpacing(5);
        content.getChildren().add(h2);
        content.getChildren().add(new Separator());
        content.getChildren().add(h3);

        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setHeading(DialogUtil.getHeading($graphic, $title, COLOR_LIGHT_GRAY));
        layout.setBody(content);
        layout.setActions(DialogUtil.getOkayButton(dialog, root));
        dialog.setContent(layout);

        if ($detail != null && $detail.length() > 0) {
            JFXTextArea textArea = new JFXTextArea();
            textArea.setEditable(false);
            textArea.setScrollTop(0);
            textArea.getStyleClass().add("popup-detail-field");
            textArea.appendText($detail);
            content.getChildren().add(textArea);
        }
        //dialog.show();
        return dialog;
    }

    private void foregroundCheckModels() {
        try {
            Files.createDirectories(new File(PathConfig.tempDirPath).toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!initModelDataset(true))
            return;
        Task<Boolean> task = createDownloadTask(false, PathConfig.urlModelsData, PathConfig.tempDirPath + PathConfig.fileModelsDataPath);
        JFXDialog dialog = foregroundTask(task, "正在下载模型版本信息...", "", true);
        task.setOnSucceeded(e -> {
            // When finished downloading the remote repo ver-info:
            DialogUtil.disposeDialog(dialog, root);
            try {
                String versionDescription;
                try {
                    // Try to parse the remote repo ver-info
                    JSONObject newModelsDataset = JSONObject.parseObject(IOUtils.FileUtil.readString(new File(PathConfig.tempDirPath + PathConfig.fileModelsDataPath), charsetDefault));
                    versionDescription = newModelsDataset.getString("gameDataVersionDescription");
                } catch (Exception ex) {
                    // When failed to parse the remote repo ver-info
                    versionDescription = "unknown";
                    popNotice(IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING), "检查模型更新", "无法判断模型仓库版本。",
                            "因发生错误，无法判断远程模型仓库版本。", null).show();
                    Logger.error("Checker", "Unable to parse remote model repo version, details see below.", ex);
                }
                // When finished parsing the remote ver-info:
                // TODO do judgment more precisely
                // Compare the remote ver-info and the local ver-info by MD5
                if (IOUtils.FileUtil.getMD5(new File(PathConfig.fileModelsDataPath)).equals(IOUtils.FileUtil.getMD5(new File(PathConfig.tempDirPath + PathConfig.fileModelsDataPath)))) {
                    popNotice(IconUtil.getIcon(IconUtil.ICON_SUCCESS_ALT, COLOR_SUCCESS), "检查模型更新", "当前模型版本与远程仓库一致。",
                            "无需进行模型仓库更新。", "提示：远程模型仓库的版本不一定和游戏官方是同步更新的。\n模型仓库版本描述：\n" + versionDescription).show();
                    Logger.info("Checker", "Model repo version check finished (up-to-dated)");
                } else {
                    // If the result of comparison is "not the same"
                    String oldVersionDescription;
                    try {
                        // Try to parse the local repo ver-info
                        JSONObject oldModelsDataset = JSONObject.parseObject(IOUtils.FileUtil.readString(new File(PathConfig.fileModelsDataPath), charsetDefault));
                        oldVersionDescription = oldModelsDataset.getString("gameDataVersionDescription");
                    } catch (Exception ex) {
                        // When failed to parse the remote local ver-info
                        oldVersionDescription = "unknown";
                        popNotice(IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING), "检查模型更新", "无法判断模型仓库版本。",
                                "因发生错误，无法判断本地模型仓库版本。", null).show();
                        Logger.error("Checker", "Unable to parse local model repo version, details see below.", ex);
                    }
                    popNotice(IconUtil.getIcon(IconUtil.ICON_INFO_ALT, COLOR_INFO), "检查模型更新", "本地模型版本与远程仓库有差异。",
                            "可以重新下载模型，即可更新模型版本。", "远程模型仓库版本描述：\n" + versionDescription + "\n\n本地模型仓库版本描述：\n" + oldVersionDescription).show();
                    Logger.info("Checker", "Model repo version check finished (not up-to-dated)");
                }
            } catch (IOException ex) {
                Logger.warn("Checker", "Model repo version check failed");
                popError(ex).show();
            }
        });
    }

    private void foregroundFetchModels() {
        try {
            Files.createDirectories(new File(PathConfig.tempDirPath).toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //1
        Task<Boolean> task1 = createDownloadTask(true, PathConfig.urlModelsZip, PathConfig.tempModelsZipCachePath);
        JFXDialog task1dialog = foregroundTask(task1, "正在下载模型资源文件...", "", true);
        task1.setOnSucceeded(e1 -> {
            DialogUtil.disposeDialog(task1dialog, root);
            //2
            Task<Boolean> task2 = createUnzipTask(PathConfig.tempModelsZipCachePath, PathConfig.tempModelsUnzipDirPath);
            JFXDialog task2dialog = foregroundTask(task2, "正在解压模型资源文件...", "这可能需要十几秒", false);
            task2.setOnSucceeded(e2 -> {
                DialogUtil.disposeDialog(task2dialog, root);
                //3
                Task<Boolean> task3 = createModelsMovingTask(PathConfig.tempModelsUnzipDirPath, PathConfig.fileModelsDataPath);
                JFXDialog task3dialog = foregroundTask(task3, "正在应用模型更新...", "即将完成", false);
                task3.setOnSucceeded(e3 -> {
                    DialogUtil.disposeDialog(task3dialog, root);
                    dealModelReload(true);
                });
            });
        });
    }

    private void foregroundVerifyModels() {
        if (!initModelDataset(true))
            return;
        final Node[] dialogGraphic = new Node[1];
        final String[] dialogHeader = new String[1];
        final String[] dialogContent = new String[1];
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                ArrayList<File> pendingDirs = new ArrayList<>();
                JSONObject modelsDatasetData = modelsDatasetFull.getJSONObject("data");
                for (AssetCtrl assetCtrl : foundModelAssets)
                    pendingDirs.add(new File(assetCtrl.getLocation()));

                Thread.sleep(100);
                boolean flag = false;
                AssetCtrl.AssetVerifier assetVerifier = new AssetCtrl.AssetVerifier(modelsDatasetData);
                for (int i = 0; i < pendingDirs.size(); i++) {
                    this.updateProgress(i, pendingDirs.size());
                    File file = pendingDirs.get(i);
                    AssetCtrl.AssetStatus result = assetVerifier.verify(file);
                    if (result == AssetCtrl.AssetStatus.VALID) {
                        Logger.info("Checker", "Model repo check finished (not integral)");
                        dialogGraphic[0] = IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING);
                        dialogHeader[0] = "已发现问题，模型资源可能不完整";
                        dialogContent[0] = "资源 " + file.getPath() + " 可能不存在，重新下载模型文件可能解决此问题。";
                        flag = true;
                        break;
                    } else if (result == AssetCtrl.AssetStatus.EXISTED) {
                        Logger.info("Checker", "Model repo check finished (checksum mismatch)");
                        dialogGraphic[0] = IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING);
                        dialogHeader[0] = "已发现问题，模型资源可能不完整";
                        dialogContent[0] = "资源 " + file.getPath() + " 可能缺少部分文件，重新下载模型文件可能解决此问题。";
                        flag = true;
                        break;
                    } else if (this.isCancelled()) {
                        Logger.info("Checker", "Model repo check was cancelled in verification stage.");
                        return false;
                    }
                }

                if (!flag) {
                    Logger.info("Checker", "Model repo check finished (okay)");
                    dialogGraphic[0] = IconUtil.getIcon(IconUtil.ICON_SUCCESS_ALT, COLOR_SUCCESS);
                    dialogHeader[0] = "模型资源是完整的。";
                    dialogContent[0] = "这只能说明本地的模型资源是完整的，但不一定是最新的。";
                }
                return true;
            }
        };
        task.stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(Worker.State.SUCCEEDED))
                if (dialogGraphic[0] != null && dialogHeader[0] != null && dialogContent[0] != null)
                    popNotice(dialogGraphic[0], "验证资源完整性", dialogHeader[0], dialogContent[0], null).show();
        });
        foregroundTask(task, "正在验证模型资源完整性...", "这可能需要数秒钟", true);
    }

    private void foregroundCheckUpdate(boolean $popNotice, String $sourceStr) {
        try {
            Files.createDirectories(new File(PathConfig.tempDirPath).toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        isUpdateAvailable = false;
        String queryStr = "?type=queryVersion&cliVer=" + appVersion + "&source=" + $sourceStr;
        Task<Boolean> task = createDownloadTask(PathConfig.urlApi + queryStr, PathConfig.tempQueryVersionCachePath);
        JFXDialog dialog = null;
        if ($popNotice)
            dialog = foregroundTask(task, "正在下载软件版本信息...", "", true);
        JFXDialog finalDialog = dialog;
        task.setOnSucceeded(e -> {
            // When finished downloading the latest app ver-info:
            if ($popNotice && finalDialog != null)
                DialogUtil.disposeDialog(finalDialog, root);
            try {
                // Try to parse the latest app ver-info
                JSONObject queryVersionResult = Objects.requireNonNull(JSONObject.parseObject(IOUtils.FileUtil.readByte(new File(PathConfig.tempQueryVersionCachePath))));
                // TODO show in-test version
                if (queryVersionResult.getString("msg").equals("success")) {
                    // If the response status is "success"
                    int[] stableVersionResult = queryVersionResult.getJSONObject("data").getObject("stableVersion", int[].class);
                    Version stableVersion = new Version(stableVersionResult);
                    if (appVersion.lessThan(stableVersion)) {
                        isUpdateAvailable = true;
                        if ($popNotice)
                            popNotice(IconUtil.getIcon(IconUtil.ICON_INFO_ALT, COLOR_INFO), "检查软件更新", "检测到软件有新的版本！",
                                    "当前版本 " + appVersion + " 可更新到 " + stableVersion + "\n请访问ArkPets官网或GitHub下载新的安装包。", null).show();
                    } else {
                        if ($popNotice)
                            popNotice(IconUtil.getIcon(IconUtil.ICON_SUCCESS_ALT, COLOR_SUCCESS), "检查软件更新", "尚未发现新的正式版本。",
                                    "当前版本 " + appVersion + " 已是最新", null).show();
                    }
                    Logger.info("Checker", "Application version check finished, newest: " + stableVersion);
                } else {
                    // If the response status isn't "success"
                    Logger.warn("Checker", "Application version check failed (api failed)");
                    if ($popNotice)
                        popNotice(IconUtil.getIcon(IconUtil.ICON_DANGER_ALT, COLOR_DANGER), "检查软件更新", "服务器返回了无效的消息。",
                                "可能是兼容性问题或服务器不可用。\n您可以访问ArkPets官网或GitHub，手动查看是否有新版本。", null).show();
                }
            } catch (IOException ex) {
                Logger.warn("Checker", "Application version check failed (parsing failed)");
                if ($popNotice)
                    popError(ex).show();
            }
        });
        if (!$popNotice) {
            Thread thread = new Thread(task);
            thread.start();
        }
    }

    public Task<Boolean> createDownloadTask(boolean $isArchive, String $remotePathSuffix, String $localPath) {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                this.updateMessage("正在选择最佳线路");
                Logger.info("Network", "Testing real delay");
                NetUtils.GitHubSource[] sources = NetUtils.GitHubSource.sortByOverallAvailability(NetUtils.ghSources);
                NetUtils.GitHubSource source = sources[0];

                try {
                    Logger.info("Network", "Selected the most available source \"" + source.tag + "\" (" + source.delay + "ms)");
                    String remotePath = ($isArchive ? source.archivePreUrl : source.rawPreUrl) + $remotePathSuffix;
                    Logger.info("Network", "Fetching " + remotePath + " to " + $localPath);
                    this.updateMessage("正在尝试与 " + source.tag + " 建立连接");

                    BufferedInputStream bis = null;
                    BufferedOutputStream bos = null;
                    File file = new File($localPath);
                    URL urlFile = new URL(remotePath);
                    HttpsURLConnection connection = NetUtils.ConnectionUtil.createHttpsConnection(urlFile, httpTimeoutDefault, httpTimeoutDefault, isHttpsTrustAll);

                    try {
                        bis = new BufferedInputStream(connection.getInputStream(), httpBufferSizeDefault);
                        bos = new BufferedOutputStream(Files.newOutputStream(file.toPath()), httpBufferSizeDefault);
                        int len = httpBufferSizeDefault;
                        long sum = 0;
                        long max = connection.getContentLengthLong();
                        byte[] bytes = new byte[len];
                        while ((len = bis.read(bytes)) != -1) {
                            bos.write(bytes, 0, len);
                            sum += len;
                            this.updateMessage("当前已下载：" + NetUtils.getFormattedSizeString(sum));
                            this.updateProgress(sum, max);
                            if (this.isCancelled()) {
                                this.updateMessage("下载进程已被取消");
                                break;
                            }
                        }
                        this.updateProgress(max, max);
                        bos.flush();
                        Logger.info("Network", "Fetched " + $localPath + " , size: " + sum);
                    } finally {
                        try {
                            connection.getInputStream().close();
                            if (bis != null)
                                bis.close();
                            if (bos != null)
                                bos.close();
                        } catch (Exception ignored) {
                        }
                    }
                } catch (Exception e) {
                    if (!(e instanceof SSLException))
                        source.receiveError();
                    throw e;
                }
                return this.isDone() && !this.isCancelled();
            }
        };
    }

    public Task<Boolean> createDownloadTask(String $remotePath, String $localPath) {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                this.updateMessage("正在尝试建立连接");
                Logger.info("Network", "Fetching " + $remotePath + " to " + $localPath);

                BufferedInputStream bis = null;
                BufferedOutputStream bos = null;
                File file = new File($localPath);
                URL urlFile = new URL($remotePath);
                HttpsURLConnection connection = NetUtils.ConnectionUtil.createHttpsConnection(urlFile, httpTimeoutDefault, httpTimeoutDefault, isHttpsTrustAll);

                try {
                    bis = new BufferedInputStream(connection.getInputStream(), httpBufferSizeDefault);
                    bos = new BufferedOutputStream(Files.newOutputStream(file.toPath()), httpBufferSizeDefault);
                    int len = httpBufferSizeDefault;
                    long sum = 0;
                    long max = connection.getContentLengthLong();
                    byte[] bytes = new byte[len];
                    while ((len = bis.read(bytes)) != -1) {
                        bos.write(bytes, 0, len);
                        sum += len;
                        this.updateMessage("当前已下载：" + NetUtils.getFormattedSizeString(sum));
                        this.updateProgress(sum, max);
                        if (this.isCancelled()) {
                            this.updateMessage("下载进程已被取消");
                            break;
                        }
                    }
                    this.updateProgress(max, max);
                    bos.flush();
                    Logger.info("Network", "Fetched " + $localPath + " , size: " + sum);
                } finally {
                    try {
                        connection.getInputStream().close();
                        if (bis != null)
                            bis.close();
                        if (bos != null)
                            bos.close();
                    } catch (Exception ignored){
                    }
                }
                return this.isDone() && !this.isCancelled();
            }
        };
    }

    public Task<Boolean> createUnzipTask(String $zipPath, String $destPath) {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                Logger.info("Unzip", "Unzipping " + $zipPath + " to " + $destPath);
                IOUtils.ZipUtil.unzip($zipPath, $destPath, true);
                Logger.info("Unzip", "Unzipped to " + $destPath + " , finished");
                return this.isDone() && !this.isCancelled();
            }
        };
    }

    public Task<Boolean> createModelsMovingTask(String $rootPath, String $modelsDataPath) {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                if (!new File($rootPath).isDirectory())
                    throw new FileNotFoundException("The directory " + $rootPath + " not found.");
                Path rootPath = new File($rootPath).toPath();
                int rootPathCount = rootPath.getNameCount();
                final boolean[] hasDataset = {false};

                Logger.info("Move", "Moving required files from unzipped files");
                Files.walkFileTree(rootPath, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.getNameCount() == (rootPathCount + 2) && file.getName(rootPathCount + 1).toString().equals($modelsDataPath)) {
                            Files.move(file, file.getFileName(), StandardCopyOption.REPLACE_EXISTING);
                            hasDataset[0] = true;
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        if (dir.getNameCount() == (rootPathCount + 2)) {
                            if (Files.exists(dir.getFileName()))
                                IOUtils.FileUtil.delete(dir.getFileName(), false);
                            Files.move(dir, dir.getFileName(), StandardCopyOption.REPLACE_EXISTING);
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                if (!hasDataset[0])
                    throw new FileNotFoundException("The file " + $modelsDataPath + " not found.");
                try {
                    IOUtils.FileUtil.delete(new File(PathConfig.tempModelsUnzipDirPath).toPath(), false);
                    IOUtils.FileUtil.delete(new File(PathConfig.tempModelsZipCachePath).toPath(), false);
                } catch (IOException e) {
                    Logger.warn("Cache", "The cache file or directory cannot be removed, because " + e.getMessage());
                }
                Logger.info("Move", "Moved required files from unzipped files, finished");
                return true;
            }
        };
    }

    private void dealModelSearch(String $keyWords) {
        searchModelList.getItems().clear();
        ArrayList<AssetCtrl> result = AssetCtrl.searchByKeyWords($keyWords, foundModelAssets);
        ArrayList<String> assetIdList = AssetCtrl.getAssetLocations(result);
        String tag = "";
        if (assertModelLoaded(false))
            for (String s : modelsDatasetFull.getJSONObject("sortTags").keySet())
                if (searchModelFilter.getValue().equals(modelsDatasetFull.getJSONObject("sortTags").getString(s)))
                    tag = s;
        for (JFXListCell<AssetCtrl> item : foundModelItems) {
            // Search by keywords and tags
            for (String assetId : assetIdList) {
                if (item.getId().equals(assetId) &&
                        (isNoFilter || tag.isEmpty() || item.getItem().sortTags == null || item.getItem().sortTags.contains(tag))) {
                    searchModelList.getItems().add(item);
                    break;
                }
            }
        }
        Logger.info("ModelManager", "Search \"" + $keyWords + "\" (" + searchModelList.getItems().size() + ")");
        searchModelList.refresh();
    }

    private void dealModelRandom() {
        if (!assertModelLoaded(true))
            return;
        int idx = (int)(Math.random() * (searchModelList.getItems().size() - 1));
        searchModelList.scrollTo(idx);
        searchModelList.getSelectionModel().select(idx);
        searchModelList.requestFocus();
    }

    private void dealModelReload(boolean $doPopNotice) {
        popLoading(e -> {
            initModelAssets($doPopNotice);
            initModelSearch();
            dealModelSearch("");
            if (foundModelItems.size() != 0 && config.character_asset != null && !config.character_asset.isEmpty()) {
                // Scroll to recent selected model
                int character_asset_idx = AssetCtrl.searchByAssetRelPath(config.character_asset, foundModelAssets);
                searchModelList.scrollTo(character_asset_idx);
                searchModelList.getSelectionModel().select(character_asset_idx);
            }
            loadFailureTip.setVisible(foundModelItems.size() == 0);
            startBtn.setDisable(foundModelItems.size() == 0);
            System.gc();
            Logger.info("ModelManager", "Reloaded");
        });
    }

    private JFXListCell<AssetCtrl> getMenuItem(AssetCtrl $assetCtrl, JFXListView<JFXListCell<AssetCtrl>> $container) {
        double width = $container.getPrefWidth();
        width -= $container.getPadding().getLeft() + $container.getPadding().getRight();
        width *= 0.75;
        double height = 30;
        double divide = 0.618;
        JFXListCell<AssetCtrl> item = new JFXListCell<>();
        item.getStyleClass().addAll("Search-models-item");
        Label name = new Label($assetCtrl.toString());
        name.getStyleClass().addAll("Search-models-label", "Search-models-label-primary");
        name.setPrefSize($assetCtrl.skinGroupName == null ? width : width * divide, height);
        name.setLayoutX(0);
        Label alias1 = new Label($assetCtrl.skinGroupName);
        alias1.getStyleClass().addAll("Search-models-label", "Search-models-label-secondary");
        alias1.setPrefSize(width * (1 - divide), height);
        alias1.setLayoutX($assetCtrl.skinGroupName == null ? 0 : width * divide);

        item.setPrefSize(width, height);
        item.setGraphic(new Group(name, alias1));
        item.setItem($assetCtrl);
        item.setId($assetCtrl.getLocation());
        return item;
    }

    private void selectModel(AssetCtrl $asset, ListCell<AssetCtrl> $item) {
        // Reset
        if (selectedModelItem != null)
            selectedModelItem.getStyleClass().setAll("Search-models-item");
        selectedModelItem = $item;
        selectedModelItem.getStyleClass().add("Search-models-item-active");
        // Display details
        selectedModelName.setText($asset.name);
        selectedModelAppellation.setText($asset.appellation);
        selectedModelSkinGroupName.setText($asset.skinGroupName);
        selectedModelType.setText($asset.type);
        Tooltip selectedModelNameTip = new Tooltip($asset.name);
        Tooltip selectedModelAppellationTip = new Tooltip($asset.appellation);
        Tooltip selectedModelSkinGroupNameTip = new Tooltip($asset.skinGroupName);
        Tooltip selectedModelTypeTip = new Tooltip($asset.type);
        selectedModelNameTip.setStyle(tooltipStyle);
        selectedModelAppellationTip.setStyle(tooltipStyle);
        selectedModelSkinGroupNameTip.setStyle(tooltipStyle);
        selectedModelTypeTip.setStyle(tooltipStyle);
        selectedModelName.setTooltip(selectedModelNameTip);
        selectedModelAppellation.setTooltip(selectedModelAppellationTip);
        selectedModelSkinGroupName.setTooltip(selectedModelSkinGroupNameTip);
        selectedModelType.setTooltip(selectedModelTypeTip);
        // Apply to config, but not to save
        config.character_asset = $asset.getLocation();
        config.character_files = $asset.assetList;
        config.character_label = $asset.name;
    }

    private boolean assertModelLoaded(boolean $doPopNotice) {
        if (modelsDatasetFull == null) {
            // Not loaded:
            if ($doPopNotice)
                popNotice(IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING), "未能加载模型", "请确保模型加载成功后再进行此操作。",
                        "请先在[选项]中进行模型下载。\n如您已下载模型，请尝试点击[重载]按钮。", null).show();
            return false;
        } else {
            // Loaded:
            return true;
        }
    }

    private static void fadeInNode(Node $node, Duration $duration, EventHandler<ActionEvent> $onFinished) {
        FadeTransition fadeT = new FadeTransition($duration, $node);
        $node.setVisible(true);
        if ($onFinished != null)
            fadeT.setOnFinished($onFinished);
        fadeT.setFromValue(0.025);
        fadeT.setToValue(1);
        fadeT.playFromStart();
    }

    private static void fadeOutNode(Node $node, Duration $duration, EventHandler<ActionEvent> $onFinished) {
        FadeTransition fadeT = new FadeTransition($duration, $node);
        fadeT.setOnFinished(e -> {
            $node.setVisible(false);
            if ($onFinished != null)
                $onFinished.handle(e);
        });
        fadeT.setFromValue(0.975);
        fadeT.setToValue(0);
        fadeT.playFromStart();
    }

    private static class DatasetException extends IOException {
        public DatasetException(String msg) {
            super(msg);
        }
    }

    private static class TrayExitHandBook extends PopupUtils.Handbook {
        @Override
        public String getTitle() {
            return "使用提示";
        }

        @Override
        public String getHeader() {
            return "如需关闭桌宠，请右键系统托盘图标后选择退出。";
        }

        @Override
        public String getContent() {
            return "看来你已经启动了你的第一个 ArkPets 桌宠！尽情享受 ArkPets 吧！";
        }
    }
}
