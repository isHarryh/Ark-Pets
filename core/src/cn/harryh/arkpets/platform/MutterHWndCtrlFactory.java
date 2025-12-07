package cn.harryh.arkpets.platform;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.rpc.MutterInterface;
import cn.harryh.arkpets.rpc.MutterPluginInterface;
import cn.harryh.arkpets.utils.Logger;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.types.Variant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class MutterHWndCtrlFactory extends HWndCtrlFactory{
    private static DBusConnection dBusConnection;

    static MutterInterface dBusInterface;

    public MutterHWndCtrlFactory() {
        try {
            dBusConnection = DBusConnectionBuilder.forSessionBus().build();
            Logger.info("System", "Connected to DBus");
            checkAndEnablePlugin();
            dBusInterface = dBusConnection.getRemoteObject("org.gnome.Shell", "/org/gnome/Shell/Extensions/ArkPets", MutterInterface.class);
            Logger.info("System", "GNOME Integration extension version " + dBusInterface.Version());
        } catch (DBusException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HWndCtrl findWindow(String className, String windowName) {
        return dBusInterface.List().stream().map(MutterHWndCtrl::new).filter((i) -> {
            if (className == null) {
                return i.windowText != null && i.windowText.equals(windowName);
            } else {
                return i.details.wclass.equals(className) && i.windowText.equals(windowName);
            }
        }).findAny().orElse(null);
    }

    @Override
    public List<? extends HWndCtrl> getWindowList(boolean onlyVisible) {
        List<MutterHWndCtrl> list = new ArrayList<>(dBusInterface.List().stream().map(MutterHWndCtrl::new).filter(w -> !onlyVisible || w.isVisible()).toList());
        Collections.reverse(list);
        return list;
    }

    @Override
    public HWndCtrl getTopmostWindow() {
        List<MutterInterface.DetailsStruct> list = dBusInterface.List();
        return new MutterHWndCtrl(list.get(list.size() - 1));
    }

    @Override
    public HWndCtrl.MousePoint getMousePos() {
        MutterInterface.PointStruct pos = dBusInterface.Mouse();
        return new HWndCtrl.MousePoint((pos.x), (pos.y));
    }

    @Override
    public void free() {
        try {
            if (dBusConnection != null) dBusConnection.close();
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

    private static void checkAndEnablePlugin() throws DBusException {
        MutterPluginInterface pi = dBusConnection.getRemoteObject("org.gnome.Shell", "/org/gnome/Shell", MutterPluginInterface.class);
        Map<String, Variant<?>> ext = pi.GetExtensionInfo(Const.gnomePluginName);
        if (ext.isEmpty()) throw new RuntimeException("GNOME Integration plugin not found.");
        Boolean enable = (Boolean) ext.get("enabled").getValue();
        if (!enable) {
            Logger.info("System","Enabling GNOME Integration plugin");
            boolean result = pi.EnableExtension(Const.gnomePluginName);
            if (!result) throw new RuntimeException("Failed to enable GNOME integration plugin.");
            try {
                Thread.sleep(500); // wait for loaded
            } catch (InterruptedException ignored) {
            }
        }
    }
}
