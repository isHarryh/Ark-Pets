package cn.harryh.arkpets.platform;

import cn.harryh.arkpets.natives.X11Extension;
import cn.harryh.arkpets.natives.X11Helper;
import cn.harryh.arkpets.natives.XextExtension;
import cn.harryh.arkpets.utils.Logger;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.ptr.IntByReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class X11HWndCtrl extends HWndCtrl {
    private static X11.Display display;
    private static final X11Extension x11 = X11Extension.INSTANCE;
    private static final XextExtension xext = XextExtension.INSTANCE;
    protected final X11.Window hWnd;

    private static boolean shapeAvailable;
    private boolean transparentEnable;

    public static final int STATE_REMOVE = 0;
    public static final int STATE_ADD = 1;

    public X11HWndCtrl(X11Extension.Window hWnd) {
        super(winText(hWnd), getWindowRect(hWnd));
        this.hWnd = hWnd;
    }

    public static void init() {
        display = x11.XOpenDisplay(null);

        if (display == null) {
            throw new RuntimeException("Cannot open X display");
        } else {
            Logger.info("System", "Connected to X display");
        }
        IntByReference evt = new IntByReference();
        IntByReference err = new IntByReference();
        boolean xshape = xext.XShapeQueryExtension(display, evt, err);
        if (!xshape) {
            Logger.warn("System", "No XShape extension");
            shapeAvailable = false;
        } else {
            shapeAvailable = true;
        }
    }

    public static HWndCtrl find(String className, String windowName) {
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

    public static List<X11HWndCtrl> getWindowList(boolean onlyVisible) {
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

    public static HWndCtrl getTopmost() {
        List<X11HWndCtrl> list = getWindowList(true);
        return list.isEmpty() ? null : list.get(0);
    }

    public static void free() {
        x11.XCloseDisplay(display);
        Logger.info("System", "Disconnected from X display");
    }

    protected static WindowRect getWindowRect(X11Extension.Window hWnd) {
        X11.WindowByReference junkRoot = new X11.WindowByReference();
        IntByReference junkX = new IntByReference();
        IntByReference junkY = new IntByReference();
        IntByReference x = new IntByReference();
        IntByReference y = new IntByReference();
        IntByReference width = new IntByReference();
        IntByReference height = new IntByReference();
        IntByReference border_width = new IntByReference();
        IntByReference depth = new IntByReference();

        x11.XGetGeometry(display, hWnd, junkRoot, junkX, junkY, width, height, border_width, depth);

        x11.XTranslateCoordinates(display, hWnd, junkRoot.getValue(), 0, 0, x, y, junkRoot);

        int xVal = x.getValue();
        int yVal = y.getValue();
        int[] netFrame = X11Helper.getWMFrameBorder(display, hWnd, false);
        int addHeight = netFrame[2] + netFrame[3];
        int addWidth = netFrame[0] + netFrame[1];
        int[] gtkFrame = X11Helper.getWMFrameBorder(display, hWnd, true);
        int removeHeight = gtkFrame[2] + gtkFrame[3];
        int removeWidth = gtkFrame[0] + gtkFrame[1];

        int finx = xVal - netFrame[0] + gtkFrame[0];
        int finy = yVal - netFrame[2] + gtkFrame[2];
        int finh = height.getValue() + addHeight - removeHeight;
        int finw = width.getValue() + addWidth - removeWidth;
        return new WindowRect(finy, finy + finh, finx, finx + finw);
    }

    @Override
    public boolean isForeground() {
        X11Extension.Window rootWindow = x11.XDefaultRootWindow(display);
        long win = X11Helper.getIntProperty(display, rootWindow, X11Extension.XA_WINDOW, X11Helper.getAtom(display, "_NET_ACTIVE_WINDOW"));
        return hWnd.longValue() == win;
    }

    @Override
    public boolean isVisible() {
        return visible(hWnd);
    }

    @Override
    public boolean close(int timeout) {
        //todo timeout
        X11Helper.clientMsg(display, hWnd, "_NET_CLOSE_WINDOW", 0, 0, 0, 0, 0);
        return true;
    }

    @Override
    public HWndCtrl updated() {
        return new X11HWndCtrl(hWnd);
    }

    @Override
    public void setForeground() {
        X11Helper.clientMsg(display, hWnd, "_NET_ACTIVE_WINDOW", 0, 0, 0, 0, 0);
        x11.XMapRaised(display, hWnd);
    }

    @Override
    public void setWindowPosition(HWndCtrl insertAfter, int x, int y, int w, int h) {
        //todo insert

        // 0000 0000 1111 0001
        //clientMsg(hWnd,"_NET_MOVERESIZE_WINDOW",3840,x,y,w,h);
        x11.XSync(display, false);
        x11.XMoveResizeWindow(display, hWnd, x, y, w, h);
    }

    @Override
    public void setTransparent(boolean transparent) {
        if (!shapeAvailable) return;
        if (transparentEnable != transparent) {
            if (transparent) {
                Pointer reg = x11.XCreateRegion();
                xext.XShapeCombineRegion(display,hWnd, X11.Xext.ShapeInput,0,0,reg, X11.Xext.ShapeSet);
                x11.XDestroyRegion(reg);
            } else {
                xext.XShapeCombineMask(display,hWnd, X11.Xext.ShapeInput,0,0,null, X11.Xext.ShapeSet);
            }
            transparentEnable = transparent;
        }
    }

    @Override
    public void setTaskbar(boolean enable) {
        if (!enable) {
            X11Helper.clientMsg(display, hWnd, "_NET_WM_STATE", STATE_ADD, X11Helper.getAtom(display, "_NET_WM_STATE_SKIP_TASKBAR").intValue(), 0, 0, 0);
            X11Helper.clientMsg(display, hWnd, "_NET_WM_STATE", STATE_ADD, X11Helper.getAtom(display, "_NET_WM_STATE_STICKY").intValue(), 0, 0, 0);
        } else {
            X11Helper.clientMsg(display, hWnd, "_NET_WM_STATE", STATE_REMOVE, X11Helper.getAtom(display, "_NET_WM_STATE_SKIP_TASKBAR").intValue(), 0, 0, 0);
            X11Helper.clientMsg(display, hWnd, "_NET_WM_STATE", STATE_REMOVE, X11Helper.getAtom(display, "_NET_WM_STATE_STICKY").intValue(), 0, 0, 0);
        }
    }

    @Override
    public void setLayered(boolean enable) {
        // unnecessary in X11.
    }

    @Override
    public void setTopmost(boolean enable) {
        if (enable) {
            X11Helper.clientMsg(display, hWnd, "_NET_WM_STATE", STATE_ADD, X11Helper.getAtom(display, "_NET_WM_STATE_ABOVE").intValue(), 0, 0, 0);
        } else {
            X11Helper.clientMsg(display, hWnd, "_NET_WM_STATE", STATE_REMOVE, X11Helper.getAtom(display, "_NET_WM_STATE_ABOVE").intValue(), 0, 0, 0);
        }
    }

    @Override
    public void sendMouseEvent(MouseEvent msg, int x, int y) {
        //todo
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        X11HWndCtrl hWndCtrl = (X11HWndCtrl) o;
        return hWnd.equals(hWndCtrl.hWnd);
    }

    @Override
    public int hashCode() {
        return hWnd.hashCode();
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

    private static String winText(X11Extension.Window hWnd) {
        String title;
        title = X11Helper.getUtf8Property(display, hWnd, X11Helper.getAtom(display, "UTF8_STRING"), X11Helper.getAtom(display, "_NET_WM_NAME"));
        return title;
    }

    private static boolean visible(X11.Window hWnd) {
        X11.XWindowAttributes attr = new X11.XWindowAttributes();
        x11.XGetWindowAttributes(display, hWnd, attr);
        if (attr.map_state != X11.IsViewable) {
            return false;
        }
        X11.Window root = x11.XDefaultRootWindow(display);
        boolean visible = X11Helper.isWMState(display, hWnd, X11Helper.getAtom(display, "_NET_WM_STATE_HIDDEN"));
        int winDesktop = X11Helper.getIntProperty(display, hWnd, X11.XA_CARDINAL, X11Helper.getAtom(display, "_NET_WM_DESKTOP"));
        int currentDesktop = X11Helper.getIntProperty(display, root, X11.XA_CARDINAL, X11Helper.getAtom(display, "_NET_CURRENT_DESKTOP"));
        boolean inWorkspace = winDesktop == currentDesktop;
        if (!visible || !inWorkspace) {
            return false;
        }

        return attr.y != attr.y + attr.height && attr.x != attr.x + attr.width;
    }

}
