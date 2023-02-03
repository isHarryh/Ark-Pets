/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets;

import com.badlogic.gdx.Gdx;
import com.isharryh.arkpets.utils.AnimData;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.AWTException;


public class ArkTray {
    private final ArkPets arkPets;
    private final SystemTray tray;
    private final TrayIcon icon;
    private final JDialog popWindow;
    private final JPopupMenu pop;
    public String name;
    public AnimData keepAnim;

    /** Initialize the ArkPets tray icon instance. <br/>
     * Must be used after Gdx.app was initialized.
     * @param boundArkPets The ArkPets instance that bound to the tray icon.
     */
    public ArkTray(ArkPets boundArkPets) {
        arkPets = boundArkPets;
        tray = SystemTray.getSystemTray();
        try {
            name = arkPets.config.character_recent.substring(arkPets.config.character_recent.lastIndexOf("_") + 1);
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
        } catch (Exception e) {
            name = "Unknown";
        }

        // Load tray icon image.
        Image image = Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("icon.png"));
        icon = new TrayIcon(image,  name + " - ArkPets");
        icon.setImageAutoSize(true);

        popWindow = new JDialog(); // JDialog is the container of JPopupMenu.
        popWindow.setUndecorated(true);
        popWindow.setSize(1, 1);

        pop = new JPopupMenu() {
            @Override
            public void firePopupMenuWillBecomeInvisible() {
                popWindow.setVisible(false); // Hide the JDialog when JPopupMenu disappeared.
            }
        };

        // Menu options:
        JMenuItem optKeepAnimEn = new JMenuItem("保持动作");
        JMenuItem optKeepAnimDis = new JMenuItem("解除保持");
        JMenuItem optExit = new JMenuItem("退出");
        optKeepAnimEn.addActionListener(e -> {
            Gdx.app.log("info","Tray:Keep-Anim Enabled");
            keepAnim = arkPets.cha.anim_queue[0];
            pop.remove(optKeepAnimEn);
            pop.add(optKeepAnimDis, 0);
        });
        optKeepAnimDis.addActionListener(e -> {
            Gdx.app.log("info","Tray:Keep-Anim Disabled");
            keepAnim = null;
            pop.remove(optKeepAnimDis);
            pop.add(optKeepAnimEn, 0);
        });
        optExit.addActionListener(e -> {
            Gdx.app.log("info","Tray:Exit");
            Gdx.app.exit();
        });
        pop.add(optKeepAnimEn);
        pop.add(optExit);
        pop.setSize(100, 24 * pop.getSubElements().length);

        // Mouse event listener:
        icon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == 3 && e.isPopupTrigger()) {
                    // After right-click on the tray icon.
                    int x = e.getX();
                    int y = e.getY();
                    /* Use `System.setProperty("sun.java2d.uiScale", "1")` can also avoid system scaling.
                    Here we will adapt the coordinate for system scaling artificially. See below. */
                    AffineTransform at = popWindow.getGraphicsConfiguration().getDefaultTransform();
                    x /= at.getScaleX();
                    y /= at.getScaleY();
                    // Show the JDialog together with the JPopupMenu.
                    popWindow.setLocation(x + 5, y - pop.getHeight());
                    popWindow.setVisible(true);
                    pop.show(popWindow, 0, 0);
                }
            }
        });

        // Add the icon to system tray.
        try {
            tray.add(icon);
        } catch (AWTException e) {
            Gdx.app.log("warning", "Tray:Unable to apply tray icon. " + e);
            User32.INSTANCE.SetWindowLong(arkPets.HWND_MINE, WinUser.GWL_EXSTYLE, 0x00000008);
        }
    }

    /** Remove the icon from system tray.
     */
    public void remove() {
        tray.remove(icon);
        pop.removeAll();
        popWindow.dispose();
    }
}
