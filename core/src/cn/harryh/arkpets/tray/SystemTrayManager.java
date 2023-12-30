package cn.harryh.arkpets.tray;

import cn.harryh.arkpets.ArkTray;
import cn.harryh.arkpets.process_pool.ProcessPool;
import cn.harryh.arkpets.process_pool.TaskStatus;
import cn.harryh.arkpets.utils.Logger;
import javafx.application.Platform;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import static cn.harryh.arkpets.Const.fontFileRegular;
import static cn.harryh.arkpets.Const.iconFilePng;

public class SystemTrayManager {
    private static SystemTrayManager instance = null;
    private static boolean initialized = false;
    private volatile SystemTray tray;
    private volatile TrayIcon trayIcon;
    private volatile JPopupMenu popupMenu;
    private volatile JDialog popWindow;
    private volatile JMenu playerMenu;
    private static double x = 0;
    private static double y = 0;
    private static ProcessPool threadPool = null;
    public static Font font;

    static {
        try {
            InputStream inputStream = Objects.requireNonNull(ArkTray.class.getResourceAsStream(fontFileRegular));
            font = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            if (font != null) {
                UIManager.put("Label.font", font.deriveFont(9f).deriveFont(Font.ITALIC));
                UIManager.put("MenuItem.font", font.deriveFont(11f));
            }
        } catch (FontFormatException | IOException e) {
            Logger.error("Tray", "Failed to load tray menu font, details see below.", e);
            font = null;
        }
    }

    public static synchronized SystemTrayManager getInstance() {
        if (instance == null)
            instance = new SystemTrayManager();
        return instance;
    }

    public SystemTray getTray() {
        return tray;
    }

    public TrayIcon getTrayIcon() {
        return trayIcon;
    }

    public void addArkPets(JMenu menu) {
        playerMenu.add(menu);
    }

    public void shutdown() {
        threadPool.shutdown();
    }

    private SystemTrayManager() {
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
            exitItem.addActionListener(e -> System.exit(0));

            popupMenu.addSeparator();
            popupMenu.add(playerMenu);
            popupMenu.add(exitItem);
            popupMenu.setSize(100, 24 * popupMenu.getSubElements().length);

            threadPool = new ProcessPool();

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

    public void showDialog(int x, int y) {
        AffineTransform at = popWindow.getGraphicsConfiguration().getDefaultTransform();

        // Show the JDialog together with the JPopupMenu.
        popWindow.setVisible(true);
        popWindow.setLocation((int) (x / at.getScaleX()), (int) (y / at.getScaleY()) - popupMenu.getHeight());
        popupMenu.show(popWindow, 0, 0);
    }

    private void sendMessage(TrayIcon.MessageType messageType, String title, String content, Object... args) {
        if (!initialized)
            return;
        trayIcon.displayMessage(title, content.formatted(args), messageType);
    }

    public void sendInfoMessage(String title, String content, Object... args) {
        sendMessage(TrayIcon.MessageType.INFO, title, content, args);
    }

    public void sendErrorMessage(String title, String content, Object... args) {
        sendMessage(TrayIcon.MessageType.ERROR, title, content, args);
    }

    public void sendWarnMessage(String title, String content, Object... args) {
        sendMessage(TrayIcon.MessageType.WARNING, title, content, args);
    }

    public void sendMessage(String title, String content, Object... args) {
        sendMessage(TrayIcon.MessageType.NONE, title, content, args);
    }

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

    public FutureTask<TaskStatus> submit(Class<?> clazz, java.util.List<String> jvmArgs, List<String> args) {
        return threadPool.submit(clazz, jvmArgs, args);
    }

    public Future<?> submit(Runnable task) {
        return threadPool.submit(task);
    }
}
