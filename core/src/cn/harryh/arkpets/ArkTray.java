/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.utils.AnimData;
import cn.harryh.arkpets.utils.Logger;
import com.badlogic.gdx.Gdx;

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

import static cn.harryh.arkpets.Const.*;


public class ArkTray {
    private final ArkPets arkPets;
    private final SystemTray tray;
    private final TrayIcon icon;
    private final JDialog popWindow;
    private final JPopupMenu pop;
    private boolean isDisplaying = false;
    public String name;
    public AnimData keepAnim;

    /** Initialize the ArkPets tray icon instance. <br/>
     * Must be used after Gdx.app was initialized.
     * @param boundArkPets The ArkPets instance that bound to the tray icon.
     */
    public ArkTray(ArkPets boundArkPets) {
        arkPets = boundArkPets;
        tray = SystemTray.getSystemTray();
        name = (arkPets.config.character_label == null || arkPets.config.character_label.isEmpty()) ? "Unknown" : arkPets.config.character_label;
        name = name + " - " + appName;

        // Load tray icon image.
        Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource(iconFilePng));
        icon = new TrayIcon(image, name);
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
        JMenuItem optKeepAnimDis = new JMenuItem("取消保持");
        JMenuItem optTransparentEn = new JMenuItem("透明模式");
        JMenuItem optTransparentDis = new JMenuItem("取消透明");
        JMenuItem optExit = new JMenuItem("退出");
        optKeepAnimEn.addActionListener(e -> {
            Logger.info("Tray", "Keep-Anim enabled");
            keepAnim = arkPets.cha.anim_queue[0];
            pop.remove(optKeepAnimEn);
            pop.add(optKeepAnimDis, 0);
        });
        optKeepAnimDis.addActionListener(e -> {
            Logger.info("Tray","Keep-Anim disabled");
            keepAnim = null;
            pop.remove(optKeepAnimDis);
            pop.add(optKeepAnimEn, 0);
        });
        optTransparentEn.addActionListener(e -> {
            Logger.info("Tray", "Transparent enabled");
            arkPets.setWindowAlphaTar(0.75f);
            arkPets.setWindowTransparent(true);
            pop.remove(optTransparentEn);
            pop.add(optTransparentDis, 1);
        });
        optTransparentDis.addActionListener(e -> {
            Logger.info("Tray", "Transparent disabled");
            arkPets.setWindowAlphaTar(1);
            arkPets.setWindowTransparent(false);
            pop.remove(optTransparentDis);
            pop.add(optTransparentEn, 1);
        });
        optExit.addActionListener(e -> {
            Logger.info("Tray","Request to exit");
            arkPets.setWindowAlphaTar(0);
            remove();
            try {
                Thread.sleep((long)(linearEasingDuration * 1000));
                Gdx.app.exit();
            } catch (InterruptedException ignored) {
            }
        });
        pop.add(optKeepAnimEn);
        pop.add(optTransparentEn);
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
            isDisplaying = true;
            Logger.info("Tray", "Tray icon applied, named \"" + name + "\"");
        } catch (AWTException e) {
            Logger.error("Tray", "Unable to apply tray icon, details see below", e);
        }
    }

    /** Remove the icon from system tray.
     */
    public void remove() {
        if (isDisplaying) {
            tray.remove(icon);
            pop.removeAll();
            popWindow.dispose();
        }
        isDisplaying = false;
    }
}
