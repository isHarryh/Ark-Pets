/**
 * Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.assets.AssetItem;
import cn.harryh.arkpets.assets.AssetItemGroup;
import cn.harryh.arkpets.assets.ModelsDataset;
import cn.harryh.arkpets.guitasks.*;
import cn.harryh.arkpets.utils.*;
import com.alibaba.fastjson.JSONObject;
import com.jfoenix.controls.*;
import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.log4j.Level;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static cn.harryh.arkpets.Const.*;
import static cn.harryh.arkpets.utils.ControlUtils.*;
import static cn.harryh.arkpets.utils.PopupUtils.*;


public final class Homepage {
    public PopupUtils.Handbook trayExitHandbook = new TrayExitHandBook();
    public JavaProcess.UnexpectedExitCodeException lastLaunchFailed = null;

    @FXML
    public StackPane root;

    @FXML
    public JFXButton getLink;
    public ArkConfig config;
    public ModelsDataset modelsDataset;
    @FXML
    private AnchorPane body;
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
    private JFXListView<JFXListCell<AssetItem>> searchModelView;
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
    private AssetItemGroup assetItemList;
    private JFXListCell<AssetItem> selectedModelCell;
    private ArrayList<JFXListCell<AssetItem>> modelCellList = new ArrayList<>();
    private final ChangeListener<String> filterListener = (observable, oldValue, newValue) -> {
        if (searchModelFilter.getValue() != null) {
            popLoading(e -> {
                Logger.info("ModelManager", "Filter \"" + searchModelFilter.getValue() + "\"");
                dealModelSearch(searchModelInput.getText());
                searchModelFilter.getSelectionModel().clearAndSelect(searchModelFilter.getSelectionModel().getSelectedIndex());
            });
        }
    };
    private NoticeBar appVersionNotice;
    private NoticeBar diskFreeSpaceNotice;
    private NoticeBar datasetIncompatibleNotice;

    public Homepage() {
    }

    private static void fadeInNode(Node node, Duration duration, EventHandler<ActionEvent> onFinished) {
        FadeTransition fadeT = new FadeTransition(duration, node);
        node.setVisible(true);
        if (onFinished != null)
            fadeT.setOnFinished(onFinished);
        fadeT.setFromValue(0.025);
        fadeT.setToValue(1);
        fadeT.playFromStart();
    }

    private static void fadeOutNode(Node node, Duration duration, EventHandler<ActionEvent> onFinished) {
        FadeTransition fadeT = new FadeTransition(duration, node);
        fadeT.setOnFinished(e -> {
            node.setVisible(false);
            if (onFinished != null)
                onFinished.handle(e);
        });
        fadeT.setFromValue(0.975);
        fadeT.setToValue(0);
        fadeT.playFromStart();
    }

    public void initialize() {
        Logger.info("Launcher", "Initializing Homepage");
        wrapper0.setVisible(true);
        popLoading(e -> {
            config = Objects.requireNonNull(ArkConfig.getConfig(), "ArkConfig returns a null instance, please check the config file.");
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
            new CheckAppUpdateTask(root, GuiTask.GuiTaskStyle.HIDDEN, "auto").start();
        });
    }

    private void initMenuBtn(Button menuBtn, int boundIdx) {
        menuBtn.getStyleClass().setAll("menu-btn");
        menuBtn.setOnAction(e -> {
            initWrapper(boundIdx);
            menuBtn1.getStyleClass().setAll("menu-btn");
            menuBtn2.getStyleClass().setAll("menu-btn");
            menuBtn3.getStyleClass().setAll("menu-btn");
            menuBtn.getStyleClass().add("menu-btn-active");
        });
    }

    private void initWrapper(int activeIdx) {
        List<Pane> wrappers = Arrays.asList(null, wrapper1, wrapper2, wrapper3);
        for (short i = 0; i < wrappers.size(); i++) {
            if (wrappers.get(i) != null) {
                if (activeIdx == i) {
                    // Show
                    fadeInNode(wrappers.get(i), durationNormal, null);
                } else {
                    // Hide
                    wrappers.get(i).setVisible(false);
                }
            }
        }
    }

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
            Set<String> filterTags = modelsDataset.sortTags.keySet();
            for (String s : filterTags)
                searchModelFilter.getItems().add(modelsDataset.sortTags.get(s));
        }
        searchModelFilter.valueProperty().addListener(filterListener);
    }

    private boolean initModelDataset(boolean doPopNotice) {
        try {
            try {
                // Read and initialize the dataset
                modelsDataset = new ModelsDataset(
                        JSONObject.parseObject(
                                IOUtils.FileUtil.readString(new File(PathConfig.fileModelsDataPath), charsetDefault)
                        )
                );
                modelsDataset.data.removeIf(Predicate.not(AssetItem::isValid));
                try {
                    // Check the dataset compatibility
                    Version acVersion = modelsDataset.arkPetsCompatibility;
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
                // Explicitly set models dataset to empty.
                modelsDataset = null;
                throw e;
            }

            // If any exception occurred during the progress above:
        } catch (FileNotFoundException e) {
            Logger.warn("ModelManager", "Failed to initialize model dataset due to file not found. (" + e.getMessage() + ")");
            if (doPopNotice) {
                JFXDialog dialog = DialogUtil.createCommonDialog(root, IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING), "模型载入失败", "模型未成功载入：未找到数据集。",
                        "模型数据集文件 " + PathConfig.fileModelsDataPath + " 可能不在工作目录下。\n请先前往 [选项] 进行模型下载。", null);
                JFXButton go2 = DialogUtil.getGotoButton(dialog, root);
                go2.setOnAction(ev -> {
                    initWrapper(3);
                    DialogUtil.disposeDialog(dialog, root);
                });
                DialogUtil.attachAction(dialog, go2, 0);
                dialog.show();
            }
        } catch (ModelsDataset.DatasetKeyException e) {
            Logger.warn("ModelManager", "Failed to initialize model dataset due to dataset parsing error. (" + e.getMessage() + ")");
            if (doPopNotice)
                DialogUtil.createCommonDialog(root, IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING), "模型载入失败", "模型未成功载入：数据集解析失败。",
                        "模型数据集可能不完整，或无法被启动器正确识别。请尝试更新模型或更新软件。", null).show();
        } catch (IOException e) {
            Logger.error("ModelManager", "Failed to initialize model dataset due to unknown reasons, details see below.", e);
            if (doPopNotice)
                DialogUtil.createCommonDialog(root, IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING), "模型载入失败", "模型未成功载入：发生意外错误。",
                        "失败原因概要：" + e.getLocalizedMessage(), null).show();
        }
        return false;
    }

    private void initModelAssets(boolean doPopNotice) {
        if (!initModelDataset(doPopNotice))
            return;
        try {
            // Find every model assets.
            assetItemList = modelsDataset.data.filter(AssetItem::isExisted);
            if (assetItemList.isEmpty())
                throw new IOException("Found no assets in the target directories.");
            // Initialize list view:
            searchModelView.getSelectionModel().getSelectedItems().addListener(
                    (ListChangeListener<JFXListCell<AssetItem>>) (observable -> observable.getList().forEach(
                            (Consumer<JFXListCell<AssetItem>>) cell -> selectModel(cell.getItem(), cell))
                    )
            );
            searchModelView.setFixedCellSize(30);
            // Write models to menu items.
            modelCellList = new ArrayList<>();
            assetItemList.forEach(assetItem -> modelCellList.add(getMenuItem(assetItem, searchModelView)));
            Logger.debug("ModelManager", "Initialized model assets successfully.");
        } catch (IOException e) {
            // Explicitly set all lists to empty.
            assetItemList = new AssetItemGroup();
            modelCellList = new ArrayList<>();
            Logger.error("ModelManager", "Failed to initialize model assets due to unknown reasons, details see below.", e);
            if (doPopNotice)
                DialogUtil.createCommonDialog(root, IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING), "模型载入失败", "模型未成功载入：读取模型列表失败。",
                        "失败原因概要：" + e.getLocalizedMessage(), null).show();
        }
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
        manageModelCheck.setOnAction(e -> {
            if (!initModelDataset(true))
                return;
            new CheckModelUpdateTask(root, GuiTask.GuiTaskStyle.COMMON).start();
        });
        manageModelFetch.setOnAction(e -> {
            /* Foreground fetch models */
            // Go to [Step 1/3]:
            new DownloadModelsTask(root, GuiTask.GuiTaskStyle.COMMON) {
                @Override
                protected void onSucceeded(boolean result) {
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
                                    dealModelReload(true);
                                    initWrapper(1);
                                }
                            }.start();
                        }
                    }.start();
                }
            }.start();
        });
        getLink.setOnAction(event -> {
            if (getLink.getText().equals("获取手动下载链接")) {

                getLink.setText("链接已复制到您的剪切板，请粘贴至浏览器下载。下载后，请选择压缩包导入");
                getLink.setPrefWidth(350);
            }
            Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection stringSelection = new StringSelection("https://github.com/isHarryh/Ark-Models");
            systemClipboard.setContents(stringSelection, null);
        });
        manageModelVerify.setOnAction(e -> {
            /* Foreground verify models */
            if (!initModelDataset(true))
                return;
            new VerifyModelsTask(root, GuiTask.GuiTaskStyle.COMMON, modelsDataset).start();
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
            File zipFile = fileChooser.showOpenDialog(root.getScene().getWindow());
            if (zipFile != null && zipFile.isFile()) {
                Logger.info("ModelManager", "Importing zip file: " + zipFile);
                // Go to [Step 1/2]:
                new UnzipModelsTask(root, GuiTask.GuiTaskStyle.STRICT, zipFile.getPath()) {
                    @Override
                    protected void onSucceeded(boolean result) {
                        // Go to [Step 2/2]:
                        new PostUnzipModelTask(root, GuiTaskStyle.STRICT) {
                            @Override
                            protected void onSucceeded(boolean result) {
                                dealModelReload(true);
                                initWrapper(1);
                            }
                        }.start();
                    }
                }.start();
            }
        });
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

        configNetworkAgent.setPromptText("示例：0.0.0.0:0");
        configNetworkAgent.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                configNetworkAgentStatus.setText("未使用代理");
                configNetworkAgentStatus.setStyle("-fx-text-fill:" + COLOR_LIGHT_GRAY);
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
                    configNetworkAgentStatus.setStyle("-fx-text-fill:" + COLOR_SUCCESS);
                    Logger.info("Network", "Set proxy to host " + ipPort[0] + ", port " + ipPort[1]);
                } else {
                    configNetworkAgentStatus.setText("输入不合法");
                    configNetworkAgentStatus.setStyle("-fx-text-fill:" + COLOR_DANGER);
                }
            }
        });
        configNetworkAgentStatus.setText("未使用代理");
        configNetworkAgentStatus.setStyle("-fx-text-fill:" + COLOR_LIGHT_GRAY);

        configAutoStartup.setSelected(ArkConfig.StartupConfig.isSetStartup());
        configAutoStartup.setOnAction(e -> {
            if (configAutoStartup.isSelected()) {
                if (ArkConfig.StartupConfig.addStartup()) {
                    DialogUtil.createCommonDialog(root, IconUtil.getIcon(IconUtil.ICON_SUCCESS_ALT, COLOR_SUCCESS), "开机自启动", "开机自启动设置成功。",
                            "下次开机时将会自动生成您最后一次启动的桌宠。", null).show();
                } else {
                    if (ArkConfig.StartupConfig.generateScript() == null)
                        DialogUtil.createCommonDialog(root, IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING), "开机自启动", "开机自启动设置失败。",
                                "无法确认目标程序的位置，其原因和相关解决方案如下：", "为确保自启动服务的稳定性，直接打开的ArkPets的\".jar\"版启动器，是不支持配置自启动的。请使用exe版的安装包安装ArkPets后运行，或使用zip版的压缩包解压程序文件后运行。另外，当您使用错误的工作目录运行启动器时也可能出现此情况。").show();
                    else
                        DialogUtil.createCommonDialog(root, IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING), "开机自启动", "开机自启动设置失败。",
                                "无法写入系统的启动目录，其原因可参见日志文件。", "这有可能是由于权限不足导致的。请尝试关闭反病毒软件，并以管理员权限运行启动器。").show();
                    configAutoStartup.setSelected(false);
                }
            } else {
                ArkConfig.StartupConfig.removeStartup();
            }
        });
    }

    private void initAbout() {
        aboutQueryUpdate.setOnMouseClicked(e -> {
            /* Foreground check app update */
            new CheckAppUpdateTask(root, GuiTask.GuiTaskStyle.COMMON, "manual").start();
        });
        aboutVisitWebsite.setOnMouseClicked(e -> NetUtils.browseWebpage(PathConfig.urlOfficial));
        aboutReadme.setOnMouseClicked(e -> NetUtils.browseWebpage(PathConfig.urlReadme));
        aboutGitHub.setOnMouseClicked(e -> NetUtils.browseWebpage(PathConfig.urlLicense));
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
                task.setOnFailed(e -> DialogUtil.createErrorDialog(root, task.getException()).show());
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

    public void popLoading(EventHandler<ActionEvent> onLoading) {
        fadeInNode(wrapper0, durationFast, e -> {
            try {
                onLoading.handle(e);
            } catch (Exception ex) {
                Logger.error("Task", "Foreground loading task failed, details see below.", ex);
            }
            fadeOutNode(wrapper0, durationFast, null);
        });
    }

    private void dealModelSearch(String keyWords) {
        searchModelView.getItems().clear();
        if (assertModelLoaded(false)) {
            // Handle tag
            String filterTag = "";
            for (String s : modelsDataset.sortTags.keySet())
                if (searchModelFilter.getValue().equals(modelsDataset.sortTags.get(s)))
                    filterTag = s;
            // Filter and search assets
            AssetItemGroup filtered = filterTag.isEmpty() ? assetItemList :
                    assetItemList.filter(AssetItem.PropertyExtractor.ASSET_ITEM_SORT_TAGS, Set.of(filterTag));
            AssetItemGroup searched = filtered.searchByKeyWords(keyWords);
            // Add cells
            for (JFXListCell<AssetItem> cell : modelCellList)
                if (searched.contains(cell.getItem()))
                    searchModelView.getItems().add(cell);
        }
        Logger.info("ModelManager", "Search \"" + keyWords + "\" (" + searchModelView.getItems().size() + ")");
        searchModelView.refresh();
    }

    private void dealModelRandom() {
        if (!assertModelLoaded(true))
            return;
        int idx = (int) (Math.random() * (searchModelView.getItems().size() - 1));
        searchModelView.scrollTo(idx);
        searchModelView.getSelectionModel().select(idx);
        searchModelView.requestFocus();
    }

    private void dealModelReload(boolean doPopNotice) {
        popLoading(e -> {
            initModelAssets(doPopNotice);
            initModelSearch();
            dealModelSearch("");
            if (!modelCellList.isEmpty() && config.character_asset != null && !config.character_asset.isEmpty()) {
                // Scroll to recent selected model
                AssetItem recentSelected = assetItemList.searchByRelPath(config.character_asset);
                if (recentSelected != null)
                    for (JFXListCell<AssetItem> cell : searchModelView.getItems())
                        if (recentSelected.equals(cell.getItem())) {
                            searchModelView.scrollTo(cell);
                            searchModelView.getSelectionModel().select(cell);
                        }
            }
            loadFailureTip.setVisible(modelCellList.isEmpty());
            startBtn.setDisable(modelCellList.isEmpty());
            System.gc();
            Logger.info("ModelManager", "Reloaded");
        });
    }

    private JFXListCell<AssetItem> getMenuItem(AssetItem assetItem, JFXListView<JFXListCell<AssetItem>> container) {
        double width = container.getPrefWidth();
        width -= container.getPadding().getLeft() + container.getPadding().getRight();
        width *= 0.75;
        double height = 30;
        double divide = 0.618;
        JFXListCell<AssetItem> item = new JFXListCell<>();
        item.getStyleClass().addAll("Search-models-item");
        Label name = new Label(assetItem.toString());
        name.getStyleClass().addAll("Search-models-label", "Search-models-label-primary");
        name.setPrefSize(assetItem.skinGroupName == null ? width : width * divide, height);
        name.setLayoutX(0);
        Label alias1 = new Label(assetItem.skinGroupName);
        alias1.getStyleClass().addAll("Search-models-label", "Search-models-label-secondary");
        alias1.setPrefSize(width * (1 - divide), height);
        alias1.setLayoutX(assetItem.skinGroupName == null ? 0 : width * divide);

        item.setPrefSize(width, height);
        item.setGraphic(new Group(name, alias1));
        item.setItem(assetItem);
        item.setId(assetItem.getLocation());
        return item;
    }

    private void selectModel(AssetItem asset, JFXListCell<AssetItem> item) {
        // Reset
        if (selectedModelCell != null)
            selectedModelCell.getStyleClass().setAll("Search-models-item");
        selectedModelCell = item;
        selectedModelCell.getStyleClass().add("Search-models-item-active");
        // Display details
        selectedModelName.setText(asset.name);
        selectedModelAppellation.setText(asset.appellation);
        selectedModelSkinGroupName.setText(asset.skinGroupName);
        selectedModelType.setText(asset.type);
        Tooltip selectedModelNameTip = new Tooltip(asset.name);
        Tooltip selectedModelAppellationTip = new Tooltip(asset.appellation);
        Tooltip selectedModelSkinGroupNameTip = new Tooltip(asset.skinGroupName);
        Tooltip selectedModelTypeTip = new Tooltip(asset.type);
        selectedModelNameTip.setStyle(tooltipStyle);
        selectedModelAppellationTip.setStyle(tooltipStyle);
        selectedModelSkinGroupNameTip.setStyle(tooltipStyle);
        selectedModelTypeTip.setStyle(tooltipStyle);
        selectedModelName.setTooltip(selectedModelNameTip);
        selectedModelAppellation.setTooltip(selectedModelAppellationTip);
        selectedModelSkinGroupName.setTooltip(selectedModelSkinGroupNameTip);
        selectedModelType.setTooltip(selectedModelTypeTip);
        // Apply to config, but not to save
        config.character_asset = asset.getLocation();
        config.character_files = asset.assetList;
        config.character_label = asset.name;
    }

    private boolean assertModelLoaded(boolean doPopNotice) {
        if (modelsDataset == null) {
            // Not loaded:
            if (doPopNotice)
                DialogUtil.createCommonDialog(root, IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING), "未能加载模型", "请确保模型加载成功后再进行此操作。",
                        "请先在[选项]中进行模型下载。\n如您已下载模型，请尝试点击[重载]按钮。", null).show();
            return false;
        } else {
            // Loaded:
            return true;
        }
    }

    private static class TrayExitHandBook extends PopupUtils.Handbook {
        @Override
        public String getTitle() {
            return "使用提示";
        }

        @Override
        public String getHeader() {
            return "如需关闭桌宠，请右键单击桌宠或系统托盘图标，然后选择退出。";
        }

        @Override
        public String getContent() {
            return "看来你已经启动了你的第一个 ArkPets 桌宠！尽情享受 ArkPets 吧！";
        }
    }
}
