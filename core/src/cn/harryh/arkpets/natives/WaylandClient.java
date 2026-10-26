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

    default void wl_region_destroy(Pointer wl_region) {
        wl_proxy_marshal_flags(wl_region, 0, null, wl_proxy_get_version(wl_region), 1);
    }

    default void wl_surface_set_input_region(Pointer wl_surface, Pointer wl_region) {
        wl_proxy_marshal_flags(wl_surface, 5, null, wl_proxy_get_version(wl_surface), 0, wl_region);
    }

    default void wl_surface_commit(Pointer wl_surface) {
        wl_proxy_marshal_flags(wl_surface, 6, null, wl_proxy_get_version(wl_surface), 0);
    }

    default Pointer wl_compositor_create_region(Pointer wl_compositor) {
        return wl_proxy_marshal_flags(wl_compositor, 1, WaylandInterface.wl_region_interface, wl_proxy_get_version(wl_compositor), 0, (Object) null);
    }

    default Pointer wl_display_get_registry(Pointer wl_display) {
        return wl_proxy_marshal_flags(wl_display, 1, WaylandInterface.wl_registry_interface, wl_proxy_get_version(wl_display), 0, (Object) null);
    }

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
