/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.guitasks.GuiTask.GuiTaskStyle;
import cn.harryh.arkpets.guitasks.requests.FetchAnnounceTask;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.StringUtils;
import cn.harryh.arkpets.utils.markdown.FxmlConvertor;
import cn.harryh.arkpets.utils.markdown.FxmlDocumentController;
import com.alibaba.fastjson.JSONObject;
import com.jfoenix.controls.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static cn.harryh.arkpets.Const.durationFast;
import static cn.harryh.arkpets.network.api.AppQueryAnnouncement.AnnounceItem;


public final class AnnounceDialog implements DialogController<ArkHomeFX> {
    @FXML
    private AnchorPane dialog;
    @FXML
    private JFXButton dialogReturn;

    @FXML
    private JFXListView<JFXListCell<AnnounceItem>> annoListView;
    @FXML
    private JFXButton annoRefetch;
    @FXML
    private Label annoTitle;
    @FXML
    private Label annoGroup;
    @FXML
    private Label annoDate;
    @FXML
    private Label annoGotoOrigin;
    @FXML
    private ScrollPane annoScroll;
    @FXML
    private VBox annoContainer;

    private JFXListCell<AnnounceItem> selectedAnnoCell;

    private AnnounceReadHandler announceReadHandler;

    private ArkHomeFX app;

    @Override
    public void initializeWith(ArkHomeFX app) {
        this.app = app;

        annoListView.getSelectionModel().getSelectedItems().addListener(
                (ListChangeListener<JFXListCell<AnnounceItem>>) (observable -> observable.getList().forEach(
                        (Consumer<JFXListCell<AnnounceItem>>) this::selectCell)
                )
        );

        annoRefetch.setOnAction(e -> this.fetchAnnounce(true, () -> {}));

        announceReadHandler = new AnnounceReadHandler(app.config);
    }

    @Override
    public AnchorPane getDialogPane() {
        return dialog;
    }

    @Override
    public JFXButton getReturnButton() {
        return dialogReturn;
    }

    public void fetchAnnounce(boolean doPopNotice, Runnable onNeedImmediateShow) {
        ObservableList<AnnounceItem> annoItemList = FXCollections.observableArrayList();
        new FetchAnnounceTask(app.body, doPopNotice ? GuiTaskStyle.COMMON : GuiTaskStyle.HIDDEN, annoItemList).start();

        annoListView.getItems().clear();
        annoItemList.addListener((ListChangeListener<AnnounceItem>) change -> {
            // Receive new items
            change.next();
            if (change.wasAdded() && change.getAddedSize() > 0) {
                Logger.info("Announce", "Fetched " + change.getAddedSize() + " announcements");
                change.getAddedSubList().forEach(anno -> annoListView.getItems().add(createCell(anno)));
                announceReadHandler.retainAll(change.getAddedSubList());
            }
            // Handle callback
            if (onNeedImmediateShow != null) {
                for (AnnounceItem anno : annoItemList) {
                    if (!announceReadHandler.isRead(anno) && anno.getParsedGroup().immediateShow) {
                        onNeedImmediateShow.run();
                        break;
                    }
                }
            }
            // Refresh listview
            annoListView.setFixedCellSize(40);
            annoListView.refresh();
        });
    }

