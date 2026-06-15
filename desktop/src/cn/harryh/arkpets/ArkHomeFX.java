/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.assets.ModelsDataset;
import cn.harryh.arkpets.concurrent.*;
import cn.harryh.arkpets.controllers.BehaviorModule;
import cn.harryh.arkpets.controllers.ModelsModule;
import cn.harryh.arkpets.controllers.RootModule;
import cn.harryh.arkpets.controllers.SettingsModule;
import cn.harryh.arkpets.tray.HostTray;
import cn.harryh.arkpets.utils.DialogComposer;
import cn.harryh.arkpets.utils.FXMLHelper;
import cn.harryh.arkpets.utils.FXMLHelper.LoadFXMLResult;
import cn.harryh.arkpets.utils.GuiComponents.Toast;
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

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.UUID;

import static cn.harryh.arkpets.Const.*;


/** ArkPets Homepage the JavaFX app.
 */
public class ArkHomeFX extends Application {
    public Stage stage;
    public ArkConfig config;
    public ModelsDataset modelsDataset;
    public StackPane body;
    public Toast toast;
    public DialogComposer<ArkHomeFX> dialogs;

    public RootModule rootModule;
    public ModelsModule modelsModule;
    public BehaviorModule behaviorModule;
    public SettingsModule settingsModule;

    static {
        FontsConfig.REGULAR.loadFontToJavafx();
        FontsConfig.BOLD.loadFontToJavafx();
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

        // Setup scene and primary stage.
        Logger.info("Launcher", "Creating main scene");
        Scene scene = new Scene(rootModule.rootContainer);
        scene.getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("/UI/Main.css")).toExternalForm());
        scene.setFill(Color.TRANSPARENT);
        stage.getIcons().setAll(new Image(Objects.requireNonNull(getClass().getResource(iconFilePng)).toExternalForm()));
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setTitle(desktopTitle);
        rootModule.titleText.setText(desktopTitle);

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
            rootModule.configNetwork();
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

    public void popLoading(EventHandler<ActionEvent> handler) {
        rootModule.popLoading(handler);
    }

    public void popBrowser(URI uri) {
        Logger.info("Launcher", "Request to open URI: " + uri);
        try {
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                // File URI
                File localFile = new File(uri);
                if (!localFile.isDirectory())
                    throw new IOException("Given file URI should be a directory");
                SwingUtilities.invokeLater(() -> {
                    try {
                        Desktop.getDesktop().open(localFile);
                    } catch (IOException e) {
                        Logger.error("Launcher", "Failed to open the file URI, details see below.", e);
                    }
                });
            } else {
                // Other types of URI (like HTTP/HTTPS)
                Desktop.getDesktop().browse(uri);
            }
        } catch (IOException e) {
            Logger.error("Launcher", "Failed to open the URI, details see below.", e);
        }
    }

    public void popBrowser(String uri) {
        try {
            popBrowser(new URI(uri));
        } catch (URISyntaxException e) {
            Logger.error("Launcher", "Failed to open URI due to bad URI syntax: " + uri);
        }
    }

    public Window getWindow() {
        return rootModule.root.getScene().getWindow();
    }
}
