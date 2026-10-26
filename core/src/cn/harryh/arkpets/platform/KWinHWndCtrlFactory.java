package cn.harryh.arkpets.platform;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.rpc.KWinInterface;
import cn.harryh.arkpets.rpc.KWinPluginInterface;
import cn.harryh.arkpets.utils.Logger;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class KWinHWndCtrlFactory extends HWndCtrlFactory{
    static DBusConnection dBusConnection;
    static KWinInterface dBusInterface;

    public KWinHWndCtrlFactory() {
        try {
            dBusConnection = DBusConnectionBuilder.forSessionBus().build();
            Logger.info("System", "Connected to DBus");
            checkAndEnablePlugin();
            dBusInterface = dBusConnection.getRemoteObject("org.kde.KWin", "/ArkPets", KWinInterface.class);
            Logger.info("System", "KDE Integration plugin version " + dBusInterface.Version());
        } catch (DBusException e) {
            closeConnection();
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            closeConnection();
            throw e;
        }
    }

    @Override
    public HWndCtrl findWindow(String className, String windowName) {
        return dBusInterface.List().stream().map(KWinHWndCtrl::new).filter((i) -> {
            if (className == null) {
                return i.windowText != null && i.windowText.equals(windowName);
            } else {
                return i.details.wclass.equals(className) && i.windowText.equals(windowName);
            }
        }).findAny().orElse(null);
    }

    @Override
    public List<? extends HWndCtrl> getWindowList(boolean onlyVisible) {
        List<KWinHWndCtrl> list = new ArrayList<>(dBusInterface.List().stream().map(KWinHWndCtrl::new).filter(w -> !onlyVisible || w.isVisible()).toList());
        Collections.reverse(list);
        return list;    }

    @Override
    public HWndCtrl getTopmostWindow() {
        List<KWinInterface.DetailsStruct> list = dBusInterface.List();
        return list.isEmpty() ? null : new KWinHWndCtrl(list.get(list.size() - 1));
    }

    @Override
    public HWndCtrl.MousePoint getMousePos() {
        KWinInterface.PointStruct pos = dBusInterface.Mouse();
        return new HWndCtrl.MousePoint(pos.x, pos.y);
    }

    @Override
    public void free() {
        try {
            dBusConnection.close();
            Logger.info("System", "Disconnected from DBus");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean needResize() {
        return true;
    }

    @Override
    public boolean needDecorated() {
        return false;
    }

    private static void closeConnection() {
        if (dBusConnection != null) {
            try {
                dBusConnection.close();
            } catch (IOException e) {
                Logger.error("System", "Failed to close DBus connection.", e);
            }
        }
    }

    private static void checkAndEnablePlugin() throws DBusException {
        KWinPluginInterface pi = dBusConnection.getRemoteObject("org.kde.KWin", "/Plugins", KWinPluginInterface.class);
        String pluginName = Const.kdePluginName + Const.kdePluginVersion;
        List<String> available = pi.getAvailablePlugins();
        List<String> enabled = pi.getLoadedPlugins();
        if (!available.contains(pluginName)) throw new RuntimeException("KDE Integration plugin not found.");
        if (!enabled.contains(pluginName)) {
            boolean result = pi.LoadPlugin(pluginName);
            if (!result) throw new RuntimeException("Failed to enable KDE integration plugin.");
            try {
                Thread.sleep(500); // wait for loaded
            } catch (InterruptedException ignored) {
            }
        }
    }
}
