package cn.harryh.arkpets.tray;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.utils.Logger;
import javafx.application.Platform;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class HostTray {
    protected TrayIcon trayIcon;
    protected boolean initialized = false;
    protected Map<UUID, MemberTray> arkPetTrays = new HashMap<>();

    private JPopupMenu popupMenu;
    private JDialog popWindow;
    private JMenu playerMenu;
    private Stage stage;

    static {
        Const.FontsConfig.loadFontsToSwing();
    }

    public HostTray(Stage boundStage) {
        if (SystemTray.isSupported()) {
            Platform.setImplicitExit(false);
            SystemTray tray = SystemTray.getSystemTray();

            // Ui Components:
            popWindow = new JDialog();
            popWindow.setUndecorated(true);
            popWindow.setSize(1, 1);
            JLabel innerLabel = new JLabel(" ArkPets ");
            innerLabel.setAlignmentX(0.5f);

            playerMenu = new JMenu("角色管理");
            JMenuItem exitItem = new JMenuItem("退出程序");
            exitItem.addActionListener(e -> {
                Logger.info("HostTray", "Request to exit");
                Platform.exit();
            });

            popupMenu = new JPopupMenu() {
                @Override
                public void firePopupMenuWillBecomeInvisible() {
                    popWindow.setVisible(false); // Hide the container when the menu is invisible.
                }
            };
            popupMenu.add(innerLabel);
            popupMenu.addSeparator();
            popupMenu.add(playerMenu);
            popupMenu.add(exitItem);
            popupMenu.setSize(100, 24 * popupMenu.getSubElements().length);

            Image image = Toolkit.getDefaultToolkit().getImage(HostTray.class.getResource(Const.iconFilePng));
            trayIcon = new TrayIcon(image, "ArkPets");
            trayIcon.setImageAutoSize(true);

            trayIcon.addMouseListener(new MouseAdapter() {
                public void mouseReleased(MouseEvent e) {
                    if (e.getButton() == 3 && e.isPopupTrigger())
                        showDialog(e.getX() + 5, e.getY());
                }
            });

            // Bind JavaFX stage:
            stage = boundStage;
            stage.xProperty().addListener((observable, oldValue, newValue) -> {
            });
            stage.yProperty().addListener(((observable, oldValue, newValue) -> {
            }));

            stage.iconifiedProperty().addListener(((observable, oldValue, newValue) -> {
                if (newValue)
                    hideStage();
            }));
            stage.setOnCloseRequest(e -> hideStage());
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == 1)
                        showStage();
                }
            });

            try {
                tray.add(trayIcon);
                initialized = true;
                Logger.info("HostTray", "HostTray icon applied");
            } catch (AWTException e) {
                Logger.error("HostTray", "Unable to apply HostTray icon, details see below.", e);
            }
        } else {
            Logger.error("HostTray", "Tray is not supported.");
        }
    }

    public void showDialog(int x, int y) {
        AffineTransform at = popWindow.getGraphicsConfiguration().getDefaultTransform();

        // Show the JDialog together with the JPopupMenu.
        popWindow.setVisible(true);
        popWindow.setLocation((int) (x / at.getScaleX()), (int) (y / at.getScaleY()) - popupMenu.getHeight());
        popupMenu.show(popWindow, 0, 0);
    }

    public void hideStage() {
        if (!initialized)
            return;
        stage.hide();
    }

    public void showStage() {
        if (!initialized)
            return;
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

    public MemberTray getMemberTray(UUID uuid) {
        return arkPetTrays.get(uuid);
    }

    public void addMemberTray(JMenu menu) {
        playerMenu.add(menu);
    }

    public void removeMemberTray(JMenu menu) {
        playerMenu.remove(menu);
    }

    public void addMemberTray(UUID uuid, MemberTray tray) {
        arkPetTrays.put(uuid, tray);
    }

    public void removeMemberTray(UUID uuid) {
        getMemberTray(uuid).remove();
        arkPetTrays.remove(uuid);
    }
}
