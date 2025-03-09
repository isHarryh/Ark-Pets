package cn.harryh.arkpets.natives;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.ptr.IntByReference;


public interface XextExtension extends X11.Xext {
    XextExtension INSTANCE = Native.load("Xext", XextExtension.class);

    void XShapeCombineRegion(X11.Display dpy, X11.Window dest, int destKind, int xOff, int yOff, Pointer r, int op);

    boolean XShapeQueryExtension(X11.Display dpy, IntByReference event_basep,IntByReference error_basep);
}