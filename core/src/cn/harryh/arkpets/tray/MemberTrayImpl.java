/** Copyright (c) 2022-2025, Harry Huang, Half Nothing
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.tray;

import cn.harryh.arkpets.ArkPets;
import cn.harryh.arkpets.animations.AnimData;
import cn.harryh.arkpets.concurrent.SocketClient;
import cn.harryh.arkpets.concurrent.SocketData;
import cn.harryh.arkpets.concurrent.SocketSession;
import cn.harryh.arkpets.utils.Logger;
import com.badlogic.gdx.Gdx;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.Timer;
import java.util.TimerTask;

import static cn.harryh.arkpets.Const.durationNormal;
import static cn.harryh.arkpets.Const.iconFilePng;


public class MemberTrayImpl extends MemberTray {
    private final ArkPets arkPets;
    private final SocketClient client;
    private final JDialog popWindow;
    private final JPopupMenu popMenu;
    private TrayIcon icon;
    public AnimData keepAnim;

    /** Initializes a per-character tray icon instance for an ArkPets. <br/>
     * Must be used after Gdx.app was initialized.
     * @param boundArkPets The ArkPets instance that bound to the tray icon.
     * @param client The socket client that bound to the tray icon.
     */
    public MemberTrayImpl(ArkPets boundArkPets, SocketClient client) {
        super(getName(boundArkPets));
        arkPets = boundArkPets;
        this.client = client;

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

        Runnable onConnected = this::onConnected;
        SocketSession session = new SocketClient.ClientSocketSession(client, this);
        client.connect(onConnected, session);
        if (!client.isConnected()) {
            onDisconnected();
            client.connectWithRetry(onConnected, session);
        }
    }

    private static String getName(ArkPets boundArkPets) {
        return (boundArkPets.config.character_label == null || boundArkPets.config.character_label.isEmpty()) ?
                "Unknown" : boundArkPets.config.character_label;
    }

    private TrayIcon getTrayIcon(Image image) {
        icon = new TrayIcon(image, name);
        icon.setImageAutoSize(true);
        icon.addMouseListener(new MouseAdapter() {
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
        return icon;
    }

    @Override
    public void onExit() {
        Logger.info("MemberTray", "Request to exit");
        remove();
        client.disconnect();
        arkPets.cha.setAlpha(0f);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Gdx.app.exit();
            }
        }, (int)durationNormal.toMillis());
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
        arkPets.setTransparentMode(false);
        popMenu.remove(optTransparentDis);
        popMenu.add(optTransparentEn, 2);
    }

    @Override
    public void onTransparentEn() {
        Logger.info("MemberTray", "Transparent enabled");
        arkPets.setTransparentMode(true);
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
    public void sendOperation(SocketData.Operation operation) {
        client.sendRequest(SocketData.ofOperation(uuid, operation));
    }

    @Override
    public void remove() {
        popMenu.removeAll();
        popWindow.dispose();
        client.disconnect();
    }

    public void onConnected() {
        // If integration was succeeded, remove the ISOLATED tray icon.
        Logger.info("MemberTray", "Integrated tray service connected");
        SystemTray.getSystemTray().remove(icon);
        client.sendRequest(SocketData.ofLogin(uuid, name));
        if (arkPets.canChangeStage())
            sendOperation(SocketData.Operation.CAN_CHANGE_STAGE);
        for (MenuElement element : popMenu.getSubElements()) {
            if (element.equals(optKeepAnimDis))
                sendOperation(SocketData.Operation.KEEP_ACTION);
            if (element.equals(optTransparentDis))
                sendOperation(SocketData.Operation.TRANSPARENT_MODE);
        }
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

    /** Hides the menu.
     */
    public synchronized void hideDialog() {
        if (popMenu.isVisible()) {
            popMenu.setVisible(false);
            Logger.debug("MemberTray", "Hidden");
        }
    }

    /** Shows the menu at the given coordinate.
     */
    public synchronized void showDialog(int x, int y) {
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
