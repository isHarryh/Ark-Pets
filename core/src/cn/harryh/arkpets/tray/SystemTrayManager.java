package cn.harryh.arkpets.tray;

import cn.harryh.arkpets.utils.Logger;
import javafx.application.Platform;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.util.UUID;

import static cn.harryh.arkpets.Const.iconFilePng;

public class SystemTrayManager extends TrayManager {
    private static SystemTrayManager instance = null;
    private volatile JPopupMenu popupMenu;
    private volatile JDialog popWindow;
    private volatile JMenu playerMenu;
    private static double x = 0;
    private static double y = 0;

    public static synchronized SystemTrayManager getInstance() {
        if (instance == null)
            instance = new SystemTrayManager();
        return instance;
    }

    private SystemTrayManager() {
        super();
        if (SystemTray.isSupported()) {
            Platform.setImplicitExit(false);
            tray = SystemTray.getSystemTray();

            popWindow = new JDialog();
            popWindow.setUndecorated(true);
            popWindow.setSize(1, 1);

            popupMenu = new JPopupMenu() {
                @Override
                public void firePopupMenuWillBecomeInvisible() {
                    popWindow.setVisible(false);
                }
            };

            JLabel innerLabel = new JLabel(" ArkPets ");
            innerLabel.setAlignmentX(0.5f);
            popupMenu.add(innerLabel);

            playerMenu = new JMenu("干员管理");
            JMenuItem exitItem = new JMenuItem("退出程序");
            exitItem.addActionListener(e -> Platform.exit());

            popupMenu.addSeparator();
            popupMenu.add(playerMenu);
            popupMenu.add(exitItem);
            popupMenu.setSize(100, 24 * popupMenu.getSubElements().length);

            Image image = Toolkit.getDefaultToolkit().getImage(SystemTrayManager.class.getResource(iconFilePng));
            trayIcon = new TrayIcon(image, "ArkPets");
            trayIcon.setImageAutoSize(true);

            trayIcon.addMouseListener(new MouseAdapter() {
                public void mouseReleased(MouseEvent e) {
                    if (e.getButton() == 3 && e.isPopupTrigger()) {
                        showDialog(e.getX() + 5, e.getY());
                    }
                }
            });

            try {
                tray.add(trayIcon);
                initialized = true;
                Logger.info("SystemTrayManager", "SystemTray icon applied");
            } catch (AWTException e) {
                Logger.error("SystemTrayManager", "Unable to apply tray icon, details see below", e);
            }
            return;
        }
        Logger.error("SystemTrayManager", "SystemTray is not supported.");
    }

    @Override
    public void showDialog(int x, int y) {
        AffineTransform at = popWindow.getGraphicsConfiguration().getDefaultTransform();

        // Show the JDialog together with the JPopupMenu.
        popWindow.setVisible(true);
        popWindow.setLocation((int) (x / at.getScaleX()), (int) (y / at.getScaleY()) - popupMenu.getHeight());
        popupMenu.show(popWindow, 0, 0);
    }

    @Override
    public void listen(Stage stage) {
        if (!initialized)
            return;
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
        x = stage.getX();
        y = stage.getY();
        trayIcon.addMouseListener(mouseListener);
    }

    @Override
    public void hide(Stage stage) {
        if (!initialized)
            return;
        Platform.runLater(() -> {
            if (SystemTray.isSupported()) {
                x = stage.getX();
                y = stage.getY();
                stage.hide();
                return;
            }
            System.exit(0);
        });
    }

    private void showStage(Stage stage) {
        if (!initialized)
            return;
        Platform.runLater(() -> {
            if (stage.isIconified()) {
                stage.setIconified(false);
            }
            if (!stage.isShowing()) {
                stage.setX(x);
                stage.setY(y);
                stage.show();
            }
            stage.toFront();
        });
    }

    @Override
    public void addTray(UUID uuid, Tray tray) {
        arkPetTrays.put(uuid, tray);
    }

    @Override
    public void removeTray(UUID uuid) {
        getTray(uuid).removeTray();
        arkPetTrays.remove(uuid);
    }

    @Override
    public Tray getTray(UUID uuid) {
        return arkPetTrays.get(uuid);
    }

    public void addTray(JMenu menu) {
        playerMenu.add(menu);
    }

    public void removeTray(JMenu menu) {
        playerMenu.remove(menu);
    }
}
