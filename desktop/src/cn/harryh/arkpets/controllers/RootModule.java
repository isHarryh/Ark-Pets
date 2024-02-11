/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.EmbeddedLauncher;
import cn.harryh.arkpets.concurrent.ProcessPool;
import cn.harryh.arkpets.guitasks.CheckAppUpdateTask;
import cn.harryh.arkpets.guitasks.GuiTask;
import cn.harryh.arkpets.utils.ArgPending;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.Logger;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static cn.harryh.arkpets.Const.*;
import static cn.harryh.arkpets.utils.GuiComponents.Handbook;


public final class RootModule implements Controller<ArkHomeFX> {
    public Handbook trayExitHandbook = new TrayExitHandBook();
    public ProcessPool.UnexpectedExitCodeException lastLaunchFailed;
    public GuiPrefabs.PeerNodeComposer moduleWrapperComposer;

    @FXML
    private StackPane root;
    @FXML
    private AnchorPane body;
    @FXML
    public AnchorPane wrapper1;
    @FXML
    public AnchorPane wrapper2;
    @FXML
    public AnchorPane wrapper3;
    @FXML
    private Pane loadingMask;
    @FXML
    private Pane splashScreen;
    @FXML
    private ImageView splashScreenIcon;

    @FXML
    private Pane sidebar;
    @FXML
    private JFXButton menuBtn1;
    @FXML
    private JFXButton menuBtn2;
    @FXML
    private JFXButton menuBtn3;
    @FXML
    public JFXButton launchBtn;

    @FXML
    public AnchorPane titleBar;
    @FXML
    public Text titleText;
    @FXML
    private JFXButton titleMinimizeBtn;
    @FXML
    private JFXButton titleCloseBtn;

    private ArkHomeFX app;
    private double xOffset;
    private double yOffset;

    @Override
    public void initializeWith(ArkHomeFX app) {
        this.app = app;
        initMenuButtons();
        initLaunchButton();
        initLaunchingStatusListener();

        // Load config file.
        app.config = Objects.requireNonNull(ArkConfig.getConfig(), "ArkConfig returns a null instance, please check the config file.");
        isNewcomer = app.config.isNewcomer();
        app.config.saveConfig();
    }

    /** Pops up the splash screen in the GUI.
     * @param handler The event to be handled when the splash screen is shown.
     * @param durationIn The fade-in transition duration.
     * @param durationOut The fade-out transition duration.
     */
    public void popSplashScreen(EventHandler<ActionEvent> handler, Duration durationIn, Duration durationOut) {
        body.setVisible(false);
        GuiPrefabs.fadeInNode(splashScreen, durationIn, e -> {
            handler.handle(e);
            GuiPrefabs.fadeOutNode(splashScreen, durationOut, ev -> {
                body.setVisible(true);
                launchBtn.requestFocus();
            });
        });
    }

    /** Pops up the loading mask in the GUI to inform the user to wait for an executing task.
     * @param handler The event to be handled when the loading is shown.
     */
    public void popLoading(EventHandler<ActionEvent> handler) {
        GuiPrefabs.fadeInNode(loadingMask, durationFast, e -> {
            try {
                handler.handle(e);
            } catch (Exception ex) {
                Logger.error("Task", "Foreground loading task failed, details see below.", ex);
            }
            GuiPrefabs.fadeOutNode(loadingMask, durationFast, null);
        });
    }

