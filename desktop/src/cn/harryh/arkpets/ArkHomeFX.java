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
import cn.harryh.arkpets.tray.SystemTrayManager;
import cn.harryh.arkpets.utils.FXMLHelper;
import cn.harryh.arkpets.utils.FXMLHelper.LoadFXMLResult;
import cn.harryh.arkpets.utils.Logger;
import javafx.application.Application;
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

        // Load fonts.
        Font.loadFont(getClass().getResourceAsStream(fontFileRegular), Font.getDefault().getSize());
        Font.loadFont(getClass().getResourceAsStream(fontFileBold), Font.getDefault().getSize());

        // Start Socket Server
        InteriorSocketServer.getInstance().startServer();

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

        SystemTrayManager.getInstance().listen(stage);

        stage.iconifiedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                SystemTrayManager.getInstance().hide(stage);
            }
        }));

        stage.setOnCloseRequest(e -> SystemTrayManager.getInstance().hide(stage));

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
        if (!(Objects.requireNonNull(ArkConfig.getConfig()).separate_arkpet_from_launcher))
            SystemTrayManager.getInstance().shutdown();
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
