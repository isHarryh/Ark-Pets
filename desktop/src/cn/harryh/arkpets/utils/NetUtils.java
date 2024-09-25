/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import javax.net.ssl.*;
import java.awt.Desktop;
import java.io.IOException;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.util.*;


public class NetUtils {
    private static final int k = 1024;
    private static final int delayTestPort = 443;
    private static final int delayUpThreshold = 1500;
    private static final DecimalFormat df = new DecimalFormat("0.0");

    public static final ArrayList<Source> ghSources;
    static {
        ghSources = new ArrayList<>();
        ghSources.add(new GitHubSource("GitHub",
                "https://raw.githubusercontent.com/",
                "https://github.com/"));
        ghSources.add(new GitHubSource("GHProxy",
                "https://ghproxy.harryh.cn/https://raw.githubusercontent.com/",
                "https://ghproxy.harryh.cn/https://github.com/"));
    }

    public static final Map<Long, String> sizeMap;
    static {
        sizeMap = new HashMap<>();
        sizeMap.put(1L, "B");
        sizeMap.put((long)k, "KB");
        sizeMap.put((long)k * k, "MB");
        sizeMap.put((long)k * k * k, "GB");
        sizeMap.put((long)k * k * k * k, "TB");
    }

    /** Gets a formatted size string, e.g."{@code 114.5 MB}".
     * @param byteSize The size value in Byte.
     * @return The formatted string. Returns "{@code Unknown size}" if conversion failed.
     */
    public static String getFormattedSizeString(long byteSize) {
        if (byteSize == 0)
            return "0";
        for (Long unitSize : sizeMap.keySet()) {
            if (unitSize <= byteSize && byteSize < unitSize * k)
                return df.format((double)byteSize / unitSize) + " " + sizeMap.get(unitSize);
        }
        return "Unknown size";
    }

    /**
     * Opens the given URL in the browser.
     *
     * @param url The URL to browse.
     */
    public static void browseWebpage(String url) {
        new Thread(() -> {
            try {
                Logger.debug("Network", "Opening the URL " + url + " in the browser");
                Desktop.getDesktop().browse(URI.create(url));
            } catch (IOException e) {
                Logger.error("Network", "Failed to open the URL in the browser, details see below.", e);
            }
        }).start();
    }


