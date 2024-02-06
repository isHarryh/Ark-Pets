/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.tray;

import cn.harryh.arkpets.ArkPets;
import cn.harryh.arkpets.animations.AnimData;
import cn.harryh.arkpets.concurrent.SocketClient;
import cn.harryh.arkpets.concurrent.SocketData;
import cn.harryh.arkpets.utils.Logger;
import com.badlogic.gdx.Gdx;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static cn.harryh.arkpets.Const.iconFilePng;
import static cn.harryh.arkpets.Const.linearEasingDuration;


public class MemberTrayImpl extends MemberTray {
    private final ArkPets arkPets;
    private final SocketClient socketClient;
    private final JDialog popWindow;
    private final JPopupMenu popMenu;
    private TrayIcon icon;
    public AnimData keepAnim;

    /** Initializes the ArkPets tray icon instance. <br/>
     * Must be used after Gdx.app was initialized.
     * @param boundArkPets The ArkPets instance that bound to the tray icon.
     */
    public MemberTrayImpl(ArkPets boundArkPets, SocketClient socketClient, UUID uuid) {
        super(uuid, getName(boundArkPets));
        arkPets = boundArkPets;
        this.socketClient = socketClient;

        // Ui Components:
        popWindow = new JDialog();
        popWindow.setUndecorated(true);
        popWindow.setSize(1, 1);
        JLabel innerLabel = new JLabel(" " + name + " ");
        innerLabel.setAlignmentX(0.5f);

        popMenu = new JPopupMenu() {
            @Override
            public void firePopupMenuWillBecomeInvisible() {
                popWindow.setVisible(false); // Hide the container when the menu is invisible.
            }
        };
        popMenu.add(innerLabel);
        popMenu.add(optKeepAnimEn);
        popMenu.add(optTransparentEn);
        if (arkPets.canChangeStage())
            popMenu.add(optChangeStage);
        popMenu.add(optExit);
        popMenu.setSize(100, 24 * popMenu.getSubElements().length);

        socketClient.connectWithRetry(() -> {
            socketClient.setHandler(new SocketClient.ClientSocketSession(socketClient, this));
            socketClient.sendRequest(new SocketData(this.uuid, SocketData.Operation.LOGIN, name, arkPets.canChangeStage()));
        });
    }

    private static String getName(ArkPets boundArkPets) {
        return (boundArkPets.config.character_label == null || boundArkPets.config.character_label.isEmpty()) ?
                "Unknown" : boundArkPets.config.character_label;
    }

    private TrayIcon getTrayIcon(Image image) {
        if (icon == null) {
            icon = new TrayIcon(image, name);
            icon.setImageAutoSize(true);
            icon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.getButton() == 3 && e.isPopupTrigger())
                        showDialog(e.getX() + 5, e.getY());
                }
            });
        }
        return icon;
    }

    @Override
    public void onExit() {
        Logger.info("MemberTray", "Request to exit");
        arkPets.windowAlpha.reset(0f);
        remove();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Gdx.app.exit();
            }
        }, (int)(linearEasingDuration * 1000));
    }

    @Override
    public void onChangeStage() {
        Logger.info("MemberTray", "Request to change stage");
        arkPets.changeStage();
        if (keepAnim != null) {
            keepAnim = null;
            popMenu.remove(optKeepAnimDis);
            popMenu.add(optKeepAnimEn, 1);
        }
    }

    @Override
    public void onTransparentDis() {
        Logger.info("MemberTray", "Transparent disabled");
        arkPets.windowAlpha.reset(1f);
        arkPets.hWndMine.setWindowTransparent(false);
        popMenu.remove(optTransparentDis);
        popMenu.add(optTransparentEn, 2);
    }

    @Override
    public void onTransparentEn() {
        Logger.info("MemberTray", "Transparent enabled");
        arkPets.windowAlpha.reset(0.75f);
        arkPets.hWndMine.setWindowTransparent(true);
        popMenu.remove(optTransparentEn);
        popMenu.add(optTransparentDis, 2);
    }

    @Override
    public void onKeepAnimDis() {
        Logger.info("MemberTray", "Keep-Anim disabled");
        keepAnim = null;
        popMenu.remove(optKeepAnimDis);
        popMenu.add(optKeepAnimEn, 1);
    }

    @Override
    public void onKeepAnimEn() {
        Logger.info("MemberTray", "Keep-Anim enabled");
        keepAnim = arkPets.cha.getPlaying();
        popMenu.remove(optKeepAnimEn);
        popMenu.add(optKeepAnimDis, 1);
    }

    @Override
    protected void sendRequest(SocketData.Operation operation) {
        socketClient.sendRequest(new SocketData(uuid, operation));
    }

    @Override
    public void remove() {
        popMenu.removeAll();
        popWindow.dispose();
        socketClient.disconnect();
    }

    public void onDisconnected() {
        // When connection was broken:
        Logger.info("MemberTray", "Integrated tray service disconnected");
        Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource(iconFilePng));
        TrayIcon icon = getTrayIcon(image);

        // Add the ISOLATED tray icon to the system tray.
        try {
            SystemTray.getSystemTray().add(icon);
            Logger.info("MemberTray", "Isolated tray icon applied");
        } catch (AWTException e) {
            Logger.error("MemberTray", "Unable to apply isolated tray icon, details see below", e);
        }
    }

    public void onReconnected() {
        // If integration was succeeded, remove the ISOLATED tray icon.
        Logger.info("MemberTray", "Integrated tray service reconnected");
        SystemTray.getSystemTray().remove(icon);
        socketClient.sendRequest(new SocketData(this.uuid, SocketData.Operation.LOGIN, name, arkPets.canChangeStage()));
        for (MenuElement element : popMenu.getSubElements()) {
            if (element.equals(optKeepAnimDis))
                sendRequest(SocketData.Operation.KEEP_ACTION);
            if (element.equals(optTransparentDis))
                sendRequest(SocketData.Operation.TRANSPARENT_MODE);
        }
    }

    /** Hides the menu.
     */
    public void hideDialog() {
        if (popMenu.isVisible()) {
            popMenu.setVisible(false);
            Logger.debug("MemberTray", "Hidden");
        }
    }

    /** Shows the menu at the given coordinate.
     */
    public void showDialog(int x, int y) {
        /* Use `System.setProperty("sun.java2d.uiScale", "1")` can also avoid system scaling.
        Here we will adapt the coordinate for system scaling artificially. See below. */
        AffineTransform at = popWindow.getGraphicsConfiguration().getDefaultTransform();
        int scaledX = (int) (x / at.getScaleX());
        int scaledY = (int) (y / at.getScaleY());

        // Show the JDialog together with the JPopupMenu.
        popWindow.setVisible(true);
        popWindow.setLocation(scaledX, scaledY - popMenu.getHeight());
        popMenu.show(popWindow, 0, 0);
        Logger.debug("MemberTray", "Shown @ " + x + ", " + y);
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
