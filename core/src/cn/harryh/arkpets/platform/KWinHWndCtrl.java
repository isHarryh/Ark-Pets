package cn.harryh.arkpets.platform;

import cn.harryh.arkpets.natives.KWinInterface;
import cn.harryh.arkpets.utils.Logger;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.types.UInt32;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class KWinHWndCtrl extends HWndCtrl {
    protected final String hWnd;
    protected KWinInterface.DetailsStruct details;
    private static DBusConnection dBusConnection;
    private static KWinInterface dBusInterface;

    protected KWinHWndCtrl(KWinInterface.DetailsStruct details) {
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
        return new KWinHWndCtrl(dBusInterface.Details(hWnd));
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
        dBusInterface.Stick(hWnd, enable);
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
            dBusInterface = dBusConnection.getRemoteObject("org.kde.KWin", "/ArkPets", KWinInterface.class);
            Logger.info("System", "KDE Integration plugin version " + dBusInterface.Version());
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

    protected static KWinHWndCtrl find(String className, String windowName) {
        return dBusInterface.List().stream().map(KWinHWndCtrl::new).filter((i) -> {
            if (className == null) {
                return i.windowText != null && i.windowText.equals(windowName);
            } else {
                return i.details.wclass.equals(className) && i.windowText.equals(windowName);
            }
        }).findAny().orElse(null);
    }

    protected static List<KWinHWndCtrl> getWindowList(boolean onlyVisible) {
        List<KWinHWndCtrl> list = new ArrayList<>(dBusInterface.List().stream().map(KWinHWndCtrl::new).filter(w -> !onlyVisible || w.isVisible()).toList());
        Collections.reverse(list);
        return list;
    }

    protected static KWinHWndCtrl getTopmostWindow() {
        List<KWinInterface.DetailsStruct> list = dBusInterface.List();
        return new KWinHWndCtrl(list.get(list.size() - 1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KWinHWndCtrl hWndCtrl = (KWinHWndCtrl) o;
        return hWnd.equals(hWndCtrl.hWnd);
    }

    @Override
    public int hashCode() {
        return hWnd.hashCode();
    }
}
