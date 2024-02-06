package cn.harryh.arkpets.concurrent;

import cn.harryh.arkpets.tray.MemberTrayImpl;
import cn.harryh.arkpets.utils.Logger;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static cn.harryh.arkpets.Const.*;


public class SocketClient {
    private boolean connected = false;
    private Socket socket;
    private SocketSession session;
    private Timer timer;

    public SocketClient() {
    }
    
    public void connectWithRetry(Runnable onConnected) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                connect(onConnected);
                if (connected)
                    timer.cancel();
            }
        }, 0, reconnectPeriodMillis);
    }

    public void connect(Runnable onConnected) {
        if (connected)
            return;
        try {
            int port = PortUtils.getServerPort(serverPorts);
            Logger.info("SocketClient", "Connecting to server on port" + port);
            try {
                socket = new Socket(serverHost, port);
                connected = true;
                if (onConnected != null)
                    onConnected.run();
            } catch (IOException e) {
                Logger.error("SocketClient", "Connecting to server on port " + port + "failed, details see below.", e);
            }
        } catch (PortUtils.NoServerRunningException e) {
            Logger.warn("SocketClient", "Connecting to server failed. " + e.getMessage());
        }
    }

    public void setHandler(SocketSession session) {
        if (!connected)
            throw new IllegalStateException("The socket was not yet connected.");
        if (this.session != null)
            this.session.close();
        Thread listener = new Thread(() -> ProcessPool.executorService.execute(session));
        ProcessPool.executorService.execute(listener);
        this.session = session;
    }

    public void disconnect() {
        if (!connected)
            return;
        connected = false;
        session.close();
    }

    public void sendRequest(SocketData socketData) {
        if (!connected)
            return;
        String data = JSONObject.toJSONString(socketData);
        session.send(data);
    }


    public static class ClientSocketSession extends SocketSession {
        private final SocketClient client;
        private final MemberTrayImpl memberTray;
        private UUID uuid = null;

        public ClientSocketSession(SocketClient client, MemberTrayImpl memberTray) {
            super(client.socket);
            this.client = client;
            this.memberTray = memberTray;
        }

        @Override
        public void receive(String request) {
            try {
                SocketData socketData = JSONObject.parseObject(request, SocketData.class);
                if (socketData.operation == null)
                    return;
                if (uuid == null)
                    uuid = socketData.uuid;
                if (socketData.uuid.compareTo(this.uuid) == 0) {
                    // If the connection is normal:
                    switch (socketData.operation) {
                        case LOGOUT                 -> memberTray.onExit();
                        case KEEP_ACTION            -> memberTray.onKeepAnimEn();
                        case NO_KEEP_ACTION         -> memberTray.onKeepAnimDis();
                        case TRANSPARENT_MODE       -> memberTray.onTransparentEn();
                        case NO_TRANSPARENT_MODE    -> memberTray.onTransparentDis();
                        case CHANGE_STAGE           -> memberTray.onChangeStage();
                    }
                }
            } catch (JSONException ignored) {
            }
        }

        @Override
        protected void onBroken() {
            memberTray.onDisconnected();
            client.disconnect();
            client.connectWithRetry(memberTray::onReconnected);
        }
    }
}
