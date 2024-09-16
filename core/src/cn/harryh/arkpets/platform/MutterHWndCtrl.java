package cn.harryh.arkpets.platform;

import cn.harryh.arkpets.utils.Logger;
import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.Position;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt32;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MutterHWndCtrl extends HWndCtrl {
    protected final UInt32 hWnd;
    protected DetailsStruct details;
    private static DBusConnection dBusConnection;
    private static ArkPetsInterface dBusInterface;

    protected MutterHWndCtrl(DetailsStruct details) {
        super(details.title, new WindowRect(details.y, details.y + details.h.intValue(), details.x + details.w.intValue(), details.x));
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
    public void setWindowAlpha(float alpha) {
        dBusInterface.Alpha(hWnd, new UInt32((long) (alpha * 255L)));
    }

    @Override
    public void setWindowPosition(HWndCtrl insertAfter, int x, int y, int w, int h) {
        dBusInterface.MoveResize(hWnd,x,y,new UInt32(w),new UInt32(h));
    }

    @Override
    public void setWindowTransparent(boolean enable) {

    }

    @Override
    public void setToolWindow(boolean enable) {

    }

    @Override
    public void setLayered(boolean enable) {

    }

    @Override
    public void setTopmost(boolean enable) {
        if (enable) {
            dBusInterface.Above(hWnd);
        } else {
            dBusInterface.Unabove(hWnd);
        }
    }

    @Override
    public void sendMouseEvent(MouseEvent msg, int x, int y) {

    }

    protected static void init() {
        try {
            dBusConnection = DBusConnectionBuilder.forSessionBus().build();
            Logger.info("System", "Connected to DBus");
            dBusInterface = dBusConnection.getRemoteObject("org.gnome.Shell", "/org/gnome/Shell/Extensions/ArkPets", ArkPetsInterface.class);
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
        List<DetailsStruct> list = dBusInterface.List();
        return new MutterHWndCtrl(list.get(list.size() - 1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MutterHWndCtrl hWndCtrl = (MutterHWndCtrl)o;
        return hWnd.equals(hWndCtrl.hWnd);
    }

    @Override
    public int hashCode() {
        return hWnd.hashCode();
    }

    @DBusInterfaceName("org.gnome.Shell.Extensions.ArkPets")
    private interface ArkPetsInterface extends DBusInterface {
        void MoveResize(UInt32 winid, int x, int y, UInt32 width, UInt32 height);

        void Activate(UInt32 winid);

        void Above(UInt32 winid);

        void Unabove(UInt32 winid);

        List<DetailsStruct> List();

        DetailsStruct Details(UInt32 winid);

        void Alpha(UInt32 winid, UInt32 alpha);

        boolean IsActive(UInt32 winid);

        UInt32 Version();
    }

    public static class DetailsStruct extends Struct {
        @Position(0)
        public final int x;
        @Position(1)
        public final int y;
        @Position(2)
        public final UInt32 w;
        @Position(3)
        public final UInt32 h;
        @Position(4)
        public final String title;
        @Position(5)
        public final String wclass;
        @Position(6)
        public final boolean visible;
        @Position(7)
        public final UInt32 id;

        public DetailsStruct(int x, int y, UInt32 w, UInt32 h, String title, String wClass, boolean visible, UInt32 id) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.title = title;
            this.wclass = wClass;
            this.visible = visible;
            this.id = id;
        }
    }
}
