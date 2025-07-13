package cn.harryh.arkpets.platform;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.sun.jna.Pointer;

import static org.lwjgl.glfw.GLFWNativeWayland.glfwGetWaylandWindow;


public abstract class WaylandHWndCtrl extends HWndCtrl {
    protected Pointer surface;

    public WaylandHWndCtrl(String windowText, WindowRect windowRect) {
        super(windowText, windowRect);
    }

    @Override
    public void attachGLFWWindow(Lwjgl3Graphics graphics) {
        super.attachGLFWWindow(graphics);
        surface = new Pointer(glfwGetWaylandWindow(glfwHandle));
    }
}

