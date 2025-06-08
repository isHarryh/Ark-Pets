/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.network;

import cn.harryh.arkpets.utils.Cached;
import cn.harryh.arkpets.utils.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import static cn.harryh.arkpets.Const.httpBufferSizeDefault;
import static cn.harryh.arkpets.Const.httpTimeoutDefault;


public class Connections {
    public static boolean trustAllUnsafe = false;

    /** Creates an HTTPS connection of the given URL, and then try to connect it.
     * @param url The URL to connect.
     * @param connectTimeout The timeout for connecting (ms).
     * @param readTimeout The timeout for reading data (ms).
     * @return The connection instance which has finished connecting.
     * @throws IOException If I/O error occurs. Typically, when a timeout occurred.
     */
    public static HttpsURLConnection createHttpsConnection(URL url, int connectTimeout, int readTimeout)
            throws IOException {
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) url.openConnection();
            if (trustAllUnsafe) {
                connection.setSSLSocketFactory(TrustUtils.getTrustAnySSLSocketFactory());
                connection.setHostnameVerifier(TrustUtils.getTrustAnyHostnameVerifier());
            }
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.connect();
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

    /** Creates an HTTPS connection of the given URL, and then try to connect it.
     * @param url The URL to connect.
     * @param timeout The timeout for connecting and reading (ms).
     * @return The connection instance which has finished connecting.
     * @throws IOException If I/O error occurs. Typically, when a timeout occurred.
     */
    public static HttpsURLConnection createHttpsConnection(URL url, int timeout)
            throws IOException {
        return createHttpsConnection(url, timeout, timeout);
    }

    /** Creates an HTTPS connection of the given URL, and then try to connect it.
     * @param url The URL to connect.
     * @return The connection instance which has finished connecting.
     * @throws IOException If I/O error occurs. Typically, when a timeout occurred.
     */
    public static HttpsURLConnection createHttpsConnection(URL url)
            throws IOException {
        return createHttpsConnection(url, httpTimeoutDefault);
    }

    /** Consumes the input stream from an HTTP/HTTPS connection, writes the data to an output stream.
     * @param connection The {@link HttpURLConnection} instance representing the already established connection.
     * @param os The output stream where the data from the connection's input stream will be written.
     * @param recorder The {@link Recorder} instance used to track the number of bytes received.
     * @throws IOException If an I/O error occurs.
     */
    public static void consume(HttpURLConnection connection, OutputStream os, Recorder recorder) throws IOException {
        InputStream is = getInputStreamOrErrorStream(connection);
        BufferedInputStream bis = new BufferedInputStream(is, httpBufferSizeDefault);
        BufferedOutputStream bos = new BufferedOutputStream(os, httpBufferSizeDefault);

        try (bis; bos; is; os) {
            int len;
            byte[] bytes = new byte[httpBufferSizeDefault];
            while ((len = bis.read(bytes)) != -1) {
                recorder.receive(len);
                bos.write(bytes, 0, len);
            }
            bos.flush();
        }
    }

    /** Throws an exception if the connection's status code isn't {@code 2xx}.
     * @param connection The connection to check.
     * @throws IOException If the status code check fails or an I/O error occurs.
     */
    public static void raiseForStatus(HttpURLConnection connection) throws IOException {
        int code = connection.getResponseCode();
        if (code >= 200 && code < 300)
            return;
        String message = connection.getResponseMessage();
        throw new HttpStatusCodeException(code, message == null ? "Unknown" : message);
    }

    /** Throws an exception if the connection's status code is neither {@code 2xx} nor in the {@code forgiveCodes}.
     * @param connection The connection to check.
     * @param forgiveCodes The array of status codes to forgive.
     * @throws IOException If the status code check fails or an I/O error occurs.
     */
    public static void raiseForStatus(HttpURLConnection connection, int[] forgiveCodes) throws IOException {
       int code = connection.getResponseCode();
       if (code >= 200 && code < 300)
           return;
       for (int forgiveCode : forgiveCodes)
           if (code == forgiveCode)
               return;
       throw new HttpStatusCodeException(connection);
    }

    private static InputStream getInputStreamOrErrorStream(HttpURLConnection connection) {
        try {
            return connection.getInputStream();
        } catch (IOException e) {
            return connection.getErrorStream();
        }
    }


    public static class Recorder {
        protected final int bufferSize;
        protected final long[] bufferNanoTimes;
        protected final Cached<String> cachedBps;
        protected final Cached<String> cachedTb;
        protected int bufferNanoTimesPtr = 0;
        protected int pending = 0;
        protected int total = 0;


        public Recorder(int bufferSize, int maxRecords, double bpsCacheAge, double tbCacheAge) {
            if (bufferSize <= 0 || maxRecords <= 0)
                throw new IllegalArgumentException("bufferSize and maxRecords should be positive.");

            this.bufferNanoTimes = new long[maxRecords];
            this.bufferSize = bufferSize;
            this.cachedBps = new Cached<>() {
                @Override
                protected String produce() {
                    return StringUtils.getFormattedSizeString(getBytesPerSecond());
                }

                @Override
                protected double cacheAge() {
                    return bpsCacheAge;
                }
            };
            this.cachedTb = new Cached<>() {
                @Override
                protected String produce() {
                    return StringUtils.getFormattedSizeString(getTotalBytes());
                }

                @Override
                protected double cacheAge() {
                    return tbCacheAge;
                }
            };
        }

        public Recorder() {
            this(httpBufferSizeDefault, 1024, 0.5, 0.1);
        }

        public void receive(int size) throws IOException {
            total += size;
            pending += size;
            if (pending >= bufferSize) {
                pending = 0;
                bufferNanoTimes[bufferNanoTimesPtr++] = System.nanoTime();
                bufferNanoTimesPtr = bufferNanoTimesPtr < bufferNanoTimes.length ? bufferNanoTimesPtr : 0;
            }
        }

        public long getBytesPerSecond() {
            int actualLength = bufferNanoTimes.length;
            while (actualLength > 0 && bufferNanoTimes[actualLength - 1] == 0)
                actualLength--;
            if (actualLength <= 1)
                return 0;

            long maxTime = bufferNanoTimes[bufferNanoTimesPtr != 0 ? bufferNanoTimesPtr - 1 : actualLength - 1];
            long minTime = bufferNanoTimes[bufferNanoTimesPtr < actualLength ? bufferNanoTimesPtr : 0];
            if (maxTime - minTime < 1000)
                return 0;

            return (actualLength - 1) * bufferSize * 1_000_000_000L / (maxTime - minTime);
        }

        public long getTotalBytes() {
            return total;
        }

        public String getBytesPerSecondString() {
            return cachedBps.getValue();
        }

        public String getTotalBytesString() {
            return cachedTb.getValue();
        }
    }


    public static class HttpStatusCodeException extends IOException {
        private final int code;
        private final String message;

        public HttpStatusCodeException(int code, String message) {
            this.code = code;
            this.message = message == null ? "" : message;
        }

        public HttpStatusCodeException(HttpURLConnection connection) throws IOException {
            this(connection.getResponseCode(), connection.getResponseMessage());
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "HTTP " + code + " : " + message;
        }
    }
}
