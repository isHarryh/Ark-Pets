/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.assets.ModelItem;
import cn.harryh.arkpets.assets.ModelItemGroup;
import cn.harryh.arkpets.assets.ModelsDataset;
import cn.harryh.arkpets.guitasks.*;
import cn.harryh.arkpets.guitasks.requests.DownloadModelDatasetTask;
import cn.harryh.arkpets.guitasks.requests.DownloadModelsTask;
import cn.harryh.arkpets.guitasks.requests.McCheckModelsUpdateTask;
import cn.harryh.arkpets.network.SourceStrategy;
import cn.harryh.arkpets.network.api.McQueryVersion;
import cn.harryh.arkpets.utils.GuiComponents.NoticeBar;
import cn.harryh.arkpets.utils.*;
import com.alibaba.fastjson2.JSONObject;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static cn.harryh.arkpets.Const.*;
import static cn.harryh.arkpets.Const.PathConfig.*;


public final class ModelsModule implements Controller<ArkHomeFX> {
    @FXML
    private Label loadEmptyAction;
    @FXML
    private Pane loadFailureTip;
    @FXML
    private Button searchModelConfirm;
    @FXML
    private Button searchModelReset;
    @FXML
    private Button searchModelRandom;
    @FXML
    private Button searchModelReload;
    @FXML
    private TextField searchModelInput;
    @FXML
    private Label searchModelStatus;
    @FXML
    private ListView<ModelItem> modelListView;
    @FXML
    private Label selectedModelName;
    @FXML
    private Label selectedModelAppellation;
    @FXML
    private Label selectedModelSkinGroupName;
    @FXML
    private Label selectedModelType;
    @FXML
    private Button modelWiki;
    @FXML
    private Button modelFavorite;
    @FXML
    public SVGPath modelFavoriteIconFill;
    @FXML
    private Button topFavorite;

    @FXML
    private AnchorPane infoPane;
    @FXML
    private AnchorPane filterPane;
    @FXML
    private AnchorPane managePane;
    @FXML
    private Button toggleFilterPane;
    @FXML
    private Button toggleManagePane;
    @FXML
    private ScrollPane infoPaneTagScroll;
    @FXML
    private FlowPane infoPaneTagFlow;
    @FXML
    private Label filterPaneTagClear;
    @FXML
    private ScrollPane filterPaneTagScroll;
    @FXML
    private FlowPane filterPaneTagFlow;

    @FXML
    private VBox noticeBox;
    @FXML
    private Button modelUpdate;
    @FXML
    private Button modelFetch;
    @FXML
    private Button modelVerify;
    @FXML
    private Button modelReFetch;
    @FXML
    private Button modelImport;
    @FXML
    private Button modelExport;
    @FXML
    private Label modelHelp;

    private ModelItemGroup assetItemList;
    private ModelItemWrapper selectedModel;
    private final ObservableList<ModelItem> targetList = FXCollections.observableArrayList();
    private ObservableSet<String> filterTagSet = FXCollections.observableSet();
    private boolean filterFavorite;

    private GuiPrefabs.PeerNodeComposer infoPaneComposer;
    private GuiPrefabs.PeerNodeComposer mngBtnComposer;
    private NoticeBar datasetTooLowVerNotice;
    private NoticeBar datasetTooHighVerNotice;

    private ArkHomeFX app;

    @Override
    public void initializeWith(ArkHomeFX app) {
        this.app = app;
        this.selectedModel = new ModelItemWrapper();
        this.modelListView.setItems(targetList);
        ScrollUtils.addSmoothScrolling(modelListView);

        infoPaneComposer = new GuiPrefabs.PeerNodeComposer();
        infoPaneComposer.add(0, infoPane);
        infoPaneComposer.add(1,
                e -> GuiPrefabs.replaceStyleClass(toggleFilterPane, "btn-secondary", "btn-primary"),
                e -> GuiPrefabs.replaceStyleClass(toggleFilterPane, "btn-primary", "btn-secondary"),
                filterPane);
        infoPaneComposer.add(2,
                e -> GuiPrefabs.replaceStyleClass(toggleManagePane, "btn-secondary", "btn-primary"),
                e -> GuiPrefabs.replaceStyleClass(toggleManagePane, "btn-primary", "btn-secondary"),
                managePane);
        mngBtnComposer = new GuiPrefabs.PeerNodeComposer();
        mngBtnComposer.add(0, modelFetch);
        mngBtnComposer.add(1, modelUpdate, modelReFetch, modelVerify, modelExport);

        initInfoPane();
        initModelSearch();
        initModelFilter();
        initModelManage();
        modelReload(false);
        Platform.runLater(() -> {
            GuiPrefabs.disableScrollPaneCache(infoPaneTagScroll);
            GuiPrefabs.disableScrollPaneCache(filterPaneTagScroll);
        });
    }

