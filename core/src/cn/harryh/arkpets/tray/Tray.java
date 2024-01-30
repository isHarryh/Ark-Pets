package cn.harryh.arkpets.tray;

import javax.swing.*;
import java.util.UUID;

public abstract class Tray {
    protected JMenuItem optKeepAnimEn = new JMenuItem("保持动作");
    protected JMenuItem optKeepAnimDis = new JMenuItem("取消保持");
    protected JMenuItem optTransparentEn = new JMenuItem("透明模式");
    protected JMenuItem optTransparentDis = new JMenuItem("取消透明");
    protected JMenuItem optChangeStage = new JMenuItem("切换形态");
    protected JMenuItem optExit = new JMenuItem("退出");
    protected final JDialog popWindow;
    protected final JPopupMenu popMenu;
    protected final UUID uuid;

    public Tray(UUID uuid) {
        this.uuid = uuid;
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
    }

    protected abstract void addComponent();
}
