package cn.harryh.arkpets.guitasks.envchecker;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.natives.MutterPluginInterface;
import cn.harryh.arkpets.utils.IOUtils;
import cn.harryh.arkpets.utils.Logger;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.types.Variant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


public class GNOMEPluginCheckTask extends EnvCheckTask {
    private DBusConnection conn;
    private MutterPluginInterface pi;
    private boolean available;
    private String reason;
    private String detail;

    public GNOMEPluginCheckTask() {
        try {
            conn = DBusConnectionBuilder.forSessionBus().build();
            pi = conn.getRemoteObject("org.gnome.Shell", "/org/gnome/Shell", MutterPluginInterface.class);
            pi.getUserExtensionsEnabled(); // check connection
            available = true;
        } catch (Exception e) {
            Logger.error("EnvCheck", "Failed to connect to DBus", e);
            reason = "获取插件信息失败";
            detail = "无法获取插件信息，请尝试手动检查并安装插件。";
        }
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
        Logger.info("EnvCheck", "Installing GNOME plugin");
        String pluginPath = System.getProperty("user.home") + "/.local/share/gnome-shell/extensions/" + Const.gnomePluginName;
        File pluginDir = new File(pluginPath);
        if (!pluginDir.exists()) {
            if (!pluginDir.mkdirs()) {
                reason = "集成插件安装失败";
                detail = "无法创建插件目录，请尝试手动安装插件。";
                return false;
            }
        }
        File tempZip = new File(Const.PathConfig.tempDirPath, "gnome-plugin.zip");
        try (InputStream pluginZip = getClass().getResourceAsStream("/utils/gnome-plugin.zip")) {
            IOUtils.FileUtil.writeByte(tempZip, pluginZip.readAllBytes(), false);
            IOUtils.ZipUtil.unzip(tempZip.getPath(), pluginPath + "/", true);
        } catch (IOException e) {
            Logger.error("EnvCheck", "Failed to install GNOME plugin", e);
            reason = "集成插件安装失败";
            detail = "无法解压文件到插件目录，请尝试手动安装插件。";
            return false;
        }
        reason = "即将完成插件安装";
        detail = "已成功安装集成插件，请注销并重新登录以使安装的插件生效。";
        return false;
    }

    @Override
    public boolean canFix() {
        return available;
    }

    @Override
    public boolean run() {
        if (!available) return false;
        try {
            Map<String, Variant<?>> ext = pi.GetExtensionInfo(Const.gnomePluginName);
            if (ext.isEmpty()) return false;
            double ver = (double) ext.get("version").getValue();
            conn.close();
            return ver == Const.gnomePluginVersion;
        } catch (Exception e) {
            Logger.error("EnvCheck", "Failed to get GNOME plugin info", e);
            available = false;
            reason = "获取插件信息失败";
            detail = "无法获取插件信息，请尝试手动检查并安装插件。";
            return false;
        }
    }

    @Override
    public boolean needConfirmFix() {
        return true;
    }

    @Override
    public String getFixReason() {
        return "安装 GNOME 集成插件";
    }

    @Override
    public String getFixDetail() {
        return """
                为了使桌宠能与 GNOME 桌面环境交互，我们需要将一个集成插件安装到您的系统上。
                您可以在 https://github.com/litwak913/Ark-Pets-Integration 处查看插件的源代码。""";
    }
}
