package cn.harryh.arkpets.tray;

import cn.harryh.arkpets.concurrent.SocketData;
import cn.harryh.arkpets.utils.Logger;
import com.alibaba.fastjson2.JSONObject;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;


public class MemberTrayProxy extends MemberTray {
    private final PrintWriter socketOut;
    private final HostTray hostTray;
    private final JMenu popMenu;

    public MemberTrayProxy(SocketData socketData, Socket clientSocket, HostTray hostTray) {
        super(socketData.uuid, new String(socketData.name, Charset.forName("GBK")));
        this.hostTray = hostTray;
        try {
            socketOut = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Ui Components:
        JLabel innerLabel = new JLabel(" " + name + " ");
        innerLabel.setAlignmentX(0.5f);

        popMenu = new JMenu(name);
        popMenu.add(innerLabel);
        popMenu.add(optKeepAnimEn);
        popMenu.add(optTransparentEn);
        if (socketData.canChangeStage)
            popMenu.add(optChangeStage);
        popMenu.add(optExit);
        popMenu.setSize(100, 24 * popMenu.getSubElements().length);

        hostTray.addMemberTray(popMenu);
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
    protected void sendRequest(SocketData.Operation operation) {
        socketOut.println(JSONObject.toJSONString(new SocketData(uuid, operation)));
    }

    @Override
    public void remove() {
        hostTray.removeMemberTray(popMenu);
        sendRequest(SocketData.Operation.LOGOUT);
        socketOut.close();
    }
}