    /** Runs the EmbeddedLauncher to launch the ArkPets app.
     * It will run in multi-threading mode.
     * @see EmbeddedLauncher
     */
    public void startArkPetsCore() {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws InterruptedException, ExecutionException {
                // Update the logging level arg to match the custom value of the Launcher.
                ArrayList<String> args = new ArrayList<>(Arrays.asList(ArgPending.argCache.clone()));
                args.remove(LogConfig.errorArg);
                args.remove(LogConfig.warnArg);
                args.remove(LogConfig.infoArg);
                args.remove(LogConfig.debugArg);
                String temp = switch (app.config.logging_level) {
                    case LogConfig.error -> LogConfig.errorArg;
                    case LogConfig.warn  -> LogConfig.warnArg;
                    case LogConfig.info  -> LogConfig.infoArg;
                    case LogConfig.debug -> LogConfig.debugArg;
                    default -> "";
                };
                args.add(temp);
                // Start ArkPets core.
                Logger.info("Launcher", "Launching " + app.config.character_asset);
                Logger.debug("Launcher", "With args " + args);
                Future<ProcessPool.ProcessResult> future = ProcessPool.getInstance().submit(EmbeddedLauncher.class, List.of(), args);
                // ArkPets core finalized.
                if (!future.get().isSuccess()) {
                    Logger.warn("Launcher", "Detected an abnormal finalization of an ArkPets thread (exit code -1). Please check the log file for details.");
                    lastLaunchFailed = new ProcessPool.UnexpectedExitCodeException(-1);
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

    /** Fetches a regular check-app-up-date request from the ArkPets server.
     */
    public void syncRemoteMetaInfo() {
        new CheckAppUpdateTask(app.root, GuiTask.GuiTaskStyle.HIDDEN, "auto").start();
    }

    @FXML
    public void titleBarPressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    public void titleBarDragged(MouseEvent event) {
        app.stage.setX(event.getScreenX() - xOffset);
        app.stage.setY(event.getScreenY() - yOffset);
    }

    @FXML
    public void windowMinimize(MouseEvent event) {
        app.stage.setIconified(true);
    }

    @FXML
    public void windowClose(MouseEvent event) {
        String solidExitTip = (app.config != null && app.config.launcher_solid_exit) ?
            "退出程序将会同时退出已启动的桌宠。" : "退出程序后已启动的桌宠将会保留。";
        GuiPrefabs.DialogUtil.createConfirmDialog(root,
                GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_HELP_ALT, GuiPrefabs.Colors.COLOR_INFO),
                "确认退出",
                "现在退出 " + appName + " 吗？",
                "根据您的设置，" + solidExitTip + "\n使用最小化 [-] 按钮可以隐藏窗口到系统托盘。",
                () -> popSplashScreen(e -> {
                    Logger.info("Launcher", "User close request");
                    Platform.exit();
                }, durationNormal, Duration.ZERO)).show();
    }

    private void initLaunchButton() {
        // Set handler for internal start button.
        launchBtn.setOnAction(e -> {
            // When request to launch ArkPets:
            launchBtn.setDisable(true);
            app.config.saveConfig();
            if (app.config.character_asset != null && !app.config.character_asset.isEmpty()) {
                app.popLoading(ev -> {
                    try {
                        // Do launch ArkPets core.
                        Thread.sleep(100);
                        startArkPetsCore();
                        Thread.sleep(1200);
                        // Show handbook in the first-run.
                        if (isNewcomer && !trayExitHandbook.hasShown()) {
                            Handbook b = trayExitHandbook;
                            GuiPrefabs.DialogUtil.createCommonDialog(app.root, b.getIcon(), b.getTitle(), b.getHeader(), b.getContent(), null).show();
                            b.setShown();
                        }
                    } catch (InterruptedException ignored) {
                    } finally {
                        launchBtn.setDisable(false);
                    }
                });
            }
        });
    }

    private void initMenuButtons() {
        // Bind the menu buttons to the corresponding modules' wrappers.
        moduleWrapperComposer = new GuiPrefabs.PeerNodeComposer();
        Button[] menuBtnList = new Button[]{menuBtn1, menuBtn2, menuBtn3};
        AnchorPane[] wrapperList = new AnchorPane[]{wrapper1, wrapper2, wrapper3};
        for (int i = 0; i < 3; i++) {
            // i = {0=Models, 1=Behavior, 2=Settings}
            final int finalI = i;
            menuBtnList[i].setOnAction(e -> moduleWrapperComposer.activate(finalI));
            menuBtnList[i].getStyleClass().setAll("menu-btn");
            moduleWrapperComposer.add(i,
                    e -> menuBtnList[finalI].getStyleClass().add("menu-btn-active"),
                    e -> menuBtnList[finalI].getStyleClass().setAll("menu-btn"),
                    wrapperList[i]
            );
        }
    }

    private void initLaunchingStatusListener() {
        ScheduledService<Boolean> ss = new ScheduledService<>() {
            @Override
            protected Task<Boolean> createTask() {
                Task<Boolean> task = new Task<>() {
                    @Override
                    protected Boolean call() throws Exception {
                        if (lastLaunchFailed != null) {
                            Exception e = lastLaunchFailed;
                            lastLaunchFailed = null;
                            throw e;
                        }
                        return false;
                    }
                };
                task.setOnFailed(e -> GuiPrefabs.DialogUtil.createErrorDialog(app.root, task.getException()).show());
                return task;
            }
        };
        ss.setDelay(new Duration(1000));
        ss.setPeriod(new Duration(500));
        ss.setRestartOnFailure(true);
        ss.start();
    }

    private static class TrayExitHandBook extends Handbook {
        @Override
        public String getTitle() {
            return "使用提示";
        }

        @Override
        public String getHeader() {
            return "可以通过右键系统托盘图标来管理已启动的桌宠。";
        }

        @Override
        public String getContent() {
            return "看来你已经启动了你的第一个 ArkPets 桌宠！尽情享受 ArkPets 吧！";
        }
    }
}
