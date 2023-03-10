/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets;

import com.isharryh.arkpets.utils.JavaProcess;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
            // When request to launch ArkPets
            startArkPets();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
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
     * It will run in multi-threading mode provided by JavaFX,
     * so this method must be invoked in {@code FXApplicationThread}.
     * @see com.isharryh.arkpets.EmbeddedLauncher
     * @see javafx.concurrent.Task
     */
    public void startArkPets() {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws IOException, InterruptedException {
                JavaProcess.exec(EmbeddedLauncher.class, false);
                return true;
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }
}
