/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.assets.ModelsDataset;
import cn.harryh.arkpets.concurrent.*;
import cn.harryh.arkpets.controllers.*;
import cn.harryh.arkpets.tray.HostTray;
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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Objects;
import java.util.UUID;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static cn.harryh.arkpets.Const.*;


/** ArkPets Homepage the JavaFX app.
 */
public class ArkHomeFX extends Application {
    public Stage         stage;
    public ArkConfig     config;
    public ModelsDataset modelsDataset;
    public StackPane     body;

    public RootModule     rootModule;
    public ModelsModule   modelsModule;
    public BehaviorModule behaviorModule;
    public SettingsModule settingsModule;
    public Titlebar titleBar;

    static {
        FontsConfig.loadFontsToJavafx();
    }

    @Override
    public void start(Stage stage) throws Exception {
        Logger.info("Launcher", "Starting");
        this.stage = stage;
        Platform.setImplicitExit(false);

        // Load FXML for root node.
        LoadFXMLResult<ArkHomeFX> fxml0 = FXMLHelper.loadFXML(getClass().getResource("/UI/RootModule.fxml"));
        fxml0.initializeWith(this);
        rootModule = (RootModule) fxml0.controller();
        body = rootModule.body;
        LoadFXMLResult<ArkHomeFX> fxmlTitlebar = FXMLHelper.loadFXML(getClass().getResource("/UI/Titlebar.fxml"));
        fxmlTitlebar.initializeWith(this);
        titleBar = (Titlebar) fxmlTitlebar.controller();
        fxmlTitlebar.addToNode(rootModule.wrapper4);

        // Setup scene and primary stage.
        Logger.info("Launcher", "Creating main scene");
        Scene scene = new Scene(rootModule.rootContainer);
        scene.getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("/UI/Main.css")).toExternalForm());
        scene.setFill(Color.TRANSPARENT);
        stage.getIcons().setAll(new Image(Objects.requireNonNull(getClass().getResource(iconFilePng)).toExternalForm()));
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);
        stage.setScene(scene);
        String title = desktopTitle + getVersionType();
        stage.setTitle(title);
        titleBar.titleText.setText(title);

        // After the stage is shown, do initialization.
        stage.show();
        rootModule.popSplashScreen(e -> {
            // Initialize socket server and HostTray.
            try {
                HostTray hostTray = HostTray.getInstance();
                hostTray.setOnCloseStage(() -> Platform.runLater(rootModule::exit));
                hostTray.setOnShowStage(() -> Platform.runLater(stage::show));
                SocketServer.getInstance().startServer(hostTray);
                hostTray.applyTrayIcon();
            } catch (PortUtils.NoPortAvailableException ex) {
                Logger.error("SocketServer", "No available port, thus server cannot be started");
                // No HostTray icon will be applied when this situation happens.
            } catch (PortUtils.ServerCollisionException ex) {
                Logger.error("SocketServer", "Server is already running");
                SocketClient socketClient = new SocketClient();
                socketClient.connect(() -> {
                            Logger.info("Launcher", "Request to start an existed Launcher");
                            socketClient.sendRequest(SocketData.ofOperation(UUID.randomUUID(), SocketData.Operation.ACTIVATE_LAUNCHER));
                            socketClient.disconnect();
                        },
                        new SocketClient.ClientSocketSession(socketClient, null));
                // Explicitly cancel the followed initialization in this start method.
                Platform.exit();
                return;
            } catch (Exception ex) {
                Logger.error("Launcher", "Failed to initialize socket server or HostTray, details see below.", ex);
            }

            // Initialize modules.
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
            rootModule.syncRemoteMetaInfo();
            rootModule.moduleWrapperComposer.activate(0);

            Logger.info("Launcher", "Finished starting");
        }, Duration.ZERO, durationFast);
    }

    @Override
    public void stop() {
        if (config != null && config.launcher_solid_exit) {
            // Notify ArkPets core instances that connected to this app to close.
            HostTray.getInstance().forEachMemberTray(memberTray -> memberTray.sendOperation(SocketData.Operation.LOGOUT));
        }
        SocketServer.getInstance().stopServer();
        ProcessPool.getInstance().shutdown();
        Logger.debug("Launcher", "Finished stopping");
    }

    public String getVersionType() {
        Class<?> clazz = this.getClass();
        try {
            CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
            if (codeSource == null) {
                return "+unknown";
            }
            URL jarUrl = codeSource.getLocation();
            JarFile jarFile = new JarFile(new File(jarUrl.toURI()));
            Manifest manifest = jarFile.getManifest();
            jarFile.close();
            return manifest.getMainAttributes().getValue("Version-Type");
        } catch (FileNotFoundException ignored) {
            // this mean run through gradle or IDEA
            return "+dev";
        } catch (Exception ignored) {
            return "+unknown";
        }
    }

    public void popLoading(EventHandler<ActionEvent> handler) {
        rootModule.popLoading(handler);
    }

    public Window getWindow() {
        return rootModule.root.getScene().getWindow();
    }
}
