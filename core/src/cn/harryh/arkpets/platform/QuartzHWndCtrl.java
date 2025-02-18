package cn.harryh.arkpets.platform;

import cn.harryh.arkpets.natives.CoreGraphicsHelper;
import cn.harryh.arkpets.natives.ObjCHelper;
import com.sun.jna.*;
import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.platform.mac.CoreFoundation.CFArrayRef;
import com.sun.jna.platform.mac.CoreFoundation.CFDictionaryRef;
import com.sun.jna.platform.mac.CoreFoundation.CFNumberRef;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.harryh.arkpets.natives.CoreGraphicsHelper.*;


public class QuartzHWndCtrl extends HWndCtrl {
    private static Pointer nsApp;

    private boolean trans = true;
    private static final boolean isArm = Platform.isARM();

    private long windowID;
    private Pointer nsWin;
    private Pointer nsScreen;
    private long layer;
    // 0:Uncheck 1:Checked,Available -1:Checked,Unavailable
    private byte nsWinUnavailable;

    public QuartzHWndCtrl(CFDictionaryRef dict) {
        super(getWindowName(dict.getValue(kCGWindowOwnerName), dict.getValue(kCGWindowName)), getWindowRect(dict.getValue(kCGWindowBounds)));
        windowID = new CFNumberRef(dict.getValue(kCGWindowNumber)).longValue();
        layer = new CFNumberRef(dict.getValue(kCGWindowLayer)).longValue();
    }

    @Override
    public boolean isForeground() {
        //todo
        return true;
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
        //todo
        /*QuartzHWndCtrl hwnd = null;
        CFIndex index = new CFIndex(1);
        LongByReference wid = new LongByReference(windowID);
        CFArrayRef arr = CFExt.INSTANCE.CFArrayCreate(null,new Pointer[] {wid.getPointer()},index,null);
        if (arr != null) {
            CFArrayRef win = CoreGraphics.INSTANCE.CGWindowListCreateDescriptionFromArray(arr);
            arr.release();
            CFDictionaryRef dict = new CFDictionaryRef(win.getValueAtIndex(0));
            hwnd=new QuartzHWndCtrl(dict);
            win.release();
        }*/
        return new NullHWndCtrl();
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
        CGRect rect = getScreenSize();
        CGRect.ByValue newRect = new CGRect.ByValue();
        newRect.origin.x = x;
        newRect.origin.y = rect.size.height - y - h;
        newRect.size.width = w;
        newRect.size.height = h;
        ObjCHelper.runOnAppKit(() -> ObjCHelper.msgSend.invokeVoid(new Object[]{
                nsWin,
                ObjCHelper.sel("setFrame:display:animate:"),
                newRect,
                1, 0
        }));
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
    public void setLayered(boolean enable) {
        // not necessary in macOS.
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
        ObjCHelper.runOnAppKit(() -> ObjCHelper.msgSend.invokeVoid(new Object[]{
                nsWin,
                ObjCHelper.sel("setIgnoresMouseEvents:"),
                enable ? 1 : 0
        }));
        trans = enable;
    }

    @Override
    public void sendMouseEvent(MouseEvent msg, int x, int y) {

    }

    protected static void init() {
        CFDictionaryRef server = CGInterface.INSTANCE.CGSessionCopyCurrentDictionary();
        if (server == null) {
            throw new RuntimeException("No window server connection.");
        } else {
            CoreFoundation.INSTANCE.CFRelease(server);
        }
        CoreGraphicsHelper.initCG();
        ObjCHelper.init();
    }

    protected static void free() {
        CoreGraphicsHelper.freeCG();
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
        CFArrayRef windows = CGInterface.INSTANCE.CGWindowListCopyWindowInfo(opt, 0);
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
        CFArrayRef windows = CGInterface.INSTANCE.CGWindowListCopyWindowInfo(kCGWindowListOptionOnScreenOnly | kCGWindowListExcludeDesktopElements, 0);
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
        }
    }

    private void getNSScreen() {
        if (this.nsWinUnavailable != 1 || this.nsScreen != null) return;
        this.nsScreen = ObjCHelper.msgSend.invokePointer(new Object[]{
                nsWin,
                ObjCHelper.sel("screen")
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
            boolean success = CGInterface.INSTANCE.CGRectMakeWithDictionaryRepresentation(new CFDictionaryRef(value), rect);
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
        getNSScreen();
        if (isArm) {
            return (CGRect.ByValue) ObjCHelper.msgSend.invoke(CGRect.ByValue.class, new Object[]{
                    nsScreen,
                    ObjCHelper.sel("frame")
            });
        } else {
            CoreGraphicsHelper.CGRect.ByReference rect = new CoreGraphicsHelper.CGRect.ByReference();
            ObjCHelper.msgSend_stret.invokeVoid(new Object[]{
                    rect,
                    nsScreen,
                    ObjCHelper.sel("frame")
            });
            return rect;
        }
    }
}
