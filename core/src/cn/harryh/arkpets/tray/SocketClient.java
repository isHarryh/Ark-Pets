package cn.harryh.arkpets.tray;

import cn.harryh.arkpets.tray.model.SocketData;
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

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public SocketClient(int port) {
        this("localhost", port);
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

    public void disconnect() {
        if (!connected) {
            return;
        }
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
            connect();
        }
        socketOut.println(JSONObject.toJSONString(socketData));
    }

    public void sendRequest(SocketData socketData, Consumer<SocketData> function) {
        if (!connected) {
            connect();
        }
        try {
            socketOut.println(JSONObject.toJSONString(socketData));
            function.accept(JSONObject.parseObject(socketIn.readLine(), SocketData.class));
        } catch (IOException e) {
            Logger.error("Socket", "Error receiving data from %s:%d".formatted(host, port));
            throw new RuntimeException(e);
        }
    }
}
