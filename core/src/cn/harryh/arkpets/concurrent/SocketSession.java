/** Copyright (c) 2022-2024, Harry Huang, Half Nothing
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.concurrent;

import cn.harryh.arkpets.utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;


abstract public class SocketSession implements Runnable {
    protected Socket target;
    protected BufferedReader in;
    protected PrintWriter out;
    private boolean hasTarget = false;
    private boolean hasRun = false;
    private boolean hasClosed = false;

    public SocketSession() {
    }

    /** Sets the target socket to I/O.
     * @param target The target socket.
     */
    public final void setTarget(Socket target) {
        try {
            this.target = target;
            in = new BufferedReader(new InputStreamReader(target.getInputStream()));
            out = new PrintWriter(target.getOutputStream(), true);
            hasTarget = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final String getHostAddress() {
        return target != null ? target.getInetAddress().getHostAddress() : "0.0.0.0";
    }

    public final int getPort() {
        return target != null ? target.getPort() : 0;
    }

    /** Closes this session together with the socket and the I/O stream.
     */
    public final void close() {
        if (!hasTarget || hasClosed)
            return;
        hasClosed = true;
        try {
            target.close();
            in.close();
            out.close();
            this.onClosed();
        } catch (IOException ignored) {
        }
    }

    @Override
    public final void run() {
        if (hasRun)
            throw new IllegalStateException("The session thread has run yet.");
        if (!hasTarget)
            throw new IllegalStateException("The target socket has not been set yet.");
        hasRun = true;
        try {
            while (!target.isClosed()) {
                try {
                    String request = in.readLine();
                    if (request == null) {
                        Logger.debug("SocketSession", "x- " + this);
                        this.onBroken();
                        this.close();
                    } else {
                        Logger.debug("SocketSession", "<- " + this + " " + request);
                        receive(request);
                    }
                } catch (SocketException e) {
                    Logger.debug("SocketSession", "x- " + this + " (" + e.getMessage() + ")");
                    this.onBroken();
                    this.close();
                }
            }
        } catch (IOException e) {
            Logger.error("SocketSession", "An unexpected error occurred on " + this + ", details see below.", e);
        }
    }

    /** Sends a request via this session.
     * @param request The request to be sent.
     */
    public final void send(Object request) {
        if (!hasTarget)
            throw new IllegalStateException("The target socket has not been set yet.");
        Logger.debug("SocketSession", "-> " + this + " " + request);
        out.println(request);
    }

    /** Handles a received request from this session.
     * @param request The received request.
     */
    abstract public void receive(String request);

    /** When the session is to be closed.
     */
    protected void onClosed() {
    }

    /** When the listener of this session is broken due to some socket issues.
     */
    protected void onBroken() {
    }

    @Override
    public String toString() {
        return "[" + getHostAddress() + ":" + getPort() +"]";
    }
}
