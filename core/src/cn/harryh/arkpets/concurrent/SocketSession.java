package cn.harryh.arkpets.concurrent;

import cn.harryh.arkpets.utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;


abstract public class SocketSession implements Runnable {
    protected final Socket target;
    protected final BufferedReader in;
    protected final PrintWriter out;
    protected boolean hasRun = false;
    protected boolean hasClosed = false;

    public SocketSession(Socket target) {
        try {
            this.target = target;
            in = new BufferedReader(new InputStreamReader(target.getInputStream()));
            out = new PrintWriter(target.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final String getHostAddress() {
        return target.getInetAddress().getHostAddress();
    }

    public final int getPort() {
        return target.getPort();
    }

    public final void close() {
        if (hasClosed)
            return;
        hasClosed = true;
        try {
            target.close();
            this.onClosed();
        } catch (IOException ignored) {
        }
    }

    @Override
    public final void run() {
        if (hasRun)
            throw new IllegalStateException("The session thread has run yet.");
        try {
            while (!target.isClosed()) {
                try {
                    String request = in.readLine();
                    if (request == null) {
                        Logger.debug("SocketSession", this + " -x");
                        this.onBroken();
                        this.close();
                    } else {
                        Logger.debug("SocketSession", this + " -> " + request);
                        receive(request);
                    }
                } catch (SocketException e) {
                    Logger.debug("SocketSession", this + " -x (" + e.getMessage() + ")");
                    this.onBroken();
                    this.close();
                }
            }
        } catch (IOException e) {
            Logger.error("SocketSession", "An unexpected error occurred on " + this + ", details see below.", e);
        }
    }

    public final void send(String request) {
        Logger.debug("SocketSession", this + " <- " + request);
        out.println(request);
    }

    abstract public void receive(String request);

    protected void onClosed() {
    }

    protected void onBroken() {
    }

    @Override
    public String toString() {
        return "[" + getHostAddress() + ":" + getPort() +"]";
    }
}
