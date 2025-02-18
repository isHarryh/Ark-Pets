/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static cn.harryh.arkpets.Const.zipBufferSizeDefault;


public class IOUtils {
    public static class FileUtil {
        /** Reads the entire file into a byte array.
         * @param file The file to read from.
         * @return A byte array.
         * @throws IOException If I/O error occurs.
         */
        public static byte[] readByte(File file)
                throws IOException {
            byte[] content = new byte[(int) file.length()];
            FileInputStream stream = new FileInputStream(file);
            //noinspection ResultOfMethodCallIgnored
            stream.read(content);
            stream.close();
            return content;
        }

        /** Reads the entire file into a string using the specified charset.
         * @param file The file to read from.
         * @param charsetName The name of the specified charset.
         * @return A String.
         * @throws IOException If I/O error occurs or the charset is not supported.
         */
        public static String readString(File file, String charsetName)
                throws IOException {
            return new String(readByte(file), charsetName);
        }

        /** Writes a byte array into a file.
         * @param file The file to write to.
         * @param content The specified bytes to be written.
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

        /** Writes a string into a file using the specified charset.
         * @param file The file to write to.
         * @param charsetName The name of the specified charset.
         * @param content The specified string to be written.
         * @param append If false, the existed file will be overwritten; If true, content will be appended to its end.
         * @throws IOException If I/O error occurs. It may be FileNotFoundException, etc.
         */
        public static void writeString(File file, String charsetName, String content, boolean append)
                throws IOException {
            writeByte(file, content.getBytes(charsetName), append);
        }

        /** Gets the MD5 checksum of the given content.
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

        /** Gets the MD5 checksum of a file.
         * @param file The file.
         * @return MD5 hex string.
         * @throws IOException If I/O error occurs. It may be FileNotFoundException.
         */
        public static String getMD5(File file)
                throws IOException {
            return getMD5(readByte(file));
        }

        /** Deletes a file or a directory.
         * @param fileOrDir The file instance of the specified file or directory.
         * @param ignoreError If true, exceptions will be ignored.
         * @throws IOException If I/O error occurs.
         */
        public static void delete(File fileOrDir, boolean ignoreError)
                throws IOException {
            if (!fileOrDir.exists())
                return;
            if (fileOrDir.isFile()) {
                try {
                    Files.delete(fileOrDir.toPath());
                } catch (IOException e) {
                    if (!ignoreError)
                        throw e;
                }
                return;
            }
            try {
                Files.walkFileTree(fileOrDir.toPath(), new SimpleFileVisitor<>() {
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

        /** Deletes a file or a directory.
         * @param path The path instance of the specified file or directory.
         * @param ignoreError If true, exceptions will be ignored.
         * @throws IOException If I/O error occurs.
         */
        public static void delete(Path path, boolean ignoreError)
                throws IOException {
            FileUtil.delete(path.toFile(), ignoreError);
        }
    }


    public static class ZipUtil {
        /** Unzips the entire zip file into the given directory.
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

            if (overwrite)
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
                    //noinspection ResultOfMethodCallIgnored
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

        /** Zips some files or directories into a zip file.
         * Note that {@code UTF-8} encoding and {@code DEFAULT_COMPRESSION} level will be used to create the zip file.
         * @param zipFilePath The path of the zip file.
         * @param contents The map whose keys are the source paths and whose values are the zipped paths.
         * @param overwrite If true, the existed zip file will be deleted before zipping.
         * @throws IOException If I/O error occurs.
         */
        public static void zip(String zipFilePath, Map<String, String> contents, boolean overwrite)
                throws IOException {
            if (contents.isEmpty())
                return;
            if (overwrite)
                FileUtil.delete(new File(zipFilePath), false);

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
                IOException[] error = new IOException[]{null};
                for (String sourcePath : contents.keySet()) {
                    File sourceFile = new File(sourcePath);
                    String zippedPath = contents.get(sourcePath).replaceAll("(^/+)|(/+$)", "");
                    if (sourceFile.isDirectory()) {
                        //noinspection resource
                        Files.walk(sourceFile.toPath())
                                .filter(path -> !Files.isDirectory(path))
                                .forEach(path -> {
                                    try {
                                        String relativePath = path.toString().substring(sourcePath.length() + 1);
                                        copyFileToZip(zos, path.toFile(), zippedPath + "/" + relativePath);
                                    } catch (IOException e) {
                                        error[0] = e;
                                    }
                                });
                        if (error[0] != null)
                            throw error[0];
                    } else if (sourceFile.isFile()) {
                        copyFileToZip(zos, new File(sourcePath), zippedPath);
                    }
                }
            }
        }

        private static void copyFileToZip(ZipOutputStream zos, File sourceFile, String zippedPath)
                throws IOException {
            zos.putNextEntry(new ZipEntry(zippedPath));
            int len;
            byte[] bytes = new byte[zipBufferSizeDefault];
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile))) {
                while ((len = bis.read(bytes, 0, zipBufferSizeDefault)) != -1)
                    zos.write(bytes, 0, len);
            }
            zos.closeEntry();
        }
    }


    public static class CommandUtil {
        /**
         * Run a command and get the output.
         * @param command The command will run.
         * @param env The environment variable.
         * @param workdir The working directory.
         * @throws IOException If I/O error occurs.
         * @return The command output,Return null if failed.
         */
        public static String runCommand(String command, String[] env, File workdir) throws IOException {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(command, env, workdir);
            try {
                process.waitFor();
            } catch (InterruptedException ignore) {
            }
            if (process.exitValue() == 0) {
                BufferedReader reader = process.inputReader();
                String line;
                StringBuilder b = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    b.append(line);
                }
                reader.close();
                return b.toString();
            } else {
                return null;
            }
        }
    }
}
