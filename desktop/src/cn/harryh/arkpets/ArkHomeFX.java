/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.controllers.Homepage;
import cn.harryh.arkpets.utils.ArgPending;
import cn.harryh.arkpets.utils.Handbook;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.JavaProcess;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static cn.harryh.arkpets.Const.*;


/** ArkPets Homepage the JavaFX app.
 */
public class ArkHomeFX extends Application {
    private Homepage ctrl;
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
            // When request to launch ArkPets:
            ctrl.config.saveConfig();
            if (ctrl.config.character_asset != null && !ctrl.config.character_asset.isEmpty()) {
                ctrl.popLoading(ev -> {
                    try {
                        // Do launch ArkPets core.
                        Thread.sleep(100);
                        startArkPets();
                        Thread.sleep(1200);
                        if (ctrl.isNewcomer && !ctrl.trayExitHandbook.hasShown()) {
                            // Show handbook.
                            Handbook b = ctrl.trayExitHandbook;
                            ctrl.popNotice(b.getIcon(), b.getTitle(), b.getHeader(), b.getContent(), null).show();
                            b.setShown();
                        }
                    } catch (InterruptedException ignored) {
                    }
                });
            }
        });
        // Setup scene and show primary stage.
        Scene scene = new Scene(root);
        stage.getIcons().setAll(new Image(Objects.requireNonNull(getClass().getResource("/" + iconFilePng)).toExternalForm()));
        scene.getStylesheets().setAll(urlStyleSheet);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setTitle(desktopTitle);
        stage.show();
        startBtn.requestFocus();
        ctrl = fxml.getController();
    }


    /** Run the EmbeddedLauncher to launch the ArkPets app.
     * It will run in multi-threading mode provided by JavaFX,
     * so this method must be invoked in {@code FXApplicationThread}.
     * @see EmbeddedLauncher
     * @see javafx.concurrent.Task
     */
    public void startArkPets() {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws IOException, InterruptedException {
                // Renew the logging level arg to match the custom value of the Launcher.
                ArrayList<String> args = new ArrayList<>(Arrays.asList(ArgPending.argCache.clone()));
                args.remove(LogLevels.errorArg);
                args.remove(LogLevels.warnArg);
                args.remove(LogLevels.infoArg);
                args.remove(LogLevels.debugArg);
                String temp = switch (ctrl.config.logging_level) {
                    case LogLevels.error -> LogLevels.errorArg;
                    case LogLevels.warn  -> LogLevels.warnArg;
                    case LogLevels.info  -> LogLevels.infoArg;
                    case LogLevels.debug -> LogLevels.debugArg;
                    default -> "";
                };
                args.add(temp);
                // Start ArkPets core.
                Logger.info("Launcher", "Launching " + ctrl.config.character_asset);
                Logger.debug("Launcher", "With args " + args);
                int code = JavaProcess.exec(
                        EmbeddedLauncher.class, true,
                        List.of(), args
                );
                // ArkPets core finalized.
                if (code != 0) {
                    Logger.warn("Launcher", "Detected an abnormal finalization of an ArkPets thread (exit code " + code + "). Please check the log file for details.");
                    ctrl.lastLaunchFailed = new JavaProcess.UnexpectedExitCodeException(code);
                    return false;
                }
                Logger.debug("Launcher", "Detected a successful finalization of an ArkPets thread.");
                return true;
            }
        };
        Thread thread = new Thread(task);
        task.setOnFailed(e ->
                Logger.error("Launcher", "Detected an unexpected failure of an ArkPets thread, details see below.", task.getException())
        );
        thread.start();
    }
}
