package cn.harryh.arkpets.socket;

import cn.harryh.arkpets.exception.NoServerRunningException;
import cn.harryh.arkpets.utils.Logger;
import com.alibaba.fastjson2.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import static cn.harryh.arkpets.utils.IOUtils.NetUtils.getServerPort;


public class SocketClient {
    private class Task extends TimerTask {

        private final Runnable callback;

        public Task(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                Logger.info("Socket", "Searching server");
                port = getServerPort();
                Logger.info("Socket", "Server found, connecting");
                timer.cancel();
                receiveThreadBreakFlag = false;
                connect(consumer);
                callback.run();
            } catch (NoServerRunningException ignored) {
            }
        }
    }

    private final static String host = "localhost";
    private int port;
    private boolean connected = false;
    private Socket socket;
    private PrintWriter socketOut;
    private BufferedReader socketIn;
    private Thread thread = null;
    private Timer timer;
    private Consumer<SocketData> consumer;
    private volatile boolean receiveThreadBreakFlag = false;

    public SocketClient() {
        try {
            port = getServerPort();
        } catch (NoServerRunningException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void reconnect(Runnable callback) {
        timer = new Timer();
        timer.schedule(new Task(callback), 0, 5000);
    }

    public void connect() {
        if (connected) {
            return;
        }
        try {
            socket = new Socket(host, port);
            socketOut = new PrintWriter(socket.getOutputStream(), true);
            socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
        } catch (IOException e) {
            Logger.error("Socket", "Error connecting to %s:%d".formatted(host, port));
            throw new RuntimeException(e);
        }
    }

    public void connect(Consumer<SocketData> consumer) {
        connect();
        this.consumer = consumer;
        thread = new Thread(() -> {
            while (!receiveThreadBreakFlag) {
                try {
                    String receive = socketIn.readLine();
                    Logger.debug("Socket", receive);
                    consumer.accept(JSONObject.parseObject(receive, SocketData.class));
                } catch (SocketException e) {
                    consumer.accept(null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void disconnect() {
        if (!connected) {
            return;
        }
        receiveThreadBreakFlag = true;
        try {
            if (thread != null)
                thread.interrupt();
            socket.close();
            socketOut.close();
            socketIn.close();
            connected = false;
        } catch (IOException e) {
            Logger.error("Socket", "Error disconnecting to %s:%d".formatted(host, port));
            throw new RuntimeException(e);
        }
    }

    public void sendRequest(SocketData socketData) {
        if (!connected) {
            return;
        }
        String data = JSONObject.toJSONString(socketData);
        Logger.debug("SocketClient", data);
        socketOut.println(data);
    }

}
