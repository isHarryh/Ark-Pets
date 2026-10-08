/** Copyright (c) 2022-2026, Harry Huang, Half Nothing
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.tray;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.concurrent.SocketData;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;


public abstract class MemberTray {
    protected JMenuItem optKeepAnimEn;
    protected JMenuItem optKeepAnimDis;
    protected JMenuItem optTransparentEn;
    protected JMenuItem optTransparentDis;
    protected JMenuItem optChangeStage;
    protected JMenuItem optExit;
    protected final UUID uuid;
    protected final String name;

    static {
        Runnable init = new Runnable() { // DO NOT USE LAMBDA IN STATIC
            @Override
            public void run() {
                String laf = UIManager.getSystemLookAndFeelClassName();
                if (laf.contains("WindowsLookAndFeel")) {
                    UIManager.put("MenuItem.margin", new Insets(0, -18, 0, 0));
                    UIManager.put("Menu.margin", new Insets(0, -18, 0, 0));
                }
                try {
                    UIManager.setLookAndFeel(laf);
                } catch (Exception ignored) {
                }
                Const.FontsConfig.REGULAR.loadFontToSwing();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            // The class may be initialized on the EDT, where invokeAndWait is not allowed.
            init.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(init);
            } catch (Exception ignored) {
            }
        }
    }

    /** Initializes a tray icon instance for a ArkPets.
     * @param name The name to be displayed in the menu, in the icon tooltip, etc.
     */
    public MemberTray(String name) {
        this.uuid = UUID.randomUUID();
        this.name = name;

        // Ui Components must be created on the EDT. Creating them synchronously guarantees
        // that the fields are non-null once the constructor returns.
        runOnEdtAndWait(() -> {
            optKeepAnimEn       = new JMenuItem("手动模式");
            optKeepAnimDis      = new JMenuItem("退出手动");
            optTransparentEn    = new JMenuItem("透明模式");
            optTransparentDis   = new JMenuItem("取消透明");
            optChangeStage      = new JMenuItem("切换形态");
            optExit             = new JMenuItem("退出");

            optKeepAnimEn.addActionListener(e -> onKeepAnimEn());
            optKeepAnimDis.addActionListener(e -> onKeepAnimDis());
            optTransparentEn.addActionListener(e -> onTransparentEn());
            optTransparentDis.addActionListener(e -> onTransparentDis());
            optChangeStage.addActionListener(e -> onChangeStage());
            optExit.addActionListener(e -> onExit());

            optKeepAnimEn.addActionListener(e -> sendOperation(SocketData.Operation.KEEP_ACTION));
            optKeepAnimDis.addActionListener(e -> sendOperation(SocketData.Operation.NO_KEEP_ACTION));
            optTransparentEn.addActionListener(e -> sendOperation(SocketData.Operation.TRANSPARENT_MODE));
            optTransparentDis.addActionListener(e -> sendOperation(SocketData.Operation.NO_TRANSPARENT_MODE));
            optChangeStage.addActionListener(e -> sendOperation(SocketData.Operation.CHANGE_STAGE));
            optExit.addActionListener(e -> sendOperation(SocketData.Operation.LOGOUT));

            optKeepAnimEn.setIcon(null);
            optKeepAnimDis.setIcon(null);
            optTransparentEn.setIcon(null);
            optTransparentDis.setIcon(null);
            optChangeStage.setIcon(null);
            optExit.setIcon(null);
        });
    }

    /** Runs the given action on the Event Dispatch Thread (EDT).
     * @param action The action to run.
     */
    protected static void runOnEdt(Runnable action) {
        if (SwingUtilities.isEventDispatchThread())
            action.run();
        else
            SwingUtilities.invokeLater(action);
    }

    /** Runs the given action on the Event Dispatch Thread (EDT) and waits for it to finish.
     * @param action The action to run.
     */
    protected static void runOnEdtAndWait(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(action);
            } catch (Exception ignored) {
            }
        }
    }

    abstract public void onExit();

    abstract public void onChangeStage();

    abstract public void onTransparentDis();

    abstract public void onTransparentEn();

    abstract public void onKeepAnimDis();

    abstract public void onKeepAnimEn();

    abstract public void remove();

    abstract public void sendOperation(SocketData.Operation operation);
}
