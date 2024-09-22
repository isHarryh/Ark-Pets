/** Copyright (c) 2022-2024, Harry Huang, Half Nothing
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.tray;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.utils.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;


public class HostTray {
    protected TrayIcon trayIcon;
    protected boolean initialized = false;
    protected Map<UUID, MemberTray> arkPetTrays = new HashMap<>();

    private JDialog popWindow;
    private JPopupMenu popMenu;
    private JMenu playerMenu;

    private Runnable onShowStage;
    private Runnable onCloseStage;

    private static HostTray instance;

    static {
        Const.FontsConfig.loadFontsToSwing();
    }

    public static HostTray getInstance() {
        if (instance == null)
            instance = new HostTray();
        return instance;
    }

    private HostTray() {
        if (SystemTray.isSupported()) {
            // Ui Components:
            popWindow = new JDialog();
            popWindow.setUndecorated(true);
            popWindow.setSize(1, 1);
            JLabel innerLabel = new JLabel(" ArkPets ");
            innerLabel.setAlignmentX(0.5f);

            playerMenu = new JMenu("角色管理");
            JMenuItem optExit = new JMenuItem("退出程序");
            optExit.addActionListener(e -> {
                Logger.info("HostTray", "Request to exit");
                if (onCloseStage != null)
                    onCloseStage.run();
            });

            popMenu = new JPopupMenu() {
                @Override
                public void firePopupMenuWillBecomeInvisible() {
                    popWindow.setVisible(false); // Hide the container when the menu is invisible.
                }
            };
            popMenu.add(innerLabel);
            popMenu.addSeparator();
            popMenu.add(playerMenu);
            popMenu.add(optExit);
            popMenu.setSize(100, 24 * popMenu.getSubElements().length);

            Image image = Toolkit.getDefaultToolkit().getImage(HostTray.class.getResource(Const.iconFilePng));
            trayIcon = new TrayIcon(image, "ArkPets");
            trayIcon.setImageAutoSize(true);

            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.getButton() == 3 && e.isPopupTrigger())
                        showDialog(e.getX() + 5, e.getY());
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.getButton() == 3 && e.isPopupTrigger())
                        showDialog(e.getX() + 5, e.getY());
                }
            });
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == 1)
                        showStage();
                }
            });
        } else {
            Logger.error("HostTray", "Tray is not supported.");
        }
    }

    public void applyTrayIcon() {
        if (initialized)
            return;
        try {
            SystemTray.getSystemTray().add(trayIcon);
            Logger.info("HostTray", "HostTray icon applied");
            initialized = true;
        } catch (AWTException e) {
            Logger.error("HostTray", "Unable to apply HostTray icon, details see below.", e);
        }
    }

    public void showDialog(int x, int y) {
        if (!initialized)
            return;
        /* Use `System.setProperty("sun.java2d.uiScale", "1")` can also avoid system scaling.
        Here we will adapt the coordinate for system scaling artificially. See below. */
        AffineTransform at = popWindow.getGraphicsConfiguration().getDefaultTransform();
        int scaledX = (int) (x / at.getScaleX());
        int scaledY = (int) (y / at.getScaleY());

        // Show the JDialog together with the JPopupMenu.
        popWindow.setVisible(true);
        popWindow.setLocation(scaledX, scaledY - popMenu.getHeight());
        popMenu.show(popWindow, 0, 0);
    }

    public void showStage() {
        if (!initialized)
            return;
        Logger.info("HostTray", "Request to show stage");
        if (onShowStage != null)
            onShowStage.run();
    }

    public void setOnShowStage(Runnable handler) {
        onShowStage = handler;
    }

    public void setOnCloseStage(Runnable handler) {
        onCloseStage = handler;
    }

    public MemberTray getMemberTray(UUID uuid) {
        return arkPetTrays.get(uuid);
    }

    public void forEachMemberTray(Consumer<MemberTray> action) {
        arkPetTrays.values().forEach(action);
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
