/** Copyright (c) 2022-2024, Harry Huang, Half Nothing
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.concurrent;

import com.alibaba.fastjson2.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;


public class PortUtils {
    /** Gets server port for client to connect to.
     * @param expectedPorts The candidate ports to query.
     * @return A server port.
     * @throws NoServerRunningException If no server is running.
     */
    public static int getServerPort(int[] expectedPorts)
            throws NoServerRunningException {
        for (int serverPort : expectedPorts) {
            try (Socket socket = new Socket("localhost", serverPort)) {
                socket.setSoTimeout(100);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out.println(SocketData.ofOperation(UUID.randomUUID(), SocketData.Operation.HANDSHAKE_REQUEST));
                SocketData socketData = JSONObject.parseObject(in.readLine(), SocketData.class);
                out.close();
                in.close();
                if (socketData.operation == SocketData.Operation.HANDSHAKE_RESPONSE)
                    return serverPort;
            } catch (IOException ignored) {
            }
        }
        throw new NoServerRunningException();
    }

    /** Gets an available port for server to bind to.
     * @param expectedPorts The candidate ports to query.
     * @return A port number.
     * @throws NoPortAvailableException If every port is busy.
     * @throws ServerCollisionException If a server is already running.
     */
    public static int getAvailablePort(int[] expectedPorts)
            throws NoPortAvailableException, ServerCollisionException {
        try {
            getServerPort(expectedPorts);
            throw new ServerCollisionException();
        } catch (NoServerRunningException ignored) {
        }
        for (int serverPort : expectedPorts) {
            try (DatagramSocket ignored = new DatagramSocket(serverPort)) {
                return serverPort;
            } catch (SocketException ignored) {
            }
        }
        throw new NoPortAvailableException();
    }


    public static class NoPortAvailableException extends IllegalStateException {
        public NoPortAvailableException() {
            super("No port is available.");
        }
    }


    public static class ServerCollisionException extends IllegalStateException {
        public ServerCollisionException() {
            super("A server is already running.");
        }
    }


    public static class NoServerRunningException extends IllegalStateException {
        public NoServerRunningException() {
            super("No running server is found.");
        }
    }
}
