package cn.harryh.arkpets.platform;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.natives.CoreGraphics;
import cn.harryh.arkpets.natives.ObjCHelper;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.platform.mac.CoreFoundation.CFArrayRef;
import com.sun.jna.platform.mac.CoreFoundation.CFDictionaryRef;
import com.sun.jna.platform.mac.CoreFoundation.CFNumberRef;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.harryh.arkpets.natives.CoreGraphics.*;


public class QuartzHWndCtrl extends HWndCtrl {
    private static Pointer nsApp;

    private final IgnoreMouseCallback igcb = new IgnoreMouseCallback();
    private final FrameCallback fcb = new FrameCallback();

    private final long windowID;
    private Pointer nsWin;
    private CGRect currentScreenRect;
    private final long layer;
    // 0:Uncheck 1:Checked,Available -1:Checked,Unavailable
    private byte nsWinUnavailable;
    private boolean trans;
    private final CGRect.ByValue newRect = new CGRect.ByValue();

    public QuartzHWndCtrl(CFDictionaryRef dict) {
        super(getWindowName(dict.getValue(kCGWindowOwnerName), dict.getValue(kCGWindowName)), getWindowRect(dict.getValue(kCGWindowBounds)));
        windowID = new CFNumberRef(dict.getValue(kCGWindowNumber)).longValue();
        layer = new CFNumberRef(dict.getValue(kCGWindowLayer)).longValue();
    }

    @Override
    public boolean isForeground() {
        getNSWindow(windowID);
        return ObjCHelper.msgSend.invokeInt(new Object[]{
                nsWin,
                ObjCHelper.sel("isKeyWindow")
        }) == 1;
    }

    @Override
    public boolean isVisible() {
        return false;
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
        if (arr != null) {
            CFArrayRef win = CoreGraphics.INSTANCE.CGWindowListCreateDescriptionFromArray(arr);
            arr.release();
            CFDictionaryRef dict = new CFDictionaryRef(win.getValueAtIndex(0));
            hwnd=new QuartzHWndCtrl(dict);
            win.release();
            carr.close();
            return hwnd;
        } else {
            return new NullHWndCtrl();
        }
    }

    @Override
    public void setForeground() {
        getNSWindow(windowID);
        ObjCHelper.msgSend.invokeVoid(new Object[]{
                nsWin,
                ObjCHelper.sel("orderFrontRegardless:")
        });
    }

    @Override
    public void setWindowPosition(HWndCtrl insertAfter, int x, int y, int w, int h) {
        getNSWindow(windowID);
        newRect.origin.x = x;
        newRect.origin.y = currentScreenRect.size.height - y - h;
        newRect.size.width = w;
        newRect.size.height = h;
        ObjCHelper.msgSend.invokeVoid(new Object[]{
                nsWin,
                ObjCHelper.sel("performSelectorOnMainThread:withObject:waitUntilDone:"),
                ObjCHelper.sel("apRunOnAppKitFrame"),
                null,
                1
        });
    }

    @Override
    public void setTaskbar(boolean enable) {
        checkNSApp();
        ObjCHelper.msgSend.invokeVoid(new Object[]{
                nsApp,
                ObjCHelper.sel("setActivationPolicy:"),
                enable ? 0 : 1
        });
    }

    @Override
    public void setTopmost(boolean enable) {
        getNSWindow(windowID);
        ObjCHelper.msgSend.invokeVoid(new Object[]{
                nsWin,
                ObjCHelper.sel("setLevel:"),
                enable ? NSStatusWindowLevel : NSNormalWindowLevel
        });
    }

    @Override
    public void setTransparent(boolean enable) {
        if (trans == enable) return;
        getNSWindow(windowID);
        trans = enable;
        ObjCHelper.msgSend.invokeVoid(new Object[]{
                nsWin,
                ObjCHelper.sel("performSelectorOnMainThread:withObject:waitUntilDone:"),
                ObjCHelper.sel("apRunOnAppKitIgnoreMouse"),
                null,
                1
        });
    }

    @Override
    public void sendMouseEvent(MouseEvent msg, int x, int y) {

    }

    protected static void init() {
        CFDictionaryRef server = CoreGraphics.INSTANCE.CGSessionCopyCurrentDictionary();
        if (server == null) {
            throw new RuntimeException("No window server connection.");
        } else {
            CoreFoundation.INSTANCE.CFRelease(server);
        }
        ObjCHelper.init();
    }

    protected static void free() {

    }

