/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.animations.AnimData;
import cn.harryh.arkpets.utils.Logger;
import com.badlogic.gdx.Gdx;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static cn.harryh.arkpets.Const.*;


public class ArkTray {
    private final ArkPets arkPets;
    private final SystemTray tray;
    private final TrayIcon icon;
    private final JDialog popWindow;
    private final JPopupMenu popMenu;
    private boolean isTrayIconApplied;
    public String name;
    public String title;
    public AnimData keepAnim;
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

    /** Initializes the ArkPets tray icon instance. <br/>
     * Must be used after Gdx.app was initialized.
     * @param boundArkPets The ArkPets instance that bound to the tray icon.
     */
    public ArkTray(ArkPets boundArkPets) {
        arkPets = boundArkPets;
        tray = SystemTray.getSystemTray();
        name = (arkPets.config.character_label == null || arkPets.config.character_label.isEmpty()) ? "Unknown" : arkPets.config.character_label;
        title = name + " - " + appName;

        // Load the tray icon image.
        Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource(iconFilePng));
        icon = new TrayIcon(image, name);
        icon.setImageAutoSize(true);

        // This Dialog is the container (the "anchor") of the PopupMenu:
        popWindow = new JDialog();
        popWindow.setUndecorated(true);
        popWindow.setSize(1, 1);

        // PopupMenu:
        popMenu = new JPopupMenu() {
            @Override
            public void firePopupMenuWillBecomeInvisible() {
                popWindow.setVisible(false); // Hide the container when the menu is invisible.
            }
        };
        JLabel innerLabel = new JLabel(" " + name + " ");
        innerLabel.setAlignmentX(0.5f);
        popMenu.add(innerLabel);

        // Menu options:
        JMenuItem optKeepAnimEn = new JMenuItem("保持动作");
        JMenuItem optKeepAnimDis = new JMenuItem("取消保持");
        JMenuItem optTransparentEn = new JMenuItem("透明模式");
        JMenuItem optTransparentDis = new JMenuItem("取消透明");
        JMenuItem optChangeStage = new JMenuItem("切换形态");
        JMenuItem optExit = new JMenuItem("退出");
        optKeepAnimEn.addActionListener(e -> {
            Logger.info("Tray", "Keep-Anim enabled");
            keepAnim = arkPets.cha.getPlaying();
            popMenu.remove(optKeepAnimEn);
            popMenu.add(optKeepAnimDis, 1);
        });
        optKeepAnimDis.addActionListener(e -> {
            Logger.info("Tray","Keep-Anim disabled");
            keepAnim = null;
            popMenu.remove(optKeepAnimDis);
            popMenu.add(optKeepAnimEn, 1);
        });
        optTransparentEn.addActionListener(e -> {
            Logger.info("Tray", "Transparent enabled");
            arkPets.setWindowAlphaTar(0.75f);
            arkPets.setWindowTransparent(true);
            popMenu.remove(optTransparentEn);
            popMenu.add(optTransparentDis, 2);
        });
        optTransparentDis.addActionListener(e -> {
            Logger.info("Tray", "Transparent disabled");
            arkPets.setWindowAlphaTar(1);
            arkPets.setWindowTransparent(false);
            popMenu.remove(optTransparentDis);
            popMenu.add(optTransparentEn, 2);
        });
        optChangeStage.addActionListener(e -> {
            Logger.info("Tray","Request to change stage");
            arkPets.changeStage();
            if (keepAnim != null) {
                keepAnim = null;
                popMenu.remove(optKeepAnimDis);
                popMenu.add(optKeepAnimEn, 1);
            }
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
        popMenu.add(optKeepAnimEn);
        popMenu.add(optTransparentEn);
        if (arkPets.canChangeStage()) popMenu.add(optChangeStage);
        popMenu.add(optExit);
        popMenu.setSize(100, 24 * popMenu.getSubElements().length);

        // Mouse event listener:
        icon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == 3 && e.isPopupTrigger()) {
                    // After right-click on the tray icon.
                    int x = e.getX();
                    int y = e.getY();
                    showDialog(x + 5, y);
                }
            }
        });

        // Add the icon to the system tray.
        try {
            tray.add(icon);
            isTrayIconApplied = true;
            Logger.info("Tray", "Tray icon applied, titled \"" + title + "\"");
        } catch (AWTException e) {
            isTrayIconApplied = false;
            Logger.error("Tray", "Unable to apply tray icon, details see below", e);
        }
    }

    /** Removes the icon from system tray.
     */
    public void remove() {
        if (isTrayIconApplied) {
            tray.remove(icon);
            popMenu.removeAll();
            popWindow.dispose();
        }
        isTrayIconApplied = false;
    }

    /** Hides the menu.
     */
    public void hideDialog() {
        if (popMenu.isVisible()) {
            popMenu.setVisible(false);
            Logger.debug("Tray", "Hidden");
        }
    }

    /** Shows the menu at the given coordinate.
     */
    public void showDialog(int x, int y) {
        /* Use `System.setProperty("sun.java2d.uiScale", "1")` can also avoid system scaling.
        Here we will adapt the coordinate for system scaling artificially. See below. */
        AffineTransform at = popWindow.getGraphicsConfiguration().getDefaultTransform();
        int scaledX = (int)(x / at.getScaleX());
        int scaledY = (int)(y / at.getScaleY());

        // Show the JDialog together with the JPopupMenu.
        popWindow.setVisible(true);
        popWindow.setLocation(scaledX, scaledY - popMenu.getHeight());
        popMenu.show(popWindow, 0, 0);
        Logger.debug("Tray", "Shown @ " + x + ", " + y);
    }

    /** Toggles the menu at the given coordinate.
     */
    public void toggleDialog(int x, int y) {
        if (popMenu.isVisible()) {
            hideDialog();
        } else {
            showDialog(x, y);
        }
    }
}