    public static class ConnectionUtil {
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
                delayMillis = (int)(stop - start);
            } catch (IOException ignored) {
            }
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            return delayMillis;
        }

        /** Creates an HTTPS connection of the given URL, and then try to connect it.
         * @param url The URL to connect.
         * @param connectTimeout The timeout of the connection (ms).
         * @param readTimeout The timeout of the reading operation (ms).
         * @param trustAll Whether to ignore SSL verification (unsafe).
         * @return The connection instance which has finished connecting.
         * @throws IOException If I/O error occurs. Typically, when a timeout occurred or the response code wasn't like 2XX.
         */
        public static HttpsURLConnection createHttpsConnection(URL url, int connectTimeout, int readTimeout, boolean trustAll)
                throws IOException {
            HttpsURLConnection connection = null;
            try {
                connection = (HttpsURLConnection)url.openConnection();
                if (trustAll) {
                    connection.setSSLSocketFactory(getTrustAnySSLSocketFactory());
                    connection.setHostnameVerifier(getTrustAnyHostnameVerifier());
                }
                connection.setConnectTimeout(connectTimeout);
                connection.setReadTimeout(readTimeout);
                connection.connect();
                HttpResponseCode responseCode = new HttpResponseCode(connection);
                if (responseCode.type != HttpResponseCodeType.SUCCESS)
                    throw new HttpResponseCodeException(responseCode);
                return connection;
            } catch (IOException e) {
                try {
                    if (connection != null && connection.getInputStream() != null)
                        connection.getInputStream().close();
                } catch (Exception ignored) {
                }
                throw e;
            }
        }

        /** Gets Socket Factory which trusts all.
         * @return SocketFactory instance.
         */
        public static SSLSocketFactory getTrustAnySSLSocketFactory() {
            try {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new SecureRandom());
                return sslContext.getSocketFactory();
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                Logger.error("Network", "Failed to get the SSL socket factory, details see below.", e);
                return null;
            }
        }

        /** Gets Hostname Verifier which trusts all.
         * @return HostnameVerifier instance.
         */
        public static HostnameVerifier getTrustAnyHostnameVerifier() {
            return new TrustAnyHostnameVerifier();
        }

        private static class TrustAnyTrustManager implements X509TrustManager {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }
        }

        private static class TrustAnyHostnameVerifier implements HostnameVerifier {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        }
    }


    public static class BufferLog {
        protected long[] bufferTimestamps;
        protected int bufferSize;
        protected int bufferTimestampsPointer = 0;
        protected long lastCalculatedResult = 0;
        protected long lastCalculatedTime = 0;

        public BufferLog(int bufferSize, int maxBufferCount) {
            if (bufferSize <= 0 || maxBufferCount <= 0)
                throw new IllegalArgumentException("bufferSize and maxBufferCount should be positive.");
            this.bufferTimestamps = new long[maxBufferCount];
            this.bufferSize = bufferSize;
        }

        public BufferLog(int bufferSize) {
            this(bufferSize, 1024);
        }

        public void receive() {
            bufferTimestamps[bufferTimestampsPointer++] = System.currentTimeMillis();
            bufferTimestampsPointer = bufferTimestampsPointer < bufferTimestamps.length ? bufferTimestampsPointer : 0;
        }

        public long getSpeedPerSecond(int cacheTimeMillis) {
            long currentTimestamp = System.currentTimeMillis();
            if (lastCalculatedTime + cacheTimeMillis <= currentTimestamp) {
                int actualLength;
                for (actualLength = bufferTimestamps.length; actualLength > 0; actualLength--)
                    if (bufferTimestamps[actualLength - 1] != 0)
                        break;
                if (actualLength <= 1)
                    return 0;

                long maxTimestamp = bufferTimestamps[bufferTimestampsPointer != 0 ? bufferTimestampsPointer - 1 : actualLength - 1];
                long minTimestamp = bufferTimestamps[bufferTimestampsPointer < actualLength ? bufferTimestampsPointer : 0];
                if (maxTimestamp - minTimestamp < 100)
                    return 0;

                lastCalculatedResult = (actualLength - 1) * bufferSize * 1000L / (maxTimestamp - minTimestamp);
                lastCalculatedTime = currentTimestamp;
            }
            return lastCalculatedResult;
        }
    }


    public enum HttpResponseCodeType {
        /** Indicates an invalid HTTP response */
        UNKNOWN,
        /** Indicates a {@code 1xx} HTTP response code */
        INFORMATION,
        /** Indicates a {@code 2xx} HTTP response code */
        SUCCESS,
        /** Indicates a {@code 3xx} HTTP response code */
        REDIRECTION,
        /** Indicates a {@code 4xx} HTTP response code */
        CLIENT_ERROR,
        /** Indicates a {@code 5xx} HTTP response code */
        SERVER_ERROR
    }


    public static class HttpResponseCode {
        public final int code;
        public final String message;
        public final NetUtils.HttpResponseCodeType type;

        public HttpResponseCode(int code, String message) {
            this.code = code;
            this.message = message;
            NetUtils.HttpResponseCodeType type;
            if (100 <= code && code < 200)
                type = NetUtils.HttpResponseCodeType.INFORMATION;
            else if (200 <= code && code < 300)
                type = NetUtils.HttpResponseCodeType.SUCCESS;
            else if (300 <= code && code < 400)
                type = NetUtils.HttpResponseCodeType.REDIRECTION;
            else if (400 <= code && code < 500)
                type = NetUtils.HttpResponseCodeType.CLIENT_ERROR;
            else if (500 <= code && code < 600)
                type = NetUtils.HttpResponseCodeType.SERVER_ERROR;
            else
                type = NetUtils.HttpResponseCodeType.UNKNOWN;
            this.type = type;
        }

        public HttpResponseCode(HttpURLConnection connection)
                throws IOException {
            this(connection.getResponseCode(), connection.getResponseMessage());
        }
    }


    public static class HttpResponseCodeException extends IOException {
        private final HttpResponseCode responseCode;

        public HttpResponseCodeException(HttpResponseCode responseCode) {
            this.responseCode = responseCode;
        }

        public int getCode() {
            return responseCode.code;
        }

        @Override
        public String getMessage() {
            return responseCode.code + ": " + responseCode.message;
        }

        public HttpResponseCodeType getType() {
            return responseCode.type;
        }
    }


    public static class Source {
        public final String tag;
        public final String preUrl;
        public int delay = -1;
        public long lastErrorTime = -1;

        public Source(String tag, String preUrl) {
            this.tag= tag;
            this.preUrl = preUrl;
        }

        public void receiveError() {
            lastErrorTime = System.currentTimeMillis();
            Logger.debug("Network", "Marked source \"" + tag + "\" as historical unavailable with timestamp " + lastErrorTime);
        }

        public void testDelay() {
            testDelay(delayTestPort, delayUpThreshold);
        }

        public void testDelay(int port, int timeoutMillis) {
            delay = ConnectionUtil.testDelay(preUrl, port, timeoutMillis);
            Logger.debug("Network", "Real delay for \"" + tag + "\" is " + delay + "ms");
        }

        public static void testDelay(List<Source> sources) {
            sources.forEach(Source::testDelay);
        }

        public static void sortByDelay(List<Source> sources) {
            testDelay(sources);
            sources.sort((o1, o2) -> {
                if (o1.delay == o2.delay)
                    return 0;
                if (o1.delay < 0 && o2.delay >= 0)
                    return 1;
                if (o1.delay >= 0 && o2.delay < 0)
                    return -1;
                return (o1.delay > o2.delay) ? 1 : -1;
            });
        }

        public static void sortByOverallAvailability(List<Source> sources) {
            sortByDelay(sources);
            sources.sort((o1, o2) -> {
                if (o1.lastErrorTime != o2.lastErrorTime)
                    return (o1.lastErrorTime > o2.lastErrorTime) ? 1 : -1;
                return 0;
            });
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + " \"" + tag + "\" (" + delay + "ms)";
        }
    }


    public static class GitHubSource extends Source {
        public final String rawPreUrl;
        public final String archivePreUrl;

        public GitHubSource(String tag, String rawPreUrl, String archivePreUrl) {
            super(tag, rawPreUrl);
            this.rawPreUrl = rawPreUrl;
            this.archivePreUrl = archivePreUrl;
        }
    }
}
