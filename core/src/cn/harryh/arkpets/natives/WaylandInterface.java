package cn.harryh.arkpets.natives;

import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;


public class WaylandInterface {
    public static final Pointer wl_registry_interface;
    public static final Pointer wl_compositor_interface;
    public static final Pointer wl_region_interface;

    static {
        NativeLibrary lib = NativeLibrary.getInstance("wayland-client");
        wl_registry_interface = lib.getGlobalVariableAddress("wl_registry_interface");
        wl_compositor_interface = lib.getGlobalVariableAddress("wl_compositor_interface");
        wl_region_interface = lib.getGlobalVariableAddress("wl_region_interface");
    }
}
