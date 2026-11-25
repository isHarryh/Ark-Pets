/** Copyright (c) 2022-2026, Harry Huang, Litwak913
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.utils.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.Locale;

import static cn.harryh.arkpets.Const.PathConfig.urlModelsRepoPage;
import static cn.harryh.arkpets.Const.PathConfig.urlOfficialHelpPage;


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
    private Label modelsHelpLink;
    @FXML
    private Label modelsRepoLink;
    @FXML
    private Button selectFile;
    @FXML
    private VBox selectFileVBox;

    private ArkHomeFX app;
    private boolean importing;

    @Override
    public void initializeWith(ArkHomeFX app) {
        this.app = app;
        modelsRepoLink.setOnMouseClicked(e -> app.popBrowser(urlModelsRepoPage));
        modelsHelpLink.setOnMouseClicked(e -> app.popBrowser(urlOfficialHelpPage));
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
        setDropZoneIdle();
        Platform.runLater(selectFile::requestFocus);
    }

    @Override
    public void notifyDialogClosed() {
        importing = false;
        setDropZoneIdle();
    }

    private void chooseFile() {
        if (importing)
            return;
        setDropZoneIdle();

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
            setDropZoneWarning();
            return;
        }
        Logger.info("ImportModelDialog", "Got file via file chooser dialog: " + zipFile);
        importAndClose(zipFile);
    }

    private void initDragAndDrop() {
        EventHandler<DragEvent> dragOngoingHandler = e -> {
            if (e.getGestureSource() == null && isSingleZip(e.getDragboard().getFiles())) {
                e.acceptTransferModes(TransferMode.COPY);
                setDropZoneActive();
            } else {
                setDropZoneWarning();
            }
        };
        dialog.setOnDragEntered(dragOngoingHandler);
        dialog.setOnDragOver(dragOngoingHandler);
        dialog.setOnDragDropped(e -> {
            List<File> files = e.getDragboard().getFiles();
            if (!importing && e.getGestureSource() == null && isSingleZip(files)) {
                e.setDropCompleted(true);
                File zipFile = files.get(0);
                Logger.info("ImportModelDialog", "Got file via drag-and-drop: " + zipFile);
                importAndClose(zipFile);
            }
        });
        dialog.setOnDragExited(e -> {
            if (!importing)
                setDropZoneIdle();
        });
    }

    private static boolean isSingleZip(List<File> files) {
        return files != null && files.size() == 1 && isZipFile(files.get(0));
    }

    private static boolean isZipFile(File file) {
        return file != null && file.isFile() && file.getName().toLowerCase(Locale.ROOT).endsWith(".zip");
    }

    private void importAndClose(File zipFile) {
        if (importing)
            return;
        importing = true;
        Logger.info("ImportModelDialog", "Importing zip file: " + zipFile.getAbsolutePath());
        triggerReturnActionCallback(new ActionEvent(this, null));
        ModelsModule.importModel(app, zipFile);
    }

    private void setDropZoneActive() {
        if (!dropZone.getStyleClass().contains("import-drop-zone-active"))
            dropZone.getStyleClass().add("import-drop-zone-active");
        dropLabel.setText("松开鼠标即可导入");
        selectFileVBox.setVisible(false);
        selectFileVBox.setManaged(false);
    }

    private void setDropZoneIdle() {
        dropZone.getStyleClass().remove("import-drop-zone-active");
        dropLabel.setText("将模型库压缩包拖入此处");
        selectFileVBox.setVisible(true);
        selectFileVBox.setManaged(true);
    }

    private void setDropZoneWarning() {
        dropZone.getStyleClass().remove("import-drop-zone-active");
        dropLabel.setText("仅接受单个 .zip 后缀的压缩包文件");
        selectFileVBox.setVisible(true);
        selectFileVBox.setManaged(true);
    }
}
