/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;


/** The interface represents an FXML controller.
 * @param <T> The type of the target JavaFX application.
 * @since ArkPets 3.0
 */
public interface Controller<T extends Application> {
    /** Initializes the FXML controller. This method is typically invoked by an FXML loading request:
     * <blockquote><pre>
     * new FXMLLoader(getClass().getResource("/path/to/myFXML.fxml")).load();</pre>
     * </blockquote>
     * @see FXMLLoader
     */
    default void initialize() {
    }

    /** Initializes the FXML controller with a bound JavaFX application.
     * @param app The bound JavaFX application.
     * @see Application
     */
    void initializeWith(T app);
}
