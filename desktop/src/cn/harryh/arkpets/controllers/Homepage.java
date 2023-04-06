/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.utils.*;
import cn.harryh.arkpets.ArkConfig;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import static cn.harryh.arkpets.Const.*;
import static cn.harryh.arkpets.utils.PopupUtils.*;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.jfoenix.controls.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.apache.log4j.Level;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.util.Duration;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;

import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.ZipException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;


public class Homepage {
    private boolean isHttpsTrustAll = false;
    private boolean isDelayTested = false;
    private boolean isNoFilter = true;
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
    private JFXComboBox<Float> configDisplayScale;
    @FXML
    private JFXComboBox<Integer> configDisplayFps;
    @FXML
    private JFXSlider configDisplayMarginBottom;
    @FXML
    private Label configDisplayMarginBottomValue;
    @FXML
    private JFXButton manageModelCheck;
    @FXML
    private JFXButton manageModelFetch;
    @FXML
    private JFXButton manageModelVerify;
    @FXML
    private JFXComboBox<String> configLoggingLevel;
    @FXML
    private JFXCheckBox configAutoStartup;
    @FXML
    private Label aboutQueryUpdate;
    @FXML
    private Label aboutVisitWebsite;

    private ListCell<AssetCtrl> selectedModelItem;
    private AssetCtrl[] foundModelAssets = {};
    private JFXListCell[] foundModelItems = {};

    public ArkConfig config;
    public JSONObject modelsDatasetFull;

    public Homepage() {
    }

    public void initialize() {
        Logger.info("Launcher", "Initializing (JavaFX " + System.getProperty("javafx.version") + ", " + "ArkPets " + appVersionStr + ")");
        wrapper0.setVisible(true);
        popLoading(e -> {
            config = Objects.requireNonNull(ArkConfig.getConfig(), "ArkConfig returns a null instance, please check the config file.");
            config.display_monitor_info = getDefaultMonitorInfo();
            initMenuBtn(menuBtn1, 1);
            initMenuBtn(menuBtn2, 2);
            initMenuBtn(menuBtn3, 3);
            initWrapper(1);
            initModelSearch();
            initModelManage();
            initConfigBehavior();
            initConfigDisplay();
            initConfigAdvanced();
            initAbout();
            initLaunchingStatusListener();
            config.saveConfig();
            menuBtn1.getStyleClass().add("menu-btn-active");
            Platform.runLater(() -> {
                initModelAssets(false);
                dealModelSearch("");
                if (foundModelItems.length != 0 && !config.character_recent.isEmpty()) {
                    // Scroll to recent selected model
                    int character_recent_idx = AssetCtrl.searchByAssetRelPath(config.character_recent, foundModelAssets);
                    searchModelList.scrollTo(character_recent_idx);
                    searchModelList.getSelectionModel().select(character_recent_idx);
                    searchModelList.refresh();
                }
                // Judge if no model available
                loadFailureTip.setVisible(foundModelItems.length == 0);
                startBtn.setDisable(foundModelItems.length == 0);
                foregroundCheckUpdate(false, "auto");
            });
        });
        Logger.info("Launcher", "Initialized");
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
        List<Node> wrappers = Arrays.asList(null, wrapper1, wrapper2, wrapper3);
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
        searchModelReload.setOnAction(e -> dealModelReload());

        searchModelFilter.getItems().setAll("全部");
        searchModelFilter.getSelectionModel().select(0);
        if (initModelAssets(false)) {
            Set<String> filterTags = modelsDatasetFull.getJSONObject("sortTags").keySet();
            for (String s : filterTags) {
                searchModelFilter.getItems().add(modelsDatasetFull.getJSONObject("sortTags").getString(s));
            }
        }
        searchModelFilter.valueProperty().addListener(observable -> {
            if (searchModelFilter.getValue() != null) {
                popLoading(e -> {
                    isNoFilter = searchModelFilter.getSelectionModel().getSelectedIndex() == 0;
                    Logger.info("ModelList", "Filter \"" + searchModelFilter.getValue() + "\"");
                    dealModelSearch(searchModelInput.getText());
                    searchModelFilter.getSelectionModel().clearAndSelect(searchModelFilter.getSelectionModel().getSelectedIndex());
                });
            }
        });
    }

