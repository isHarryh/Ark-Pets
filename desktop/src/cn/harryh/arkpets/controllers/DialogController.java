/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;


/** The interface represents a specialized dialog FXML controller.
 * @param <T> The type of the target JavaFX application.
 * @since ArkPets 3.9
 */
public interface DialogController<T extends Application> extends Controller<T> {
    /** Returns the dialog pane node of this dialog.
     * @return The pane.
     */
    Pane getDialogPane();

    /** Returns the return button of this dialog.
     * @return The button.
     */
    ButtonBase getReturnButton();

    /** Notifies the dialog controller that the dialog is opened.
     * @param data The optional data to be transferred to the dialog.
     */
    default void notifyDialogOpened(Object data) {
    }

    /** Notifies the dialog controller that the dialog is closed.
     */
    default void notifyDialogClosed() {
    }

    /** Sets the action handler of the return event.
     * @param handler The new handler.
     */
    default void setReturnActionCallback(EventHandler<ActionEvent> handler) {
        getReturnButton().setOnAction(handler);
    }

    /** Triggers the action handler of the return event.
     */
    default void triggerReturnActionCallback(Event event) {
        EventHandler<ActionEvent> handler = getReturnButton().getOnAction();
        if (handler != null)
            handler.handle(new ActionEvent(event.getSource(), event.getTarget()));
    }
}
