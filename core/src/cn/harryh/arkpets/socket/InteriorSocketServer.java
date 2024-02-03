package cn.harryh.arkpets.socket;

import cn.harryh.arkpets.exception.NoPortAvailableException;
import cn.harryh.arkpets.exception.ServerRunningException;
import cn.harryh.arkpets.tray.ClientTrayHandler;
import cn.harryh.arkpets.utils.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static cn.harryh.arkpets.utils.IOUtils.NetUtils.getAvailablePort;


public class InteriorSocketServer {
    private int port;
    private boolean checked = false;
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
    private ServerSocket serverSocket = null;
    private static Thread mainThread;
    private static InteriorSocketServer instance = null;

    public static ExecutorService getThreadPool() {
        return executorService;
    }

    public static InteriorSocketServer getInstance() {
        if (instance == null) {
            instance = new InteriorSocketServer();
        }
        return instance;
    }

    public int checkServerAvailable() {
        try {
            this.port = getAvailablePort();
            checked = true;
            return 1;
        } catch (NoPortAvailableException e) {
            return 0;
        } catch (ServerRunningException e) {
            return -1;
        }
    }

    private InteriorSocketServer() {
    }

    public synchronized void startServer() {
        if (!checked)
            return;
        mainThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                Logger.info("Socket", "Server is running on port " + port);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    clientSockets.add(clientSocket);
                    Logger.info("Socket", "New client connection from %s:%d".formatted(clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort()));
                    ClientTrayHandler clientTrayHandler = new ClientTrayHandler(clientSocket);
                    clientHandlers.add(clientTrayHandler);
                    if (executorService.isShutdown())
                        break;
                    executorService.execute(clientTrayHandler);
                }
                Logger.info("Socket", "Server stop");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        executorService.execute(mainThread);
    }

    public synchronized void stopServer() {
        if (!checked)
            return;
        mainThread.interrupt();
        clientHandlers.forEach(ClientTrayHandler::stopThread);
        executorService.shutdown();
    }

    public void removeClientSocket(Socket socket) {
        if (!checked)
            return;
        clientSockets.remove(socket);
    }

    public void removeClientHandler(ClientTrayHandler handler) {
        if (!checked)
            return;
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
