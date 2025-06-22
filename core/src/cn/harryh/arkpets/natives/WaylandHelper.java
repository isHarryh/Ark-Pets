package cn.harryh.arkpets.natives;

import cn.harryh.arkpets.utils.Logger;
import com.sun.jna.Pointer;
import org.lwjgl.glfw.GLFWNativeWayland;


public class WaylandHelper {
    private static boolean ready;
    private static boolean unavailable;
    private static WaylandClient.wl_registry_listener listener; // Prevent callback
    private static Pointer compositor;
    private static Pointer region;

    private static void init() {
        Pointer display = new Pointer(GLFWNativeWayland.glfwGetWaylandDisplay());
        Pointer reg = WaylandClient.INSTANCE.wl_display_get_registry(display);
        listener = new WaylandClient.wl_registry_listener();
        listener.global = (data, registry, name, iface, version) -> {
            if (iface.equals("wl_compositor")) {
                compositor = WaylandClient.INSTANCE.wl_proxy_marshal_flags(registry, 0,
                        WaylandInterface.wl_compositor_interface, version, 0, name, "wl_compositor", version, null);
            }
        };
        listener.global_remove = (data, registry, name) -> {
            if (compositor != null) {
                WaylandClient.INSTANCE.wl_proxy_destroy(compositor);
                compositor = null;
            }
        };
        listener.write();
        WaylandClient.INSTANCE.wl_proxy_add_listener(reg, listener.getPointer(), null);
        WaylandClient.INSTANCE.wl_display_dispatch(display);
        WaylandClient.INSTANCE.wl_display_roundtrip(display);
        if (compositor == null) {
            Logger.warn("System", "Failed to bind wl_compositor");
            unavailable = true;
            return;
        }
        region = WaylandClient.INSTANCE.wl_compositor_create_region(compositor);
        ready = true;
    }

    public static void setTransparent(Pointer surface, boolean enable) {
        if (unavailable) return;
        if (!ready) init();
        if (enable) {
            WaylandClient.INSTANCE.wl_surface_set_input_region(surface, region);
            WaylandClient.INSTANCE.wl_surface_commit(surface);
        } else {
            WaylandClient.INSTANCE.wl_surface_set_input_region(surface, null);
            WaylandClient.INSTANCE.wl_surface_commit(surface);
        }
    }
}