    protected static List<QuartzHWndCtrl> getWindowList(boolean onlyVisible) {
        ArrayList<QuartzHWndCtrl> list = new ArrayList<>();
        //todo
        int opt;
        if (onlyVisible) {
            opt = kCGWindowListOptionOnScreenOnly | kCGWindowListExcludeDesktopElements;
        } else {
            opt = kCGWindowListExcludeDesktopElements;
        }
        CFArrayRef windows = CoreGraphics.INSTANCE.CGWindowListCopyWindowInfo(opt, 0);
        int numWindows = windows.getCount();
        for (int i = 0; i < numWindows; i++) {
            Pointer result = windows.getValueAtIndex(i);
            CFDictionaryRef windowRef = new CFDictionaryRef(result);
            QuartzHWndCtrl win = new QuartzHWndCtrl(windowRef);
            if (!onlyVisible || (win.layer >= 0 && win.layer != 20)) {
                list.add(win);
            }
        }
        windows.release();
        return list;
    }

    protected static QuartzHWndCtrl find(String className, String windowText) {
        CFArrayRef windows = CoreGraphics.INSTANCE.CGWindowListCopyWindowInfo(kCGWindowListOptionOnScreenOnly | kCGWindowListExcludeDesktopElements, 0);
        int numWindows = windows.getCount();
        QuartzHWndCtrl win = null;
        for (int i = 0; i < numWindows; i++) {
            Pointer result = windows.getValueAtIndex(i);
            CFDictionaryRef windowRef = new CFDictionaryRef(result);
            String cname = getWindowName(windowRef.getValue(kCGWindowOwnerName));
            String wname = getWindowName(windowRef.getValue(kCGWindowName));
            if (className == null) {
                if (wname.equals(windowText) || cname.equals(windowText)) {
                    win = new QuartzHWndCtrl(windowRef);
                    break;
                }
            } else {
                if (cname.equals(className) && wname.equals(windowText)) {
                    win = new QuartzHWndCtrl(windowRef);
                    break;
                }
            }
        }
        windows.release();
        return win;
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
            nsApp = ObjCHelper.msgSend.invokePointer(new Object[]{
                    ObjCHelper.cls("NSApplication"),
                    ObjCHelper.sel("sharedApplication")
            });
        }
    }

    private void getNSWindow(long CGWindowId) {
        checkNSApp();
        if (nsWinUnavailable == 0) {
            Pointer nswin = ObjCHelper.msgSend.invokePointer(new Object[]{
                    nsApp,
                    ObjCHelper.sel("windowWithWindowNumber:"),
                    CGWindowId
            });
            if (nswin == null) {
                nsWinUnavailable = -1;
            }
            this.nsWin = nswin;
            nsWinUnavailable = 1;
            registerMethods(nsWin);
            currentScreenRect = getScreenSize();
        }
    }

    private void registerMethods(Pointer nsWin) {
        Pointer cls = ObjCHelper.msgSend.invokePointer(new Object[]{
                nsWin,
                ObjCHelper.sel("class")
        });
        ObjCHelper.addRunOnAppKitMethod(cls, fcb, "Frame");
        ObjCHelper.addRunOnAppKitMethod(cls, igcb, "IgnoreMouse");
        ObjCHelper.msgSend.invokeVoid(new Object[]{
                nsWin,
                ObjCHelper.sel("performSelectorOnMainThread:withObject:waitUntilDone:"),
                ObjCHelper.sel("apRunOnAppKitIgnoreMouse"),
                null,
                1
        });
    }

    private static String getWindowName(Pointer value) {
        return value == null ? "" : new CFStringRef(value).stringValue();
    }

    private static String getWindowName(Pointer own, Pointer title) {
        String ownName;
        String titleName;
        ownName = own == null ? "" : new CFStringRef(own).stringValue();
        titleName = title == null ? "" : new CFStringRef(title).stringValue();
        if (titleName.isEmpty()) return ownName;
        return titleName;
    }

    private static WindowRect getWindowRect(Pointer value) {
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

    private CGRect getScreenSize() {
        Pointer nsScreen = ObjCHelper.msgSend.invokePointer(new Object[]{
                nsWin,
                ObjCHelper.sel("screen")
        });
        if (Const.isARM) {
            return (CGRect.ByValue) ObjCHelper.msgSend.invoke(CGRect.ByValue.class, new Object[]{
                    nsScreen,
                    ObjCHelper.sel("frame")
            });
        } else {
            CGRect.ByReference rect = new CGRect.ByReference();
            ObjCHelper.msgSend_stret.invokeVoid(new Object[]{
                    rect,
                    nsScreen,
                    ObjCHelper.sel("frame")
            });
            return rect;
        }
    }

    private class IgnoreMouseCallback implements ObjCHelper.ThreadCallback {
        @Override
        public void callback(Pointer id, Pointer _cmd) {
            ObjCHelper.msgSend.invokeVoid(new Object[]{
                    id,
                    ObjCHelper.sel("setIgnoresMouseEvents:"),
                    trans ? 1 : 0
            });
        }
    }

    private class FrameCallback implements ObjCHelper.ThreadCallback {
        @Override
        public void callback(Pointer id, Pointer _cmd) {
            ObjCHelper.msgSend.invokeVoid(new Object[]{
                    nsWin,
                    ObjCHelper.sel("setFrame:display:animate:"),
                    newRect,
                    1, 0
            });
        }
    }
}
