package cn.harryh.arkpets.concurrent;

import cn.harryh.arkpets.tray.HostTray;
import cn.harryh.arkpets.tray.MemberTray;
import cn.harryh.arkpets.tray.MemberTrayProxy;
import cn.harryh.arkpets.utils.Logger;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RejectedExecutionException;

import static cn.harryh.arkpets.Const.serverPorts;


public class SocketServer {
    private int port;
    private ServerSocket serverSocket = null;
    private final Set<ServerSocketSession> sessionList = new CopyOnWriteArraySet<>();
    private Thread listener;

    private static SocketServer instance = null;

    public static synchronized SocketServer getInstance() {
        if (instance == null)
            instance = new SocketServer();
        return instance;
    }

    private SocketServer() {
    }

    public synchronized void startServer(HostTray hostTray)
            throws PortUtils.NoPortAvailableException, PortUtils.ServerCollisionException {
        Logger.info("SocketServer", "Request to start server");
        this.port = PortUtils.getAvailablePort(serverPorts);
        listener = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                Logger.info("SocketServer", "Server is running on port " + port);
                while (!listener.isInterrupted() && !ProcessPool.executorService.isShutdown()) {
                    Socket clientSocket = serverSocket.accept();
                    ServerSocketSession session = new ServerSocketSession(clientSocket, hostTray);
                    sessionList.add(session);
                    ProcessPool.executorService.execute(session);
                    Logger.info("SocketServer", "(+)" + session + " connected");
                }
                serverSocket.close();
                Logger.info("SocketServer", "Server was stopped");
            } catch (IOException e) {
                Logger.error("SocketServer", "An unexpected error occurred while listening, details see below.", e);
            } catch (RejectedExecutionException ignored) {
            }
        });
        ProcessPool.executorService.execute(listener);
    }

    public synchronized void stopServer() {
        Logger.info("SocketServer", "Request to stop server");
        if (listener != null)
            listener.interrupt();
        sessionList.forEach(ServerSocketSession::close);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SocketServer:\n");
        stringBuilder.append("\tlisten: 0.0.0.0:").append(port).append("\n");
        stringBuilder.append("\tclients: ");
        if (sessionList.isEmpty())
            return stringBuilder.append("None").toString();
        stringBuilder.append("\n");
        sessionList.forEach(socket -> stringBuilder
                .append("\t\t")
                .append(socket.getHostAddress())
                .append(":")
                .append(socket.getPort())
                .append("\n")
        );
        return stringBuilder.toString();
    }


    public static class ServerSocketSession extends SocketSession {
        private final HostTray hostTray;
        private MemberTray tray;
        private UUID uuid = null;

        public ServerSocketSession(Socket target, HostTray hostTray) {
            super(target);
            this.hostTray = hostTray;
        }

        @Override
        public void receive(String request) {
            try {
                SocketData socketData = JSONObject.parseObject(request, SocketData.class);
                if (socketData.operation == null)
                    return;
                if (uuid == null)
                    uuid = socketData.uuid;

                switch (socketData.operation) {
                    case VERIFY -> {
                        this.send(JSONObject.toJSONString(new SocketData(uuid, SocketData.Operation.SERVER_ONLINE)));
                        close();
                    }
                    case ACTIVATE_LAUNCHER -> hostTray.showStage();
                    case LOGIN -> {
                        tray = new MemberTrayProxy(socketData, target, hostTray);
                        hostTray.addMemberTray(socketData.uuid, tray);
                    }
                    case LOGOUT -> {
                        hostTray.removeMemberTray(socketData.uuid);tray.onExit();
                        close();
                    }
                    case KEEP_ACTION -> tray.onKeepAnimEn();
                    case NO_KEEP_ACTION -> tray.onKeepAnimDis();
                    case TRANSPARENT_MODE -> tray.onTransparentEn();
                    case NO_TRANSPARENT_MODE -> tray.onTransparentDis();
                    case CHANGE_STAGE -> tray.onChangeStage();
                }
            } catch (JSONException ignored) {
            }
        }

        @Override
        protected void onClosed() {
            Logger.info("SocketServer", "(-)" + this + " closed");
            SocketServer.getInstance().sessionList.remove(this);
        }
    }
}
