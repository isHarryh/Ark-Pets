package cn.harryh.arkpets.socket;

import cn.harryh.arkpets.tray.ClientTrayHandler;
import cn.harryh.arkpets.utils.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class InteriorSocketServer {
    private final int port;
    private static final List<Socket> clientSockets = new ArrayList<>();
    private static final List<ClientTrayHandler> clientHandlers = new ArrayList<>();
    private static final ExecutorService executorService =
            new ThreadPoolExecutor(20, Integer.MAX_VALUE,
                    60L, TimeUnit.SECONDS, new SynchronousQueue<>(),
                    r -> {
                        Thread thread = Executors.defaultThreadFactory().newThread(r);
                        thread.setDaemon(true);
                        return thread;
                    });
    private ServerSocket serverSocket;
    private static volatile boolean mainThreadExitFlag = false;

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
                    ClientTrayHandler clientTrayHandler = new ClientTrayHandler(clientSocket, this);
                    clientHandlers.add(clientTrayHandler);
                    executorService.execute(clientTrayHandler);
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
        clientHandlers.forEach(ClientTrayHandler::stopThread);
        executorService.shutdown();
    }

    public void removeClientSocket(Socket socket) {
        clientSockets.remove(socket);
    }

    public void removeClientHandler(ClientTrayHandler handler) {
        clientHandlers.remove(handler);
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
}
