/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.guitasks.GuiTask.GuiTaskStyle;
import cn.harryh.arkpets.guitasks.requests.FetchAnnounceTask;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.ScrollUtils;
import cn.harryh.arkpets.utils.StringUtils;
import cn.harryh.arkpets.utils.markdown.FxmlConvertor;
import cn.harryh.arkpets.utils.markdown.FxmlDocumentController;
import com.alibaba.fastjson2.JSONObject;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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
import static cn.harryh.arkpets.Const.durationLong;
import static cn.harryh.arkpets.network.api.AppQueryAnnouncement.AnnounceItem;


public final class AnnounceDialog implements DialogController<ArkHomeFX> {
    @FXML
    private AnchorPane dialog;
    @FXML
    private Button dialogReturn;

    @FXML
    private ListView<AnnounceItem> annoListView;
    @FXML
    private Button annoRefetch;
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

    private AnnounceReadHandler announceReadHandler;

    private ArkHomeFX app;

    private AnnounceItemWrapper selectedAnnounce;

    private final ObservableList<AnnounceItem> annoItemList = FXCollections.observableArrayList();

    @Override
    public void initializeWith(ArkHomeFX app) {
        this.app = app;
        ScrollUtils.addSmoothScrolling(annoScroll);
        this.selectedAnnounce = new AnnounceItemWrapper();

        annoListView.setItems(annoItemList);
        annoListView.getSelectionModel().getSelectedItems().addListener(
                (ListChangeListener<AnnounceItem>) (observable -> observable.getList().forEach(
                        (Consumer<AnnounceItem>) this::selectCell)
                )
        );
        annoListView.setCellFactory(this::createCell);
        ScrollUtils.addSmoothScrolling(annoListView);

        annoRefetch.setOnAction(e -> this.fetchAnnounce(true, () -> {}));

        announceReadHandler = new AnnounceReadHandler(app.config);

        annoTitle.textProperty().bind(selectedAnnounce.titleProperty);
        annoDate.textProperty().bind(selectedAnnounce.dateProperty);
        annoDate.visibleProperty().bind(selectedAnnounce.dateProperty.isNotEmpty());
        annoDate.managedProperty().bind(selectedAnnounce.dateProperty.isNotEmpty());
        annoGroup.textProperty().bind(selectedAnnounce.groupProperty);
        annoGroup.managedProperty().bind(selectedAnnounce.groupProperty.isNotEmpty());
        annoGroup.visibleProperty().bind(selectedAnnounce.groupProperty.isNotEmpty());
        annoGotoOrigin.visibleProperty().bind(selectedAnnounce.sourceProperty.isNotEmpty());
        annoGotoOrigin.managedProperty().bind(selectedAnnounce.sourceProperty.isNotEmpty());
        annoGotoOrigin.setOnMouseClicked(e -> app.popBrowser(selectedAnnounce.sourceProperty.get()));
    }

    @Override
    public AnchorPane getDialogPane() {
        return dialog;
    }

    @Override
    public Button getReturnButton() {
        return dialogReturn;
    }

    public void fetchAnnounce(boolean doPopNotice, Runnable onNeedImmediateShow) {
        new FetchAnnounceTask(app.body, doPopNotice ? GuiTaskStyle.COMMON : GuiTaskStyle.HIDDEN, annoItemList) {
            @Override
            protected void onReceivedData(JSONObject json) {
                super.onReceivedData(json);

                if (doPopNotice) {
                    app.toast.showText("已载入 " + annoItemList.size() + " 条公告",
                            GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_CHECK, GuiPrefabs.COLOR_SUCCESS),durationLong);
                }
            }
        }.start();

        annoListView.getItems().clear();
        annoItemList.addListener((ListChangeListener<AnnounceItem>) change -> {
            // Receive new items
            change.next();
            if (change.wasAdded() && change.getAddedSize() > 0) {
                Logger.info("Announce", "Fetched " + change.getAddedSize() + " announcements");
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

    private ListCell<AnnounceItem> createCell(ListView<AnnounceItem> announceItemListView) {
        return new AnnounceListCell(announceItemListView.getPrefWidth());
    }

    private void selectCell(AnnounceItem cell) {
        // Display info
        selectedAnnounce.setAnnounceItem(cell);
        // Display announcement
        GuiPrefabs.fadeOutNode(annoContainer, durationFast, e -> {
            GuiPrefabs.disableScrollPaneCache(annoScroll);
            annoScroll.setVvalue(0.0);
            annoContainer.getChildren().clear();
            FxmlDocumentController document = FxmlConvertor.toFxmlController(cell.markdown);
            document.getBodyNode().setMaxWidth(annoScroll.getWidth());
            document.setHyperlinkConsumer(string -> app.popBrowser(string));
            annoContainer.getChildren().add(document.getBodyNode());
            GuiPrefabs.fadeInNode(annoContainer, durationFast, null);
        });
        // Mark as read
        announceReadHandler.setRead(cell);
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


    private class AnnounceListCell extends GuiPrefabs.RipperListCell<AnnounceItem> {
        private final double width;
        private final double offset;
        private final SVGPath dot;
        private final Label name;
        private final static DropShadow dotShadow = new DropShadow(null, GuiPrefabs.COLOR_WHITE, 4.0, 0.5, 0.0, 0.0);

        public AnnounceListCell(double listWidth) {
            this.width = listWidth * 0.925;
            this.offset = listWidth * 0.175;

            dot = GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_DIAMOND, GuiPrefabs.COLOR_BLACK);
            dot.setLayoutX(-offset);
            dot.setScaleX(0.6);
            dot.setScaleY(0.6);
            dot.setEffect(dotShadow);
            name = new Label();
            name.getStyleClass().addAll("list-item-label");

            getContent().setAll(dot, name);
            setPrefWidth(width);
        }

        @Override
        protected void updateItem(AnnounceItem anno, boolean empty) {
            super.updateItem(anno, empty);
            if (empty || anno == null) {
                setContentVisible(false);
            } else {
                setId(anno.getAnnoId());
                name.setText(anno.title);
                name.setPrefWidth(width - (announceReadHandler.isRead(anno) ? 0 : offset));
                name.setPrefHeight(35);
                dot.setFill(anno.getParsedGroup().color);
                dot.setVisible(!announceReadHandler.isRead(anno));
                setContentVisible(true);
            }
        }
    }


    private static class AnnounceItemWrapper {
        private final StringProperty titleProperty = new SimpleStringProperty("暂未选择任何公告");
        private final StringProperty sourceProperty = new SimpleStringProperty();
        private AnnounceItem announceItem;

        private final StringBinding dateProperty = new StringBinding() {
            @Override
            protected String computeValue() {
                if (announceItem == null)
                    return "日期";
                if (announceItem.date == null || announceItem.date.isEmpty())
                    return "";
                return StringUtils.getSimpleTimeString(announceItem.getParsedDate());
            }
        };
        private final StringBinding groupProperty = new StringBinding() {
            @Override
            protected String computeValue() {
                if (announceItem == null) return "等级";
                return switch (announceItem.getParsedGroup()) {
                    case DEFAULT -> null;
                    case INFO -> "普通公告";
                    case WARN -> "重要公告";
                    case DANGER -> "紧急公告";
                };
            }
        };

        public void setAnnounceItem(AnnounceItem announceItem) {
            this.announceItem = announceItem;
            titleProperty.set(announceItem.title);
            sourceProperty.set(announceItem.source);
            dateProperty.invalidate();
            groupProperty.invalidate();
        }
    }
}
