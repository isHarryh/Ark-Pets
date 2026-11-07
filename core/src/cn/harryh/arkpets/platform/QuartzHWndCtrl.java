package cn.harryh.arkpets.platform;

import cn.harryh.arkpets.natives.CoreGraphics;
import cn.harryh.arkpets.natives.MacHelper;
import cn.harryh.arkpets.natives.ObjCHelper;
import cn.harryh.arkpets.utils.Logger;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.platform.mac.CoreFoundation.CFArrayRef;
import com.sun.jna.platform.mac.CoreFoundation.CFDictionaryRef;
import com.sun.jna.platform.mac.CoreFoundation.CFNumberRef;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;
import org.lwjgl.glfw.GLFWNativeCocoa;

import java.util.Objects;

import static cn.harryh.arkpets.natives.CoreGraphics.*;
import static cn.harryh.arkpets.platform.QuartzHWndCtrlFactory.currentScreenRect;
import static org.lwjgl.glfw.GLFWNativeCocoa.glfwGetCocoaWindow;


public class QuartzHWndCtrl extends HWndCtrl {
    private static Pointer nsApp;

    private final long windowID;
    private Pointer nsWin;
    long layer;
    boolean onScreen;
    // 0:Uncheck 1:Checked,Available -1:Checked,Unavailable
    private final CGRect.ByValue newRect = new CGRect.ByValue();

    public QuartzHWndCtrl(CFDictionaryRef dict) {
        super(getWindowName(dict.getValue(kCGWindowOwnerName), dict.getValue(kCGWindowName)), getWindowRect(dict));
        windowID = new CFNumberRef(dict.getValue(kCGWindowNumber)).longValue();
        layer = new CFNumberRef(dict.getValue(kCGWindowLayer)).longValue();
        Pointer onScreenRef = dict.getValue(kCGWindowIsOnscreen);
        if (onScreenRef!=null) {
            onScreen = new CoreFoundation.CFBooleanRef(onScreenRef).booleanValue();
        } else {
            onScreen = false;
        }
    }

    @Override
    public boolean isForeground() {
        if (nsWin == null) return false;
        return ObjCHelper.sendInt(nsWin, "isKeyWindow") != 0;
    }

    @Override
    public boolean isVisible() {
        return onScreen;
    }

    @Override
    public boolean close(int timeout) {
        //todo
        return true;
    }

    @Override
    public HWndCtrl updated() {
        QuartzHWndCtrl hwnd;
        CoreFoundation.CFIndex index = new CoreFoundation.CFIndex(1);
        Memory carr = new Memory(Native.getNativeSize(Integer.class));
        carr.write(0,new int[] {(int) windowID},0,1);
        CFArrayRef arr = CoreFoundation.INSTANCE.CFArrayCreate(null,carr,index,null);
        CFArrayRef win = null;
        try {
            if (arr == null) {
                return new NullHWndCtrl();
            }
            win = CoreGraphics.INSTANCE.CGWindowListCreateDescriptionFromArray(arr);
            if (win == null || win.getCount() == 0) {
                return new NullHWndCtrl();
            }
            Pointer first = win.getValueAtIndex(0);
            if (first == null) {
                return new NullHWndCtrl();
            }
            hwnd = new QuartzHWndCtrl(new CFDictionaryRef(first));
            return hwnd;
        } finally {
            if (arr != null) arr.release();
            if (win != null) win.release();
            carr.close();
        }
    }

    @Override
    public void setForeground() {
        if (nsWin == null) return;
        ObjCHelper.runOnMain(() -> ObjCHelper.sendVoid(nsWin, "orderFrontRegardless:"));
    }

    @Override
    public void setWindowPosition(HWndCtrl insertAfter, int x, int y, int w, int h) {
        if (nsWin == null) return;
        newRect.origin.x = x;
        newRect.origin.y = currentScreenRect.getValue().size.height - y - h;
        newRect.size.width = (w);
        newRect.size.height = (h);
        ObjCHelper.runOnMain(() ->
                ObjCHelper.sendVoid(nsWin, "setFrame:display:animate:", newRect, true, false));
    }

