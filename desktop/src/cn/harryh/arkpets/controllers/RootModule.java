/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.EmbeddedLauncher;
import cn.harryh.arkpets.guitasks.CheckAppUpdateTask;
import cn.harryh.arkpets.guitasks.GuiTask;
import cn.harryh.arkpets.process_pool.Status;
import cn.harryh.arkpets.process_pool.TaskStatus;
import cn.harryh.arkpets.tray.SystemTrayManager;
import cn.harryh.arkpets.utils.ArgPending;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.JavaProcess;
import cn.harryh.arkpets.utils.Logger;
import com.jfoenix.controls.*;
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

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static cn.harryh.arkpets.Const.*;
import static cn.harryh.arkpets.utils.GuiPrefabs.DialogUtil;
import static cn.harryh.arkpets.utils.GuiPrefabs.Handbook;


public final class RootModule implements Controller<ArkHomeFX> {
    public Handbook trayExitHandbook = new TrayExitHandBook();
    public JavaProcess.UnexpectedExitCodeException lastLaunchFailed = null;

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
    public JFXButton menuBtn1;
    @FXML
    public JFXButton menuBtn2;
    @FXML
    public JFXButton menuBtn3;
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
                    case LogConfig.warn -> LogConfig.warnArg;
                    case LogConfig.info -> LogConfig.infoArg;
                    case LogConfig.debug -> LogConfig.debugArg;
                    default -> "";
                };
                args.add(temp);
                // Start ArkPets core.
                Logger.info("Launcher", "Launching " + app.config.character_asset);
                Logger.debug("Launcher", "With args " + args);
                FutureTask<TaskStatus> future = SystemTrayManager.getInstance().submit(EmbeddedLauncher.class, List.of(), args);
                // ArkPets core finalized.
                if (Objects.equals(future.get().getStatus(), Status.FAILURE)) {
                    Logger.warn("Launcher", "Detected an abnormal finalization of an ArkPets thread (exit code -1). Please check the log file for details.");
                    lastLaunchFailed = new JavaProcess.UnexpectedExitCodeException(-1);
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
        popSplashScreen(e -> {
            Logger.info("Launcher", "User close request");
            app.stage.close();
        }, durationNormal, Duration.ZERO);
    }

    private void initLaunchButton() {
        // Set handler for internal start button.
        launchBtn.setOnAction(e -> {
            // When request to launch ArkPets:
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
                            GuiPrefabs.Handbook b = trayExitHandbook;
                            GuiPrefabs.DialogUtil.createCommonDialog(app.root, b.getIcon(), b.getTitle(), b.getHeader(), b.getContent(), null).show();
                            b.setShown();
                        }
                    } catch (InterruptedException ignored) {
                    }
                });
            }
        });
    }

    private void initMenuButtons() {
        // Bind the menu buttons to the corresponding modules' wrappers.
        Map<Button, AnchorPane> btnMap = Map.of(menuBtn1, wrapper1, menuBtn2, wrapper2, menuBtn3, wrapper3);
        btnMap.forEach((btn, wrapper) -> {
            btn.getStyleClass().setAll("menu-btn");
            btn.setOnAction(e -> {
                switchModule(btnMap.get(btn));
                // Restore all button's style.
                btnMap.keySet().forEach(btn0 -> btn0.getStyleClass().setAll("menu-btn"));
                // Set this active button's style.
                btn.getStyleClass().add("menu-btn-active");
            });
        });
    }

    private void switchModule(Pane activeWrapper) {
        List<Pane> wrapperList = List.of(wrapper1, wrapper2, wrapper3);
        wrapperList.forEach(wrapper -> {
            if (wrapper.equals(activeWrapper)) {
                // Show this active wrapper.
                GuiPrefabs.fadeInNode(wrapper, durationNormal, null);
            } else {
                // Hide other wrappers.
                wrapper.setVisible(false);
            }
        });
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
                task.setOnFailed(e -> DialogUtil.createErrorDialog(app.root, task.getException()).show());
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
            return "如需关闭桌宠，请右键系统托盘图标后选择退出。";
        }

        @Override
        public String getContent() {
            return "看来你已经启动了你的第一个 ArkPets 桌宠！尽情享受 ArkPets 吧！";
        }
    }
}
