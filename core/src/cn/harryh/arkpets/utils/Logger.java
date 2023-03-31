/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import org.apache.log4j.*;
import org.apache.log4j.helpers.LogLog;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class Logger {
    private static org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
    private static org.apache.log4j.Logger currentLogger = rootLogger;
    private static final long pid = ProcessHandle.current().pid();
    private static boolean isFileLoggerAvailable = false;
    private static boolean isInitialized = false;
    private static int maxFileCount = 256;
    private static Level level = Level.INFO;

    public static final int ERROR = 40000;
    public static final int WARN  = 30000;
    public static final int INFO  = 20000;
    public static final int DEBUG = 10000;

    /** Initialize the static logger for the app.
     * The default log level is {@code INFO}.
     * @param logPrefix The prefix of the log's path-and-basename, eg.{@code "logs/myLog"}.
     * @param maxFileCount The maximum count of the logs that shares the same prefix,
     *                     overmuch logs will be deleted.
     */
    public static void initialize(String logPrefix, int maxFileCount) {
        final String header = "ArkPets Log - " + new File(logPrefix).getName() + " (PID" + pid + ")";
        PatternLayout fileLayout = new PatternLayout("%d{ABSOLUTE} [%p] %m%n") {
            @Override
            public String getHeader() {
                return "# *** " + header + " ***" + Layout.LINE_SEP +
                        "# Created: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS")) + Layout.LINE_SEP +
                        "# OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")" + Layout.LINE_SEP +
                        "# Java version: " + System.getProperty("java.version") + Layout.LINE_SEP +
                        "# Working directory: " + System.getProperty("user.dir") + Layout.LINE_SEP +
                        Layout.LINE_SEP;
            }
        };
        PatternLayout consoleLayout = new PatternLayout("[%p] %m%n");

        Logger.maxFileCount = Math.max(1, maxFileCount);
        try {
            Cleaner.cleanByModifiedTime(logPrefix, Logger.maxFileCount - 1);
            FileAppender fileAppender = new FileAppender(
                    fileLayout, logPrefix + "." + pid + ".log", false, false, 0
            );
            currentLogger.addAppender(fileAppender);
            isFileLoggerAvailable = true;
        } catch (IOException e) {
            LogLog.error("Failed to initialize the file logger.");
        }
        ConsoleAppender consoleAppender = new ConsoleAppender(consoleLayout);
        rootLogger.addAppender(consoleAppender);

        setLevel(level);
        isInitialized = true;
    }

    /** Set a new log level.
     * @param level The new level.
     */
    public static void setLevel(Level level) {
        Logger.level = level;
        rootLogger.setLevel(level);
        currentLogger.setLevel(level);
    }

    /** Set a new log level.
     * @param level The new level in int format.
     */
    public static void setLevel(int level) {
        Logger.level = Level.toLevel(level);
        rootLogger.setLevel(Logger.level);
        currentLogger.setLevel(Logger.level);
    }

    /** Get the level of root logger.
     * @return The level object.
     */
    public static Level getLevel() {
        return rootLogger.getLevel();
    }

    /** Log a message that has the level {@code DEBUG}.
     */
    public static void debug(String tag, String message) {
        if (isFileLoggerAvailable)
            currentLogger.log(Level.DEBUG, combine(tag, message));
    }

    /** Log a message that has the level {@code INFO}.
     */
    public static void info(String tag, String message) {
        if (isFileLoggerAvailable)
            currentLogger.log(Level.INFO, combine(tag, message));
    }

    /** Log a message that has the level {@code WARN}.
     */
    public static void warn(String tag, String message) {
        if (isFileLoggerAvailable)
            currentLogger.log(Level.WARN, combine(tag, message));
    }

    /** Log a message that has the level {@code ERROR}.
     */
    public static void error(String tag, String message) {
        if (isFileLoggerAvailable)
            currentLogger.log(Level.ERROR, combine(tag, message));
    }

    /** Log a message that has the level {@code ERROR},
     * together with the detailed information.
     */
    public static void error(String tag, String message, Throwable error) {
        if (isFileLoggerAvailable)
            currentLogger.error(combine(tag, message), error);
    }

    private static String combine(String tag, String message) {
        return tag + ": " + message;
    }

    public static class Cleaner {
        private static final SimpleDateFormat sdf = new SimpleDateFormat();

        public static void cleanByModifiedTime(String logPrefixName, int maxFileCount) {
            List<File> fileList = getAllLogs(logPrefixName);
            maxFileCount = Math.max(1, maxFileCount);
            if (fileList.size() >= maxFileCount) {
                sortByModifiedTime(fileList);
                deleteButKeep(fileList, maxFileCount);
            }
        }

        private static void deleteButKeep(List<File> fileList, int maxFileCount) {
            if (fileList.size() > maxFileCount) {
                for (int i = 0; i < fileList.size() - maxFileCount; i++) {
                    try {
                        fileList.get(i).delete();
                        LogLog.debug("Delete file: " + fileList.get(i));
                    } catch (SecurityException e) {
                        LogLog.error("Cannot delete file: " + fileList.get(i), e);
                    }
                }
            }
        }

        @Deprecated
        private static void sortByDateStr(String prefixName, List<File> fileList) {
            fileList.sort((o1, o2) -> {
                try {
                    if (getDateStr(prefixName, o1).isEmpty())
                        return 1;
                    if (getDateStr(prefixName, o2).isEmpty())
                        return -1;
                    long t1 = sdf.parse(getDateStr(prefixName, o1)).getTime();
                    long t2 = sdf.parse(getDateStr(prefixName, o2)).getTime();
                    if (t1 != t2)
                        return t1 > t2 ? 1 : -1;
                } catch (ParseException ignored) {
                }
                return 0;
            });
        }

        private static void sortByModifiedTime(List<File> fileList) {
            if (fileList.isEmpty())
                return;
            fileList.sort((o1, o2) -> {
                long t1 = o1.lastModified();
                long t2 = o2.lastModified();
                if (t1 != t2)
                    return t1 > t2 ? 1 : -1;
                return 0;
            });
        }

        private static String getDateStr(String prefixName, File file) {
            return file == null ? "" : file.getName().replaceAll(new File(prefixName).getName(), "");
        }

        private static List<File> getAllLogs(String logPrefixName) {
            File file = new File(logPrefixName);
            File dir = file.getParentFile() == null ? new File(".") : file.getParentFile();
            String finalLogPrefixName = file.getName();
            File[] files = dir.listFiles(pathname -> pathname.getName().indexOf(finalLogPrefixName) == 0);
            if (files == null)
                return List.of();
            return Arrays.asList(files);
        }
    }
}
