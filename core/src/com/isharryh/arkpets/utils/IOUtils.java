/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets.utils;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.isharryh.arkpets.Const.*;


public class IOUtils {

    public static class FileUtil {
        /** Read the entire file into a byte array.
         * @param file The file to be read.
         * @return A byte array.
         * @throws IOException If I/O error occurs. It may be FileNotFoundException, etc.
         */
        public static byte[] readByte(File file)
                throws IOException {
            byte[] content = new byte[(int)file.length()];
            FileInputStream stream = new FileInputStream(file);
            stream.read(content);
            stream.close();
            return content;
        }

        /** Read the entire file into a string using the specified charset.
         * @param file        The file to be read.
         * @param charsetName The name of the specified charset.
         * @return A String.
         * @throws IOException If I/O error occurs. It may be FileNotFoundException, UnsupportedEncodingException, etc.
         */
        public static String readString(File file, String charsetName)
                throws IOException {
            return new String(readByte(file), charsetName);
        }

        /** Write a byte array into a file.
         * @param file The file to be written.
         * @param content The specified bytes.
         * @param append If false, the existed file will be overwritten; If true, content will be appended to its end.
         * @throws IOException If I/O error occurs. It may be FileNotFoundException, etc.
         */
        public static void writeByte(File file, byte[] content, boolean append)
                throws IOException {
            if (!append && file.exists() && !file.delete())
                throw new IOException("Cannot delete file:" + file.getAbsolutePath());
            if (!file.exists() && !file.createNewFile())
                throw new IOException("Cannot create file:" + file.getAbsolutePath());
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(content);
            stream.close();
        }

        /** Write a string into a file using the specified charset.
         * @param file The file to be written.
         * @param charsetName The name of the specified charset.
         * @param content The specified string.
         * @param append If false, the existed file will be overwritten; If true, content will be appended to its end.
         * @throws IOException If I/O error occurs. It may be FileNotFoundException, etc.
         */
        public static void writeString(File file, String charsetName, String content, boolean append)
                throws IOException {
            writeByte(file, content.getBytes(charsetName), append);
        }

        /**
         * Get the MD5 hex string of the given content.
         * @param content A byte array.
         * @return MD5 hex String.
         */
        public static String getMD5(byte[] content) {
            StringBuilder sb = new StringBuilder();
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(content);
                byte[] hash = md.digest();
                for (byte b : hash) {
                    int i = b;
                    if (i < 0)
                        i = b & 0xFF;
                    if (i < 0x10)
                        sb.append("0");
                    sb.append(Integer.toHexString(i));
                }
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e.getMessage());
            }
            return sb.toString();
        }

        /**
         * Get the MD5 hex string of a file.
         * @param file The file.
         * @return MD5 hex string.
         * @throws IOException If I/O error occurs. It may be FileNotFoundException.
         */
        public static String getMD5(File file)
                throws IOException {
            return getMD5(readByte(file));
        }

        /** Delete a file or a directory.
         * @param path The path instance of the specified file or directory.
         * @param ignoreError If true, exceptions will be ignored.
         * @throws IOException If I/O error occurs.
         */
        public static void delete(Path path, boolean ignoreError)
                throws IOException {
            if (!path.toFile().exists())
                return;
            if (path.toFile().isFile()) {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    if (!ignoreError)
                        throw e;
                }
                return;
            }
            try {
                Files.walkFileTree(path, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        try {
                            Files.delete(file);
                        } catch (IOException e) {
                            if (!ignoreError)
                                throw e;
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        try {
                            Files.delete(dir);
                        } catch (IOException e) {
                            if (!ignoreError)
                                throw e;
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                if (!ignoreError)
                    throw e;
            }
        }
    }


    public static class NetUtil {
        public static HttpsURLConnection createHttpsConnection(URL url, int connectTimeout, int readTimeout, boolean trustAll)
                throws IOException {
            HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
            if (trustAll) {
                connection.setSSLSocketFactory(NetUtil.getTrustAnySSLSocketFactory());
                connection.setHostnameVerifier(NetUtil.getTrustAnyHostnameVerifier());
            }
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.connect();
            int code = connection.getResponseCode();
            if (200 > code || code >= 300 )
                throw new HttpResponseCodeException(code, connection.getResponseMessage());
            return connection;
        }

        /**
         * Get Socket Factory which trusts all.
         * @return SocketFactory instance.
         */
        public static SSLSocketFactory getTrustAnySSLSocketFactory() {
            try {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new SecureRandom());
                return sslContext.getSocketFactory();
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Get Hostname Verifier which trusts all.
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
    }


    public static class ZipUtil {
        /** Unzip the entire zip file into the given directory.
         * @param zipFilePath The path of the zip file.
         * @param destDirPath The path of the destination directory which will be created if it is not existed.
         *                    Should be ended with a path separator.
         * @param overwrite If true, the existed destination directory will be deleted before unzipping.
         * @throws IOException If I/O error occurs.
         */
        public static void unzip(String zipFilePath, String destDirPath, boolean overwrite)
                throws IOException {
            BufferedInputStream bis;
            BufferedOutputStream bos;
            ZipEntry entry;
            ZipFile zipfile = new ZipFile(zipFilePath);
            File destDir = new File(destDirPath);

            if (overwrite && destDir.exists())
                FileUtil.delete(destDir.toPath(), true);
            if (!destDir.exists())
                Files.createDirectories(destDir.toPath());

            // Create every directory
            Enumeration<? extends ZipEntry> dir = zipfile.entries();
            String name;
            while (dir.hasMoreElements()) {
                entry = dir.nextElement();
                if (entry.isDirectory()) {
                    name = entry.getName();
                    name = name.substring(0, name.length() - 1);
                    File fileObject = new File(destDirPath + name);
                    fileObject.mkdir();
                }
            }

            // Extract every file
            Enumeration<? extends ZipEntry> item = zipfile.entries();
            while (item.hasMoreElements()) {
                entry = item.nextElement();
                if (!entry.isDirectory()) {
                    int len;
                    byte[] bytes = new byte[zipBufferSizeDefault];
                    FileOutputStream fos = new FileOutputStream(destDirPath + entry.getName());
                    bos = new BufferedOutputStream(fos, zipBufferSizeDefault);
                    bis = new BufferedInputStream(zipfile.getInputStream(entry));
                    while ((len = bis.read(bytes, 0, zipBufferSizeDefault)) != -1)
                        bos.write(bytes, 0, len);
                    bos.flush();
                    bos.close();
                    bis.close();
                }
            }
        }
    }
}
