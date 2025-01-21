package cn.harryh.arkpets.platform;

import cn.harryh.arkpets.utils.Logger;
import com.sun.jna.*;
import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.platform.mac.CoreFoundation.CFNumberRef;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;
import com.sun.jna.platform.mac.CoreFoundation.CFArrayRef;
import com.sun.jna.platform.mac.CoreFoundation.CFDictionaryRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class QuartzHWndCtrl extends HWndCtrl{
    private static Pointer nsapp;
    private static CFStringRef kCGWindowNumber;
    private static CFStringRef kCGWindowLayer;
    private static CFStringRef kCGWindowBounds;
    private static CFStringRef kCGWindowName;
    private static CFStringRef kCGWindowOwnerName;
    private static final int kCGWindowListExcludeDesktopElements = (1 << 4);
    private static final int kCGWindowListOptionOnScreenOnly = 1;

    private long windowID;
    private Pointer nsWin;
    private long layer;
    // 0:Uncheck 1:Checked,Available -1:Checked,Unavailable
    private byte nsWinUnavailable;

    public QuartzHWndCtrl(CFDictionaryRef dict){
        super(getWindowName(dict.getValue(kCGWindowOwnerName),dict.getValue(kCGWindowName)),getWindowRect(dict.getValue(kCGWindowBounds)));
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
        GoldenGlow.INSTANCE.APActive(nsWin);
    }

    @Override
    public void setWindowPosition(HWndCtrl insertAfter, int x, int y, int w, int h) {
        getNSWindow(windowID);
        GoldenGlow.INSTANCE.APResize(nsWin,x,y,w,h);
    }

    @Override
    public void setTaskbar(boolean enable) {
        checkNSApp();
        GoldenGlow.INSTANCE.APSetDock(nsapp,enable);
    }

    @Override
    public void setLayered(boolean enable) {
        // not necessary in macOS.
    }

    @Override
    public void setTopmost(boolean enable) {
        getNSWindow(windowID);
        GoldenGlow.INSTANCE.APSetTopmost(nsWin,enable);
    }

    @Override
    public void setTransparent(boolean enable) {
        // not necessary in macOS.
    }

    @Override
    public void sendMouseEvent(MouseEvent msg, int x, int y) {

    }
    protected static void init() {
        Logger.info("System", "Objective-C bridge library version " + GoldenGlow.INSTANCE.APVersion());
        CFDictionaryRef server = CoreGraphics.INSTANCE.CGSessionCopyCurrentDictionary();
        if (server == null) {
            throw new RuntimeException("No window server connection.");
        } else {
            CoreFoundation.INSTANCE.CFRelease(server);
        }
        kCGWindowNumber = CFStringRef.createCFString("kCGWindowNumber");
        kCGWindowBounds = CFStringRef.createCFString("kCGWindowBounds");
        kCGWindowLayer = CFStringRef.createCFString("kCGWindowLayer");
        kCGWindowName = CFStringRef.createCFString("kCGWindowName");
        kCGWindowOwnerName = CFStringRef.createCFString("kCGWindowOwnerName");
    }

    protected static void free() {
        kCGWindowNumber.release();
        kCGWindowLayer.release();
        kCGWindowName.release();
        kCGWindowBounds.release();
        kCGWindowOwnerName.release();
    }

    protected static List<QuartzHWndCtrl> getWindowList(boolean onlyVisible) {
        ArrayList<QuartzHWndCtrl> list = new ArrayList<>();
        //todo
        int opt;
        if(onlyVisible) {
            opt = kCGWindowListOptionOnScreenOnly | kCGWindowListExcludeDesktopElements;
        } else {
            opt = kCGWindowListExcludeDesktopElements;
        }
        CFArrayRef windows = CoreGraphics.INSTANCE.CGWindowListCopyWindowInfo(opt,0);
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
        //Collections.reverse(list);
        return list;
    }

    protected static QuartzHWndCtrl find(String className, String windowText) {
        CFArrayRef windows = CoreGraphics.INSTANCE.CGWindowListCopyWindowInfo(kCGWindowListExcludeDesktopElements,0);
        int numWindows = windows.getCount();
        QuartzHWndCtrl win = null;
        for (int i = 0; i < numWindows; i++) {
            Pointer result = windows.getValueAtIndex(i);
            CFDictionaryRef windowRef = new CFDictionaryRef(result);
            String cname = getWindowName(windowRef.getValue(kCGWindowOwnerName));
            String wname = getWindowName(windowRef.getValue(kCGWindowName));
            if (className == null) {
                if (wname.equals(windowText)) {
                    win = new QuartzHWndCtrl(windowRef);
                    break;
                }
            } else {
                if(cname.equals(className) && wname.equals(windowText)) {
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
        if(nsapp == null) {
            nsapp = GoldenGlow.INSTANCE.APGetApp();
            Logger.debug("System", "get NSApplication");
        }
    }

    private void getNSWindow(long CGWindowId) {
        checkNSApp();
        if (nsWinUnavailable == 0) {
            Pointer nswin = GoldenGlow.INSTANCE.APGetNSWindow(nsapp,CGWindowId);
            if(nswin == null) {
                nsWinUnavailable = -1;
            }
            Logger.debug("System","get NSWindow");
            this.nsWin =nswin;
            nsWinUnavailable = 1;
        }
    }
    private static String getWindowName(Pointer value) {
        return value == null ? "" : new CFStringRef(value).stringValue();
    }
    private static String getWindowName(Pointer own,Pointer title) {
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
            boolean success = CoreGraphics.INSTANCE.CGRectMakeWithDictionaryRepresentation(new CFDictionaryRef(value),rect);
            if (success) {
                return new WindowRect(
                        (int)Math.round(rect.origin.y),
                        (int)Math.round(rect.origin.y+rect.size.height),
                        (int)Math.round(rect.origin.x),
                        (int)Math.round(rect.origin.x+rect.size.width)
                );
            }
        }
        return new WindowRect(0,0,0,0);
    }

    // JNA Definition

    private interface CoreGraphics extends Library {
        CoreGraphics INSTANCE = Native.load("CoreGraphics", CoreGraphics.class);

        CFArrayRef CGWindowListCopyWindowInfo(int option, int relativeToWindow);
        CFArrayRef CGWindowListCreateDescriptionFromArray(CFArrayRef windowArray);
        boolean CGRectMakeWithDictionaryRepresentation(CFDictionaryRef dict,CGRect.ByReference rect);
        CFDictionaryRef CGSessionCopyCurrentDictionary();
    }

    private interface GoldenGlow extends Library {
        GoldenGlow INSTANCE =Native.load(System.getProperty("user.dir")+"/libgoldenglow.dylib", GoldenGlow.class);

        void APResize(Pointer p,int x,int y,int w,int h);
        Pointer APGetApp();
        void APSetDock(Pointer app,boolean enable);
        void APSetTopmost(Pointer win,boolean enable);
        Pointer APGetNSWindow(Pointer app,long cgid);
        void APActive(Pointer win);
        int APVersion();
    }

    @Structure.FieldOrder({"origin","size"})
    public static class CGRect extends Structure {
        public CGPoint origin;
        public CGSize size;

        public static class ByReference extends CGRect implements Structure.ByReference {}
    }

    @Structure.FieldOrder({"x","y"})
    public static class CGPoint extends Structure {
        public double x;
        public double y;
    }

    @Structure.FieldOrder({"width","height"})
    public static class CGSize extends Structure {
        public double width;
        public double height;
    }
}
