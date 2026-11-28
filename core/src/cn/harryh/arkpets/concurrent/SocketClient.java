/** Copyright (c) 2022-2026, Harry Huang, Half Nothing
 * At GPL-3.0 License
 */
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
    private volatile boolean connected = false;
    /** Indicates that the ongoing closure of this client was intentionally initiated
     * (either locally by {@link #disconnect()} or remotely by a logout request),
     * rather than caused by an unexpected connection break. <br/>
     * When set, the broken-session callback will neither perform the
     * disconnected-notification nor start the reconnection workflow.
     */
    private volatile boolean intentionalClose = false;
    private SocketSession session;
    private Timer timer;

    public SocketClient() {
    }

    public void connectWithRetry(Runnable onConnected, SocketSession session) {
        if (timer != null)
            timer.cancel();
        intentionalClose = false;
        Timer retryTimer = new Timer("SocketClient-Retry", true);
        timer = retryTimer;
        retryTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    connect(onConnected, session);
                                    if (connected)
                                        retryTimer.cancel();
                                }
                            },
                reconnectDelayMillis,
                reconnectDelayMillis
        );
    }

    public void connect(Runnable onConnected, SocketSession session) {
        if (connected || intentionalClose)
            return;
        try {
            int port = PortUtils.getServerPort(serverPorts);
            Logger.info("SocketClient", "Connecting to server on port " + port);
            try {
                Socket socket = new Socket(serverHost, port);
                connected = true;
                if (this.session != null)
                    this.session.close();
                session.setTarget(socket);
                ProcessPool.getInstance().execute(session);
                this.session = session;
                Logger.info("SocketClient", "(+)" + session + " connected");
                if (onConnected != null)
                    onConnected.run();
            } catch (IOException e) {
                Logger.error("SocketClient", "Connecting to server on port " + port + "failed, details see below.", e);
            }
        } catch (PortUtils.NoServerRunningException e) {
            Logger.debug("SocketClient", "Connecting to server failed. " + e.getMessage());
        }
    }

    /** Marks the ongoing closure as intentional, so that the subsequent broken-session
     * callback will neither perform the disconnected-notification nor start the
     * reconnection workflow.
     */
    public void markIntentionalClose() {
        intentionalClose = true;
    }

    public void disconnect() {
        intentionalClose = true;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (connected)
            connected = false;
        if (session != null)
            session.close();
    }

    public boolean isConnected() {
        return connected;
    }

    public void sendRequest(SocketData socketData) {
        if (connected && session != null)
            session.send(JSONObject.toJSONString(socketData));
    }


    public static class ClientSocketSession extends SocketSession {
        private final SocketClient client;
        private final MemberTrayImpl memberTray;
        private UUID uuid = null;

        public ClientSocketSession(SocketClient client, MemberTrayImpl memberTray) {
            super();
            this.client = client;
            this.memberTray = memberTray;
        }

        @Override
        public void receive(String request) {
            try {
                SocketData socketData = SocketData.of(request);
                if (socketData == null || socketData.operation == null)
                    return;
                if (uuid == null)
                    uuid = socketData.uuid;
                if (socketData.uuid.compareTo(this.uuid) == 0 && memberTray != null) {
                    // If the connection is normal:
                    switch (socketData.operation) {
                        case LOGOUT                 -> {
                            // A logout always leads to the exit of this member, so mark the
                            // upcoming closure as intentional before the exit workflow starts.
                            client.markIntentionalClose();
                            memberTray.onExit();
                        }
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
        protected void onClosed() {
            Logger.info("SocketClient", "(-)" + this + " closed");
        }

        @Override
        protected void onBroken() {
            if (client.intentionalClose) {
                Logger.info("SocketClient", "(x)" + this + " closed intentionally, skip reconnecting");
                return;
            }
            Logger.info("SocketClient", "(x)" + this + " broken");
            memberTray.onDisconnected();
            client.disconnect();
            client.connectWithRetry(memberTray::onConnected, new ClientSocketSession(client, memberTray));
        }
    }
}
