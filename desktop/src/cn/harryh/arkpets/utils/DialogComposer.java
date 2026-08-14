/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import cn.harryh.arkpets.controllers.DialogController;
import com.jfoenix.controls.JFXDialog;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static cn.harryh.arkpets.Const.durationNormal;


public class DialogComposer<T extends Application> {
    private final T app;
    private final Map<String, DialogRecord<T>> dialogs;
    private final Set<DialogRecord<T>> activatedDialogs;

    public DialogComposer(T app) {
        this.app = app;
        dialogs = new HashMap<>();
        activatedDialogs = new HashSet<>();
    }

    public DialogController<T> getDialogController(String dialogId) {
        if (!dialogs.containsKey(dialogId))
            throw new IllegalStateException("No such dialog ID");
        return dialogs.get(dialogId).dialogController();
    }

    /** Pops up a dialog with the specified identifier, passing data to it and executing a callback when closed.
     * @param dialogId The unique identifier of the dialog to be displayed.
     * @param data The data object to be passed to the dialog for processing.
     * @param onClosed A runnable callback that is executed after the dialog is closed.
     */
    public void popDialog(String dialogId, Object data, Runnable onClosed) {
        if (!dialogs.containsKey(dialogId))
            throw new IllegalStateException("No such dialog ID");
        DialogRecord<T> dialogRecord = dialogs.get(dialogId);
        // Avoid duplicated popup
        if (activatedDialogs.contains(dialogRecord))
            return;
        activatedDialogs.add(dialogRecord);
        // Blur out background nodes
        dialogRecord.backgroundNodes().forEach(node -> GuiPrefabs.blurNode(node, durationNormal, null));
        // Setup popup
        DialogController<T> controller = dialogRecord.dialogController();
        JFXDialog popup = createPopup(dialogRecord.parent(), controller);
        // Bind return actions
        controller.setReturnActionCallback(e -> {
            controller.setReturnActionCallback(null);
            popup.setOnDialogOpened(null);
            // Register post-close procedure
            BooleanProperty observer = new SimpleBooleanProperty();
            observer.addListener((observable, oldValue, newValue) -> {
                if (newValue && onClosed != null)
                    onClosed.run();
                controller.notifyDialogClosed();
                activatedDialogs.remove(dialogRecord);
            });
            // Close popup and deblur background nodes
            popup.close();
            dialogRecord.backgroundNodes()
                    .forEach(node -> GuiPrefabs.deblurNode(node, durationNormal, ev -> observer.set(true)));
        });
        // Show popup
        popup.show();
        controller.notifyDialogOpened(data);
    }

    /** Pops up a dialog with the specified identifier, passing data to it.
     * @param dialogId The unique identifier of the dialog to be displayed.
     * @param data The data object to be passed to the dialog for processing.
     */
    public void popDialog(String dialogId, Object data) {
        popDialog(dialogId, data, null);
    }

    /** Pops up a dialog with the specified identifier.
     * @param dialogId The unique identifier of the dialog to be displayed.
     */
    public void popDialog(String dialogId) {
        popDialog(dialogId, null);
    }

    public DialogController<T> registerDialog(String dialogId,
                                              StackPane parent,
                                              Collection<Node> backgroundNodes,
                                              FXMLHelper.LoadFXMLResult<T> fxml
    ) {
        if (dialogs.containsKey(dialogId))
            throw new IllegalStateException("Duplicated dialog ID");
        DialogController<T> dialogController = (DialogController<T>) fxml.initializeWith(app);
        dialogs.put(dialogId, new DialogRecord<>(dialogController, parent, backgroundNodes));
        return dialogs.get(dialogId).dialogController();
    }

    public DialogController<T> registerDialog(String dialogId,
                                              StackPane parent,
                                              Collection<Node> backgroundNodes,
                                              URL fxmlLocation
    ) {
        try {
            FXMLHelper.LoadFXMLResult<T> fxml = FXMLHelper.loadFXML(fxmlLocation);
            return registerDialog(dialogId, parent, backgroundNodes, fxml);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DialogController<T> registerDialog(String dialogId,
                                              StackPane parent,
                                              List<Node> backgroundNodes,
                                              String fxmlLocation
    ) {
        return registerDialog(dialogId, parent, backgroundNodes, FXMLHelper.class.getResource(fxmlLocation));
    }

    private static <T extends Application> JFXDialog createPopup(StackPane parent, DialogController<T> controller) {
        JFXDialog popup = new JFXDialog(parent, controller.getDialogPane(), JFXDialog.DialogTransition.TOP, false);
        popup.setOnDialogOpened(e -> {
            popup.requestFocus();
            // Listen overlay-close action
            popup.setOnMouseClicked(ev -> {
                popup.setOnMouseClicked(null);
                controller.triggerReturnActionCallback(ev);
            });
            // Listen the escape key action
            popup.setOnKeyPressed(ev -> {
                if (ev.getCode() == KeyCode.ESCAPE) {
                    popup.setOnMouseClicked(null);
                    controller.triggerReturnActionCallback(ev);
                }
            });
        });
        return popup;
    }


    private record DialogRecord<T extends Application>
            (DialogController<T> dialogController, StackPane parent, Collection<Node> backgroundNodes) {
    }
}
