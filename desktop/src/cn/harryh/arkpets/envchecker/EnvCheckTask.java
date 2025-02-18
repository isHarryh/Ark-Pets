package cn.harryh.arkpets.envchecker;

import com.sun.jna.Platform;

import java.util.ArrayList;
import java.util.List;


public abstract class EnvCheckTask {
    public abstract String getFailureReason();

    public abstract String getFailureDetail();

    public abstract boolean tryFix();

    public abstract boolean canFix();

    public boolean needConfirmFix() {
        return false;
    }

    public String getFixReason() {
        return "";
    }

    public String getFixDetail() {
        return "";
    }

    public abstract boolean run();

    @Override
    public String toString() {
        String name = getClass().getSimpleName();
        return name.isEmpty() ? getClass().getSuperclass().getSimpleName() : name;
    }

    public static List<EnvCheckTask> getAvailableTasks() {
        ArrayList<EnvCheckTask> list = new ArrayList<>();
        list.add(new SleepEnvCheckTask(1500));
        if (Platform.isWindows()) {
            list.add(new WinGraphicsEnvCheckTask());
        }
        return list;
    }
}
