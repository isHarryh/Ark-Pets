/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.assets.ModelsDataset;
import cn.harryh.arkpets.controllers.BehaviorModule;
import cn.harryh.arkpets.controllers.ModelsModule;
import cn.harryh.arkpets.controllers.RootModule;
import cn.harryh.arkpets.controllers.SettingsModule;
import cn.harryh.arkpets.socket.InteriorSocketServer;
import cn.harryh.arkpets.socket.SocketClient;
import cn.harryh.arkpets.socket.SocketData;
import cn.harryh.arkpets.tray.SystemTrayManager;
import cn.harryh.arkpets.utils.FXMLHelper;
import cn.harryh.arkpets.utils.FXMLHelper.LoadFXMLResult;
import cn.harryh.arkpets.utils.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Objects;
import java.util.UUID;

import static cn.harryh.arkpets.Const.*;


/** ArkPets Homepage the JavaFX app.
 */
public class ArkHomeFX extends Application {
    public Stage stage;
    public ArkConfig config;
    public ModelsDataset modelsDataset;
    public StackPane root;

    public RootModule rootModule;
    public ModelsModule modelsModule;
    public BehaviorModule behaviorModule;
    public SettingsModule settingsModule;

    @Override
    public void start(Stage stage) throws Exception {
        Logger.info("Launcher", "Starting");
        this.stage = stage;

        // Socket server initialize
        Logger.info("Socket", "Check server status");
        int status = InteriorSocketServer.getInstance().checkServerAvailable();
        switch (status) {
            case 1: {
                // No server started, start server
                Logger.info("Socket", "Server starting");
                InteriorSocketServer.getInstance().startServer();
                break;
            }
            case 0: {
                // No available port
                Logger.error("Socket", "No available port");
                // TODO
                //  What if there are no port available
                Platform.exit();
            }
            case -1: {
                // Server is running
                Logger.error("Socket", "Server is running");
                SocketClient socketClient = new SocketClient();
                socketClient.connect();
                socketClient.sendRequest(new SocketData(UUID.randomUUID(), SocketData.OperateType.ACTIVATE_LAUNCHER));
                socketClient.disconnect();
                Logger.info("Launcher", "ArkPets Launcher has started.");
                System.exit(0);
            }
        }

        // Load fonts.
        Font.loadFont(getClass().getResourceAsStream(fontFileRegular), Font.getDefault().getSize());
        Font.loadFont(getClass().getResourceAsStream(fontFileBold), Font.getDefault().getSize());

        // Load FXML for root node.
        LoadFXMLResult<ArkHomeFX> fxml0 = FXMLHelper.loadFXML(getClass().getResource("/UI/RootModule.fxml"));
        fxml0.initializeWith(this);
        root = (StackPane) fxml0.content();
        rootModule = (RootModule) fxml0.controller();

        // Setup scene and primary stage.
        Logger.info("Launcher", "Creating main scene");
        Scene scene = new Scene(root);
        scene.getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("/UI/Main.css")).toExternalForm());
        stage.getIcons().setAll(new Image(Objects.requireNonNull(getClass().getResource(iconFilePng)).toExternalForm()));
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setTitle(desktopTitle);
        rootModule.titleText.setText(desktopTitle);

        // Add system tray and listen stage
        SystemTrayManager.getInstance().listen(stage);

        // Listen for tray click event
        stage.iconifiedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                SystemTrayManager.getInstance().hide();
            }
        }));

        // Listen window close event
        stage.setOnCloseRequest(e -> SystemTrayManager.getInstance().hide());

        // After the stage is shown, do initialize modules.
        stage.show();
        rootModule.popSplashScreen(e -> {
            Logger.info("Launcher", "Loading modules");
            try {
                LoadFXMLResult<ArkHomeFX> fxml1 = FXMLHelper.loadFXML("/UI/ModelsModule.fxml");
                LoadFXMLResult<ArkHomeFX> fxml2 = FXMLHelper.loadFXML("/UI/BehaviorModule.fxml");
                LoadFXMLResult<ArkHomeFX> fxml3 = FXMLHelper.loadFXML("/UI/SettingsModule.fxml");
                fxml1.addToNode(rootModule.wrapper1);
                fxml2.addToNode(rootModule.wrapper2);
                fxml3.addToNode(rootModule.wrapper3);
                modelsModule = (ModelsModule) fxml1.initializeWith(this);
                behaviorModule = (BehaviorModule) fxml2.initializeWith(this);
                settingsModule = (SettingsModule) fxml3.initializeWith(this);
            } catch (Exception ex) {
                Logger.error("Launcher", "Failed to initialize module, details see below.", ex);
            }

            // Post initialization.
            syncRemoteMetaInfo();
            switchToModelsPane();
            Logger.info("Launcher", "Finished starting");
        }, Duration.ZERO, durationFast);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        InteriorSocketServer.getInstance().stopServer();
        // function shutdown will kill all arkpets started by this launcher
//        ProcessPool.getInstance().shutdown();
    }

    public boolean initModelsDataset(boolean popNotice) {
        return modelsModule.initModelsDataset(popNotice);
    }

    public void popLoading(EventHandler<ActionEvent> handler) {
        rootModule.popLoading(handler);
    }

    public void modelReload(boolean popNotice) {
        modelsModule.modelReload(popNotice);
    }

    public void switchToModelsPane() {
        rootModule.menuBtn1.getOnAction().handle(new ActionEvent());
    }

    public void switchToBehaviorPane() {
        rootModule.menuBtn2.getOnAction().handle(new ActionEvent());
    }

    public void switchToSettingsPane() {
        rootModule.menuBtn3.getOnAction().handle(new ActionEvent());
    }

    public void syncRemoteMetaInfo() {
        rootModule.syncRemoteMetaInfo();
    }
}
