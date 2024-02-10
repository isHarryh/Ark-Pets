/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.assets.AssetItem;
import cn.harryh.arkpets.assets.AssetItemGroup;
import cn.harryh.arkpets.assets.ModelsDataset;
import cn.harryh.arkpets.guitasks.*;
import cn.harryh.arkpets.utils.*;
import com.alibaba.fastjson.JSONObject;
import com.jfoenix.controls.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static cn.harryh.arkpets.Const.*;
import static cn.harryh.arkpets.Const.PathConfig.*;
import static cn.harryh.arkpets.utils.GuiPrefabs.tooltipStyle;


public final class ModelsModule implements Controller<ArkHomeFX> {
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
    private Label searchModelStatus;
    @FXML
    private JFXListView<JFXListCell<AssetItem>> searchModelView;
    @FXML
    private Label selectedModelName;
    @FXML
    private Label selectedModelAppellation;
    @FXML
    private Label selectedModelSkinGroupName;
    @FXML
    private Label selectedModelType;

    @FXML
    private AnchorPane infoPane;
    @FXML
    private AnchorPane filterPane;
    @FXML
    private AnchorPane managePane;
    @FXML
    private JFXButton toggleFilterPane;
    @FXML
    private JFXButton toggleManagePane;
    @FXML
    private FlowPane infoPaneTagFlow;
    @FXML
    public Label filterPaneTagClear;
    @FXML
    private FlowPane filterPaneTagFlow;

    @FXML
    private VBox noticeBox;
    @FXML
    private JFXButton modelUpdate;
    @FXML
    private JFXButton modelFetch;
    @FXML
    private JFXButton modelVerify;
    @FXML
    private JFXButton modelReFetch;
    @FXML
    private JFXButton modelImport;
    @FXML
    private JFXButton modelExport;
    @FXML
    private Label modelHelp;

    private AssetItemGroup assetItemList;
    private JFXListCell<AssetItem> selectedModelCell;
    private ArrayList<JFXListCell<AssetItem>> modelCellList = new ArrayList<>();
    private ObservableSet<String> filterTagSet = FXCollections.observableSet();
    private GuiPrefabs.PeerNodeComposer infoPaneComposer;
    private GuiPrefabs.PeerNodeComposer mngBtnComposer;
    private GuiComponents.NoticeBar datasetTooLowVerNotice;
    private GuiComponents.NoticeBar datasetTooHighVerNotice;

    private ArkHomeFX app;

