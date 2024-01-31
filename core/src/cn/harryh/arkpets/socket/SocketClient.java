package cn.harryh.arkpets.socket;

import cn.harryh.arkpets.utils.Logger;
import com.alibaba.fastjson2.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class SocketClient {
    private final String host;
    private final int port;
    private boolean connected = false;
    private Socket socket;
    private PrintWriter socketOut;
    private BufferedReader socketIn;
    private volatile boolean receiveThreadBreakFlag = false;

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public SocketClient(int port) {
        this("localhost", port);
    }

    public void connect(Consumer<SocketData> consumer) {
        if (connected) {
            return;
        }
        try {
            socket = new Socket(host, port);
            socketOut = new PrintWriter(socket.getOutputStream(), true);
            socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Thread thread = new Thread(() -> {
                while (!receiveThreadBreakFlag) {
                    try {
                        consumer.accept(JSONObject.parseObject(socketIn.readLine(), SocketData.class));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
            connected = true;
        } catch (IOException e) {
            Logger.error("Socket", "Error connecting to %s:%d".formatted(host, port));
            throw new RuntimeException(e);
        }
    }

    public void disconnect() {
        if (!connected) {
            return;
        }
        receiveThreadBreakFlag = true;
        try {
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
