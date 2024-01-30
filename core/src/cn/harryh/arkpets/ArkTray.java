/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.animations.AnimData;
import cn.harryh.arkpets.tray.SocketClient;
import cn.harryh.arkpets.tray.Tray;
import cn.harryh.arkpets.tray.model.SocketData;
import cn.harryh.arkpets.utils.Logger;
import com.badlogic.gdx.Gdx;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

import static cn.harryh.arkpets.Const.fontFileRegular;
import static cn.harryh.arkpets.Const.linearEasingDuration;


public class ArkTray extends Tray {
    private final ArkPets arkPets;
    private final SocketClient socketClient;
    public String name;
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
    public ArkTray(ArkPets boundArkPets, SocketClient socket, UUID uuid) {
        super(uuid);
        arkPets = boundArkPets;
        socketClient = socket;
        name = (arkPets.config.character_label == null || arkPets.config.character_label.isEmpty()) ? "Unknown" : arkPets.config.character_label;
        SocketData socketData = new SocketData(this.uuid, SocketData.OperateType.LOGIN, name);
        socketClient.sendRequest(socketData);
        addComponent();
    }

    @Override
    protected void addComponent() {
        JLabel innerLabel = new JLabel(" " + name + " ");
        innerLabel.setAlignmentX(0.5f);
        popMenu.add(innerLabel);

        // Menu options:
        optKeepAnimEn.addActionListener(e -> {
            Logger.info("Tray", "Keep-Anim enabled");
            keepAnim = arkPets.cha.getPlaying();
            SocketData data = new SocketData(uuid, SocketData.OperateType.KEEP_ACTION);
            socketClient.sendRequest(data);
            popMenu.remove(optKeepAnimEn);
            popMenu.add(optKeepAnimDis, 1);
        });
        optKeepAnimDis.addActionListener(e -> {
            Logger.info("Tray","Keep-Anim disabled");
            keepAnim = null;
            SocketData data = new SocketData(uuid, SocketData.OperateType.NO_KEEP_ACTION);
            socketClient.sendRequest(data);
            popMenu.remove(optKeepAnimDis);
            popMenu.add(optKeepAnimEn, 1);
        });
        optTransparentEn.addActionListener(e -> {
            Logger.info("Tray", "Transparent enabled");
            arkPets.windowAlpha.reset(0.75f);
            arkPets.hWndMine.setWindowTransparent(true);
            SocketData data = new SocketData(uuid, SocketData.OperateType.TRANSPARENT_MODE);
            socketClient.sendRequest(data);
            popMenu.remove(optTransparentEn);
            popMenu.add(optTransparentDis, 2);
        });
        optTransparentDis.addActionListener(e -> {
            Logger.info("Tray", "Transparent disabled");
            arkPets.windowAlpha.reset(1f);
            arkPets.hWndMine.setWindowTransparent(false);
            SocketData data = new SocketData(uuid, SocketData.OperateType.NO_TRANSPARENT_MODE);
            socketClient.sendRequest(data);
            popMenu.remove(optTransparentDis);
            popMenu.add(optTransparentEn, 2);
        });
        optChangeStage.addActionListener(e -> {
            Logger.info("Tray","Request to change stage");
            arkPets.changeStage();
            if (keepAnim != null) {
                keepAnim = null;
                SocketData data = new SocketData(uuid, SocketData.OperateType.CHANGE_STAGE);
                socketClient.sendRequest(data);
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
    }

    public void removeTray() {
        SocketData data = new SocketData(uuid, SocketData.OperateType.LOGOUT);
        socketClient.sendRequest(data);
        popMenu.removeAll();
        popWindow.dispose();
        socketClient.disconnect();
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
