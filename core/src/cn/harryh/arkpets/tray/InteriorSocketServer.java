package cn.harryh.arkpets.tray;

import cn.harryh.arkpets.tray.model.SocketData;
import cn.harryh.arkpets.utils.Logger;
import com.alibaba.fastjson2.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InteriorSocketServer {
    private final int port;
    private static List<Socket> clientSockets = new ArrayList<>();
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private ServerSocket serverSocket;
    private static boolean mainThreadExitFlag = false;

    public InteriorSocketServer(int port) {
        this.port = port;
    }

    public synchronized void startServer() {
        executorService.execute(() -> {
            try {
                serverSocket = new ServerSocket(port);
                Logger.info("Socket", "Server is running on port " + port);
                while (!mainThreadExitFlag) {
                    Socket clientSocket = serverSocket.accept();
                    clientSockets.add(clientSocket);
                    Logger.info("Socket", "New client connection from %s:%d".formatted(clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort()));
                    executorService.execute(new ClientHandler(clientSocket));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public synchronized void stopServer() {
        mainThreadExitFlag = true;
        clientSockets.forEach(socket -> {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        executorService.shutdown();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("InteriorSocketServer:\n");
        stringBuilder.append("\tlisten: 0.0.0.0:").append(port).append("\n");
        stringBuilder.append("\tclients: ");
        if (clientSockets.isEmpty()) {
            return stringBuilder.append("None").toString();
        }
        stringBuilder.append("\n");
        for (Socket socket : clientSockets) {
            stringBuilder
                    .append("\t\t")
                    .append(socket.getInetAddress().getHostAddress())
                    .append(":")
                    .append(socket.getPort())
                    .append("\n");
        }
        return stringBuilder.toString();
    }

    static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final static SystemTrayManager systemTrayManager = SystemTrayManager.getInstance();

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
            ) {
                while (!mainThreadExitFlag) {
                    String request = in.readLine();
                    if (request == null)
                        break;
                    SocketData socketData = JSONObject.parseObject(request, SocketData.class);
                    switch (socketData.operateType) {
                        case LOGIN -> systemTrayManager.addTray(socketData.uuid);
                        case LOGOUT -> systemTrayManager.removeTray(socketData.uuid);
                    }
                    socketData = new SocketData(socketData.uuid, SocketData.OperateType.SUCCESS);
                    out.println(JSONObject.toJSONString(socketData));
                    Logger.debug("Socket", "Send data to client：" + request);
                }

                Logger.info("Socket", "Client(%s:%d) disconnected.".formatted(clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort()));
                clientSockets.remove(clientSocket);
                if (clientSocket.isClosed()) {
                    return;
                }
                clientSocket.close();
            } catch (IOException e) {
                Logger.error("Socket", e.getMessage());
            }
        }
    }

}