    public boolean initModelsDataset(boolean doPopNotice) {
        try {
            try {
                // Read and initialize the dataset
                app.modelsDataset = new ModelsDataset(
                        JSONObject.parseObject(
                                IOUtils.FileUtil.readString(new File(PathConfig.fileModelsDataPath), charsetDefault)
                        )
                );
                app.modelsDataset.data.removeIf(Predicate.not(ModelItem::isValid));
                try {
                    // Check the dataset compatibility
                    Version compatibleVersion = app.modelsDataset.arkPetsCompatibility;
                    if (appVersion.lessThan(compatibleVersion)) {
                        datasetTooHighVerNotice.activate();
                        Logger.warn("ModelManager", "The model dataset version may be too high which requiring program version " + compatibleVersion);
                    } else {
                        datasetTooHighVerNotice.suppress();
                    }
                    if (datasetLowestVersion.greaterThan(compatibleVersion)) {
                        datasetTooLowVerNotice.activate();
                        Logger.warn("ModelManager", "The model dataset version may be too low");
                    } else {
                        datasetTooLowVerNotice.suppress();
                    }
                } catch (Exception ex) {
                    Logger.warn("ModelManager", "Failed to get the compatibility of the model database.");
                }
                if (mngBtnComposer.getActivatedId() != 1)
                    mngBtnComposer.activate(1);
                Logger.debug("ModelManager", "Initialized model dataset successfully.");
                return true;
            } catch (Exception e) {
                // Explicitly set models dataset to empty.
                app.modelsDataset = null;
                throw e;
            }

            // If any exception occurred during the progress above:
        } catch (FileNotFoundException e) {
            Logger.warn("ModelManager", "Failed to initialize model dataset due to file not found. (" + e.getMessage() + ")");
            if (doPopNotice) {
                GuiPrefabs.Dialogs.createCommonDialog(app.body,
                        GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_WARNING_ALT, GuiPrefabs.COLOR_WARNING),
                        "模型载入失败",
                        "模型未成功载入：未找到数据集。",
                        "模型数据集文件 " + PathConfig.fileModelsDataPath + " 可能不在工作目录下。\n请先前往 [选项] 进行模型下载。",
                        null).show();
            }
        } catch (ModelsDataset.DatasetKeyException e) {
            Logger.warn("ModelManager", "Failed to initialize model dataset due to dataset parsing error. (" + e.getMessage() + ")");
            if (doPopNotice)
                GuiPrefabs.Dialogs.createCommonDialog(app.body,
                        GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_WARNING_ALT, GuiPrefabs.COLOR_WARNING),
                        "模型载入失败",
                        "模型未成功载入：数据集解析失败。",
                        "模型数据集可能不完整，或无法被启动器正确识别。请尝试更新模型或更新软件。",
                        null).show();
        } catch (IOException e) {
            Logger.error("ModelManager", "Failed to initialize model dataset due to unknown reasons, details see below.", e);
            if (doPopNotice)
                GuiPrefabs.Dialogs.createCommonDialog(app.body,
                        GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_WARNING_ALT, GuiPrefabs.COLOR_WARNING),
                        "模型载入失败",
                        "模型未成功载入：发生意外错误。",
                        "失败原因概要：" + e.getLocalizedMessage(),
                        null).show();
        }
        if (mngBtnComposer.getActivatedId() != 0)
            mngBtnComposer.activate(0);
        return false;
    }

    private void initInfoPane() {
        // Model info labels
        toggleFilterPane.setOnAction(e -> infoPaneComposer.toggle(1, 0));
        toggleManagePane.setOnAction(e -> infoPaneComposer.toggle(2, 0));
        selectedModelName.textProperty().bind(selectedModel.nameProperty);
        selectedModelAppellation.textProperty().bind(selectedModel.appellationProperty);
        selectedModelType.textProperty().bind(selectedModel.typeProperty);
        selectedModelSkinGroupName.textProperty().bind(selectedModel.skinGroupNameProperty);
        GuiPrefabs.addTooltip(selectedModelName, selectedModel.nameProperty);
        GuiPrefabs.addTooltip(selectedModelAppellation, selectedModel.appellationProperty);
        GuiPrefabs.addTooltip(selectedModelType, selectedModel.typeProperty);
        GuiPrefabs.addTooltip(selectedModelSkinGroupName, selectedModel.skinGroupNameProperty);

        // Model quick operations
        modelWiki.setOnAction(e -> {
            String name = selectedModel.nameProperty.get();
            if (name != null && !name.isEmpty()) {
                app.popBrowser(urlWikiPrefix + URLEncoder.encode(name, StandardCharsets.UTF_8));
            }
        });
        modelWiki.visibleProperty().bind(selectedModel.getHasWikiProperty());
        GuiPrefabs.addTooltip(modelWiki, "Wiki");

        modelFavorite.setOnAction(e -> {
            selectedModel.setFavorite(!selectedModel.getFavoriteProperty().get());
            modelListView.refresh();
            app.config.save();
        });
        modelFavoriteIconFill.visibleProperty().bind(selectedModel.getFavoriteProperty());
        GuiPrefabs.addTooltip(modelFavorite, "收藏");
    }

    private void initModelSearch() {
        searchModelInput.setPromptText("输入关键字");
        searchModelInput.setOnKeyPressed(e -> {
            if (e.getCode().getName().equals(KeyCode.ENTER.getName()))
                modelSearch(searchModelInput.getText());
        });

        searchModelConfirm.setOnAction(e -> modelSearch(searchModelInput.getText()));

        searchModelReset.setOnAction(e -> app.popLoading(ev -> {
            Logger.debug("ModelManager", "Reset search and filter conditions");
            searchModelInput.clear();
            searchModelInput.requestFocus();
            filterTagSet.clear();
            modelSearch("");
            infoPaneComposer.activate(0);
        }));

        loadEmptyAction.setOnMouseClicked(e -> {
            Logger.debug("ModelManager", "Reset requested from placeholder");
            if (filterFavorite)
                topFavorite.getOnAction().handle(new ActionEvent(loadEmptyAction, topFavorite));
            searchModelReset.getOnAction().handle(new ActionEvent(loadEmptyAction, searchModelReset));
        });

        searchModelRandom.setOnAction(e -> modelRandom());

        searchModelReload.setOnAction(e -> modelReload(true));
    }

    private void initModelFilter() {
        ScrollUtils.addSmoothScrolling(filterPaneTagScroll);
        filterPaneTagClear.setOnMouseClicked(e -> app.popLoading(ev -> {
            filterTagSet.clear();
            modelSearch(searchModelInput.getText());
            infoPaneComposer.activate(0);
        }));

        if (app.config.character_favorites == null) {
            app.config.character_favorites = new JSONObject();
            app.config.save();
        }

        topFavorite.setOnAction(e -> {
            Logger.debug("ModelManager", "Toggle favorite display");
            modelListView.scrollTo(0);
            if (filterFavorite) {
                GuiPrefabs.replaceStyleClass(topFavorite, "btn-primary", "btn-secondary");
            } else {
                GuiPrefabs.replaceStyleClass(topFavorite, "btn-secondary", "btn-primary");
            }
            filterFavorite = !filterFavorite;
            modelSearch(searchModelInput.getText());
            ModelItem recentSelected = assetItemList.searchByRelPath(app.config.character_asset);
            if (recentSelected != null)
                for (ModelItem cell : modelListView.getItems())
                    if (recentSelected.equals(cell)) {
                        modelListView.scrollTo(cell);
                        modelListView.getSelectionModel().select(cell);
                    }
        });
    }

    private void initModelManage() {
        datasetTooLowVerNotice = new NoticeBar(noticeBox) {
            @Override
            protected Color getColor() {
                return GuiPrefabs.COLOR_WARNING;
            }

            @Override
            protected String getIconSVGPath() {
                return GuiPrefabs.Icons.SVG_WARNING_ALT;
            }

            @Override
            protected String getText() {
                return "模型库版本太旧，可能不被软件兼容，请您重新下载模型。";
            }
        };
        datasetTooHighVerNotice = new NoticeBar(noticeBox) {
            @Override
            protected Color getColor() {
                return GuiPrefabs.COLOR_WARNING;
            }

            @Override
            protected String getIconSVGPath() {
                return GuiPrefabs.Icons.SVG_WARNING_ALT;
            }

            @Override
            protected String getText() {
                return "软件版本太旧，可能不被模型库兼容，建议您更新软件。";
            }

            @Override
            protected void onClick(MouseEvent event) {
                app.popBrowser(urlDownload);
            }
        };

        EventHandler<ActionEvent> modelFetchEventHandler = e -> {
            /* Foreground fetch models */
            // Go to [Step 1/3]:
            DownloadModelsTask task = new DownloadModelsTask(app.body, GuiTask.GuiTaskStyle.COMMON) {
                @Override
                protected void onDownloadedFile(File file) {
                    // Go to [Step 2/3]:
                    new UnzipModelsTask(parent, GuiTaskStyle.STRICT, file.getPath()) {
                        @Override
                        protected void onSucceeded(boolean result) {
                            // Go to [Step 3/3]:
                            new PostUnzipModelTask(parent, GuiTaskStyle.STRICT) {
                                @Override
                                protected void onSucceeded(boolean result) {
                                    try {
                                        IOUtils.FileUtil.delete(file.toPath(), false);
                                    } catch (IOException ex) {
                                        Logger.warn("Task", "The zip file cannot be deleted, because " + ex.getMessage());
                                    }
                                    app.modelsModule.modelReload(true);
                                }
                            }.start();
                        }
                    }.start();
                }
            };

            assertDownloadSource(true, task::start);
        };

        modelUpdate.setOnAction(e -> {
            /* Foreground check models update */
            if (!app.modelsModule.initModelsDataset(true))
                return;
            Logger.info("ModelManager", "Attempting checking model repo update by MirrorChyan");
            checkModelUpdateByMc(() -> {
                Logger.info("ModelManager", "Attempting checking model repo update by dataset file");
                checkModelUpdateByDataset(() -> {
                    Logger.error("ModelManager", "All approaches to check model repo update failed");
                    GuiPrefabs.Dialogs.createCommonDialog(app.body,
                            GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_DANGER, GuiPrefabs.COLOR_DANGER),
                            "检查模型更新",
                            "尝试了两种渠道都未能检查更新",
                            "这可能是网络原因导致的，详情参见日志。",
                            null).show();
                });
            });
        });

        modelFetch.setOnAction(modelFetchEventHandler);
        modelReFetch.setOnAction(modelFetchEventHandler);

        modelVerify.setOnAction(e -> {
            /* Foreground verify models */
            if (!app.modelsModule.initModelsDataset(true))
                return;
            new VerifyModelsTask(app.body, GuiTask.GuiTaskStyle.COMMON, app.modelsDataset).start();
        });

        modelImport.setOnAction(e -> {
            // Initialize the file chooser
            Logger.info("ModelManager", "Opening file chooser to import zip file");
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extensionFilter1 = new FileChooser.ExtensionFilter("All Files", "*.*");
            FileChooser.ExtensionFilter extensionFilter2 = new FileChooser.ExtensionFilter("Archives", "*.zip");
            fileChooser.getExtensionFilters().addAll(extensionFilter1, extensionFilter2);
            fileChooser.setSelectedExtensionFilter(extensionFilter2);
            // Handle the chosen file
            File zipFile = fileChooser.showOpenDialog(app.getWindow());
            if (zipFile != null && zipFile.isFile()) {
                Logger.info("ModelManager", "Importing zip file: " + zipFile);
                // Go to [Step 1/2]:
                new UnzipModelsTask(app.body, GuiTask.GuiTaskStyle.STRICT, zipFile.getPath()) {
                    @Override
                    protected void onSucceeded(boolean result) {
                        // Go to [Step 2/2]:
                        new PostUnzipModelTask(parent, GuiTaskStyle.STRICT) {
                            @Override
                            protected void onSucceeded(boolean result) {
                                app.modelsModule.modelReload(true);
                            }
                        }.start();
                    }
                }.start();
            }
        });

        modelExport.setOnAction(e -> {
            // Initialize the file chooser
            Logger.info("ModelManager", "Opening file chooser to export zip file");
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archives", "*.zip"));
            fileChooser.setInitialFileName(fileModelsZipName + ".zip");
            // Handle the chosen file
            File zipFile = fileChooser.showSaveDialog(app.getWindow());
            if (zipFile != null) {
                Logger.info("ModelManager", "Exporting zip file: " + zipFile);
                HashMap<String, String> contents = new java.util.HashMap<>();
                contents.put(fileModelsDataPath, fileModelsZipName + "/" + fileModelsDataPath);
                for (File dir : app.modelsDataset.storageDirectory.values())
                    contents.put(dir.toString(), fileModelsZipName + "/" + dir);
                new ZipTask(app.body, GuiTask.GuiTaskStyle.STRICT, zipFile.toString(), contents).start();
            }
        });

        modelHelp.setOnMouseClicked(e -> app.popBrowser(urlHelp));
    }

    public void modelSearch(String keyWords) {
        modelListView.getItems().clear();

        searchModelStatus.setText("");
        if (assertModelLoaded(false)) {
            // Filter assets
            int rawSize = assetItemList.size();
            ModelItemGroup favoured = !filterFavorite ? assetItemList :
                    assetItemList.filter(ModelItem.PropertyExtractor.ASSET_ITEM_KEY, app.config.character_favorites.keySet(), ModelItemGroup.FilterMode.MATCH_ANY);
            ModelItemGroup filtered = filterTagSet.isEmpty() ? favoured :
                    favoured.filter(ModelItem.PropertyExtractor.ASSET_ITEM_SORT_TAGS, filterTagSet);
            // Search assets
            long tStart = System.nanoTime();
            ModelItemGroup searched = filtered.searchByKeyWords(keyWords);
            long tEnd = System.nanoTime();
            int curSize = searched.size();
            searchModelStatus.setText((rawSize == curSize ? rawSize : curSize + " / " + rawSize) + " 个模型");
            targetList.clear();
            targetList.setAll(searched);
            Logger.info("ModelManager", "Search \"%s\" (%d results, %.1f ms)"
                    .formatted(keyWords, curSize, (tEnd - tStart) / 1000000f));
        }
        modelListView.refresh();
    }

    public void modelRandom() {
        if (!assertModelLoaded(true))
            return;
        int idx = (int) (Math.random() * (modelListView.getItems().size() - 1));
        modelListView.scrollTo(idx);
        modelListView.getSelectionModel().select(idx);
        modelListView.requestFocus();
    }

    public void modelReload(boolean doPopNotice) {
        app.popLoading(e -> {
            Logger.info("ModelManager", "Reloading");
            boolean willGc = !targetList.isEmpty();
            assetItemList = new ModelItemGroup();

            if (initModelsDataset(doPopNotice)) {
                // 1. Update list cells and asset items:
                try {
                    // Find every model assets.
                    assetItemList.addAll(app.modelsDataset.data.filter(ModelItem::isExisted));
                    if (assetItemList.isEmpty())
                        throw new IOException("Found no assets in the target directories.");
                    // Initialize list view.
                    modelListView.getSelectionModel().getSelectedItems().addListener(
                            (ListChangeListener<ModelItem>) (observable -> observable.getList().forEach(
                                    (Consumer<ModelItem>) this::selectCell)
                            )
                    );
                    modelListView.setCellFactory(listView -> new ModelListCell(listView.getPrefWidth() - 30, 30));
                    modelListView.setFixedCellSize(30);
                    // Write models to menu items.
                    selectedModel.setSortTags(app.modelsDataset.sortTags);
                    Logger.debug("ModelManager", "Initialized model assets successfully.");
                } catch (IOException ex) {
                    // Explicitly set all lists to empty.
                    Logger.error("ModelManager", "Failed to initialize model assets due to unknown reasons, details see below.", ex);
                    assetItemList = new ModelItemGroup();
                    if (doPopNotice)
                        GuiPrefabs.Dialogs.createCommonDialog(app.body,
                                GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_WARNING_ALT, GuiPrefabs.COLOR_WARNING),
                                "模型载入失败",
                                "模型未成功载入：读取模型列表失败。",
                                "失败原因概要：" + ex.getLocalizedMessage(),
                                null).show();
                }

                // 2. Reset filter pane:
                filterTagSet = FXCollections.observableSet();
                filterTagSet.addListener((SetChangeListener<String>) change -> {
                    Logger.debug("ModelManager", "Filter tag " + change);
                    if (change.getElementAdded() == null && change.getElementRemoved() == null)
                        return;
                    String s = change.getElementAdded() == null ? change.getElementRemoved() : change.getElementAdded();
                    String t = app.modelsDataset.sortTags == null ? s : app.modelsDataset.sortTags.getOrDefault(s, s);
                    for (Node node : filterPaneTagFlow.getChildren())
                        if (node instanceof Button tag && t.equals(tag.getText())) {
                            String styleFrom = change.getElementAdded() == null ? "info-tag-badge-active" : "info-tag-badge";
                            String styleTo = change.getElementAdded() == null ? "info-tag-badge" : "info-tag-badge-active";
                            GuiPrefabs.replaceStyleClass(tag, styleFrom, styleTo);
                        }
                });
                filterPaneTagFlow.getChildren().clear();
                if (assetItemList != null && app.modelsDataset != null) {
                    ArrayList<String> sortTags = new ArrayList<>(assetItemList.extract(ModelItem.PropertyExtractor.ASSET_ITEM_SORT_TAGS));
                    sortTags.sort(Comparator.naturalOrder());
                    sortTags.forEach(s -> {
                        String t = app.modelsDataset.sortTags == null ? s : app.modelsDataset.sortTags.getOrDefault(s, s);
                        Button tag = new GuiPrefabs.ButtonBuilder()
                                .setText(t)
                                .setAdditionalStyleClass("info-tag-badge")
                                .setOnAction(ev -> {
                                    if (filterTagSet.contains(s))
                                        filterTagSet.remove(s);
                                    else
                                        filterTagSet.add(s);
                                    modelSearch(searchModelInput.getText());
                                })
                                .build();
                        filterPaneTagFlow.getChildren().add(tag);
                    });
                }
                toggleFilterPane.getStyleClass().add("btn-noticeable");

                // 3. Update model list:
                modelSearch("");
                searchModelInput.clear();
                if (assetItemList != null && !targetList.isEmpty() &&
                        app.config.character_asset != null && !app.config.character_asset.isEmpty()) {
                    // Scroll to recent selected model and then select it.
                    ModelItem recentSelected = assetItemList.searchByRelPath(app.config.character_asset);
                    if (recentSelected != null) {
                        for (ModelItem cell : modelListView.getItems())
                            if (recentSelected.equals(cell)) {
                                modelListView.scrollTo(cell);
                                modelListView.getSelectionModel().select(cell);
                                selectedModel.setModelItem(cell);
                            }
                    }
                }
            }

            // Post process:
            loadFailureTip.setVisible(targetList.isEmpty());
            app.rootModule.launchBtn.setDisable(targetList.isEmpty());
            if (willGc)
                System.gc();
            Logger.info("ModelManager", "Reloaded");
        });
    }

    private void selectCell(ModelItem model) {
        selectedModel.setModelItem(model);
        // Setup tag flow pane
        infoPaneTagFlow.getChildren().clear();
        model.sortTags.forEach(o -> {
            String s = o.toString();
            String t = app.modelsDataset.sortTags == null ?
                    s : app.modelsDataset.sortTags.getOrDefault(s, s);
            Button tag = new GuiPrefabs.ButtonBuilder()
                    .setText(t)
                    .setAdditionalStyleClass("info-tag-badge-active")
                    .setOnAction(e -> {
                        filterTagSet.clear();
                        filterTagSet.add(s);
                        infoPaneComposer.activate(1);
                        modelSearch(searchModelInput.getText());
                    })
                    .build();
            infoPaneTagFlow.getChildren().add(tag);
        });
        // Switch info pane
        if (infoPaneComposer.getActivatedId() != 0)
            infoPaneComposer.activate(0);
        // Apply to app.config, but not to save
        app.config.character_asset = model.getLocation();
        app.config.character_files = model.assetList;
        app.config.character_label = model.name;
    }

    private boolean assertModelLoaded(boolean doPopNotice) {
        if (app.modelsDataset == null) {
            // Not loaded:
            if (doPopNotice)
                GuiPrefabs.Dialogs.createCommonDialog(app.body,
                        GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_WARNING_ALT, GuiPrefabs.COLOR_WARNING),
                        "未能加载模型",
                        "请确保模型加载成功后再进行此操作。",
                        "请先在[选项]中进行模型下载。\n如您已下载模型，请尝试点击[重载]按钮。",
                        null).show();
            return false;
        } else {
            // Loaded:
            return true;
        }
    }

    private void assertDownloadSource(boolean doDoubleCheck, Runnable onDone) {
        SourceStrategy.getStrategy("ModelDownload").clearPrimarySource();
        String cdk;
        if ((cdk = app.config.getMcCdk()) != null) {
            Logger.debug("ModelManager", "Attempting using CDK to fetch resource");
            new McCheckModelsUpdateTask(app.body, GuiTask.GuiTaskStyle.STRICT, cdk) {
                @Override
                protected void onReceivedData(JSONObject json) {
                    McQueryVersion value = json.toJavaObject(McQueryVersion.class);
                    try {
                        value.raiseForCode();
                        SourceStrategy.getStrategy("ModelDownload").setPrimarySource("MirrorChyan", value.data.url);

                        Logger.info("ModelManager", "CDK assertion passed");
                        if (onDone != null)
                            onDone.run();
                    } catch (McQueryVersion.McException e) {
                        Logger.warn("ModelManager", "CDK assertion not passed, " + e.getMessage());
                        if (doDoubleCheck)
                            app.dialogs.popDialog("downloadDialog", (Runnable) () -> assertDownloadSource(false, onDone));
                        else if (onDone != null)
                            onDone.run();
                    }
                }
            }.start();
        } else {
            Logger.info("ModelManager", "CDK assertion not passed due to not set");
            if (doDoubleCheck)
                app.dialogs.popDialog("downloadDialog", (Runnable) () -> assertDownloadSource(false, onDone));
            else if (onDone != null)
                onDone.run();
        }
    }

    private void checkModelUpdateByMc(Runnable onFail) {
        new McCheckModelsUpdateTask(app.body, GuiTask.GuiTaskStyle.COMMON, "") {
            @Override
            protected void onReceivedData(JSONObject json) {
                McQueryVersion value = json.toJavaObject(McQueryVersion.class);
                try {
                    value.raiseForCode();
                    checkModelUpdateByMD5(value.data.version_name);
                } catch (McQueryVersion.McException | IOException e) {
                    this.onFailed(e);
                }
            }

            @Override
            protected void onFailed(Throwable e) {
                Logger.error("ModelManager", "Model repo version check (mc) failed, details see below.", e);
                if (onFail != null)
                    onFail.run();
            }
        }.start();
    }

    private void checkModelUpdateByDataset(Runnable onFail) {
        new DownloadModelDatasetTask(app.body, GuiTask.GuiTaskStyle.COMMON) {
            @Override
            protected void onDownloadedFile(File file) {
                try {
                    String remoteMD5 = IOUtils.FileUtil.getMD5(file);
                    checkModelUpdateByMD5(remoteMD5);
                } catch (IOException e) {
                    this.onFailed(e);
                }
            }

            @Override
            protected void onFailed(Throwable e) {
                Logger.error("ModelManager", "Model repo version check (dataset) failed, details see below.", e);
                if (onFail != null)
                    onFail.run();
            }
        }.start();
    }

    private void checkModelUpdateByMD5(String remoteMD5) throws IOException {
        if (!app.modelsModule.initModelsDataset(true))
            return;
        // Compare the remote models dataset and the local models dataset by their MD5
        String localMD5 = IOUtils.FileUtil.getMD5(new File(PathConfig.fileModelsDataPath));
        String detail = "本地摘要值：" + localMD5 + "\n远程摘要值：" + remoteMD5;
        if (localMD5.equals(remoteMD5)) {
            // Same result: no update
            Logger.info("ModelManager", "Model repo version check finished (up-to-dated)");
            GuiPrefabs.Dialogs.createCommonDialog(app.body,
                    GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_SUCCESS_ALT, GuiPrefabs.COLOR_SUCCESS),
                    "检查模型更新",
                    "无需进行模型库更新",
                    "本地模型库的版本与远程模型库的一致。",
                    "提示：远程模型库的版本不一定和游戏官方同步更新。\n\n" + detail).show();
        } else {
            // Different result: has update
            GuiPrefabs.Dialogs.createCommonDialog(app.body,
                    GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_INFO_ALT, GuiPrefabs.COLOR_INFO),
                    "检查模型更新",
                    "模型库似乎有更新！",
                    "您可以 [重新下载] 模型，以更新模型库版本。",
                    detail).show();
            Logger.info("ModelManager", "Model repo version check finished (not up-to-dated)");
        }
    }


    private class ModelListCell extends GuiPrefabs.RipperListCell<ModelItem> {
        private final double width;
        private final double height;
        private final SVGPath icon;
        private final Label name;
        private final Label alias;
        private static final double divide = 0.618;
        private final static DropShadow iconShadow = new DropShadow(null, GuiPrefabs.COLOR_WHITE, 4.0, 0.5, 0.0, 0.0);

        public ModelListCell(double width, double height) {
            super();
            this.width = width;
            this.height = height;

            icon = GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_STAR_FILLED, GuiPrefabs.COLOR_WARNING);
            icon.setLayoutX(3);
            icon.setLayoutY(3);
            icon.setScaleX(0.75);
            icon.setScaleY(0.75);
            icon.setOpacity(0);
            icon.setEffect(iconShadow);
            name = new Label();
            name.getStyleClass().add("list-item-label");
            name.setTranslateX(25);
            alias = new Label();
            alias.setPrefSize(width * (1 - divide), height);
            alias.getStyleClass().add("list-item-label-sub");
            alias.setTranslateX(20);

            getContent().setAll(icon, name, alias);
            setPrefSize(width, height);
        }

        @Override
        protected void updateItem(ModelItem model, boolean empty) {
            super.updateItem(model, empty);
            if (empty || model == null) {
                setContentVisible(false);
            } else {
                name.setText(model.toString());
                name.setPrefSize(model.skinGroupName == null ? width : width * divide, height);
                alias.setText(model.skinGroupName);
                alias.setLayoutX(model.skinGroupName == null ? 0 : width * divide);
                icon.setOpacity(app.config.character_favorites.containsKey(model.key) ? 1 : 0);
                setId(model.getLocation());
                setContentVisible(true);
            }
        }
    }


    private class ModelItemWrapper {
        private final StringProperty nameProperty = new SimpleStringProperty();
        private final StringProperty typeProperty = new SimpleStringProperty();
        private final StringProperty skinGroupNameProperty = new SimpleStringProperty();
        private final StringProperty appellationProperty = new SimpleStringProperty();
        private HashMap<String, String> sortTags;
        private ModelItem modelItem;

        private final BooleanBinding hasWikiProperty = new BooleanBinding() {
            @Override
            protected boolean computeValue() {
                return modelItem != null && nameProperty.get() != null && !nameProperty.get().isEmpty();
            }
        };
        private final BooleanBinding favoriteProperty = new BooleanBinding() {
            @Override
            protected boolean computeValue() {
                return modelItem != null && app.config.character_favorites.containsKey(modelItem.key);
            }
        };

        public void setSortTags(HashMap<String, String> sortTags) {
            this.sortTags = sortTags;
        }

        public void setModelItem(ModelItem modelItem) {
            if (modelItem == null) return;
            this.modelItem = modelItem;
            nameProperty.set(modelItem.name);
            typeProperty.set(sortTags == null ?
                    modelItem.type : sortTags.getOrDefault(modelItem.type, modelItem.type));
            skinGroupNameProperty.set(modelItem.skinGroupName);
            appellationProperty.set(modelItem.appellation);
            favoriteProperty.invalidate();
            hasWikiProperty.invalidate();
        }

        public void setFavorite(boolean favorite) {
            if (modelItem == null) return;
            app.config.character_favorites.remove(modelItem.key);
            if (favorite) {
                app.config.character_favorites.put(modelItem.key, new JSONObject());
                Logger.debug("ModelManager", "Add favorite model " + modelItem.key);
            } else {
                Logger.debug("ModelManager", "Remove favorite model " + modelItem.key);
            }
            favoriteProperty.invalidate();
            hasWikiProperty.invalidate();
        }

        public BooleanBinding getFavoriteProperty() {
            return favoriteProperty;
        }

        public BooleanBinding getHasWikiProperty() {
            return hasWikiProperty;
        }
    }
}
