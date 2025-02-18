package cn.harryh.arkpets.natives;

import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.Position;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt32;

import java.util.List;


@DBusInterfaceName("org.gnome.Shell.Extensions.ArkPets")
public interface MutterInterface extends DBusInterface {
    void MoveResize(UInt32 winid, int x, int y, UInt32 width, UInt32 height);

    void Activate(UInt32 winid);

    void Above(UInt32 winid, boolean above);

    void Stick(UInt32 winid, boolean stick);

    List<DetailsStruct> List();

    DetailsStruct Details(UInt32 winid);

    boolean IsActive(UInt32 winid);

    String Version();

    class DetailsStruct extends Struct {
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