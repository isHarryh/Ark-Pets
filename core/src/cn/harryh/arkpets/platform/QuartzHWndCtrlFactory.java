package cn.harryh.arkpets.platform;

import cn.harryh.arkpets.natives.CoreGraphics;
import cn.harryh.arkpets.natives.MacHelper;
import cn.harryh.arkpets.natives.ObjCHelper;
import cn.harryh.arkpets.utils.Cached;
import cn.harryh.arkpets.utils.Logger;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation;

import java.util.ArrayList;
import java.util.List;

import static cn.harryh.arkpets.natives.CoreGraphics.*;
import static cn.harryh.arkpets.natives.CoreGraphics.kCGWindowName;
import static cn.harryh.arkpets.platform.QuartzHWndCtrl.getWindowName;


public class QuartzHWndCtrlFactory extends HWndCtrlFactory{
    static final Cached<CoreGraphics.CGRect.ByValue> currentScreenRect;
    static final Cached<CoreGraphics.CGRect> dockFrame;
    static volatile long dockPid = MacHelper.findDockPid();

    static {
        currentScreenRect = new Cached<>();
        currentScreenRect.setCacheAge(1.0);
        currentScreenRect.setValueProducer(() -> CoreGraphics.INSTANCE.CGDisplayBounds(CoreGraphics.INSTANCE.CGMainDisplayID()));
        dockFrame = new Cached<>();
        dockFrame.setCacheAge(1.0);
        // The producer reads the static field on every invocation, so an updated Dock PID takes effect automatically.
        dockFrame.setValueProducer(() -> MacHelper.getDockRealFrame((int) dockPid));
    }

    /**
     * Gets the PID of the running Dock, re-detecting it when the cached one is no longer alive.
     *
     * @return The PID of the Dock, or a non-positive value if the Dock is not running.
     */
    private static long getDockPid() {
        long pid = dockPid;
        if (pid > 0 && ProcessHandle.of(pid).map(ProcessHandle::isAlive).orElse(false))
            return pid;

        long newPid = MacHelper.findDockPid();
        if (newPid != pid) {
            dockPid = newPid;
            dockFrame.removeCachedValue();
            Logger.info("System", newPid > 0
                    ? "Dock process detected, pid: " + newPid
                    : "Dock process not found");
        }
        return newPid;
    }

    public QuartzHWndCtrlFactory() {
        CoreFoundation.CFDictionaryRef server = CoreGraphics.INSTANCE.CGSessionCopyCurrentDictionary();
        if (server == null) {
            throw new RuntimeException("No window server connection.");
        } else {
            CoreFoundation.INSTANCE.CFRelease(server);
        }
        ObjCHelper.init();
    }

    @Override
    public HWndCtrl findWindow(String className, String windowText) {
        CoreFoundation.CFArrayRef windows = CoreGraphics.INSTANCE.CGWindowListCopyWindowInfo(kCGWindowListOptionOnScreenOnly | kCGWindowListExcludeDesktopElements, 0);
        int numWindows = windows.getCount();
        QuartzHWndCtrl win = null;
        for (int i = 0; i < numWindows; i++) {
            Pointer result = windows.getValueAtIndex(i);
            CoreFoundation.CFDictionaryRef windowRef = new CoreFoundation.CFDictionaryRef(result);
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
    public List<? extends HWndCtrl> getWindowList(boolean onlyVisible) {
        ArrayList<QuartzHWndCtrl> list = new ArrayList<>();
        int opt;
        if (onlyVisible) {
            opt = kCGWindowListOptionOnScreenOnly | kCGWindowListExcludeDesktopElements;
        } else {
            opt = kCGWindowListExcludeDesktopElements;
        }
        CoreFoundation.CFArrayRef windows = CoreGraphics.INSTANCE.CGWindowListCopyWindowInfo(opt, 0);
        int numWindows = windows.getCount();
        long dock = getDockPid();
        boolean dockChecked = false;
        boolean dockFrameAvailable = false;
        for (int i = 0; i < numWindows; i++) {
            Pointer result = windows.getValueAtIndex(i);
            CoreFoundation.CFDictionaryRef windowRef = new CoreFoundation.CFDictionaryRef(result);
            int winpid = QuartzHWndCtrl.getWindowPid(windowRef);
            boolean isDock = dock > 0 && dock == winpid;
            if (isDock) {
                if (!dockChecked) {
                    dockChecked = true;
                    dockFrameAvailable = dockFrame.getValue() != null;
                }
                if (!dockFrameAvailable) {
                    // Without accessibility, the Dock geometry is unknown and its CGWindowBounds covers the
                    // whole screen. Exclude it rather than letting it shadow the barriers of other windows.
                    continue;
                }
            }
            QuartzHWndCtrl win = new QuartzHWndCtrl(windowRef);
            if (!onlyVisible || (win.layer >= 0 && win.layer != 20) || isDock) {
                list.add(win);
            }
        }
        windows.release();
        return list;
    }

    @Override
    public HWndCtrl getTopmostWindow() {
        return new NullHWndCtrl(); // todo
    }

    @Override
    public HWndCtrl.MousePoint getMousePos() {
        CGPoint.ByValue point = ObjCHelper.sendStruct(
                CGPoint.ByValue.class, ObjCHelper.cls("NSEvent"), "mouseLocation");
        return new HWndCtrl.MousePoint((int) Math.floor(point.x), (int) Math.floor(currentScreenRect.getValue().size.height - point.y));
    }

    @Override
    public void free() {

    }

    @Override
    public boolean needResize() {
        return false;
    }

    @Override
    public boolean needDecorated() {
        return false;
    }
}
