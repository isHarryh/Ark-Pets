/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.assets.AssetItem;
import cn.harryh.arkpets.assets.AssetItemGroup;
import cn.harryh.arkpets.assets.ModelsDataset;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.IOUtils;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.Version;
import com.alibaba.fastjson.JSONObject;
import com.jfoenix.controls.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static cn.harryh.arkpets.Const.*;
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

    private AssetItemGroup assetItemList;
    private JFXListCell<AssetItem> selectedModelCell;
    private ArrayList<JFXListCell<AssetItem>> modelCellList = new ArrayList<>();

    private ArkHomeFX app;

    @Override
    public void initializeWith(ArkHomeFX app) {
        this.app = app;
        modelReload(false);
        initModelSearch();
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
                    Version acVersion = app.modelsDataset.arkPetsCompatibility;
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
                app.modelsDataset = null;
                throw e;
            }

            // If any exception occurred during the progress above:
        } catch (FileNotFoundException e) {
            Logger.warn("ModelManager", "Failed to initialize model dataset due to file not found. (" + e.getMessage() + ")");
            if (doPopNotice) {
                JFXDialog dialog = GuiPrefabs.DialogUtil.createCommonDialog(app.root, GuiPrefabs.IconUtil.getIcon(GuiPrefabs.IconUtil.ICON_WARNING_ALT, GuiPrefabs.COLOR_WARNING), "模型载入失败", "模型未成功载入：未找到数据集。",
                        "模型数据集文件 " + PathConfig.fileModelsDataPath + " 可能不在工作目录下。\n请先前往 [选项] 进行模型下载。", null);
                JFXButton go2 = GuiPrefabs.DialogUtil.getGotoButton(dialog, app.root);
                go2.setOnAction(ev -> {
                    app.switchToSettingsPane();
                    GuiPrefabs.DialogUtil.disposeDialog(dialog, app.root);
                });
                GuiPrefabs.DialogUtil.attachAction(dialog, go2, 0);
                dialog.show();
            }
        } catch (ModelsDataset.DatasetKeyException e) {
            Logger.warn("ModelManager", "Failed to initialize model dataset due to dataset parsing error. (" + e.getMessage() + ")");
            if (doPopNotice)
                GuiPrefabs.DialogUtil.createCommonDialog(app.root, GuiPrefabs.IconUtil.getIcon(GuiPrefabs.IconUtil.ICON_WARNING_ALT, GuiPrefabs.COLOR_WARNING), "模型载入失败", "模型未成功载入：数据集解析失败。",
                        "模型数据集可能不完整，或无法被启动器正确识别。请尝试更新模型或更新软件。", null).show();
        } catch (IOException e) {
            Logger.error("ModelManager", "Failed to initialize model dataset due to unknown reasons, details see below.", e);
            if (doPopNotice)
                GuiPrefabs.DialogUtil.createCommonDialog(app.root, GuiPrefabs.IconUtil.getIcon(GuiPrefabs.IconUtil.ICON_WARNING_ALT, GuiPrefabs.COLOR_WARNING), "模型载入失败", "模型未成功载入：发生意外错误。",
                        "失败原因概要：" + e.getLocalizedMessage(), null).show();
        }
        return false;
    }

    private void initModelAssets(boolean doPopNotice) {
        if (!app.initModelsDataset(doPopNotice))
            return;
        try {
            // Find every model assets.
            assetItemList = app.modelsDataset.data.filter(AssetItem::isExisted);
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
                GuiPrefabs.DialogUtil.createCommonDialog(app.root, GuiPrefabs.IconUtil.getIcon(GuiPrefabs.IconUtil.ICON_WARNING_ALT, GuiPrefabs.COLOR_WARNING), "模型载入失败", "模型未成功载入：读取模型列表失败。",
                        "失败原因概要：" + e.getLocalizedMessage(), null).show();
        }
    }

    private final ChangeListener<String> filterListener = (observable, oldValue, newValue) -> {
        if (searchModelFilter.getValue() != null) {
            app.popLoading(e -> {
                Logger.info("ModelManager", "Filter \"" + searchModelFilter.getValue() + "\"");
                modelSearch(searchModelInput.getText());
                searchModelFilter.getSelectionModel().clearAndSelect(searchModelFilter.getSelectionModel().getSelectedIndex());
            });
        }
    };

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
            searchModelFilter.getSelectionModel().select(0);
            modelSearch("");
        }));

        searchModelRandom.setOnAction(e -> modelRandom());

        searchModelReload.setOnAction(e -> modelReload(true));

        searchModelFilter.valueProperty().removeListener(filterListener);
        searchModelFilter.getItems().setAll("全部");
        searchModelFilter.getSelectionModel().select(0);
        if (assertModelLoaded(false)) {
            Set<String> filterTags = app.modelsDataset.sortTags.keySet();
            for (String s : filterTags)
                searchModelFilter.getItems().add(app.modelsDataset.sortTags.get(s));
        }
        searchModelFilter.valueProperty().addListener(filterListener);
    }

    public void modelSearch(String keyWords) {
        searchModelView.getItems().clear();
        if (assertModelLoaded(false)) {
            // Handle tag
            String filterTag = "";
            for (String s : app.modelsDataset.sortTags.keySet())
                if (searchModelFilter.getValue().equals(app.modelsDataset.sortTags.get(s)))
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
            initModelAssets(doPopNotice);
            initModelSearch();
            modelSearch("");
            if (!modelCellList.isEmpty() && app.config.character_asset != null && !app.config.character_asset.isEmpty()) {
                // Scroll to recent selected model
                AssetItem recentSelected = assetItemList.searchByRelPath(app.config.character_asset);
                if (recentSelected != null)
                    for (JFXListCell<AssetItem> cell : searchModelView.getItems())
                        if (recentSelected.equals(cell.getItem())) {
                            searchModelView.scrollTo(cell);
                            searchModelView.getSelectionModel().select(cell);
                        }
            }
            loadFailureTip.setVisible(modelCellList.isEmpty());
            app.rootModule.launchBtn.setDisable(true);
            Button startBtn = (Button)app.root.lookup("#Start-btn");
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
        // Apply to app.config, but not to save
        app.config.character_asset = asset.getLocation();
        app.config.character_files = asset.assetList;
        app.config.character_label = asset.name;
    }

    private boolean assertModelLoaded(boolean doPopNotice) {
        if (app.modelsDataset == null) {
            // Not loaded:
            if (doPopNotice)
                GuiPrefabs.DialogUtil.createCommonDialog(app.root, GuiPrefabs.IconUtil.getIcon(GuiPrefabs.IconUtil.ICON_WARNING_ALT, GuiPrefabs.COLOR_WARNING), "未能加载模型", "请确保模型加载成功后再进行此操作。",
                        "请先在[选项]中进行模型下载。\n如您已下载模型，请尝试点击[重载]按钮。", null).show();
            return false;
        } else {
            // Loaded:
            return true;
        }
    }
}
