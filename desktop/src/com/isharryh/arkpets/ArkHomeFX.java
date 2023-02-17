/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets;

import com.isharryh.arkpets.utils.JavaProcess;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;


/** ArkPets Homepage the JavaFX app.
 */
public class ArkHomeFX extends Application {
    public final static int[] appVersion = {2, 0, 0};
    public final static String appVersionStr = appVersion[0] + "." + appVersion[1] + "." + appVersion[2];
    public final String urlStyleSheet = Objects.requireNonNull(getClass().getResource("/UI/Main.css")).toExternalForm();

    @Override
    public void start(Stage stage) throws Exception {
        // Load FXML for Homepage.
        FXMLLoader fxml = new FXMLLoader();
        fxml.setLocation(getClass().getResource("/UI/Homepage.fxml"));
        Parent root = fxml.load();
        // Set handler for internal start button.
        Button startBtn = (Button)root.lookup("#Start-btn");
        startBtn.setOnAction(e -> {
            stage.close();
            startArkPets();
            Platform.exit();
        });
        // Setup scene and show primary stage.
        Scene scene = new Scene(root);
        stage.getIcons().setAll(new Image(Objects.requireNonNull(getClass().getResource("/icon.png")).toExternalForm()));
        scene.getStylesheets().setAll(urlStyleSheet);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setTitle("ArkPets Launcher TEST");
        stage.show();
        startBtn.requestFocus();
    }



    /** Run the EmbeddedLauncher to launch the ArkPets app.
     * @see com.isharryh.arkpets.EmbeddedLauncher
     */
    public void startArkPets() {
        try {
            JavaProcess.exec(EmbeddedLauncher.class, false);
        } catch (IOException e) {
            System.err.println("[error] CAUGHT: IOException");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("[error] CAUGHT: InterruptedException");
            e.printStackTrace();
        }
    }

}
