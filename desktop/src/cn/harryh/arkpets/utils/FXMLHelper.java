/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import cn.harryh.arkpets.controllers.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;


/** The class provides some functions to load a JavaFX FXML.
 * @since ArkPets 3.0
 */
public class FXMLHelper {
    private FXMLHelper() {
    }

    /** Loads an FXML document.
     * @param location The resource path to the FXML document.
     * @return A record containing the controller and the FXML object hierarchy.
     * @throws IOException If an I/O exception occurs during loading.
     */
    public static <T extends Application> LoadFXMLResult<T> loadFXML( URL location)
            throws IOException {
        FXMLLoader fxml = new FXMLLoader(location);
        Node content = Objects.requireNonNull(fxml.load());
        Controller<T> controller = Objects.requireNonNull(fxml.getController());
        return new LoadFXMLResult<>(controller, content);
    }

    /** Loads an FXML document.
     * @param location The resource path to the FXML document.
     * @return A record containing the controller and the FXML object hierarchy.
     * @throws IOException If an I/O exception occurs during loading.
     */
    public static <T extends Application> LoadFXMLResult<T> loadFXML(String location)
            throws IOException {
        return loadFXML(FXMLHelper.class.getResource(location));
    }


    /** Result record that containing the returned value of the {@link #loadFXML(URL)} method.
     * @param controller The controller instance of the loaded FXML.
     * @param content The object hierarchy from the loaded FXML.
     * @param <T> The type of the target JavaFX application.
     * @see Controller
     */
    public record LoadFXMLResult<T extends Application>(Controller<T> controller, Node content) {
        /** Invokes the {@link Controller#initializeWith(Application)} method of the controller.
         * @param app The bound JavaFX application.
         * @return The controller instance.
         */
        public Controller<T> initializeWith(T app) {
            controller.initializeWith(app);
            return controller;
        }

        /** Adds the object hierarchy to a node's children list.
         * @param parent The given parent node.
         */
        public void addToNode(Pane parent) {
            parent.getChildren().add(content);
        }
    }
}
