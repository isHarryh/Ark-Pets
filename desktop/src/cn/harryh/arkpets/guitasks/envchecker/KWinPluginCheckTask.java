package cn.harryh.arkpets.guitasks.envchecker;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.natives.KWinPluginInterface;
import cn.harryh.arkpets.utils.IOUtils;
import cn.harryh.arkpets.utils.Logger;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class KWinPluginCheckTask extends EnvCheckTask {
    private boolean available;
    private String reason;
    private String detail;

    private List<String> availableList;

    public KWinPluginCheckTask() {
        try {
            DBusConnection conn = DBusConnectionBuilder.forSessionBus().build();
            KWinPluginInterface pi = conn.getRemoteObject("org.kde.KWin", "/Plugins", KWinPluginInterface.class);
            availableList = pi.getAvailablePlugins(); // check connection
            conn.close();
        } catch (Exception e) {
            Logger.error("EnvCheck", "Failed to connect to DBus", e);
            reason = "获取插件信息失败";
            detail = "无法获取插件信息，请尝试手动检查并安装插件。";
            return;
        }
        available = true;
    }

    @Override
    public String getFailureReason() {
        return reason;
    }

    @Override
    public String getFailureDetail() {
        return detail;
    }

    @Override
    public boolean tryFix(ArkConfig cfg) {
        return false;
    }

    @Override
    public boolean canFix() {
        return false;
    }

    @Override
    public boolean run() {
        if (!available) {
            return false;
        }
        try {
            String result = IOUtils.CommandUtil.runCommand("plasmashell --version", null, null);
            Pattern pattern = Pattern.compile("plasmashell (\\d+)");
            if (result == null) return false;
            Matcher matcher = pattern.matcher(result);
            if (matcher.find()) {
                int ver = Integer.parseInt(matcher.group(1));
                if (ver == 5) { // Plasma 5.x
                    reason = "不支持 KDE 5 Wayland";
                    detail = "桌宠不支持在 KDE 5 Wayland 会话下运行，请使用 X11 会话。";
                    return false;
                }
            } else {
                return false;
            }
        } catch (IOException e) {
            Logger.error("EnvCheck", "Failed to get KDE plugin info", e);
            return false;
        }
        String pluginName = Const.kdePluginName + Const.kdePluginVersion;
        if (!availableList.contains(pluginName)) {
            reason = "安装 KDE 集成插件";
            detail = "当前系统未找到 KDE 集成插件。\n由于不同 Linux 发行版之间 KDE/Qt 版本不一致，请前往 https://github.com/litwak913/Ark-Pets-Integration 并根据说明进行集成插件的编译和安装。";
            return false;
        }
        return true;
    }
}
