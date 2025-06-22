package cn.harryh.arkpets.platform;

import cn.harryh.arkpets.natives.WaylandHelper;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.sun.jna.Pointer;

import static org.lwjgl.glfw.GLFWNativeWayland.glfwGetWaylandWindow;


public abstract class WaylandHWndCtrl extends HWndCtrl {
    private boolean isTransparent;
    private Pointer surface;

    public WaylandHWndCtrl(String windowText, WindowRect windowRect) {
        super(windowText, windowRect);
    }

    public void attachSurface(Lwjgl3Graphics graphics) {
        long glfwHandle = graphics.getWindow().getWindowHandle();
        surface = new Pointer(glfwGetWaylandWindow(glfwHandle));
    }

    @Override
    public void setTransparent(boolean enable) {
        if (isTransparent != enable) {
            WaylandHelper.setTransparent(surface, enable);
            isTransparent = enable;
        }
    }
}

