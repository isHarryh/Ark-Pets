/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.guitasks.GuiTask;
import cn.harryh.arkpets.guitasks.ZipTask;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.IOUtils.FileUtil;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.StringUtils;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

import static cn.harryh.arkpets.Const.LogConfig.logDir;


public final class LogDialog implements DialogController<ArkHomeFX> {
    @FXML
    private AnchorPane dialog;
    @FXML
    private JFXButton dialogReturn;

    @FXML
    private TreeTableView<LogItem> logView;
    @FXML
    private JFXButton logRefetch;
    @FXML
    private JFXButton logExplore;

    @FXML
    private Label logName;
    @FXML
    private Label logSize;
    @FXML
    private Label logCreatedTime;
    @FXML
    private Label logModifiedTime;
    @FXML
    private Label logSummary;
    @FXML
    private Label logSelectedCount;
    @FXML
    public JFXButton quickSelectAll;
    @FXML
    public JFXButton quickSelectRecent;
    @FXML
    private JFXButton exportSelected;

    private ObservableList<LogItem> coreLogList;
    private ObservableList<LogItem> desktopLogList;

    private ArkHomeFX app;

    @Override
    public void initializeWith(ArkHomeFX app) {
        this.app = app;

        coreLogList = FXCollections.observableArrayList();
        desktopLogList = FXCollections.observableArrayList();
        prepareTable();
        prepareInfoPane();

        logRefetch.setOnAction(e -> refreshTable());
        logExplore.setOnAction(e -> app.popBrowser(new File(logDir).toURI()));
    }

    @Override
    public AnchorPane getDialogPane() {
        return dialog;
    }

    @Override
    public JFXButton getReturnButton() {
        return dialogReturn;
    }

    @Override
    public void notifyDialogOpened(Object data) {
        refreshTable();
    }

    @Override
    public void notifyDialogClosed() {
        clearTable();
    }

    public void refreshTable() {
        Logger.info("LogDialog", "Refreshing table");
        logView.getSelectionModel().clearSelection();
        loadLogFiles(
                desktopLogList,
                new File(logDir),
                (dir, name) -> name.startsWith("desktop") && name.endsWith(".log")
        );
        loadLogFiles(
                coreLogList,
                new File(logDir),
                (dir, name) -> name.startsWith("core") && name.endsWith(".log")
        );
        GuiPrefabs.Tables.autoResizeColumns(logView, 20.0);
        GuiPrefabs.Tables.autoExpandRows(logView);
        logView.refresh();
    }

    public void clearTable() {
        Logger.debug("LogDialog", "Clearing table");
        coreLogList.clear();
        desktopLogList.clear();
    }

