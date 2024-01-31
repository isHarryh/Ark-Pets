/**
 * Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.controllers.Homepage;
import cn.harryh.arkpets.process_pool.Status;
import cn.harryh.arkpets.process_pool.TaskStatus;
import cn.harryh.arkpets.socket.InteriorSocketServer;
import cn.harryh.arkpets.tray.SystemTrayManager;
import cn.harryh.arkpets.utils.ArgPending;
import cn.harryh.arkpets.utils.JavaProcess;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.PopupUtils.DialogUtil;
import cn.harryh.arkpets.utils.PopupUtils.Handbook;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static cn.harryh.arkpets.Const.*;


/**
 * ArkPets Homepage the JavaFX app.
 */
public class ArkHomeFX extends Application {
    private Homepage ctrl;
    public final String urlStyleSheet = Objects.requireNonNull(getClass().getResource("/UI/Main.css")).toExternalForm();

    @Override
    public void start(Stage stage) throws Exception {
        // Load FXML for Homepage.
        Logger.info("Launcher", "Starting");
        FXMLLoader fxml = new FXMLLoader();
        fxml.setLocation(getClass().getResource("/UI/Homepage.fxml"));
        Parent root = fxml.load();
        Logger.info("Socket", "Server starting");

        // Start Socket Server
        InteriorSocketServer.getInstance().startServer();

        // Load fonts.
        Font.loadFont(getClass().getResourceAsStream(fontFileRegular), Font.getDefault().getSize());
        Font.loadFont(getClass().getResourceAsStream(fontFileBold), Font.getDefault().getSize());

        // Set handler for internal start button.
        Button startBtn = (Button) root.lookup("#Start-btn");
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
                        if (isNewcomer && !ctrl.trayExitHandbook.hasShown()) {
                            // Show handbook.
                            Handbook b = ctrl.trayExitHandbook;
                            DialogUtil.createCommonDialog(ctrl.root, b.getIcon(), b.getTitle(), b.getHeader(), b.getContent(), null).show();
                            b.setShown();
                        }
                    } catch (InterruptedException ignored) {
                    }
                });
            }
        });

        // Setup scene and show primary stage.
        Scene scene = new Scene(root);
        stage.getIcons().setAll(new Image(Objects.requireNonNull(getClass().getResource(iconFilePng)).toExternalForm()));
        scene.getStylesheets().setAll(urlStyleSheet);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setTitle(desktopTitle);
        stage.show();

        SystemTrayManager.getInstance().listen(stage);

        stage.iconifiedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                SystemTrayManager.getInstance().hide(stage);
            }
        }));

        stage.setOnCloseRequest(e -> SystemTrayManager.getInstance().hide(stage));

        // Finish.
        startBtn.requestFocus();
        ctrl = fxml.getController();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        InteriorSocketServer.getInstance().stopServer();
        if (!(Objects.requireNonNull(ArkConfig.getConfig()).separate_arkpet_from_launcher))
            SystemTrayManager.getInstance().shutdown();
    }

    /**
     * Runs the EmbeddedLauncher to launch the ArkPets app.
     * It will run in multi-threading mode provided by JavaFX,
     * so this method must be invoked in {@code FXApplicationThread}.
     *
     * @see EmbeddedLauncher
     * @see javafx.concurrent.Task
     */
    public void startArkPets() {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws IOException, InterruptedException, ExecutionException {
                // Renew the logging level arg to match the custom value of the Launcher.
                ArrayList<String> args = new ArrayList<>(Arrays.asList(ArgPending.argCache.clone()));
                args.remove(LogConfig.errorArg);
                args.remove(LogConfig.warnArg);
                args.remove(LogConfig.infoArg);
                args.remove(LogConfig.debugArg);
                String temp = switch (ctrl.config.logging_level) {
                    case LogConfig.error -> LogConfig.errorArg;
                    case LogConfig.warn -> LogConfig.warnArg;
                    case LogConfig.info -> LogConfig.infoArg;
                    case LogConfig.debug -> LogConfig.debugArg;
                    default -> "";
                };
                args.add(temp);
                // Start ArkPets core.
                Logger.info("Launcher", "Launching " + ctrl.config.character_asset);
                Logger.debug("Launcher", "With args " + args);
                FutureTask<TaskStatus> future = SystemTrayManager.getInstance().submit(EmbeddedLauncher. class, List.of(), args);
//                int code = JavaProcess.exec(
//                        EmbeddedLauncher.class, true,
//                        List.of(), args
//                );
                // ArkPets core finalized.
                if (Objects.equals(future.get().getStatus(), Status.FAILURE)) {
                    Logger.warn("Launcher", "Detected an abnormal finalization of an ArkPets thread (exit code -1). Please check the log file for details.");
                    ctrl.lastLaunchFailed = new JavaProcess.UnexpectedExitCodeException(-1);
                    return false;
                }
                Logger.debug("Launcher", "Detected a successful finalization of an ArkPets thread.");
                return true;
            }
        };

        task.setOnFailed(e ->
                Logger.error("Launcher", "Detected an unexpected failure of an ArkPets thread, details see below.", task.getException())
        );

        SystemTrayManager.getInstance().submit(task);
    }
}