    @Override
    public void setTaskbar(boolean enable) {
        checkNSApp();
        if (nsApp == null) return;
        long policy = enable ? 0 : 1;
        ObjCHelper.runOnMain(() -> ObjCHelper.sendVoid(nsApp, "setActivationPolicy:", policy));
    }

    @Override
    public void setTopmost(boolean enable) {
        if(nsWin == null) return;
        layer = enable ? NSStatusWindowLevel : NSNormalWindowLevel;
        long level = layer;
        ObjCHelper.runOnMain(() -> ObjCHelper.sendVoid(nsWin, "setLevel:", level));
    }

    @Override
    public void sendMouseEvent(MouseEvent msg, int x, int y) {

    }

    @Override
    public void attachGLFWWindow(Lwjgl3Graphics graphics) {
        super.attachGLFWWindow(graphics);
        nsWin = new Pointer(glfwGetCocoaWindow(glfwHandle));

        long cocoaView = GLFWNativeCocoa.glfwGetCocoaView(glfwHandle);
        if (cocoaView != 0) {
            MacHelper.tryFixLayers(new Pointer(cocoaView));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuartzHWndCtrl hWndCtrl = (QuartzHWndCtrl) o;
        return windowID == hWndCtrl.windowID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(windowID);
    }

    private void checkNSApp() {
        if (nsApp == null) {
            nsApp = ObjCHelper.sendPointer(ObjCHelper.cls("NSApplication"), "sharedApplication");
        }
    }

    static String getWindowName(Pointer value) {
        return value == null ? "" : new CFStringRef(value).stringValue();
    }

    static int getWindowPid(CFDictionaryRef dict) {
        Pointer value = dict.getValue(kCGWindowOwnerPID);
        return value == null ? -1 : new CoreFoundation.CFNumberRef(value).intValue();
    }

    private static boolean isDockWindow(CFDictionaryRef dict) {
        long dockPid = QuartzHWndCtrlFactory.dockPid;
        return dockPid > 0 && getWindowPid(dict) == dockPid;
    }

    static String getWindowName(Pointer own, Pointer title) {
        String ownName;
        String titleName;
        ownName = own == null ? "" : new CFStringRef(own).stringValue();
        titleName = title == null ? "" : new CFStringRef(title).stringValue();
        if (titleName.isEmpty()) return ownName;
        return titleName;
    }

    private static WindowRect getWindowRect(CFDictionaryRef dict) {
        if (isDockWindow(dict)) {
            CoreGraphics.CGRect dockRect = QuartzHWndCtrlFactory.dockFrame.getValue();
            if (dockRect != null) {
                return new WindowRect(
                        (int) Math.round(dockRect.origin.y),
                        (int) Math.round(dockRect.origin.y + dockRect.size.height),
                        (int) Math.round(dockRect.origin.x),
                        (int) Math.round(dockRect.origin.x + dockRect.size.width)
                );
            }
            // The CGWindowBounds of the Dock covers the whole screen, so it must never be used as a fallback:
            // it would shadow the barriers of every other window. Return an empty rect instead, which never
            // matches the pet or the walkable line.
            Logger.debug("System", "Dock frame is unavailable, ignoring the Dock window");
            return new WindowRect(0, 0, 0, 0);
        }
        Pointer value = dict.getValue(kCGWindowBounds);
        if (value != null) {
            CGRect.ByReference rect = new CGRect.ByReference();
            boolean success = CoreGraphics.INSTANCE.CGRectMakeWithDictionaryRepresentation(new CFDictionaryRef(value), rect);
            if (success) {
                return new WindowRect(
                        (int) Math.round(rect.origin.y),
                        (int) Math.round(rect.origin.y + rect.size.height),
                        (int) Math.round(rect.origin.x),
                        (int) Math.round(rect.origin.x + rect.size.width)
                );
            }
        }
        return new WindowRect(0, 0, 0, 0);
    }
}
