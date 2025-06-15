package cn.harryh.arkpets.platform;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.sun.jna.Pointer;

import static org.lwjgl.glfw.GLFWNativeWayland.glfwGetWaylandWindow;


public interface WaylandHWnd {
    default void initSurface(Lwjgl3Graphics graphics) {
        long glfwHandle = graphics.getWindow().getWindowHandle();
        setSurface(new Pointer(glfwGetWaylandWindow(glfwHandle)));
    }
    void setSurface(Pointer surface);
}

