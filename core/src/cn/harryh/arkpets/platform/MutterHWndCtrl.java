package cn.harryh.arkpets.platform;

import cn.harryh.arkpets.natives.MutterInterface;
import cn.harryh.arkpets.utils.Logger;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class MutterHWndCtrl extends HWndCtrl {
    protected final UInt32 hWnd;
    protected MutterInterface.DetailsStruct details;
    private static DBusConnection dBusConnection;
    private static MutterInterface dBusInterface;

    protected MutterHWndCtrl(MutterInterface.DetailsStruct details) {
        super(details.title, new WindowRect(details.y, details.y + details.h.intValue(), details.x, details.x + details.w.intValue()));
        this.hWnd = details.id;
        this.details = details;
    }

    @Override
    public boolean isForeground() {
        return dBusInterface.IsActive(hWnd);
    }

    @Override
    public boolean isVisible() {
        return details.visible;
    }

    @Override
    public boolean close(int timeout) {
        return false;
    }

    @Override
    public HWndCtrl updated() {
        return new MutterHWndCtrl(dBusInterface.Details(hWnd));
    }

    @Override
    public void setForeground() {
        dBusInterface.Activate(hWnd);
    }

    @Override
    public void setWindowPosition(HWndCtrl insertAfter, int x, int y, int w, int h) {
        dBusInterface.MoveResize(hWnd, x, y, new UInt32(w), new UInt32(h));
    }

    @Override
    public void setTransparent(boolean enable) {

    }

    @Override
    public void setTaskbar(boolean enable) {
        dBusInterface.Stick(hWnd, !enable);
    }

    @Override
    public void setLayered(boolean enable) {

    }

    @Override
    public void setTopmost(boolean enable) {
        dBusInterface.Above(hWnd, enable);
    }

    @Override
    public void sendMouseEvent(MouseEvent msg, int x, int y) {

    }

    protected static void init() {
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

    protected static void free() {
        try {
            dBusConnection.close();
            Logger.info("System", "Disconnected from DBus");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkAndEnablePlugin() throws DBusException {
        PluginInterface pi = dBusConnection.getRemoteObject("org.gnome.Shell", "/org/gnome/Shell", PluginInterface.class);
        Map<String, Variant<?>> ext = pi.GetExtensionInfo("arkpets-integration@harryh.cn");
        if (ext.isEmpty()) throw new RuntimeException("GNOME Integration plugin not found.");
        Variant<Boolean> enable = (Variant<Boolean>) ext.get("enabled");
        if (!enable.getValue()) {
            Logger.info("System","Enabling GNOME Integration plugin");
            pi.EnableExtension("arkpets-integration@harryh.cn");
            try {
                Thread.sleep(500); // wait for loaded
            } catch (InterruptedException ignored) {
            }
        }
    }

    protected static MutterHWndCtrl find(String className, String windowName) {
        return dBusInterface.List().stream().map(MutterHWndCtrl::new).filter((i) -> {
            if (className == null) {
                return i.windowText != null && i.windowText.equals(windowName);
            } else {
                return i.details.wclass.equals(className) && i.windowText.equals(windowName);
            }
        }).findAny().orElse(null);
    }

    protected static List<MutterHWndCtrl> getWindowList(boolean onlyVisible) {
        List<MutterHWndCtrl> list = new ArrayList<>(dBusInterface.List().stream().map(MutterHWndCtrl::new).filter(w -> !onlyVisible || w.isVisible()).toList());
        Collections.reverse(list);
        return list;
    }

    protected static MutterHWndCtrl getTopmostWindow() {
        List<MutterInterface.DetailsStruct> list = dBusInterface.List();
        return new MutterHWndCtrl(list.get(list.size() - 1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MutterHWndCtrl hWndCtrl = (MutterHWndCtrl) o;
        return hWnd.equals(hWndCtrl.hWnd);
    }

    @Override
    public int hashCode() {
        return hWnd.hashCode();
    }

    @DBusInterfaceName("org.gnome.Shell.Extensions")
    private interface PluginInterface extends DBusInterface {
        boolean EnableExtension(String uuid);

        Map<String, Variant<?>> GetExtensionInfo(String uuid);
    }
}