    private void prepareTable() {
        TreeTableColumn<LogItem, String> nameCol = new JFXTreeTableColumn<>("文件名");
        nameCol.setCellValueFactory(
                new SimpleCellValueFactory<>(logItem -> logItem.name)
        );
        nameCol.setReorderable(false);

        TreeTableColumn<LogItem, HumanSize> sizeCol = new JFXTreeTableColumn<>("大小");
        sizeCol.setCellValueFactory(
                new SimpleCellValueFactory<>(logItem -> new HumanSize(logItem.size, !logItem.isAvailable()))
        );
        sizeCol.setReorderable(false);

        TreeTableColumn<LogItem, HumanInstant> modifiedTimeCol = new JFXTreeTableColumn<>("更新时间");
        modifiedTimeCol.setCellValueFactory(
                new SimpleCellValueFactory<>(logItem -> new HumanInstant(logItem.modifiedTime, !logItem.isAvailable()))
        );
        modifiedTimeCol.setReorderable(false);

        LogItem coreLogRoot = new LogItem("桌宠日志");
        LogItem desktopLogRoot = new LogItem("启动器日志");
        coreLogRoot.setChildren(coreLogList);
        desktopLogRoot.setChildren(desktopLogList);

        RecursiveTreeItem<LogItem> root = new RecursiveTreeItem<>(
                FXCollections.observableArrayList(coreLogRoot, desktopLogRoot),
                RecursiveTreeObject::getChildren
        );
        root.setExpanded(true);

        logView.setRoot(root);
        logView.setShowRoot(false);
        logView.getColumns().add(nameCol);
        logView.getColumns().add(sizeCol);
        logView.getColumns().add(modifiedTimeCol);
        logView.getSelectionModel().setCellSelectionEnabled(false);
        logView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void loadLogFiles(ObservableList<LogItem> targetList, File directory, FilenameFilter filter) {
        targetList.clear();
        File[] logFiles = directory.listFiles(filter);
        if (logFiles != null) {
            List<LogItem> logItems = Arrays.stream(logFiles)
                    .map(LogItem::new)
                    .filter(LogItem::isAvailable)
                    .sorted(Comparator.comparingLong(logItem -> -logItem.modifiedTime.toEpochMilli()))
                    .toList();
            targetList.addAll(logItems);
            Logger.debug("LogDialog", "Found " + logItems.size() + " log files in \"" + directory + "\"");
        } else {
            Logger.warn("LogDialog", "Directory \"" + directory + "\" is not available");
        }
    }

    private void prepareInfoPane() {
        TreeTableView.TreeTableViewSelectionModel<LogItem> selections = logView.getSelectionModel();

        selections.getSelectedItems().addListener((ListChangeListener<? super TreeItem<LogItem>>) observable -> {
            observable.next();
            if (observable.wasAdded() && observable.getAddedSize() > 0) {
                List<? extends TreeItem<LogItem>> addedList = observable.getAddedSubList();
                if (!addedList.isEmpty()) {
                    // Inform selecting this tree item
                    selectLog(addedList.get(0).getValue());
                    // Recursively select its children items
                    if (addedList.stream().anyMatch(treeItem -> !treeItem.getChildren().isEmpty())) {
                        Platform.runLater(() -> addedList.forEach(treeItem -> {
                            if (treeItem != null)
                                treeItem.getChildren().forEach(selections::select);
                        }));
                    }
                }
            }

            // Count and show selected item size
            int selectedSize = selections.getSelectedItems().filtered(
                    treeItem -> treeItem.getValue().isAvailable()
            ).size();
            if (selectedSize > 0) {
                logSelectedCount.setText("已选择 " + selectedSize + " 个文件");
            } else {
                selectLog(null);
                logSelectedCount.setText("未选择任何文件");
            }
        });

        quickSelectAll.setOnAction(e -> {
            GuiPrefabs.Tables.autoExpandRows(logView);
            logView.requestFocus();
            selections.selectAll();
            logView.scrollTo(0);
        });

        quickSelectRecent.setOnAction(e -> {
            GuiPrefabs.Tables.autoExpandRows(logView);
            logView.requestFocus();
            selections.selectAll();
            Instant threshold = Instant.now().minusSeconds(3600); // 1h
            List<? extends TreeItem<LogItem>> treeItemList = selections.getSelectedItems().stream()
                    .filter(position -> position.getValue().isAvailable())
                    .filter(position -> position.getValue().modifiedTime.isAfter(threshold))
                    .toList();
            selections.getSelectedCells().stream()
                    .map(TablePositionBase::getRow)
                    .max(Comparator.naturalOrder())
                    .ifPresent(i -> logView.scrollTo(i));
            Platform.runLater(() -> {
                selections.clearSelection();
                treeItemList.forEach(selections::select);
            });
        });

        exportSelected.setOnAction(e -> exportSelectedLog(
                selections.getSelectedItems().stream().map(TreeItem::getValue).toList()
        ));

        selectLog(null);
        logSelectedCount.setText("未选择任何文件");
    }

    private void selectLog(LogItem item) {
        if (item == null || !item.isAvailable()) {
            List.of(logName, logSize, logCreatedTime, logModifiedTime, logSummary).forEach(label -> label.setText(""));
            return;
        }
        logName.setText("文件名：" + item.name);
        logSize.setText("大小：" + StringUtils.getFormattedSizeString(item.size));
        logCreatedTime.setText("创建于：" + StringUtils.getSimpleTimeString(item.createdTime));
        logModifiedTime.setText("更新于：" + StringUtils.getSimpleTimeString(item.modifiedTime));

        if (item.size > 4 << 20) // 4 MB
            logSummary.setText("此文件较大，已禁用分析");
        else {
            try {
                String content = FileUtil.readString(item.file, "UTF-8");
                int errors = StringUtils.countMatches(content, "\\[ERROR\\]");
                int warnings = StringUtils.countMatches(content, "\\[WARN\\]");
                logSummary.setText("包含 " + warnings + " 个警告，" + errors + " 个错误");
            } catch (IOException e) {
                logSummary.setText("无法分析此文件");
            }
        }
    }

    private void exportSelectedLog(Collection<LogItem> items) {
        Logger.debug("LogDialog", "Ready to export logs");
        List<String> pathList = items.stream()
                .filter(LogItem::isAvailable)
                .filter(logItem -> Objects.requireNonNull(logItem.file).exists())
                .map(logItem -> Objects.requireNonNull(logItem.file).getPath())
                .toList();
        if (pathList.isEmpty()) {
            Logger.info("LogDialog", "Logs not exported, no log file to export");
            return;
        }

        // Open file chooser
        Logger.info("LogDialog", "Opening file chooser to export logs, " + pathList.size() + " files included");
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archives", "*.zip"));
        fileChooser.setInitialFileName(LocalDateTime.now().format(DateTimeFormatter.ofPattern("'ArkPets_Logs_'yyyy-MM-dd-HH-mm-ss'.zip'")));
        File zipFile = fileChooser.showSaveDialog(app.getWindow());
        if (zipFile == null) {
            Logger.info("LogDialog", "Logs not exported, user cancelled");
            return;
        }

        // Export log files
        Logger.info("LogDialog", "Staring to export logs");
        new ZipTask(app.body, GuiTask.GuiTaskStyle.STRICT, zipFile.toString(), pathList).start();
    }


    public static class LogItem extends RecursiveTreeObject<LogItem> {
        public final File file;
        public final String name;
        public final Instant createdTime;
        public final Instant modifiedTime;
        public final long size;
        public final boolean readable;

        public LogItem(String displayName) {
            file = null;
            name = displayName;
            createdTime = Instant.EPOCH;
            modifiedTime = Instant.EPOCH;
            size = 0;
            readable = false;
        }

        public LogItem(File logFile) {
            file = logFile;
            name = file.getName();

            Instant createdTime = Instant.EPOCH;
            Instant modifiedTime = Instant.EPOCH;
            long size = 0;
            boolean readable = false;

            BasicFileAttributes attr;
            try {
                attr = Files.readAttributes(logFile.toPath(), BasicFileAttributes.class);
                createdTime = attr.creationTime().toInstant();
                modifiedTime = attr.lastModifiedTime().toInstant();
                size = attr.size();
                readable = logFile.isFile() && logFile.canRead() && attr.isRegularFile();
            } catch (IOException ignored) {
            }

            this.createdTime = createdTime;
            this.modifiedTime = modifiedTime;
            this.readable = readable;
            this.size = size;
        }

        public boolean isAvailable() {
            return createdTime != null && modifiedTime != null && size > 0 && readable;
        }
    }


    private record SimpleCellValueFactory<S, T>(Function<S, T> extractor)
            implements Callback<TreeTableColumn.CellDataFeatures<S, T>, ObservableValue<T>> {
        @Override
        public ObservableValue<T> call(TreeTableColumn.CellDataFeatures<S, T> cellDataFeatures) {
            S value = cellDataFeatures.getValue().getValue();
            if (value == null || extractor == null)
                return null;
            return new SimpleObjectProperty<>(extractor.apply(value));
        }
    }


    private record HumanSize(long size, boolean hide) implements Comparable<HumanSize> {
        @Override
        public String toString() {
            return hide ? "" : StringUtils.getFormattedSizeString(size);
        }

        @Override
        public int compareTo(HumanSize other) {
            return Long.compare(this.size, other.size);
        }
    }


    private record HumanInstant(Instant instant, boolean hide) implements Comparable<HumanInstant> {
        @Override
        public String toString() {
            return hide ? "" : StringUtils.getRelatedTimeString(instant);
        }

        @Override
        public int compareTo(HumanInstant other) {
            return Long.compare(this.instant.toEpochMilli(), other.instant.toEpochMilli());
        }
    }
}