    @Override
    public void initializeWith(ArkHomeFX app) {
        this.app = app;
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
                app.modelsDataset.data.removeIf(Predicate.not(AssetItem::isValid));
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
                JFXDialog dialog = GuiPrefabs.DialogUtil.createCommonDialog(app.root, GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_WARNING_ALT, GuiPrefabs.Colors.COLOR_WARNING), "模型载入失败", "模型未成功载入：未找到数据集。",
                        "模型数据集文件 " + PathConfig.fileModelsDataPath + " 可能不在工作目录下。\n请先前往 [选项] 进行模型下载。", null);
                dialog.show();
            }
        } catch (ModelsDataset.DatasetKeyException e) {
            Logger.warn("ModelManager", "Failed to initialize model dataset due to dataset parsing error. (" + e.getMessage() + ")");
            if (doPopNotice)
                GuiPrefabs.DialogUtil.createCommonDialog(app.root, GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_WARNING_ALT, GuiPrefabs.Colors.COLOR_WARNING), "模型载入失败", "模型未成功载入：数据集解析失败。",
                        "模型数据集可能不完整，或无法被启动器正确识别。请尝试更新模型或更新软件。", null).show();
        } catch (IOException e) {
            Logger.error("ModelManager", "Failed to initialize model dataset due to unknown reasons, details see below.", e);
            if (doPopNotice)
                GuiPrefabs.DialogUtil.createCommonDialog(app.root, GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_WARNING_ALT, GuiPrefabs.Colors.COLOR_WARNING), "模型载入失败", "模型未成功载入：发生意外错误。",
                        "失败原因概要：" + e.getLocalizedMessage(), null).show();
        }
        if (mngBtnComposer.getActivatedId() != 0)
            mngBtnComposer.activate(0);
        return false;
    }

    private void initModelAssets(boolean doPopNotice) {
        modelCellList = new ArrayList<>();
        assetItemList = new AssetItemGroup();
        if (!initModelsDataset(doPopNotice))
            return;
        try {
            // Find every model assets.
            assetItemList.addAll(app.modelsDataset.data.filter(AssetItem::isExisted));
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
            assetItemList.forEach(assetItem -> modelCellList.add(getMenuItem(assetItem, searchModelView)));
            Logger.debug("ModelManager", "Initialized model assets successfully.");
        } catch (IOException e) {
            // Explicitly set all lists to empty.
            Logger.error("ModelManager", "Failed to initialize model assets due to unknown reasons, details see below.", e);
            if (doPopNotice)
                GuiPrefabs.DialogUtil.createCommonDialog(app.root, GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_WARNING_ALT, GuiPrefabs.Colors.COLOR_WARNING), "模型载入失败", "模型未成功载入：读取模型列表失败。",
                        "失败原因概要：" + e.getLocalizedMessage(), null).show();
        }
    }

    private void initInfoPane() {
        toggleFilterPane.setOnAction(e -> infoPaneComposer.toggle(1, 0));
        toggleManagePane.setOnAction(e -> infoPaneComposer.toggle(2, 0));
    }

    private void initModelSearch() {
        searchModelInput.setPromptText("输入关键字");
        searchModelInput.setOnKeyPressed(e -> {
            if (e.getCode().getName().equals(KeyCode.ENTER.getName()))
                modelSearch(searchModelInput.getText());
        });

        searchModelConfirm.setOnAction(e -> modelSearch(searchModelInput.getText()));

        searchModelReset.setOnAction(e -> app.popLoading(ev -> {
            searchModelInput.setText("");
            searchModelInput.requestFocus();
            filterTagSet.clear();
            modelSearch("");
            infoPaneComposer.activate(0);
        }));

        searchModelRandom.setOnAction(e -> modelRandom());

        searchModelReload.setOnAction(e -> modelReload(true));
    }

    private void initModelFilter() {
        filterPaneTagClear.setOnMouseClicked(e -> app.popLoading(ev -> {
            filterTagSet.clear();
            modelSearch(searchModelInput.getText());
            infoPaneComposer.activate(0);
        }));
    }

    private void initModelManage() {
        datasetTooLowVerNotice = new GuiComponents.NoticeBar(noticeBox) {
            @Override
            protected String getColorString() {
                return GuiPrefabs.Colors.COLOR_WARNING;
            }

            @Override
            protected String getIconSVGPath() {
                return GuiPrefabs.Icons.ICON_WARNING_ALT;
            }

            @Override
            protected String getText() {
                return "模型库版本太旧，可能不被软件兼容，请您重新下载模型。";
            }
        };
        datasetTooHighVerNotice = new GuiComponents.NoticeBar(noticeBox) {
            @Override
            protected String getColorString() {
                return GuiPrefabs.Colors.COLOR_WARNING;
            }

            @Override
            protected String getIconSVGPath() {
                return GuiPrefabs.Icons.ICON_WARNING_ALT;
            }

            @Override
            protected String getText() {
                return "软件版本太旧，可能不被模型库兼容，建议您更新软件。";
            }

            @Override
            protected void onClick(MouseEvent event) {
                NetUtils.browseWebpage(PathConfig.urlDownload);
            }
        };

        EventHandler<ActionEvent> modelFetchEventHandler = e -> {
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
                                    app.modelsModule.modelReload(true);
                                }
                            }.start();
                        }
                    }.start();
                }
            }.start();
        };

        modelUpdate.setOnAction(e -> {
            /* Foreground check models update */
            if (!app.modelsModule.initModelsDataset(true))
                return;
            new CheckModelUpdateTask(app.root, GuiTask.GuiTaskStyle.COMMON).start();
        });

        modelFetch.setOnAction(modelFetchEventHandler);
        modelReFetch.setOnAction(modelFetchEventHandler);

        modelVerify.setOnAction(e -> {
            /* Foreground verify models */
            if (!app.modelsModule.initModelsDataset(true))
                return;
            new VerifyModelsTask(app.root, GuiTask.GuiTaskStyle.COMMON, app.modelsDataset).start();
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
            File zipFile = fileChooser.showSaveDialog(app.root.getScene().getWindow());
            if (zipFile != null) {
                Logger.info("ModelManager", "Exporting zip file: " + zipFile);
                HashMap<String, String> contents = new java.util.HashMap<>();
                contents.put(fileModelsDataPath, fileModelsZipName + "/" + fileModelsDataPath);
                for (File dir : app.modelsDataset.storageDirectory.values())
                    contents.put(dir.toString(), fileModelsZipName + "/" + dir);
                new ZipTask(app.root, GuiTask.GuiTaskStyle.STRICT, zipFile.toString(), contents).start();
            }
        });

        modelHelp.setOnMouseClicked(e -> NetUtils.browseWebpage(urlHelp));
    }

    public void modelSearch(String keyWords) {
        searchModelView.getItems().clear();
        searchModelStatus.setText("");
        if (assertModelLoaded(false)) {
            // Filter and search assets
            int rawSize = assetItemList.size();
            AssetItemGroup filtered = filterTagSet.isEmpty() ? assetItemList :
                    assetItemList.filter(AssetItem.PropertyExtractor.ASSET_ITEM_SORT_TAGS, filterTagSet);
            AssetItemGroup searched = filtered.searchByKeyWords(keyWords);
            int curSize = searched.size();
            searchModelStatus.setText((rawSize == curSize ? rawSize : curSize + " / " + rawSize) + " 个模型");
            // Add cells
            for (JFXListCell<AssetItem> cell : modelCellList)
                if (searched.contains(cell.getItem()))
                    searchModelView.getItems().add(cell);
        }
        Logger.info("ModelManager", "Search \"" + keyWords + "\" (" + searchModelView.getItems().size() + ")");
        searchModelView.refresh();
    }

    public void modelRandom() {
        if (!assertModelLoaded(true))
            return;
        int idx = (int)(Math.random() * (searchModelView.getItems().size() - 1));
        searchModelView.scrollTo(idx);
        searchModelView.getSelectionModel().select(idx);
        searchModelView.requestFocus();
    }

    public void modelReload(boolean doPopNotice) {
        app.popLoading(e -> {
            Logger.info("ModelManager", "Reloading");
            initModelAssets(doPopNotice);
            initModelSearch();
            modelSearch("");
            // Select recent model
            if (assetItemList != null && !modelCellList.isEmpty() &&
                    app.config.character_asset != null && !app.config.character_asset.isEmpty()) {
                // Scroll to recent selected model
                AssetItem recentSelected = assetItemList.searchByRelPath(app.config.character_asset);
                if (recentSelected != null)
                    for (JFXListCell<AssetItem> cell : searchModelView.getItems())
                        if (recentSelected.equals(cell.getItem())) {
                            searchModelView.scrollTo(cell);
                            searchModelView.getSelectionModel().select(cell);
                        }
            }
            // Setup filter pane
            filterTagSet = FXCollections.observableSet();
            filterTagSet.addListener((SetChangeListener<String>)change -> {
                Logger.debug("ModelManager", "Filter tag " + change);
                if (change.getElementAdded() == null && change.getElementRemoved() == null)
                    return;
                String s = change.getElementAdded() == null ? change.getElementRemoved() : change.getElementAdded();
                String t = app.modelsDataset.sortTags == null ? s : app.modelsDataset.sortTags.getOrDefault(s, s);
                for (Node node : filterPaneTagFlow.getChildren())
                    if (node instanceof JFXButton tag && t.equals(tag.getText())) {
                        String styleFrom = change.getElementAdded() == null ? "info-tag-badge-active" : "info-tag-badge";
                        String styleTo = change.getElementAdded() == null ? "info-tag-badge" : "info-tag-badge-active";
                        GuiPrefabs.replaceStyleClass(tag, styleFrom, styleTo);
                    }
            });
            filterPaneTagFlow.getChildren().clear();
            if (assetItemList != null && app.modelsDataset != null) {
                assetItemList.extract(AssetItem.PropertyExtractor.ASSET_ITEM_SORT_TAGS).forEach(s -> {
                    String t = app.modelsDataset.sortTags == null ? s : app.modelsDataset.sortTags.getOrDefault(s, s);
                    JFXButton tag = new JFXButton(t);
                    tag.getStyleClass().add("info-tag-badge");
                    tag.setOnAction(ev -> {
                        if (filterTagSet.contains(s))
                            filterTagSet.remove(s);
                        else
                            filterTagSet.add(s);
                        modelSearch(searchModelInput.getText());
                    });
                    filterPaneTagFlow.getChildren().add(tag);
                });
            }
            // Finish reload
            loadFailureTip.setVisible(modelCellList.isEmpty());
            app.rootModule.launchBtn.setDisable(modelCellList.isEmpty());
            System.gc();
            Logger.info("ModelManager", "Reloaded");
        });
    }

    private JFXListCell<AssetItem> getMenuItem(AssetItem assetItem, JFXListView<JFXListCell<AssetItem>> container) {
        double width = container.getPrefWidth() - 50;
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
        selectedModelType.setText(app.modelsDataset.sortTags == null ?
                asset.type : app.modelsDataset.sortTags.getOrDefault(asset.type, asset.type));
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
        // Setup tag flow pane
        infoPaneTagFlow.getChildren().clear();
        asset.sortTags.forEach(o -> {
            String s = o.toString();
            String t = app.modelsDataset.sortTags == null ?
                    s : app.modelsDataset.sortTags.getOrDefault(s, s);
            JFXButton tag = new JFXButton(t);
            tag.getStyleClass().add("info-tag-badge-active");
            tag.setOnAction(e -> {
                filterTagSet.clear();
                filterTagSet.add(s);
                infoPaneComposer.activate(1);
                modelSearch(searchModelInput.getText());
            });
            infoPaneTagFlow.getChildren().add(tag);
        });
        // Switch info pane
        if (infoPaneComposer.getActivatedId() != 0)
            infoPaneComposer.activate(0);
        // Apply to app.config, but not to save
        app.config.character_asset = asset.getLocation();
        app.config.character_files = asset.assetList;
        app.config.character_label = asset.name;
    }

    private boolean assertModelLoaded(boolean doPopNotice) {
        if (app.modelsDataset == null) {
            // Not loaded:
            if (doPopNotice)
                GuiPrefabs.DialogUtil.createCommonDialog(app.root, GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_WARNING_ALT, GuiPrefabs.Colors.COLOR_WARNING), "未能加载模型", "请确保模型加载成功后再进行此操作。",
                        "请先在[选项]中进行模型下载。\n如您已下载模型，请尝试点击[重载]按钮。", null).show();
            return false;
        } else {
            // Loaded:
            return true;
        }
    }
}
