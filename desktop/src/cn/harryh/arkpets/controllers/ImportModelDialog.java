/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.Locale;

import static cn.harryh.arkpets.controllers.ModelsModule.importModel;


/** The dialog controller for importing a model library from a zip archive.
 */
public final class ImportModelDialog implements DialogController<ArkHomeFX> {
    @FXML
    private AnchorPane dialog;
    @FXML
    private Button dialogReturn;
    @FXML
    private Label dropLabel;
    @FXML
    private StackPane dropZone;
    @FXML
    private Button selectFile;
    @FXML
    private VBox selectFileVBox;
    @FXML
    private Label statusLabel;

    private ArkHomeFX app;
    private boolean importing;

    @Override
    public void initializeWith(ArkHomeFX app) {
        this.app = app;
        selectFile.setOnAction(e -> chooseFile());
        initDragAndDrop();
    }

    @Override
    public AnchorPane getDialogPane() {
        return dialog;
    }

    @Override
    public Button getReturnButton() {
        return dialogReturn;
    }

    @Override
    public void notifyDialogOpened(Object data) {
        importing = false;
        setStatus(null);
        setDropActive(false);
        Platform.runLater(selectFile::requestFocus);
    }

    @Override
    public void notifyDialogClosed() {
        importing = false;
        setStatus(null);
        setDropActive(false);
    }

    private void chooseFile() {
        Logger.info("ImportModelDialog", "Opening file chooser to import zip file");
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter allFiles = new FileChooser.ExtensionFilter("All Files", "*.*");
        FileChooser.ExtensionFilter archives = new FileChooser.ExtensionFilter("Archives", "*.zip");
        fileChooser.getExtensionFilters().addAll(allFiles, archives);
        fileChooser.setSelectedExtensionFilter(archives);
        File zipFile = fileChooser.showOpenDialog(app.getWindow());
        if (zipFile == null)
            return;
        if (!isZipFile(zipFile)) {
            Logger.warn("ImportModelDialog", "The selected file is not a zip archive: " + zipFile);
            setStatus("仅支持 .zip 格式的模型库压缩包");
            return;
        }
        importAndClose(zipFile);
    }

    private void initDragAndDrop() {
        dialog.setOnDragEntered(e -> {
            if (isExternalZipDrag(e))
                setDropActive(true);
        });
        dialog.setOnDragOver(e -> {
            if (isExternalZipDrag(e)) {
                e.acceptTransferModes(TransferMode.COPY);
                setDropActive(true);
            }
        });
        dialog.setOnDragDropped(e -> {
            e.setDropCompleted(false);
            if (isExternalZipDrag(e)) {
                e.setDropCompleted(true);
                setDropActive(false);
                importAndClose(e.getDragboard().getFiles().get(0));
            } else {
                List<File> files = e.getDragboard().getFiles();
                if (files != null && !files.isEmpty())
                    setStatus("仅支持单个 .zip 格式的模型库压缩包");
                setDropActive(false);
            }
        });
        dialog.setOnDragExited(e -> setDropActive(false));
    }

    private boolean isExternalZipDrag(DragEvent event) {
        return event.getGestureSource() == null && isSingleZip(event.getDragboard().getFiles());
    }

    private static boolean isSingleZip(List<File> files) {
        return files != null && files.size() == 1 && isZipFile(files.get(0));
    }

    private static boolean isZipFile(File file) {
        return file != null && file.isFile() &&
                file.getName().toLowerCase(Locale.ROOT).endsWith(".zip");
    }

    private void importAndClose(File zipFile) {
        if (importing)
            return;
        importing = true;
        Logger.info("ImportModelDialog", "Importing zip file: " + zipFile.getAbsolutePath());
        triggerReturnActionCallback(new ActionEvent(this, null));
        importModel(app, zipFile);
    }

    private void setDropActive(boolean active) {
        if (active && !dropZone.getStyleClass().contains("import-drop-zone-active")) {
            dropZone.getStyleClass().add("import-drop-zone-active");
            selectFileVBox.setVisible(false);
            dropLabel.setText("松开鼠标以导入");
        }
        else if (!active) {
            dropZone.getStyleClass().remove("import-drop-zone-active");
            selectFileVBox.setVisible(true);
            dropLabel.setText("将模型库压缩包 (zip) 拖入此处");
        }
    }

    private void setStatus(String text) {
        GuiPrefabs.replaceTextAutoVisibility(statusLabel, text);
    }
}
