/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.guitasks.FetchAnnounceTask;
import cn.harryh.arkpets.guitasks.GuiTask.GuiTaskStyle;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.NetUtils;
import cn.harryh.arkpets.utils.markdown.FxmlConvertor;
import cn.harryh.arkpets.utils.markdown.FxmlDocumentController;
import com.alibaba.fastjson.annotation.JSONField;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;


public final class AnnounceDialog implements Controller<ArkHomeFX> {
    @FXML
    public AnchorPane dialog;
    @FXML
    public JFXButton dialogReturn;

    @FXML
    private JFXListView<JFXListCell<AnnounceItem>> annoListView;
    @FXML
    private JFXButton annoRefetch;
    @FXML
    private Label annoTitle;
    @FXML
    private Label annoDate;
    @FXML
    private Label annoGotoOrigin;
    @FXML
    private ScrollPane annoScroll;
    @FXML
    private VBox annoContainer;

    private JFXListCell<AnnounceItem> selectedAnnoCell;

    private ArkHomeFX app;

    @Override
    public void initializeWith(ArkHomeFX app) {
        this.app = app;

        annoListView.getSelectionModel().getSelectedItems().addListener(
                (ListChangeListener<JFXListCell<AnnounceItem>>) (observable -> observable.getList().forEach(
                        (Consumer<JFXListCell<AnnounceItem>>) this::selectCell)
                )
        );

        annoRefetch.setOnAction(e -> this.fetchAnnounce());

        Platform.runLater(() -> GuiPrefabs.disableScrollPaneCache(annoScroll));
    }

    public void fetchAnnounce() {
        ObservableList<AnnounceItem> annoItemList = FXCollections.observableArrayList();
        new FetchAnnounceTask(app.body, GuiTaskStyle.COMMON, annoItemList).start();

        annoListView.getItems().clear();
        annoItemList.addListener((ListChangeListener<AnnounceItem>) change -> {
            change.next();
            if (change.wasAdded()) {
                Logger.info("Announce", "Fetched " + change.getAddedSize() + " announcements");
                change.getAddedSubList().forEach(anno -> annoListView.getItems().add(createCell(anno)));
            }
            if (!annoListView.getItems().isEmpty()) {
                annoListView.setFixedCellSize(40);
                annoListView.getSelectionModel().select(0);
            }
            annoListView.refresh();
        });
    }

    private JFXListCell<AnnounceItem> createCell(AnnounceItem anno) {
        double width = annoListView.getPrefWidth() * 0.799;
        JFXListCell<AnnounceItem> cell = new JFXListCell<>();
        cell.getStyleClass().addAll("list-item");
        Label name = new Label(anno.title);
        name.getStyleClass().addAll("list-item-label");
        name.setPrefWidth(width);
        cell.setPrefWidth(width);
        cell.setGraphic(name);
        cell.setItem(anno);
        return cell;
    }

    private void selectCell(JFXListCell<AnnounceItem> cell) {
        // Reset
        if (selectedAnnoCell != null) {
            selectedAnnoCell.getStyleClass().setAll("list-item");
        }
        selectedAnnoCell = cell;
        selectedAnnoCell.getStyleClass().add("list-item-active");
        // Display info
        AnnounceItem anno = cell.getItem();
        annoTitle.setText(anno.title);
        if (anno.date != null && !anno.date.isBlank()) {
            annoDate.setText(anno.date);
            annoDate.setManaged(true);
            annoDate.setVisible(true);
        } else {
            annoDate.setText("");
            annoDate.setManaged(false);
            annoDate.setVisible(false);
        }
        if (anno.source != null && !anno.source.isBlank()) {
            annoGotoOrigin.setOnMouseClicked(e -> NetUtils.browseWebpage(anno.source));
            annoGotoOrigin.setManaged(true);
            annoGotoOrigin.setVisible(true);
        } else {
            annoGotoOrigin.setOnMouseClicked(null);
            annoGotoOrigin.setManaged(false);
            annoGotoOrigin.setVisible(false);
        }
        // Display announcement
        annoContainer.getChildren().clear();
        FxmlDocumentController document = FxmlConvertor.toFxmlController(anno.markdown);
        document.getBodyNode().setMaxWidth(annoScroll.getWidth());
        document.setHyperlinkConsumer(string -> {
            if (string.startsWith("https://") || string.startsWith("http://")) {
                NetUtils.browseWebpage(string);
            }
        });
        annoContainer.getChildren().add(document.getBodyNode());
    }


    public static class AnnounceItem {
        /** @since ArkPets 3.7 */ @JSONField
        public String title;
        /** @since ArkPets 3.7 */ @JSONField
        public String date;
        /** @since ArkPets 3.7 */ @JSONField
        public String markdown;
        /** @since ArkPets 3.7 */ @JSONField
        public String source;
    }
}
