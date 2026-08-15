package cn.harryh.arkpets.platform;

import cn.harryh.arkpets.natives.CoreGraphics;
import cn.harryh.arkpets.natives.ObjCHelper;
import cn.harryh.arkpets.utils.Cached;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation;

import java.util.ArrayList;
import java.util.List;

import static cn.harryh.arkpets.natives.CoreGraphics.*;
import static cn.harryh.arkpets.natives.CoreGraphics.kCGWindowName;
import static cn.harryh.arkpets.platform.QuartzHWndCtrl.getWindowName;


public class QuartzHWndCtrlFactory extends HWndCtrlFactory{
    static Cached<CoreGraphics.CGRect.ByValue> currentScreenRect;

    public QuartzHWndCtrlFactory() {
        CoreFoundation.CFDictionaryRef server = CoreGraphics.INSTANCE.CGSessionCopyCurrentDictionary();
        if (server == null) {
            throw new RuntimeException("No window server connection.");
        } else {
            CoreFoundation.INSTANCE.CFRelease(server);
        }
        ObjCHelper.init();
        currentScreenRect = new Cached<>();
        currentScreenRect.setCacheAge(1.0);
        currentScreenRect.setValueProducer(() -> CoreGraphics.INSTANCE.CGDisplayBounds(CoreGraphics.INSTANCE.CGMainDisplayID()));
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
        //todo
        int opt;
        if (onlyVisible) {
            opt = kCGWindowListOptionOnScreenOnly | kCGWindowListExcludeDesktopElements;
        } else {
            opt = kCGWindowListExcludeDesktopElements;
        }
        CoreFoundation.CFArrayRef windows = CoreGraphics.INSTANCE.CGWindowListCopyWindowInfo(opt, 0);
        int numWindows = windows.getCount();
        for (int i = 0; i < numWindows; i++) {
            Pointer result = windows.getValueAtIndex(i);
            CoreFoundation.CFDictionaryRef windowRef = new CoreFoundation.CFDictionaryRef(result);
            QuartzHWndCtrl win = new QuartzHWndCtrl(windowRef);
            if (!onlyVisible || (win.layer >= 0 && win.layer != 20)) {
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
        CGPoint.ByValue point = (CGPoint.ByValue) ObjCHelper.msgSend.invoke(CGPoint.ByValue.class,new Object[]{
                ObjCHelper.cls("NSEvent"),
                ObjCHelper.sel("mouseLocation")
        });
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
