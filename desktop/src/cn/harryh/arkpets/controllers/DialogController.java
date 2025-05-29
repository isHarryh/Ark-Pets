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
    Button getReturnButton();

    /** Sets the action handler of the return event.
     * @param handler The new handler.
     */
    default void setReturnActionHandler(EventHandler<ActionEvent> handler) {
        getReturnButton().setOnAction(handler);
    }

    /** Triggers the action handler of the return event.
     */
    default void triggerReturnActionHandler(Event event) {
        getReturnButton().getOnAction().handle(new ActionEvent(event.getSource(), event.getTarget()));
    }
}
