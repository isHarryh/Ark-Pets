/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import org.apache.log4j.*;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.varia.LevelRangeFilter;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static cn.harryh.arkpets.Const.charsetDefault;


public class Logger {
    protected static final org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
    protected static final org.apache.log4j.Logger currentLogger = rootLogger;
    protected static final long pid = ProcessHandle.current().pid();
    protected static boolean isFileLoggerAvailable = false;
    protected static boolean isInitialized = false;
    protected static int maxFileCount = 256;
    protected static Level level = Level.INFO;
    protected static String logFilePath = null;

    public static final int ERROR = 40000;
    public static final int WARN  = 30000;
    public static final int INFO  = 20000;
    public static final int DEBUG = 10000;

    protected static final LevelRangeFilter sysOutFilter = new LevelRangeFilter();
    protected static final LevelRangeFilter sysErrFilter = new LevelRangeFilter();

    static {
        sysOutFilter.setLevelMax(Level.toLevel(WARN));
        sysErrFilter.setLevelMin(Level.toLevel(ERROR));
    }

    /** Initializes the static logger for the app.
     * The default log level is {@code INFO}.
     * @param logPrefix The prefix of the log's path-and-basename, eg.{@code "logs/myLog"}.
     * @param maxFileCount The maximum count of the logs that shares the same prefix,
     *                     overmuch logs will be deleted.
     */
    public static void initialize(String logPrefix, int maxFileCount) {
        // Reset log appender
        rootLogger.removeAllAppenders();

        // Generate log header
        PatternLayout fileLayout = getPatternLayout(logPrefix);
        PatternLayout consoleLayout = new PatternLayout("[%p] %m%n");

        // Initialize log appender
        Logger.maxFileCount = Math.max(1, maxFileCount);
        logFilePath = logPrefix + "." + pid + ".log";
        try {
            Cleaner.cleanByModifiedTime(logPrefix, Logger.maxFileCount - 1);
            FileAppender fileAppender = new FileAppender(
                    fileLayout, logFilePath, false, false, 0
            );
            fileAppender.setEncoding(charsetDefault);
            currentLogger.addAppender(fileAppender);
            isFileLoggerAvailable = true;
        } catch (IOException e) {
            LogLog.error("Failed to initialize the file logger.");
        }
        ConsoleAppender sysOutAppender = new ConsoleAppender(consoleLayout);
        ConsoleAppender sysErrAppender = new ConsoleAppender(consoleLayout);
        sysOutAppender.addFilter(sysOutFilter);
        sysErrAppender.addFilter(sysErrFilter);
        sysOutAppender.setTarget(ConsoleAppender.SYSTEM_OUT);
        sysErrAppender.setTarget(ConsoleAppender.SYSTEM_ERR);
        rootLogger.addAppender(sysOutAppender);
        rootLogger.addAppender(sysErrAppender);

        // Reset log level
        setLevel(level);
        isInitialized = true;
    }

    private static PatternLayout getPatternLayout(String logPrefix) {
        final String header = "ArkPets Log - " + new File(logPrefix).getName() + " (PID" + pid + ")";
        return new PatternLayout("%d{ABSOLUTE} [%p] %m%n") {
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
    }

    /** Sets a new log level.
     * @param level The new level.
     */
    public static void setLevel(Level level) {
        Logger.level = level;
        rootLogger.setLevel(level);
        currentLogger.setLevel(level);
    }

    /** Sets a new log level.
     * @param level The new level in int format.
     */
    public static void setLevel(int level) {
        Logger.level = Level.toLevel(level);
        rootLogger.setLevel(Logger.level);
        currentLogger.setLevel(Logger.level);
    }

    /** Sets a new log level.
     * @param level The new level in string format.
     */
    public static void setLevel(String level) {
        setLevel(Level.toLevel(level));
    }

    /** Gets the level of root logger.
     * @return The level object.
     */
    public static Level getLevel() {
        return rootLogger.getLevel();
    }

    /** Logs a message with the level {@code DEBUG}.
     */
    public static void debug(String tag, String message) {
        if (isFileLoggerAvailable)
            currentLogger.debug(combine(tag, message));
    }

    /** Logs a message with the level {@code INFO}.
     */
    public static void info(String tag, String message) {
        if (isFileLoggerAvailable)
            currentLogger.info(combine(tag, message));
    }

    /** Logs a message with the level {@code WARN}.
     */
    public static void warn(String tag, String message) {
        if (isFileLoggerAvailable)
            currentLogger.warn(combine(tag, message));
    }

    /** Logs a message with the level {@code ERROR}.
     */
    public static void error(String tag, String message) {
        if (isFileLoggerAvailable)
            currentLogger.error(combine(tag, message));
    }

    /** Logs a message with the level {@code ERROR},
     * together with the detailed information (such as stacktrace).
     */
    public static void error(String tag, String message, Throwable error) {
        if (isFileLoggerAvailable)
            currentLogger.error(combine(tag, message), error);
    }

    protected static String combine(String tag, String message) {
        return tag + ": " + message;
    }


    protected static class Cleaner {
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
                        //noinspection ResultOfMethodCallIgnored
                        fileList.get(i).delete();
                        LogLog.debug("Delete file: " + fileList.get(i));
                    } catch (SecurityException e) {
                        LogLog.error("Cannot delete file: " + fileList.get(i), e);
                    }
                }
            }
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