    private JFXListCell<AnnounceItem> createCell(AnnounceItem anno) {
        double width = annoListView.getPrefWidth() * 0.75;
        double offset = width * 0.175;
        JFXListCell<AnnounceItem> cell = new JFXListCell<>();
        cell.getStyleClass().addAll("list-item");
        cell.setPrefWidth(width);
        cell.setItem(anno);
        cell.setId(anno.getAnnoId());
        SVGPath dot = GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_DIAMOND, anno.getParsedGroup().color);
        dot.setLayoutX(-offset);
        dot.setScaleX(0.6);
        dot.setScaleY(0.6);
        dot.setEffect(new DropShadow(null, GuiPrefabs.COLOR_WHITE, 4.0, 0.5, 0.0, 0.0));
        Label name = new Label(anno.title);
        name.getStyleClass().addAll("list-item-label");
        cell.setGraphic(new Group(dot, name));
        refreshCellGraphic(cell);
        return cell;
    }

    private void refreshCellGraphic(JFXListCell<AnnounceItem> cell) {
        double width = annoListView.getPrefWidth() * 0.75;
        double offset = width * 0.175;
        SVGPath dot = (SVGPath) ((Group) (cell.getGraphic())).getChildrenUnmodifiable().get(0);
        dot.setVisible(!announceReadHandler.isRead(cell.getItem()));
        Label name = (Label) ((Group) (cell.getGraphic())).getChildrenUnmodifiable().get(1);
        name.setPrefWidth(width - (announceReadHandler.isRead(cell.getItem()) ? 0 : offset));
    }

    private void selectCell(JFXListCell<AnnounceItem> cell) {
        // Reset
        if (selectedAnnoCell != null) {
            selectedAnnoCell.getStyleClass().setAll("list-item");
            refreshCellGraphic(selectedAnnoCell);
        }
        selectedAnnoCell = cell;
        selectedAnnoCell.getStyleClass().add("list-item-active");
        // Display info
        AnnounceItem anno = cell.getItem();
        annoTitle.setText(anno.title);
        GuiPrefabs.replaceTextAutoVisibility(annoGroup, switch (anno.getParsedGroup()) {
            case DEFAULT -> null;
            case INFO -> "普通公告";
            case WARN -> "重要公告";
            case DANGER -> "紧急公告";
        });
        GuiPrefabs.replaceTextAutoVisibility(annoDate,
                anno.date != null && !anno.date.isEmpty() ? StringUtils.getSimpleTimeString(anno.getParsedDate()) : "");
        GuiPrefabs.replaceTextAutoVisibility(annoGotoOrigin, anno.source != null ? "查看原文" : null);
        annoGotoOrigin.setOnMouseClicked(e -> app.popBrowser(anno.source));
        // Display announcement
        GuiPrefabs.fadeOutNode(annoContainer, durationFast, e -> {
            GuiPrefabs.disableScrollPaneCache(annoScroll);
            annoScroll.setVvalue(0.0);
            annoContainer.getChildren().clear();
            FxmlDocumentController document = FxmlConvertor.toFxmlController(anno.markdown);
            document.getBodyNode().setMaxWidth(annoScroll.getWidth());
            document.setHyperlinkConsumer(string -> app.popBrowser(string));
            annoContainer.getChildren().add(document.getBodyNode());
            GuiPrefabs.fadeInNode(annoContainer, durationFast, null);
        });
        // Mark as read
        announceReadHandler.setRead(anno);
    }


    public enum AnnounceGroup {
        DEFAULT(GuiPrefabs.COLOR_LIGHT_GRAY, false),
        INFO(GuiPrefabs.COLOR_INFO, false),
        WARN(GuiPrefabs.COLOR_WARNING, true),
        DANGER(GuiPrefabs.COLOR_DANGER, true);

        private final Color color;
        private final boolean immediateShow;

        AnnounceGroup(Color color, boolean immediateShow) {
            this.color = color;
            this.immediateShow = immediateShow;
        }
    }


    private static class AnnounceReadHandler {
        public ArkConfig config;

        public AnnounceReadHandler(ArkConfig config) {
            this.config = config;
            if (this.config.user_announcement_read == null)
                this.config.user_announcement_read = new JSONObject();
        }

        public boolean isRead(AnnounceItem anno) {
            if (anno == null)
                return false;
            String annoId = anno.getAnnoId();
            if (config.user_announcement_read.containsKey(annoId)) {
                try {
                    Number timestamp = config.user_announcement_read.getObject(annoId, Number.class);
                    return timestamp != null && timestamp.longValue() > 0;
                } catch (NumberFormatException ignored) {
                }
            }
            return false;
        }

        public void setRead(AnnounceItem anno) {
            String annoId = anno.getAnnoId();
            Logger.debug("Announce", "Read announce id " + annoId);
            config.user_announcement_read.put(annoId, Instant.now().toEpochMilli() / 1000L);
            config.save();
        }

        public void retainAll(List<? extends AnnounceItem> annoList) {
            Set<String> annoIdSet = annoList.stream().map(AnnounceItem::getAnnoId).collect(Collectors.toSet());
            int size = config.user_announcement_read.size();
            if (config.user_announcement_read.keySet().retainAll(annoIdSet)) {
                int delta = size - config.user_announcement_read.size();
                Logger.info("Announce", "Removed " + delta + " legacy local announcement records");
                config.save();
            }
        }
    }
}
