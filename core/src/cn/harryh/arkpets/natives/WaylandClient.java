package cn.harryh.arkpets.natives;

import com.sun.jna.*;


public interface WaylandClient extends Library {
    WaylandClient INSTANCE = Native.load("wayland-client", WaylandClient.class);

    int wl_display_dispatch(Pointer display);

    int wl_display_roundtrip(Pointer display);

    int wl_proxy_get_version(Pointer proxy);

    Pointer wl_proxy_marshal_flags(Pointer proxy, int opcode, Pointer iface, int version, int flags, Object... obj);

    int wl_proxy_add_listener(Pointer proxy, Pointer implementation, Pointer data);

    void wl_proxy_destroy(Pointer compositor);

    @Structure.FieldOrder({"global", "global_remove"})
    class wl_registry_listener extends Structure {
        public GlobalCallback global;
        public GlobalRemoveCallback global_remove;
    }

    interface GlobalCallback extends Callback {
        void callback(Pointer data, Pointer registry, int name, String iface, int version);
    }

    interface GlobalRemoveCallback extends Callback {
        void callback(Pointer data, Pointer registry, int name);
    }
}
