package cn.harryh.arkpets.natives;

import cn.harryh.arkpets.utils.Logger;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import org.lwjgl.glfw.GLFWNativeWayland;


public class WaylandHelper {
    private static boolean ready;
    private static boolean unavailable;
    private static WaylandClient.wl_registry_listener listener; // Prevent callback
    private static Pointer compositor;
    private static int compositorVer;

    private static Pointer wl_compositor_interface;
    private static Pointer wl_region_interface;

    private static void init() {
        NativeLibrary lib = NativeLibrary.getInstance("wayland-client");
        Pointer wl_registry_interface = lib.getGlobalVariableAddress("wl_registry_interface");
        wl_compositor_interface = lib.getGlobalVariableAddress("wl_compositor_interface");
        wl_region_interface = lib.getGlobalVariableAddress("wl_region_interface");
        Pointer display = new Pointer(GLFWNativeWayland.glfwGetWaylandDisplay());
        int ver = WaylandClient.INSTANCE.wl_proxy_get_version(display);
        Pointer reg = WaylandClient.INSTANCE.wl_proxy_marshal_flags(display, 1, wl_registry_interface, ver, 0, (Object) null);
        listener = new WaylandClient.wl_registry_listener();
        listener.global = (data, registry, name, iface, version) -> {
            if (iface.equals("wl_compositor")) {
                compositor = WaylandClient.INSTANCE.wl_proxy_marshal_flags(registry, 0,
                        wl_compositor_interface, 4, 0, name, "wl_compositor", 4, null);
                compositorVer = WaylandClient.INSTANCE.wl_proxy_get_version(compositor);
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
        ready = true;
    }

    public static void setTransparent(Pointer surface, boolean enable) {
        if (unavailable) return;
        if (!ready) init();
        int surfaceVer = WaylandClient.INSTANCE.wl_proxy_get_version(surface);
        if (enable) {
            Pointer region = WaylandClient.INSTANCE.wl_proxy_marshal_flags(compositor, 1, wl_region_interface, compositorVer, 0, (Object) null);
            int regionVer = WaylandClient.INSTANCE.wl_proxy_get_version(region);
            WaylandClient.INSTANCE.wl_proxy_marshal_flags(surface, 5, null, surfaceVer, 0, region);
            WaylandClient.INSTANCE.wl_proxy_marshal_flags(surface, 6, null, surfaceVer, 0);
            WaylandClient.INSTANCE.wl_proxy_marshal_flags(region, 0, null, regionVer, 1);
        } else {
            WaylandClient.INSTANCE.wl_proxy_marshal_flags(surface, 5, null, surfaceVer, 0, (Object) null);
            WaylandClient.INSTANCE.wl_proxy_marshal_flags(surface, 6, null, surfaceVer, 0);
        }
    }
}
