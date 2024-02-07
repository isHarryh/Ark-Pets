package cn.harryh.arkpets.tray;

import cn.harryh.arkpets.concurrent.SocketData;
import cn.harryh.arkpets.concurrent.SocketSession;
import cn.harryh.arkpets.utils.Logger;

import javax.swing.*;


public class MemberTrayProxy extends MemberTray {
    private final SocketSession session;
    private final HostTray hostTray;
    private final JMenu popMenu;

    public MemberTrayProxy(SocketData socketData, SocketSession session, HostTray hostTray) {
        super(socketData.uuid, socketData.getMsgString());
        this.session = session;
        this.hostTray = hostTray;

        // Ui Components:
        JLabel innerLabel = new JLabel(" " + name + " ");
        innerLabel.setAlignmentX(0.5f);

        popMenu = new JMenu(name);
        popMenu.add(innerLabel);
        popMenu.add(optKeepAnimEn);
        popMenu.add(optTransparentEn);
        popMenu.add(optExit);
        popMenu.setSize(100, 24 * popMenu.getSubElements().length);

        hostTray.addMemberTray(popMenu);
    }

    public void onCanChangeStage() {
        Logger.info("ProxyTray", "Can change stage");
        popMenu.add(optChangeStage, popMenu.getSubElements().length - 2);
    }

    @Override
    public void onExit() {
        Logger.info("ProxyTray", "Request to exit");
        remove();
    }

    @Override
    public void onChangeStage() {
        Logger.info("ProxyTray", "Request to change stage");
        popMenu.remove(optKeepAnimDis);
        popMenu.add(optKeepAnimEn, 1);
    }

    @Override
    public void onTransparentDis() {
        Logger.info("ProxyTray", "Transparent disabled");
        popMenu.remove(optTransparentDis);
        popMenu.add(optTransparentEn, 2);
    }

    @Override
    public void onTransparentEn() {
        Logger.info("ProxyTray", "Transparent enabled");
        popMenu.remove(optTransparentEn);
        popMenu.add(optTransparentDis, 2);
    }

    @Override
    public void onKeepAnimDis() {
        Logger.info("ProxyTray", "Keep-Anim disabled");
        popMenu.remove(optKeepAnimDis);
        popMenu.add(optKeepAnimEn, 1);
    }

    @Override
    public void onKeepAnimEn() {
        Logger.info("ProxyTray", "Keep-Anim enabled");
        popMenu.remove(optKeepAnimEn);
        popMenu.add(optKeepAnimDis, 1);
    }

    @Override
    protected void sendOperation(SocketData.Operation operation) {
        session.send(SocketData.ofOperation(uuid, operation));
    }

    @Override
    public void remove() {
        hostTray.removeMemberTray(popMenu);
        sendOperation(SocketData.Operation.LOGOUT);
    }
}
