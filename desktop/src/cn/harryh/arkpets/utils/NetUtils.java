/** Copyright (c) 2022-2023, Harry Huang
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class NetUtils {
    private static final int k = 1024;
    private static final int delayTestPort = 443;
    private static final int delayUpThreshold = 2000;
    private static final DecimalFormat df = new DecimalFormat("0.0");

    public static GitHubSource[] ghSources = new GitHubSource[] {
            new GitHubSource("GitHub", "https://raw.githubusercontent.com/", "https://github.com/"),
            new GitHubSource("GHProxy", "https://ghproxy.com/https://raw.githubusercontent.com/", "https://ghproxy.com/https://github.com/")
    };
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
            if (unitSize <= byteSize && byteSize < unitSize * k * 10)
                return df.format((double)byteSize / unitSize) + " " + sizeMap.get(unitSize);
        }
        return "Unknown size";
    }

    /** Opens the given URL in the browser.
     * @param url The URL to browse.
     * @return true if success, otherwise false.
     */
    public static boolean browseWebpage(String url) {
        try {
            Logger.debug("Network", "Opening the URL " + url + " in the browser");
            Desktop.getDesktop().browse(URI.create(url));
            return true;
        } catch (IOException e) {
            Logger.error("Network", "Failed to open the URL in the browser, details see below.", e);
            return false;
        }
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
                int code = connection.getResponseCode();
                if (200 > code || code >= 300)
                    throw new HttpResponseCodeException(code, connection.getResponseMessage());
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


    public static class HttpResponseCodeException extends IOException {
        private final int code;
        private final String message;

        public HttpResponseCodeException(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return code + ": " + message;
        }

        public boolean isInformation() {
            return code >= 100 && code < 200;
        }

        public boolean isSuccess() {
            return code >= 200 && code < 300;
        }

        public boolean isRedirection() {
            return code >= 300 && code < 400;
        }

        public boolean isClientError() {
            return code >= 400 && code < 500;
        }

        public boolean isServerError() {
            return code >= 500 && code < 600;
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

        public int testDelay() {
            return testDelay(delayTestPort, delayUpThreshold);
        }

        public int testDelay(int port, int timeoutMillis) {
            delay = ConnectionUtil.testDelay(preUrl, port, timeoutMillis);
            Logger.debug("Network", "Real delay for \"" + tag + "\" is " + delay + "ms");
            return delay;
        }

        public static Source[] sortByDelay(Source[] sources) {
            for (Source s : sources)
                s.testDelay();
            ArrayList<Source> sourcesList = new ArrayList<>(Arrays.stream(sources).toList());
            sourcesList.sort((o1, o2) -> {
                if (o1.delay == o2.delay)
                    return 0;
                if (o1.delay < 0 && o2.delay >= 0)
                    return 1;
                if (o1.delay >= 0 && o2.delay < 0)
                    return -1;
                return (o1.delay > o2.delay) ? 1 : -1;
            });
            return sourcesList.toArray(new Source[0]);
        }

        public static Source[] sortByOverallAvailability(Source[] sources) {
            for (Source s : sources)
                s.testDelay();
            ArrayList<Source> sourcesList = new ArrayList<>(Arrays.stream(sources).toList());
            sourcesList.sort((o1, o2) -> {
                if (o1.lastErrorTime != o2.lastErrorTime)
                    return (o1.lastErrorTime > o2.lastErrorTime) ? 1 : -1;
                if (o1.delay == o2.delay)
                    return 0;
                if (o1.delay < 0 && o2.delay >= 0)
                    return 1;
                if (o1.delay >= 0 && o2.delay < 0)
                    return -1;
                return (o1.delay > o2.delay) ? 1 : -1;
            });
            return sourcesList.toArray(new Source[0]);
        }
    }


    public static class GitHubSource extends Source {
        public final String rawPreUrl;
        public final String archivePreUrl;

        public GitHubSource(String tag, String preUrl) {
            super(tag, preUrl);
            rawPreUrl = preUrl;
            archivePreUrl = preUrl;
        }

        public GitHubSource(String tag, String rawPreUrl, String archivePreUrl) {
            super(tag, rawPreUrl);
            this.rawPreUrl = rawPreUrl;
            this.archivePreUrl = archivePreUrl;
        }

        public static GitHubSource[] sortByDelay(GitHubSource[] sources) {
            for (GitHubSource s : sources)
                s.testDelay();
            ArrayList<GitHubSource> sourcesList = new ArrayList<>(Arrays.stream(sources).toList());
            sourcesList.sort((o1, o2) -> {
                if (o1.delay == o2.delay)
                    return 0;
                if (o1.delay < 0 && o2.delay >= 0)
                    return 1;
                if (o1.delay >= 0 && o2.delay < 0)
                    return -1;
                return (o1.delay > o2.delay) ? 1 : -1;
            });
            return sourcesList.toArray(new GitHubSource[0]);
        }

        public static GitHubSource[] sortByOverallAvailability(GitHubSource[] sources) {
            for (GitHubSource s : sources)
                s.testDelay();
            ArrayList<GitHubSource> sourcesList = new ArrayList<>(Arrays.stream(sources).toList());
            sourcesList.sort((o1, o2) -> {
                if (o1.lastErrorTime != o2.lastErrorTime)
                    return (o1.lastErrorTime > o2.lastErrorTime) ? 1 : -1;
                if (o1.delay == o2.delay)
                    return 0;
                if (o1.delay < 0 && o2.delay >= 0)
                    return 1;
                if (o1.delay >= 0 && o2.delay < 0)
                    return -1;
                return (o1.delay > o2.delay) ? 1 : -1;
            });
            return sourcesList.toArray(new GitHubSource[0]);
        }
    }
}
