/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;


public class NetworkUtils {
    /** Sets the system's proxy property, applying on both HTTP and HTTPS.
     * @param host The proxy host, pass empty string to disable proxy.
     * @param port The proxy port, pass empty string to disable proxy.
     */
    public static void setProxy(String host, String port) {
        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", port);
        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", port);
    }

    /** Tests the real connection delay of the given URL (with a specified port).
     * @param url The URL to be tested.
     * @param port The port to connect.
     * @param timeoutMillis Timeout (ms).
     * @return The delay (ms). {@code -1} when connection failed or timeout.
     */
    public static int testDelay(String url, int port, int timeoutMillis) {
        Socket socket = new Socket();
        int delayMillis = -1;
        try {
            SocketAddress address = new InetSocketAddress(new URL(url).getHost(), port);
            long start = System.currentTimeMillis();
            socket.connect(address, timeoutMillis);
            long stop = System.currentTimeMillis();
            delayMillis = (int) (stop - start);
        } catch (IOException ignored) {
        }
        try {
            socket.close();
        } catch (IOException ignored) {
        }
        return delayMillis;
    }
}
