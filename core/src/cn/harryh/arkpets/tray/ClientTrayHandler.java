package cn.harryh.arkpets.tray;

import cn.harryh.arkpets.socket.InteriorSocketServer;
import cn.harryh.arkpets.socket.SocketData;
import cn.harryh.arkpets.utils.Logger;
import com.alibaba.fastjson2.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.UUID;

public class ClientTrayHandler implements Runnable {
    private final Socket clientSocket;
    private Tray tray;
    private UUID uuid = null;
    private final static SystemTrayManager systemTrayManager = SystemTrayManager.getInstance();
    private volatile boolean threadExitFlag = false;

    public ClientTrayHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;

    }

    public synchronized void stopThread() {
        threadExitFlag = true;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            boolean flag = false;
            while (!threadExitFlag) {
                String request = in.readLine();
                if (request == null)
                    break;
                Logger.debug("SocketServer", request);
                SocketData socketData = JSONObject.parseObject(request, SocketData.class);
                if (uuid == null)
                    uuid = socketData.uuid;
                switch (socketData.operateType) {
                    case LOGIN -> {
                        tray = new TrayInstance(socketData.uuid, clientSocket, new String(socketData.name, "GBK"), socketData.canChangeStage);
                        systemTrayManager.addTray(socketData.uuid, tray);
                    }
                    case LOGOUT -> {
                        systemTrayManager.removeTray(socketData.uuid);
                        flag = true;
                    }
                    case KEEP_ACTION -> tray.optKeepAnimEnHandler();
                    case NO_KEEP_ACTION -> tray.optKeepAnimDisHandler();
                    case TRANSPARENT_MODE -> tray.optTransparentEnHandler();
                    case NO_TRANSPARENT_MODE -> tray.optTransparentDisHandler();
                    case CHANGE_STAGE -> tray.optChangeStageHandler();
                }
                if (flag)
                    break;
            }

            Logger.info("Socket", "Client(%s:%d) disconnected.".formatted(clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort()));
            tray.optExitHandler();
            InteriorSocketServer.getInstance().removeClientSocket(clientSocket);
            InteriorSocketServer.getInstance().removeClientHandler(this);
            if (clientSocket.isClosed()) {
                return;
            }
            clientSocket.close();
        } catch (IOException e) {
            Logger.error("Socket", e.getMessage());
            tray.optExitHandler();
            InteriorSocketServer.getInstance().removeClientSocket(clientSocket);
            InteriorSocketServer.getInstance().removeClientHandler(this);
        }
    }
}
