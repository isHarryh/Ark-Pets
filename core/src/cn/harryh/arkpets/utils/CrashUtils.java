package cn.harryh.arkpets.utils;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.concurrent.ProcessPool;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class CrashUtils {
    public static List<String> collectLogs(ProcessPool.UnexpectedExitCodeException e) {
        // Collect related log files
        List<String> pathList = new ArrayList<>();
        pathList.add("%s.%d.log".formatted(Const.LogConfig.logDesktopPath, ProcessHandle.current().pid()));
        if (e!=null) {
            pathList.add("%s.%d.log".formatted(Const.LogConfig.logCorePath, e.getProcessId()));
        }
        pathList.removeIf(logFile -> Files.notExists(Path.of(logFile)));
        if (pathList.isEmpty()) {
            Logger.info("CrashUtils", "No log file to collect");
            return List.of();
        }
        return pathList;
    }

    public static void writeException(File file, Exception e) throws IOException {
        if (file.exists() && !file.delete())
            throw new IOException("Cannot delete file:" + file.getAbsolutePath());
        if (!file.exists() && !file.createNewFile())
            throw new IOException("Cannot create file:" + file.getAbsolutePath());
        FileOutputStream out = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(e);
        oos.close();
    }

    public static Exception readException(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(in);
        Exception e;
        try {
            e = (Exception) ois.readObject();
        } catch (ClassNotFoundException ex) {
            throw new IOException("Cannot load exception class");
        }
        return e;
    }
}
