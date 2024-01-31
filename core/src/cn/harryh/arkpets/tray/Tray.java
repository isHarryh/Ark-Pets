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
    protected final UUID uuid;
    protected String name;

    public Tray(UUID uuid) {
        this.uuid = uuid;
        // This Dialog is the container (the "anchor") of the PopupMenu:
        optKeepAnimEn.addActionListener(e -> optKeepAnimEnHandler());
        optKeepAnimDis.addActionListener(e -> optKeepAnimDisHandler());
        optTransparentEn.addActionListener(e -> optTransparentEnHandler());
        optTransparentDis.addActionListener(e -> optTransparentDisHandler());
        optChangeStage.addActionListener(e -> optChangeStageHandler());
        optExit.addActionListener(e -> optExitHandler());
    }

    protected abstract void addComponent();

    protected abstract void optExitHandler();

    protected abstract void optChangeStageHandler();

    protected abstract void optTransparentDisHandler();

    protected abstract void optTransparentEnHandler();

    protected abstract void optKeepAnimDisHandler();

    protected abstract void optKeepAnimEnHandler();
    public abstract void removeTray();
}
