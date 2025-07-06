package cn.harryh.arkpets.guitasks.envchecker;

import cn.harryh.arkpets.Const;

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
        list.add(new SleepEnvCheckTask(1000));
        if (Const.isWindows) {
            list.add(new WinGraphicsEnvCheckTask());
        }
        if (Const.isLinux) {
            String desktop = System.getenv("XDG_CURRENT_DESKTOP");
            String type = System.getenv("XDG_SESSION_TYPE");
            if (desktop != null && type != null) {
                if (type.equals("wayland") && desktop.equals("GNOME")) {
                    list.add(new GNOMEPluginCheckTask());
                } else if (type.equals("wayland") && desktop.equals("KDE")) {
                    list.add(new KWinPluginCheckTask());
                } else {
                    list.add(new X11CompositorCheckTask());
                }
            }
        }
        if (Const.isMac) {
            list.add(new AccessibilityCheckTask());
        }
        return list;
    }
}