    private boolean initModelDataset(boolean $doPopNotice) {
        try {
            try {
                modelsDatasetFull = Objects.requireNonNull(JSONObject.parseObject(IOUtils.FileUtil.readString(new File(PathConfig.fileModelsDataPath), charsetDefault)));
                if (!modelsDatasetFull.containsKey("data"))
                    throw new JSONException("The key 'data' may not in the dataset.");
                if (!modelsDatasetFull.containsKey("storageDirectory"))
                    throw new JSONException("The key 'storageDirectory' may not in the dataset.");
                // TODO check dataset capability
                return true;
            } catch (Exception e) {
                modelsDatasetFull = null;
                throw e;
            }
        } catch (FileNotFoundException e) {
            if ($doPopNotice)
                popNotice(IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING), "模型载入失败", "模型未成功载入：未找到模型数据集。",
                        "模型数据集文件 " + PathConfig.fileModelsDataPath + " 可能不在工作目录下。\n请先前往[选项]进行模型下载。", null).show();
        } catch (IOException e) {
            e.printStackTrace();
            if ($doPopNotice)
                popNotice(IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING), "模型载入失败", "模型未成功载入：未知原因。",
                        "失败原因概要：" + e.getLocalizedMessage(), null).show();
        }
        return false;
    }

    private boolean initModelAssets(boolean $doPopNotice) {
        try {
            try {
                if (!initModelDataset($doPopNotice))
                    return false;
                // Find every model.
                ArrayList<AssetCtrl> foundModelAssetsL = new ArrayList<>();
                for (String key : modelsDatasetFull.getJSONObject("storageDirectory").keySet())
                    foundModelAssetsL.addAll(Arrays.asList(
                            AssetCtrl.getAssetList(new File(modelsDatasetFull.getJSONObject("storageDirectory").getString(key)), modelsDatasetFull.getJSONObject("data"))
                    ));
                foundModelAssets = AssetCtrl.sortAssetList(foundModelAssetsL.toArray(new AssetCtrl[0]));
                if (foundModelAssets.length == 0)
                    throw new RuntimeException("Found no assets in the target directories.");
                // Models to menu items.
                ArrayList<JFXListCell<AssetCtrl>> foundModelItemsL = new ArrayList<>();
                searchModelList.getSelectionModel().getSelectedItems().addListener((ListChangeListener<JFXListCell<AssetCtrl>>)(observable -> {
                    observable.getList().forEach((Consumer<JFXListCell<AssetCtrl>>) cell -> selectModel(cell.getItem(), cell));
                }));
                searchModelList.setFixedCellSize(30);
                for (AssetCtrl asset : foundModelAssets)
                    foundModelItemsL.add(getMenuItem(asset, searchModelList));
                foundModelItems = foundModelItemsL.toArray(new JFXListCell[0]);
                return true;
            } catch (Exception e) {
                foundModelAssets = new AssetCtrl[0];
                foundModelItems = new JFXListCell[0];
                throw e;
            }
        } catch (RuntimeException e) {
            if ($doPopNotice)
                popNotice(IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING), "模型载入失败", "模型未成功载入：未能成功解析模型数据集。",
                        "可能是数据集损坏、版本不兼容或模型存放位置错误。\n失败原因概要：" + e.getLocalizedMessage(), null).show();
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
        configBehaviorAiActivation.setMax(8);
        configBehaviorAiActivation.setMin(0);
        configBehaviorAiActivation.setMajorTickUnit(1);
        configBehaviorAiActivation.setMinorTickCount(0);
        configBehaviorAiActivation.setShowTickLabels(false);
        configBehaviorAiActivation.setValue(config.behavior_ai_activation);
        configBehaviorAiActivation.valueProperty().addListener(((observable, oldValue, newValue) -> {
            long value = Math.round((double)newValue);
            configBehaviorAiActivation.setValue(value);
            configBehaviorAiActivationValue.setText(String.valueOf(value));
            config.behavior_ai_activation = (int)value;
            config.saveConfig();
        }));
        configBehaviorAiActivationValue.setText(String.valueOf(config.behavior_ai_activation));
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
        configDisplayMarginBottom.setMax(120);
        configDisplayMarginBottom.setMin(0);
        configDisplayMarginBottom.setMajorTickUnit(10);
        configDisplayMarginBottom.setMinorTickCount(5);
        configDisplayMarginBottom.setValue(config.display_margin_bottom);
        configDisplayMarginBottom.valueProperty().addListener(((observable, oldValue, newValue) -> {
            int value = (int)Math.round((double)newValue);
            //configDisplayMarginBottom.setValue(value);
            configDisplayMarginBottomValue.setText(String.valueOf(value));
            config.display_margin_bottom = value;
            config.saveConfig();
        }));
        configDisplayMarginBottomValue.setText(String.valueOf(config.display_margin_bottom));
    }

    private void initModelManage() {
        manageModelCheck.setOnAction(e -> foregroundCheckModels());
        manageModelFetch.setOnAction(e -> foregroundFetchModels());
        manageModelVerify.setOnAction(e -> foregroundVerifyModels());
    }

    private void initConfigAdvanced() {
        configLoggingLevel.getItems().setAll(LogLevels.debug, LogLevels.info, LogLevels.warn, LogLevels.error);
        configLoggingLevel.valueProperty().addListener(observable -> {
            if (configLoggingLevel.getValue() != null) {
                Logger.setLevel(Level.toLevel(configLoggingLevel.getValue(), Level.INFO));
                config.logging_level = Logger.getLevel().toString();
                config.saveConfig();
            }
        });
        String level = config.logging_level;
        List<String> args = Arrays.asList(ArgPending.argCache);
        if (args.contains(LogLevels.errorArg))
            level = LogLevels.error;
        else if (args.contains(LogLevels.warnArg))
            level = LogLevels.warn;
        else if (args.contains(LogLevels.infoArg))
            level = LogLevels.info;
        else if (args.contains(LogLevels.debugArg))
            level = LogLevels.debug;
        configLoggingLevel.getSelectionModel().select(level);

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
        aboutQueryUpdate.setOnMouseClicked(e -> foregroundCheckUpdate(true, "manual"));
        aboutVisitWebsite.setOnMouseClicked(e -> {
            try {
                Desktop.getDesktop().browse(URI.create(PathConfig.urlOfficial));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
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

    public JFXDialog foregroundTask(Task $task, String $header, String $defaultContent, Boolean $cancelable) {
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
        }
        dialog.show();

        final double[] cachedProgress = {-1};
        $task.progressProperty().addListener((observable, oldValue, newValue) -> {
            if (Math.abs((double)newValue - cachedProgress[0]) >= 0.005) {
                cachedProgress[0] = (double)newValue;
                bar.setProgress((double)newValue);
            }
        });
        $task.messageProperty().addListener(((observable, oldValue, newValue) -> h3.setText(newValue)));
        $task.setOnCancelled(e -> {
            Logger.info("Task", "Foreground task was cancelled.");
            DialogUtil.disposeDialog(dialog, root);
        });
        $task.setOnFailed(e -> {
            Logger.error("Task", "Foreground task failed, details see below.", $task.getException());
            popError($task.getException()).show();
            DialogUtil.disposeDialog(dialog, root);
        });
        $task.setOnSucceeded(e -> {
            Logger.info("Task", "Foreground task completed.");
            DialogUtil.disposeDialog(dialog, root);
        });
        Thread thread = new Thread($task);
        thread.start();
        return dialog;
    }

    public void popLoading(EventHandler<ActionEvent> $onLoading) {
        fadeInNode(wrapper0, durationFast, e -> {
            $onLoading.handle(e);
            fadeOutNode(wrapper0, durationFast, null);
        });
    }

    public JFXDialog popError(Throwable $e) {
        JFXDialog dialog = DialogUtil.createCenteredDialog(root, false);

        VBox content = new VBox();
        Label h2 = (Label)DialogUtil.getPrefabsH2("啊哦~ ArkPets启动器抛出了一个异常。");
        Label h3 = (Label)DialogUtil.getPrefabsH3("请重试操作，或查看帮助文档。如需联系开发者，请提供下述信息：");
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
        if ($e instanceof IOUtils.NetUtil.HttpResponseCodeException) {
            h2.setText("神经递质接收异常");
            if (((IOUtils.NetUtil.HttpResponseCodeException)$e).isRedirection()) {
                h3.setText("请求的网络地址被重定向转移。详细信息：");
            }
            if (((IOUtils.NetUtil.HttpResponseCodeException)$e).isClientError()) {
                h3.setText("可能是客户端引发的网络错误，详细信息：");
                if (((IOUtils.NetUtil.HttpResponseCodeException)$e).getCode() == 403) {
                    h3.setText("(403)访问被拒绝。详细信息：");
                }
                if (((IOUtils.NetUtil.HttpResponseCodeException)$e).getCode() == 404) {
                    h3.setText("(404)找不到要访问的目标。详细信息：");
                }
            }
            if (((IOUtils.NetUtil.HttpResponseCodeException)$e).isServerError()) {
                h3.setText("可能是服务器引发的网络错误，详细信息：");
                if (((IOUtils.NetUtil.HttpResponseCodeException)$e).getCode() == 500) {
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
            DialogUtil.disposeDialog(dialog, root);
            try {
                String versionDescription;
                try {
                    JSONObject newModelsDataset = JSONObject.parseObject(IOUtils.FileUtil.readString(new File(PathConfig.tempDirPath + PathConfig.fileModelsDataPath), charsetDefault));
                    versionDescription = newModelsDataset.getString("gameDataVersionDescription");
                } catch (Exception ex) {
                    versionDescription = "unknown";
                }
                // TODO do judgment more precisely
                if (IOUtils.FileUtil.getMD5(new File(PathConfig.fileModelsDataPath)).equals(IOUtils.FileUtil.getMD5(new File(PathConfig.tempDirPath + PathConfig.fileModelsDataPath)))) {
                    popNotice(IconUtil.getIcon(IconUtil.ICON_SUCCESS_ALT, COLOR_SUCCESS), "检查模型更新", "当前模型版本与远程仓库一致。",
                            "提示：远程仓库的版本不一定和游戏同步更新。", "模型仓库版本描述：\n" + versionDescription).show();
                    Logger.info("Checker", "Model repo version check finished (up-to-dated)");
                } else {
                    String oldVersionDescription;
                    try {
                        JSONObject oldModelsDataset = JSONObject.parseObject(IOUtils.FileUtil.readString(new File(PathConfig.fileModelsDataPath), charsetDefault));
                        oldVersionDescription = oldModelsDataset.getString("gameDataVersionDescription");
                    } catch (Exception ex) {
                        oldVersionDescription = "unknown";
                    }
                    popNotice(IconUtil.getIcon(IconUtil.ICON_INFO_ALT, COLOR_INFO), "检查模型更新", "当前模型版本与远程仓库有差异。",
                            "可以重新下载模型，以进行更新模型版本。", "远程模型仓库版本描述：\n" + versionDescription + "\n\n当前模型仓库版本描述：\n" + oldVersionDescription).show();
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
        // TODO change download source
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
                    dealModelReload();
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
                this.updateProgress(0.1, 1);
                Path rootPath = new File("").toPath();
                int rootPathCount = rootPath.getNameCount();
                JSONObject cachedMDSD = (JSONObject) modelsDatasetFull.getJSONObject("storageDirectory").clone();
                JSONObject cachedMDD = (JSONObject) modelsDatasetFull.getJSONObject("data").clone();
                Thread.sleep(100);
                this.updateProgress(0.2, 1);
                final boolean[] flag = {false};
                Files.walkFileTree(rootPath, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        if (dir.getNameCount() == (rootPathCount) && !rootPath.equals(dir))
                            return cachedMDSD.containsValue(dir.getFileName().toString()) ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
                        if (dir.getNameCount() == (rootPathCount + 1) && cachedMDD.containsKey(dir.getFileName().toString())) {
                            if (AssetCtrl.isVerifiedAsset(dir.toFile(), cachedMDD)) {
                                cachedMDD.remove(dir.getFileName().toString());
                                return FileVisitResult.CONTINUE;
                            } else if (!AssetCtrl.isValidAsset(dir.toFile(), cachedMDD)) {
                                return FileVisitResult.CONTINUE;
                            } else {
                                dialogGraphic[0] = IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING);
                                dialogHeader[0] = "已发现问题，模型资源可能不完整。";
                                dialogContent[0] = "位于 " + dir + " 的模型资源，可能" + (AssetCtrl.isIntegralAsset(dir.toFile(), cachedMDD) ? "已被修改" : "缺少关键文件") + "。\n请尝试重新下载模型。";
                                flag[0] = true;
                                return FileVisitResult.TERMINATE;
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                if (flag[0] || this.isCancelled()) {
                    Logger.info("Checker", "Model repo check finished (may modified or lost)");
                    return false;
                }
                Thread.sleep(100);
                this.updateProgress(0.7, 1);
                for (String key : cachedMDD.keySet()) {
                    try {
                        if (!cachedMDD.getJSONObject("key").getJSONObject("checksum").isEmpty()) {
                            dialogGraphic[0] = IconUtil.getIcon(IconUtil.ICON_WARNING_ALT, COLOR_WARNING);
                            dialogHeader[0] = "已发现问题，模型资源可能不完整。";
                            dialogContent[0] = "没有找到 " + key + " 的模型资源。\n请尝试重新下载模型。";
                            flag[0] = true;
                        }
                    } catch (Exception ignored) {
                    }
                }
                Thread.sleep(100);
                this.updateProgress(1, 1);
                if (flag[0] || this.isCancelled()) {
                    Logger.info("Checker", "Model repo check finished (not integral or cancelled)");
                    return false;
                }
                dialogGraphic[0] = IconUtil.getIcon(IconUtil.ICON_SUCCESS_ALT, COLOR_SUCCESS);
                dialogHeader[0] = "模型资源是完整的。";
                dialogContent[0] = "这只能说明本地的模型资源是完整的，但不一定是最新的。";
                Logger.info("Checker", "Model repo check finished (okay)");
                return true;
            }
        };
        task.stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(Worker.State.SUCCEEDED))
                if (dialogGraphic[0] != null && dialogHeader[0] != null && dialogContent[0] != null)
                    popNotice(dialogGraphic[0], "验证资源完整性", dialogHeader[0], dialogContent[0], null).show();
        });
        foregroundTask(task, "正在检验模型资源完整性...", "这可能需要数秒", true);
    }

    private void foregroundCheckUpdate(boolean $popNotice, String $sourceStr) {
        try {
            Files.createDirectories(new File(PathConfig.tempDirPath).toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String queryStr = "?type=queryVersion&cliVer=" + appVersionStr + "&source=" + $sourceStr;
        Task<Boolean> task = createDownloadTask(PathConfig.urlApi + queryStr, PathConfig.tempQueryVersionCachePath);
        JFXDialog dialog = null;
        if ($popNotice)
            dialog = foregroundTask(task, "正在下载软件版本信息...", "", true);
        JFXDialog finalDialog = dialog;
        task.setOnSucceeded(e -> {
            if ($popNotice && finalDialog != null)
                DialogUtil.disposeDialog(finalDialog, root);
            try {
                JSONObject queryVersionResult = Objects.requireNonNull(JSONObject.parseObject(IOUtils.FileUtil.readByte(new File(PathConfig.tempQueryVersionCachePath))));
                // TODO show in-test version
                if (queryVersionResult.getString("msg").equals("success")) {
                    int[] stableVersionResult = queryVersionResult.getJSONObject("data").getObject("stableVersion", int[].class);
                    if (stableVersionResult[0] > appVersion[0] ||
                            (stableVersionResult[0] == appVersion[0] && stableVersionResult[1] > appVersion[1]) ||
                            (stableVersionResult[0] == appVersion[0] && stableVersionResult[1] == appVersion[1] && stableVersionResult[2] > appVersion[2])) {
                        popNotice(IconUtil.getIcon(IconUtil.ICON_INFO_ALT, COLOR_INFO), "检查软件更新", "恭喜，检测到软件有新的版本。",
                                appVersionStr + " -> " + stableVersionResult[0] + "." + stableVersionResult[1] + "." +stableVersionResult[2] + "\n请访问官网或GitHub下载新的版本。", null).show();
                    } else {
                        popNotice(IconUtil.getIcon(IconUtil.ICON_SUCCESS_ALT, COLOR_SUCCESS), "检查软件更新", "尚未发现新的稳定版本。",
                                "当前版本：" + appVersionStr, null).show();
                    }
                    Logger.info("Checker", "Application version check finished");
                } else {
                    Logger.warn("Checker", "Application version check failed (api failed)");
                    if ($popNotice)
                        popNotice(IconUtil.getIcon(IconUtil.ICON_DANGER_ALT, COLOR_DANGER), "检查软件更新", "服务器返回了无效的消息。",
                                "可能是兼容性问题或服务器不可用。\n您可以访问官网或GitHub，手动查看是否有新版本。", null).show();
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
                Logger.info("Downloader", "Testing real delay");
                Downloader.GitHubSource[] sources = Downloader.GitHubSource.sortByDelay(Downloader.ghSources);
                Downloader.GitHubSource source = sources[0];
                Logger.info("Downloader", "Selected the shortest delayed source \"" + source.tag + "\" (" + source.delay + "ms)");
                String remotePath = ($isArchive ? source.archivePreUrl : source.rawPreUrl) + $remotePathSuffix;
                Logger.info("Downloader", "Downloading " + remotePath + " to " + $localPath);
                this.updateMessage("正在尝试与 " + source.tag + " 建立连接");

                URL urlFile;
                HttpsURLConnection connection = null;
                BufferedInputStream bis = null;
                BufferedOutputStream bos = null;
                File file = new File($localPath);
                try {
                    urlFile = new URL(remotePath);
                    connection = IOUtils.NetUtil.createHttpsConnection(urlFile, httpTimeoutDefault, httpTimeoutDefault, isHttpsTrustAll);
                    bis = new BufferedInputStream(connection.getInputStream(), httpBufferSizeDefault);
                    bos = new BufferedOutputStream(Files.newOutputStream(file.toPath()), httpBufferSizeDefault);
                } catch (IOException e) {
                    try {
                        if (connection != null && connection.getInputStream() != null)
                            connection.getInputStream().close();
                        if (bis != null)
                            bis.close();
                        if (bos != null)
                            bos.close();
                    } catch (Exception ignored){
                    }
                    throw e;
                }
                int len = httpBufferSizeDefault;
                long sum = 0;
                long max = connection.getContentLengthLong();
                byte[] bytes = new byte[len];
                try {
                    while ((len = bis.read(bytes)) != -1) {
                        bos.write(bytes, 0, len);
                        sum += len;
                        this.updateMessage("当前已下载：" + Downloader.getFormattedSizeString(sum));
                        this.updateProgress(sum, max);
                        if (this.isCancelled()) {
                            this.updateMessage("下载进程已被取消");
                            break;
                        }
                    }
                    this.updateProgress(max, max);
                    bos.flush();
                } catch (IOException e) {
                    throw e;
                } finally {
                    try {
                        if (connection != null && connection.getInputStream() != null)
                            connection.getInputStream().close();
                        if (bis != null)
                            bis.close();
                        if (bos != null)
                            bos.close();
                    } catch (Exception ignored){
                    }
                }
                Logger.info("Downloader", "Downloaded " + $localPath + " , file size: " + sum);
                return this.isDone() && !this.isCancelled();
            }
        };
    }

    public Task<Boolean> createDownloadTask(String $remotePath, String $localPath) {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                this.updateMessage("正在尝试建立连接");
                Logger.info("Downloader", "Downloading " + $remotePath + " to " + $localPath);

                URL urlFile;
                HttpsURLConnection connection = null;
                BufferedInputStream bis = null;
                BufferedOutputStream bos = null;
                File file = new File($localPath);
                try {
                    urlFile = new URL($remotePath);
                    connection = IOUtils.NetUtil.createHttpsConnection(urlFile, httpTimeoutDefault, httpTimeoutDefault, isHttpsTrustAll);
                    bis = new BufferedInputStream(connection.getInputStream(), httpBufferSizeDefault);
                    bos = new BufferedOutputStream(Files.newOutputStream(file.toPath()), httpBufferSizeDefault);
                } catch (IOException e) {
                    try {
                        if (connection != null && connection.getInputStream() != null)
                            connection.getInputStream().close();
                        if (bis != null)
                            bis.close();
                        if (bos != null)
                            bos.close();
                    } catch (Exception ignored){
                    }
                    throw e;
                }
                int len = httpBufferSizeDefault;
                long sum = 0;
                long max = connection.getContentLengthLong();
                byte[] bytes = new byte[len];
                try {
                    while ((len = bis.read(bytes)) != -1) {
                        bos.write(bytes, 0, len);
                        sum += len;
                        this.updateMessage("当前已下载：" + Downloader.getFormattedSizeString(sum));
                        this.updateProgress(sum, max);
                        if (this.isCancelled()) {
                            this.updateMessage("下载进程已被取消");
                            break;
                        }
                    }
                    this.updateProgress(max, max);
                    bos.flush();
                } catch (IOException e) {
                    throw e;
                } finally {
                    try {
                        if (connection != null && connection.getInputStream() != null)
                            connection.getInputStream().close();
                        if (bis != null)
                            bis.close();
                        if (bos != null)
                            bos.close();
                    } catch (Exception ignored){
                    }
                }
                Logger.info("Downloader", "Downloaded " + $localPath + " , file size: " + sum);
                return this.isDone() && !this.isCancelled();
            }
        };
    }

    public Task<Boolean> createUnzipTask(String $zipPath, String $destPath) {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                Logger.info("Unzip", "Unzipping " + $zipPath + " to " + $destPath);
                try {
                    IOUtils.ZipUtil.unzip($zipPath, $destPath, true);
                } catch (IOException e) {
                    //e.printStackTrace();
                    throw e;
                }
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
                    IOUtils.FileUtil.delete(new File(PathConfig.tempModelsUnzipDirPath).toPath(), true);
                    IOUtils.FileUtil.delete(new File(PathConfig.tempModelsZipCachePath).toPath(), true);
                } catch (IOException ignored) {
                }
                return true;
            }
        };
    }

    private void dealModelSearch(String $keyWords) {
        searchModelList.getItems().clear();
        AssetCtrl[] result = AssetCtrl.searchByKeyWords($keyWords, foundModelAssets);
        String[] assetIdList = AssetCtrl.getAssetIdList(result);
        String tag = "";
        for (String s : modelsDatasetFull.getJSONObject("sortTags").keySet())
            if (searchModelFilter.getValue().equals(modelsDatasetFull.getJSONObject("sortTags").getString(s)))
                tag = s;
        for (JFXListCell<AssetCtrl> item : foundModelItems) {
            for (String assetId : assetIdList) {
                if (item.getId().equals(assetId) &&
                        (isNoFilter || (item.getItem().sortTags != null && item.getItem().sortTags.contains(tag)))) {
                    searchModelList.getItems().add(item);
                    break;
                }
            }
        }
        Logger.info("ModelList", "Search \"" + $keyWords + "\" (" + searchModelList.getItems().size() + ")");
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

    private void dealModelReload() {
        popLoading(e -> {
            initModelAssets(true);
            initModelSearch();
            loadFailureTip.setVisible(foundModelItems.length == 0);
            startBtn.setDisable(foundModelItems.length == 0);
            dealModelSearch("");
            System.gc();
            Logger.info("ModelList", "Reloaded");
        });
    }

    private JFXListCell<AssetCtrl> getMenuItem(AssetCtrl $assetCtrl, JFXListView<JFXListCell<AssetCtrl>> $container) {
        double width = $container.getPrefWidth();
        width -= $container.getPadding().getLeft() + $container.getPadding().getRight();
        width *= 0.75;
        double height = 30;
        double divide = 0.5;
        JFXListCell<AssetCtrl> item = new JFXListCell<>();
        item.getStyleClass().addAll("Search-models-item", "scroll-v");
        Label name = new Label($assetCtrl.toString());
        name.getStyleClass().addAll("Search-models-label", "Search-models-label-primary");
        name.setPrefSize($assetCtrl.skinGroupName == null ? width : width * divide, height);
        name.setLayoutX(0);
        Label alias1 = new Label($assetCtrl.skinGroupName);
        alias1.getStyleClass().addAll("Search-models-label", "Search-models-label-secondary");
        alias1.setPrefSize(width * (1 - divide), height);
        alias1.setLayoutX($assetCtrl.skinGroupName == null ? 0 : width * (1 - divide));

        item.setPrefSize(width, height);
        item.setGraphic(new Group(name, alias1));
        item.setItem($assetCtrl);
        item.setId($assetCtrl.assetId);
        return item;
    }

    private void selectModel(AssetCtrl $asset, ListCell<AssetCtrl> $item) {
        // Reset
        if (selectedModelItem != null)
            selectedModelItem.getStyleClass().setAll("Search-models-item");
        selectedModelItem = $item;
        selectedModelItem.getStyleClass().add("Search-models-item-active");
        // Display details
        final String tooltipStyle = "-fx-font-size:10px;-fx-font-weight:normal;";
        selectedModelName.setText($asset.name);
        selectedModelAppellation.setText($asset.appellation);
        selectedModelSkinGroupName.setText($asset.skinGroupName);
        selectedModelType.setText($asset.type);
        Tooltip selectedModelNameTip = new Tooltip("ID: " + $asset.assetId);
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
        config.character_recent = $asset.getAssetFilePath("");
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

    private static int[] getDefaultMonitorInfo() {
        Graphics.DisplayMode displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
        return new int[] {displayMode.width, displayMode.height, displayMode.refreshRate, displayMode.bitsPerPixel};
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
}
