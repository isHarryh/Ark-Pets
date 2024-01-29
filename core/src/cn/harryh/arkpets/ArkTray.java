/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.animations.AnimData;
import cn.harryh.arkpets.tray.Tray;
import cn.harryh.arkpets.utils.Logger;
import com.badlogic.gdx.Gdx;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static cn.harryh.arkpets.Const.fontFileRegular;
import static cn.harryh.arkpets.Const.linearEasingDuration;


public class ArkTray extends Tray {
    private final ArkPets arkPets;
//    private final SystemTray tray;
//    private final TrayIcon icon;
    private boolean isTrayIconApplied;
    public String name;
//    public String title;
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
        super();
        arkPets = boundArkPets;
//        tray = SystemTray.getSystemTray();
        name = (arkPets.config.character_label == null || arkPets.config.character_label.isEmpty()) ? "Unknown" : arkPets.config.character_label;
//        title = name + " - " + appName;

        // Load the tray icon image.
//        Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource(iconFilePng));
//        icon = new TrayIcon(image, name);
//        icon.setImageAutoSize(true);

        JLabel innerLabel = new JLabel(" " + name + " ");
        innerLabel.setAlignmentX(0.5f);
        popMenu.add(innerLabel);

        // Menu options:
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
            arkPets.windowAlpha.reset(0.75f);
            arkPets.hWndMine.setWindowTransparent(true);
            popMenu.remove(optTransparentEn);
            popMenu.add(optTransparentDis, 2);
        });
        optTransparentDis.addActionListener(e -> {
            Logger.info("Tray", "Transparent disabled");
            arkPets.windowAlpha.reset(1f);
            arkPets.hWndMine.setWindowTransparent(false);
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
            arkPets.windowAlpha.reset(0f);
            removeTray();
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
//        icon.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseReleased(MouseEvent e) {
//                if (e.getButton() == 3 && e.isPopupTrigger()) {
//                    // After right-click on the tray icon.
//                    int x = e.getX();
//                    int y = e.getY();
//                    showDialog(x + 5, y);
//                }
//            }
//        });

        // Add the icon to the system tray.
//        try {
//            tray.add(icon);
//            isTrayIconApplied = true;
//            Logger.info("Tray", "Tray icon applied, titled \"" + title + "\"");
//        } catch (AWTException e) {
//            isTrayIconApplied = false;
//            Logger.error("Tray", "Unable to apply tray icon, details see below", e);
//        }
    }

    @Override
    public void addTray(SystemTray systemTray) {

    }

    @Override
    public void removeTray(SystemTray systemTray) {

    }

    /** Removes the icon from system tray.
     */

    public void removeTray() {
        if (isTrayIconApplied) {
//            tray.remove(icon);
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
