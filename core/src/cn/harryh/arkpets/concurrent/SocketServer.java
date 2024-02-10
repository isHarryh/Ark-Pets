/** Copyright (c) 2022-2024, Harry Huang, Half Nothing
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.concurrent;

import cn.harryh.arkpets.tray.HostTray;
import cn.harryh.arkpets.tray.MemberTrayProxy;
import cn.harryh.arkpets.utils.Logger;
import com.alibaba.fastjson2.JSONException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RejectedExecutionException;

import static cn.harryh.arkpets.Const.serverPorts;


public final class SocketServer {
    private int port;
    private ServerSocket serverSocket = null;
    private final Set<SocketSession> sessionList = new CopyOnWriteArraySet<>();
    private Thread listener;

    private static SocketServer instance = null;

    public static synchronized SocketServer getInstance() {
        if (instance == null)
            instance = new SocketServer();
        return instance;
    }

    private SocketServer() {
    }

    /** Starts the server.
     * @param hostTray The bound HostTray.
     * @throws PortUtils.NoPortAvailableException If every port is busy.
     * @throws PortUtils.ServerCollisionException If a server is already running.
     */
    public synchronized void startServer(HostTray hostTray)
            throws PortUtils.NoPortAvailableException, PortUtils.ServerCollisionException {
        Logger.info("SocketServer", "Request to start server");
        this.port = PortUtils.getAvailablePort(serverPorts);
        listener = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                Logger.info("SocketServer", "Server is running on port " + port);
                while (!listener.isInterrupted()) {
                    Socket socket = serverSocket.accept();
                    SocketSession session = new ServerSocketSession(hostTray);
                    session.setTarget(socket);
                    sessionList.add(session);
                    ProcessPool.getInstance().execute(session);
                    Logger.info("SocketServer", "(+)" + session + " connected");
                }
                serverSocket.close();
                Logger.info("SocketServer", "Server was stopped");
            } catch (IOException e) {
                Logger.error("SocketServer", "An unexpected error occurred while listening, details see below.", e);
            } catch (RejectedExecutionException ignored) {
            }
        });
        ProcessPool.getInstance().execute(listener);
    }

    /** Stops the server and close all the sessions.
     */
    public synchronized void stopServer() {
        Logger.info("SocketServer", "Request to stop server");
        if (listener != null)
            listener.interrupt();
        sessionList.forEach(SocketSession::close);
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
        private MemberTrayProxy tray;
        private UUID uuid = null;

        public ServerSocketSession(HostTray hostTray) {
            super();
            this.hostTray = hostTray;
        }

        @Override
        public void receive(String request) {
            try {
                SocketData socketData = SocketData.of(request);
                if (socketData == null || socketData.operation == null)
                    return;
                if (uuid == null)
                    uuid = socketData.uuid;

                switch (socketData.operation) {
                    case HANDSHAKE_REQUEST -> {
                        this.send(SocketData.ofOperation(uuid, SocketData.Operation.HANDSHAKE_RESPONSE));
                        close();
                    }
                    case ACTIVATE_LAUNCHER -> hostTray.showStage();
                    case LOGIN -> {
                        tray = new MemberTrayProxy(socketData, this, hostTray);
                        hostTray.addMemberTray(uuid, tray);
                    }
                    case LOGOUT -> {
                        hostTray.removeMemberTray(uuid);
                        tray.onExit();
                        close();
                    }
                    case KEEP_ACTION            -> tray.onKeepAnimEn();
                    case NO_KEEP_ACTION         -> tray.onKeepAnimDis();
                    case TRANSPARENT_MODE       -> tray.onTransparentEn();
                    case NO_TRANSPARENT_MODE    -> tray.onTransparentDis();
                    case CAN_CHANGE_STAGE       -> tray.onCanChangeStage();
                    case CHANGE_STAGE           -> tray.onChangeStage();
                }
            } catch (JSONException ignored) {

            }
        }

        @Override
        protected void onClosed() {
            Logger.info("SocketServer", "(-)" + this + " closed");
            SocketServer.getInstance().sessionList.remove(this);
        }

        @Override
        protected void onBroken() {
            Logger.info("SocketServer", "(x)" + this + " broken");
            hostTray.removeMemberTray(uuid);
        }
    }
}
