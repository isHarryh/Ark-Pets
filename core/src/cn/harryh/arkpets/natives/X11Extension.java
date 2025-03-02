package cn.harryh.arkpets.natives;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.X11;


public interface X11Extension extends X11 {
    X11Extension INSTANCE = Native.load("X11", X11Extension.class);

    void XMoveResizeWindow(Display display, Window w, int x, int y, int width, int height);

    Pointer XCreateRegion();

    int XDestroyRegion(Pointer r);

    Window XGetSelectionOwner(Display dis, Atom a);
}