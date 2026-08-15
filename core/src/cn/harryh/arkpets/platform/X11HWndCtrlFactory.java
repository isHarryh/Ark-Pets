package cn.harryh.arkpets.platform;

import cn.harryh.arkpets.natives.X11Extension;
import cn.harryh.arkpets.natives.X11Helper;
import cn.harryh.arkpets.utils.Logger;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.ptr.IntByReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cn.harryh.arkpets.platform.X11HWndCtrl.visible;
import static cn.harryh.arkpets.platform.X11HWndCtrl.winText;


public class X11HWndCtrlFactory extends HWndCtrlFactory{
    private static final X11Extension x11 = X11Extension.INSTANCE;

    private static final X11.XErrorHandler handler = new ErrorHandler();

    static X11.Display display;


    public X11HWndCtrlFactory() {
        display = x11.XOpenDisplay(null);

        if (display == null) {
            throw new RuntimeException("Cannot open X display");
        } else {
            Logger.info("System", "Connected to X display");
        }

        x11.XSetErrorHandler(handler);
    }

    @Override
    public HWndCtrl findWindow(String className, String windowName) {
        X11Extension.Window[] wids = getWindows();
        for (X11Extension.Window win : wids) {
            String wtitle = winText(win);
            String wclass = X11Helper.getUtf8Property(display, win, X11.XA_STRING, X11.XA_WM_CLASS);
            if (className == null) {
                if (wtitle.equals(windowName)) {
                    return new X11HWndCtrl(win);
                }
            } else {
                if (wclass.equals(className) && wtitle.equals(windowName)) {
                    return new X11HWndCtrl(win);
                }
            }
        }
        return null;
    }

    @Override
    public List<? extends HWndCtrl> getWindowList(boolean onlyVisible) {
        ArrayList<X11HWndCtrl> windowList = new ArrayList<>();
        X11Extension.Window[] wins = getWindows();
        for (X11.Window win : wins) {
            if (!onlyVisible || visible(win)) {
                windowList.add(new X11HWndCtrl(win));
            }
        }
        Collections.reverse(windowList);
        return windowList;
    }

    @Override
    public HWndCtrl getTopmostWindow() {
        List<? extends HWndCtrl> list = getWindowList(true);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public HWndCtrl.MousePoint getMousePos() {
        IntByReference rootX = new IntByReference();
        IntByReference rootY = new IntByReference();
        IntByReference junk = new IntByReference();
        X11.WindowByReference junkW = new X11.WindowByReference();
        x11.XQueryPointer(display,x11.XDefaultRootWindow(display),junkW,junkW,rootX,rootY,junk,junk,junk);
        return new HWndCtrl.MousePoint(rootX.getValue(),rootY.getValue());
    }

    @Override
    public void free() {
        if(display != null) x11.XCloseDisplay(display);
        Logger.info("System", "Disconnected from X display");
    }

    @Override
    public boolean needResize() {
        return true;
    }

    @Override
    public boolean needDecorated() {
        return false;
    }

    private static X11Extension.Window[] getWindows() {
        X11Extension.Window rootWindow = x11.XDefaultRootWindow(display);
        byte[] bytes = X11Helper.getProperty(display, rootWindow, X11Extension.XA_WINDOW, X11Helper.getAtom(display, "_NET_CLIENT_LIST_STACKING"));

        X11Extension.Window[] windowList = new X11Extension.Window[bytes.length / X11.Window.SIZE];

        for (int i = 0; i < windowList.length; i++) {
            windowList[i] = new X11.Window(X11Helper.bytesToInt(bytes, X11.XID.SIZE * i));
        }

        return windowList;
    }

    private static class ErrorHandler implements X11.XErrorHandler {
        @Override
        public int apply(X11.Display display, X11.XErrorEvent errorEvent) {
            Logger.error("System","X Error " + errorEvent.toString());
            return 0;
        }
    }
}
