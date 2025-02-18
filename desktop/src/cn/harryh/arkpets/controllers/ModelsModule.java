/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.assets.ModelItem;
import cn.harryh.arkpets.assets.ModelItemGroup;
import cn.harryh.arkpets.assets.ModelsDataset;
import cn.harryh.arkpets.guitasks.*;
import cn.harryh.arkpets.utils.*;
import cn.harryh.arkpets.utils.GuiComponents.*;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    private JFXListView<JFXListCell<ModelItem>> searchModelView;
    @FXML
    private Label selectedModelName;
    @FXML
    private Label selectedModelAppellation;
    @FXML
    private Label selectedModelSkinGroupName;
    @FXML
    private Label selectedModelType;
    @FXML
    private JFXButton modelFavorite;
    @FXML
    private JFXButton topFavorite;

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

    private ModelItemGroup assetItemList;
    private JFXListCell<ModelItem> selectedModelCell;
    private ArrayList<JFXListCell<ModelItem>> modelCellList = new ArrayList<>();
    private ObservableSet<String> filterTagSet = FXCollections.observableSet();

    private GuiPrefabs.PeerNodeComposer infoPaneComposer;
    private GuiPrefabs.PeerNodeComposer mngBtnComposer;
    private NoticeBar datasetTooLowVerNotice;
    private NoticeBar datasetTooHighVerNotice;

    private final SVGPath favIcon = GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_STAR, GuiPrefabs.COLOR_LIGHT_GRAY);
    private final SVGPath favFillIcon = GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_STAR_FILLED, GuiPrefabs.COLOR_WARNING);
    private boolean filterFavorite;

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
        initModelFavorite();
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
                JFXDialog dialog = GuiPrefabs.Dialogs.createCommonDialog(app.body,
                        GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_WARNING_ALT, GuiPrefabs.COLOR_WARNING),
                        "模型载入失败",
                        "模型未成功载入：未找到数据集。",
                        "模型数据集文件 " + PathConfig.fileModelsDataPath + " 可能不在工作目录下。\n请先前往 [选项] 进行模型下载。",
                        null);
                dialog.show();
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
        filterPaneTagClear.setOnMouseClicked(e -> app.popLoading(ev -> {
            filterTagSet.clear();
            modelSearch(searchModelInput.getText());
            infoPaneComposer.activate(0);
        }));
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
                NetUtils.browseWebpage(PathConfig.urlDownload);
            }
        };

        EventHandler<ActionEvent> modelFetchEventHandler = e -> {
            /* Foreground fetch models */
            // Go to [Step 1/3]:
            new DownloadModelsTask(app.body, GuiTask.GuiTaskStyle.COMMON) {
                @Override
                protected void onSucceeded(boolean result){
                    // Go to [Step 2/3]:
                    new UnzipModelsTask(parent, GuiTaskStyle.STRICT, PathConfig.tempModelsZipCachePath) {
                        @Override
                        protected void onSucceeded(boolean result) {
                            // Go to [Step 3/3]:
                            new PostUnzipModelTask(parent, GuiTaskStyle.STRICT) {
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
            new CheckModelUpdateTask(app.body, GuiTask.GuiTaskStyle.COMMON).start();
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

        modelHelp.setOnMouseClicked(e -> NetUtils.browseWebpage(urlHelp));
    }

    private void initModelFavorite() {
        if (app.config.character_favorites == null) {
            app.config.character_favorites = new JSONObject();
            app.config.save();
        }

        modelFavorite.setGraphic(favIcon);
        modelFavorite.setRipplerFill(Color.GRAY);
        modelFavorite.setOnAction(e -> {
            String key = selectedModelCell.getItem().key;
            if (app.config.character_favorites.containsKey(key)) {
                app.config.character_favorites.remove(key);
                selectedModelCell.getStyleClass().remove("Search-models-item-favorite");
                modelFavorite.setGraphic(favIcon);
                Logger.debug("ModelManager", "Remove favorite model " + key);
            } else {
                app.config.character_favorites.put(key, new ModelItem.AssetPrefab());
                selectedModelCell.getStyleClass().add("Search-models-item-favorite");
                modelFavorite.setGraphic(favFillIcon);
                Logger.debug("ModelManager", "Add favorite model " + key);
            }
            app.config.save();
        });

        topFavorite.setOnAction(e -> {
            Logger.debug("ModelManager", "Toggle favorite display");
            searchModelView.scrollTo(0);
            if (filterFavorite) {
                GuiPrefabs.replaceStyleClass(topFavorite, "btn-primary", "btn-secondary");
            } else {
                GuiPrefabs.replaceStyleClass(topFavorite, "btn-secondary", "btn-primary");
            }
            filterFavorite = !filterFavorite;
            modelSearch(searchModelInput.getText());
            ModelItem recentSelected = assetItemList.searchByRelPath(app.config.character_asset);
            if (recentSelected != null)
                for (JFXListCell<ModelItem> cell : searchModelView.getItems())
                    if (recentSelected.equals(cell.getItem())) {
                        searchModelView.scrollTo(cell);
                        searchModelView.getSelectionModel().select(cell);
                    }
        });
    }

    public void modelSearch(String keyWords) {
        searchModelView.getItems().clear();
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
            // Add cells
            for (JFXListCell<ModelItem> cell : modelCellList)
                if (searched.contains(cell.getItem()))
                    searchModelView.getItems().add(cell);
            Logger.info("ModelManager", "Search \"%s\" (%d results, %.1f ms)"
                    .formatted(keyWords, curSize, (tEnd - tStart) / 1000000f));
        }
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
            boolean willGc = modelCellList != null;
            modelCellList = new ArrayList<>();
            assetItemList = new ModelItemGroup();

            if (initModelsDataset(doPopNotice)) {
                // 1. Update list cells and asset items:
                try {
                    // Find every model assets.
                    assetItemList.addAll(app.modelsDataset.data.filter(ModelItem::isExisted));
                    if (assetItemList.isEmpty())
                        throw new IOException("Found no assets in the target directories.");
                    // Initialize list view.
                    searchModelView.getSelectionModel().getSelectedItems().addListener(
                            (ListChangeListener<JFXListCell<ModelItem>>) (observable -> observable.getList().forEach(
                                    (Consumer<JFXListCell<ModelItem>>) cell -> selectModel(cell.getItem(), cell))
                            )
                    );
                    searchModelView.setFixedCellSize(30);
                    // Write models to menu items.
                    assetItemList.forEach(assetItem -> modelCellList.add(getMenuItem(assetItem, searchModelView)));
                    Logger.debug("ModelManager", "Initialized model assets successfully.");
                } catch (IOException ex) {
                    // Explicitly set all lists to empty.
                    Logger.error("ModelManager", "Failed to initialize model assets due to unknown reasons, details see below.", ex);
                    modelCellList = new ArrayList<>();
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
                        if (node instanceof JFXButton tag && t.equals(tag.getText())) {
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
                toggleFilterPane.getStyleClass().add("btn-noticeable");

                // 3. Update model list:
                modelSearch("");
                searchModelInput.clear();
                if (assetItemList != null && !modelCellList.isEmpty() &&
                        app.config.character_asset != null && !app.config.character_asset.isEmpty()) {
                    // Scroll to recent selected model and then select it.
                    ModelItem recentSelected = assetItemList.searchByRelPath(app.config.character_asset);
                    if (recentSelected != null) {
                        for (JFXListCell<ModelItem> cell : searchModelView.getItems())
                            if (recentSelected.equals(cell.getItem())) {
                                searchModelView.scrollTo(cell);
                                searchModelView.getSelectionModel().select(cell);
                            }
                        // Check model favorite:
                        if (app.config.character_favorites.containsKey(recentSelected.key)) {
                            modelFavorite.setGraphic(favFillIcon);
                        }
                    }
                }
            }

            // Post process:
            loadFailureTip.setVisible(modelCellList.isEmpty());
            app.rootModule.launchBtn.setDisable(modelCellList.isEmpty());
            if (willGc)
                System.gc();
            Logger.info("ModelManager", "Reloaded");
        });
    }

    private JFXListCell<ModelItem> getMenuItem(ModelItem modelItem, JFXListView<JFXListCell<ModelItem>> container) {
        double width = container.getPrefWidth() - 50;
        double height = 30;
        double divide = 0.618;
        JFXListCell<ModelItem> item = new JFXListCell<>();
        item.getStyleClass().addAll("Search-models-item");
        Label name = new Label(modelItem.toString());
        name.getStyleClass().addAll("Search-models-label", "Search-models-label-primary");
        name.setPrefSize(modelItem.skinGroupName == null ? width : width * divide, height);
        name.setLayoutX(15);
        Label alias1 = new Label(modelItem.skinGroupName);
        alias1.getStyleClass().addAll("Search-models-label", "Search-models-label-secondary");
        alias1.setPrefSize(width * (1 - divide), height);
        alias1.setLayoutX(modelItem.skinGroupName == null ? 0 : width * divide);
        SVGPath fav = GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_STAR_FILLED, GuiPrefabs.COLOR_WARNING);
        fav.getStyleClass().add("Search-models-star");
        fav.setLayoutX(0);
        fav.setLayoutY(3);
        fav.setScaleX(0.75);
        fav.setScaleY(0.75);
        item.setPrefSize(width, height);
        item.setGraphic(new Group(fav, name, alias1));
        item.setItem(modelItem);
        item.setId(modelItem.getLocation());
        if (app.config.character_favorites.containsKey(modelItem.key))
            item.getStyleClass().add("Search-models-item-favorite");
        return item;
    }

    private void selectModel(ModelItem asset, JFXListCell<ModelItem> item) {
        // Reset
        if (selectedModelCell != null) {
            selectedModelCell.getStyleClass().setAll("Search-models-item");
            if (app.config.character_favorites.containsKey(selectedModelCell.getItem().key))
                selectedModelCell.getStyleClass().add("Search-models-item-favorite");
        }
        selectedModelCell = item;
        selectedModelCell.getStyleClass().add("Search-models-item-active");
        // Display details
        selectedModelName.setText(asset.name);
        selectedModelAppellation.setText(asset.appellation);
        selectedModelSkinGroupName.setText(asset.skinGroupName);
        selectedModelType.setText(app.modelsDataset.sortTags == null ?
                asset.type : app.modelsDataset.sortTags.getOrDefault(asset.type, asset.type));
        GuiPrefabs.addTooltip(selectedModelName, asset.name);
        GuiPrefabs.addTooltip(selectedModelAppellation, asset.appellation);
        GuiPrefabs.addTooltip(selectedModelSkinGroupName, asset.skinGroupName);
        GuiPrefabs.addTooltip(selectedModelType, asset.type);
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
        // Check favorite
        if (app.config.character_favorites.containsKey(asset.key)) {
            modelFavorite.setGraphic(favFillIcon);
        } else {
            modelFavorite.setGraphic(favIcon);
        }
        // Apply to app.config, but not to save
        app.config.character_asset = asset.getLocation();
        app.config.character_files = asset.assetList;
        app.config.character_label = asset.name;
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
}
