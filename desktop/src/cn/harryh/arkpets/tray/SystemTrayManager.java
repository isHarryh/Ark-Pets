package cn.harryh.arkpets.tray;

import cn.harryh.arkpets.utils.Logger;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static cn.harryh.arkpets.Const.iconFilePng;

public class SystemTrayManager {
    public static final SystemTrayManager INSTANCE = new SystemTrayManager();
    private static boolean initialized = false;
    private final SystemTray tray;
    private final MenuItem exitItem;
    private final TrayIcon trayIcon;
    private final PopupMenu popupMenu;
    private static final ArkPetThreadPool threadPool =
            new ArkPetThreadPool(10, 10, 10,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>());

    public Future<?> submit(Runnable task) {
        return threadPool.submit(task);
    }

    public void execute(Runnable command) {
        threadPool.execute(command);
    }

    private SystemTrayManager() {
        if (SystemTray.isSupported()) {
            Platform.setImplicitExit(false);
            tray = SystemTray.getSystemTray();

            popupMenu = new PopupMenu();
            exitItem = new MenuItem("退出程序");
            exitItem.addActionListener(e -> System.exit(0));
            popupMenu.add(exitItem);

            Image image = Toolkit.getDefaultToolkit().getImage(SystemTrayManager.class.getResource(iconFilePng));
            trayIcon = new TrayIcon(image, "ArkPets", popupMenu);
            trayIcon.setImageAutoSize(true);
            try {
                tray.add(trayIcon);
                initialized = true;
                Logger.info("Tray", "Tray icon applied");
            } catch (AWTException e) {
                Logger.error("Tray", "Unable to apply tray icon, details see below", e);
            }
            return;
        }
        tray = null;
        exitItem = null;
        trayIcon = null;
        popupMenu = null;
        Logger.error("System", "SystemTray is not supported.");
    }

    public void listen(Stage stage) {
        trayIcon.removeMouseListener(new MouseAdapter() {
        });
        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    showStage(stage);
                }
            }
        };
        trayIcon.addMouseListener(mouseListener);
    }

    public void hide(Stage stage) {
        Platform.runLater(() -> {
            if (SystemTray.isSupported()) {
                stage.hide();
                return;
            }
            System.exit(0);
        });
    }

    private void showStage(Stage stage) {
        Platform.runLater(() -> {
            if (stage.isIconified()) {
                stage.setIconified(false);
            }
            if (!stage.isShowing()) {
                stage.show();
            }
            stage.toFront();
        });
    }
}
