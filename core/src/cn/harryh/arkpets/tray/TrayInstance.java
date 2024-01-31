package cn.harryh.arkpets.tray;

import cn.harryh.arkpets.socket.SocketData;
import cn.harryh.arkpets.utils.Logger;
import com.alibaba.fastjson2.JSONObject;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;


public class TrayInstance extends Tray {
    private final PrintWriter socketOut;
    private final boolean canChangeStage;
    private final JMenu popMenu;

    public TrayInstance(UUID uuid, Socket socket, String name, boolean canChangeStage) {
        super(uuid);
        this.name = name;
        this.canChangeStage = canChangeStage;
        popMenu = new JMenu(name);
        try {
            socketOut = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        addComponent();
    }

    private void sendRequest(SocketData data) {
        socketOut.println(JSONObject.toJSONString(data));
    }

    @Override
    protected void addComponent() {
        JLabel innerLabel = new JLabel(" " + name + " ");
        innerLabel.setAlignmentX(0.5f);
        popMenu.add(innerLabel);

        optKeepAnimEn.addActionListener(e -> sendRequest(new SocketData(uuid, SocketData.OperateType.KEEP_ACTION)));
        optKeepAnimDis.addActionListener(e -> sendRequest(new SocketData(uuid, SocketData.OperateType.NO_KEEP_ACTION)));
        optTransparentEn.addActionListener(e -> sendRequest(new SocketData(uuid, SocketData.OperateType.TRANSPARENT_MODE)));
        optTransparentDis.addActionListener(e -> sendRequest(new SocketData(uuid, SocketData.OperateType.NO_TRANSPARENT_MODE)));
        optChangeStage.addActionListener(e -> sendRequest(new SocketData(uuid, SocketData.OperateType.CHANGE_STAGE)));
        optExit.addActionListener(e -> sendRequest(new SocketData(uuid, SocketData.OperateType.LOGOUT)));

        popMenu.add(optKeepAnimEn);
        popMenu.add(optTransparentEn);
        if (canChangeStage) popMenu.add(optChangeStage);
        popMenu.add(optExit);
        popMenu.setSize(100, 24 * popMenu.getSubElements().length);
        SystemTrayManager.getInstance().addTray(popMenu);
    }

    @Override
    public void removeTray() {
        SystemTrayManager.getInstance().removeTray(popMenu);
        sendRequest(new SocketData(uuid, SocketData.OperateType.LOGOUT));
        socketOut.close();
    }

    @Override
    protected void optExitHandler() {
        Logger.info("SocketTray", "Request to exit");
        removeTray();
    }

    @Override
    protected void optChangeStageHandler() {
        Logger.info("SocketTray", "Request to change stage");
        popMenu.remove(optKeepAnimDis);
        popMenu.add(optKeepAnimEn, 1);
    }

    @Override
    protected void optTransparentDisHandler() {
        Logger.info("SocketTray", "Transparent disabled");
        popMenu.remove(optTransparentDis);
        popMenu.add(optTransparentEn, 2);
    }

    @Override
    protected void optTransparentEnHandler() {
        Logger.info("SocketTray", "Transparent enabled");
        popMenu.remove(optTransparentEn);
        popMenu.add(optTransparentDis, 2);
    }

    @Override
    protected void optKeepAnimDisHandler() {
        Logger.info("SocketTray", "Keep-Anim disabled");
        popMenu.remove(optKeepAnimDis);
        popMenu.add(optKeepAnimEn, 1);
    }

    @Override
    protected void optKeepAnimEnHandler() {
        Logger.info("SocketTray", "Keep-Anim enabled");
        popMenu.remove(optKeepAnimEn);
        popMenu.add(optKeepAnimDis, 1);
    }
}
