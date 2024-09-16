package cn.harryh.arkpets.platform;

import cn.harryh.arkpets.utils.Logger;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.ptr.*;

import java.nio.charset.StandardCharsets;
import java.util.*;


public class X11HWndCtrl extends HWndCtrl {
    private static final HashMap<String, X11Ext.Atom> atomsHash = new HashMap<>();
    private static X11.Display display;
    private static final X11Ext x11 = X11Ext.INSTANCE;
    protected final X11.Window hWnd;

    public static final int MAX_PROPERTY_VALUE_LEN = 4096;
    public static final int STATE_REMOVE = 0;
    public static final int STATE_ADD = 1;

    public X11HWndCtrl(X11Ext.Window hWnd) {
        super(winText(hWnd),getWindowRect(hWnd));
        this.hWnd=hWnd;
    }

    public static void init() {
        display = x11.XOpenDisplay(null);

        if (display == null) {
            throw new RuntimeException("Cannot open X display");
        } else {
            Logger.info("System", "Connected to X display");
        }
    }

    public static HWndCtrl find(String className, String windowName) {
        X11Ext.Window[] wids = getWindows();
        for (X11Ext.Window win : wids) {
            String wtitle = winText(win);
            String wclass = getUtf8Property(win, X11.XA_STRING, X11.XA_WM_CLASS);
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
        X11.Window[] wins = getWindows();
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

    protected static WindowRect getWindowRect(X11Ext.Window hWnd) {
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
        int[] netFrame = getWMFrameBorder(hWnd, false);
        int addHeight = netFrame[2] + netFrame[3];
        int addWidth = netFrame[0] + netFrame[1];
        int[] gtkFrame = getWMFrameBorder(hWnd, true);
        int removeHeight = gtkFrame[2] + gtkFrame[3];
        int removeWidth = gtkFrame[0] + gtkFrame[1];

        int finx = xVal - netFrame[0] + gtkFrame[0];
        int finy = yVal - netFrame[2] + gtkFrame[2];
        int finh = height.getValue() + addHeight - removeHeight;
        int finw = width.getValue() + addWidth - removeWidth;
        return new WindowRect(finy, finy + finh, finx + finw, finx);
    }

    @Override
    public boolean isForeground() {
        X11Ext.Window rootWindow = x11.XDefaultRootWindow(display);
        long win = getIntProperty(rootWindow, X11Ext.XA_WINDOW, getAtom("_NET_ACTIVE_WINDOW"));
        return hWnd.longValue() == win;
    }

    @Override
    public boolean isVisible() {
        return visible(hWnd);
    }

    @Override
    public boolean close(int timeout) {
        //todo timeout
        clientMsg(hWnd, "_NET_CLOSE_WINDOW", 0, 0, 0, 0, 0);
        return true;
    }

    @Override
    public HWndCtrl updated() {
        return new X11HWndCtrl(hWnd);
    }

    @Override
    public void setForeground() {
        clientMsg(hWnd, "_NET_ACTIVE_WINDOW", 0, 0, 0, 0, 0);
        x11.XMapRaised(display, hWnd);
    }

    @Override
    public void setWindowAlpha(float alpha) {
        //Pointer mem=new Memory(Native.getNativeSize());
        if (alpha == 1.0) {
            x11.XDeleteProperty(display, hWnd, getAtom("_NET_WM_WINDOW_OPACITY"));
            return;
        }
        LongByReference opacity = new LongByReference((long) (0xffffffffL * alpha));
        x11.XChangeProperty(display, hWnd, getAtom("_NET_WM_WINDOW_OPACITY"), X11.XA_CARDINAL, 32, X11.PropModeReplace, opacity.getPointer(), 1);
    }

    @Override
    public void setWindowPosition(HWndCtrl insertAfter, int x, int y, int w, int h) {
        //todo insert

        // 0000 0000 1111 0001
        //clientMsg(hWnd,"_NET_MOVERESIZE_WINDOW",3840,x,y,w,h);
        x11.XMoveResizeWindow(display, hWnd, x, y, w, h);
    }

    @Override
    public void setWindowTransparent(boolean transparent) {
        // unnecessary in X11.
    }

    @Override
    public void setToolWindow(boolean enable) {
        if (enable) {
            clientMsg(hWnd, "_NET_WM_STATE", STATE_ADD, getAtom("_NET_WM_STATE_SKIP_TASKBAR").intValue(), 0, 0, 0);
        } else {
            clientMsg(hWnd, "_NET_WM_STATE", STATE_REMOVE, getAtom("_NET_WM_STATE_SKIP_TASKBAR").intValue(), 0, 0, 0);
        }
    }

    @Override
    public void setLayered(boolean enable) {
        // unnecessary in X11.
    }

    @Override
    public void setTopmost(boolean enable) {
        if (enable) {
            clientMsg(hWnd, "_NET_WM_STATE", STATE_ADD, getAtom("_NET_WM_STATE_ABOVE").intValue(), 0, 0, 0);
        } else {
            clientMsg(hWnd, "_NET_WM_STATE", STATE_REMOVE, getAtom("_NET_WM_STATE_ABOVE").intValue(), 0, 0, 0);
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

        X11HWndCtrl hWndCtrl = (X11HWndCtrl)o;
        return hWnd.equals(hWndCtrl.hWnd);
    }

    @Override
    public int hashCode() {
        return hWnd.hashCode();
    }

    private static int bytesToInt(byte[] prop, int offset) {
        return ((prop[3 + offset] & 0xff) << 24)
                | ((prop[2 + offset] & 0xff) << 16)
                | ((prop[1 + offset] & 0xff) << 8)
                | ((prop[offset] & 0xff));
    }

    private static X11.Atom getAtom(String name) {
        X11.Atom atom = atomsHash.get(name);
        if (atom == null) {
            atom = x11.XInternAtom(display, name, false);
            atomsHash.put(name, atom);
        }
        return atom;
    }

    private static byte[] getProperty(X11.Window win, X11.Atom xa_prop_type, X11.Atom xa_prop_name) {
        X11.AtomByReference xa_ret_type_ref = new X11.AtomByReference();
        IntByReference ret_format_ref = new IntByReference();
        NativeLongByReference ret_nitems_ref = new NativeLongByReference();
        NativeLongByReference ret_bytes_after_ref = new NativeLongByReference();
        PointerByReference ret_prop_ref = new PointerByReference();

        NativeLong long_offset = new NativeLong(0);
        NativeLong long_length = new NativeLong(MAX_PROPERTY_VALUE_LEN / 4);

        /* MAX_PROPERTY_VALUE_LEN / 4 explanation (XGetWindowProperty manpage):
         *
         * long_length = Specifies the length in 32-bit multiples of the
         *               data to be retrieved.
         */
        if (x11.XGetWindowProperty(display, win, xa_prop_name, long_offset, long_length, false,
                xa_prop_type, xa_ret_type_ref, ret_format_ref,
                ret_nitems_ref, ret_bytes_after_ref, ret_prop_ref) != X11.Success) {
            String prop_name = x11.XGetAtomName(display, xa_prop_name);
            throw new RuntimeException("Cannot get " + prop_name + " property.");
        }

        X11.Atom xa_ret_type = xa_ret_type_ref.getValue();
        Pointer ret_prop = ret_prop_ref.getValue();

        if (xa_ret_type == null) {
            //the specified property does not exist for the specified window
            throw new RuntimeException("Type not found");
        }

        if (xa_prop_type == null ||
                !xa_ret_type.toNative().equals(xa_prop_type.toNative())) {
            x11.XFree(ret_prop);
            String prop_name = x11.XGetAtomName(display, xa_prop_name);
            throw new RuntimeException("Invalid type of " + prop_name + " property");
        }

        int ret_format = ret_format_ref.getValue();
        long ret_nitems = ret_nitems_ref.getValue().longValue();

        // null terminate the result to make string handling easier
        int nbytes;
        if (ret_format == 32)
            nbytes = Native.LONG_SIZE;
        else if (ret_format == 16)
            nbytes = Native.LONG_SIZE / 2;
        else if (ret_format == 8)
            nbytes = 1;
        else if (ret_format == 0)
            nbytes = 0;
        else
            throw new RuntimeException("Invalid return format");
        int length = Math.min((int) ret_nitems * nbytes, MAX_PROPERTY_VALUE_LEN);

        byte[] ret = ret_prop.getByteArray(0, length);

        x11.XFree(ret_prop);
        return ret;
    }

    private static X11Ext.Window[] getWindows() {
        X11Ext.Window rootWindow = x11.XDefaultRootWindow(display);
        byte[] bytes = getProperty(rootWindow, X11Ext.XA_WINDOW, getAtom("_NET_CLIENT_LIST_STACKING"));

        X11Ext.Window[] windowList = new X11Ext.Window[bytes.length / X11.Window.SIZE];

        for (int i = 0; i < windowList.length; i++) {
            windowList[i] = new X11.Window(bytesToInt(bytes, X11.XID.SIZE * i));
        }

        return windowList;
    }

    private static String getUtf8Property(X11.Window win, X11.Atom xa_prop_type, X11.Atom xa_prop_name) {
        byte[] property = getNullReplacedStringProperty(win, xa_prop_type, xa_prop_name);
        if (property == null) {
            return "";
        }
        return new String(property, StandardCharsets.UTF_8);
    }

    public static byte[] getNullReplacedStringProperty(X11.Window win, X11.Atom xa_prop_type, X11.Atom xa_prop_name) {
        byte[] bytes = getProperty(win, xa_prop_type, xa_prop_name);

        if (bytes == null) {
            return null;
        }

        // search for '\0'
        int i;
        for (i = 0; i < bytes.length; i++) {
            if (bytes[i] == '\0') {
                bytes[i] = '.';
            }
        }

        return bytes;
    }

    private static String winText(X11Ext.Window hWnd) {
        String title;
        title = getUtf8Property(hWnd, getAtom("UTF8_STRING"), getAtom("_NET_WM_NAME"));
        return title;
    }

    private static int[] getWMFrameBorder(X11.Window hWnd, boolean gtkFrame) {
        X11.Atom xa_prop_type = getAtom("CARDINAL");
        X11.Atom xa_prop_name;
        if (gtkFrame) {
            xa_prop_name = getAtom("_GTK_FRAME_EXTENTS");
        } else {
            xa_prop_name = getAtom("_NET_FRAME_EXTENTS");
        }
        X11.AtomByReference xa_ret_type_ref = new X11.AtomByReference();
        IntByReference ret_format_ref = new IntByReference();
        NativeLongByReference ret_nitems_ref = new NativeLongByReference();
        NativeLongByReference ret_bytes_after_ref = new NativeLongByReference();
        PointerByReference ret_prop_ref = new PointerByReference();

        NativeLong long_offset = new NativeLong(0);
        NativeLong long_length = new NativeLong(MAX_PROPERTY_VALUE_LEN / 4);

        /* MAX_PROPERTY_VALUE_LEN / 4 explanation (XGetWindowProperty manpage):
         *
         * long_length = Specifies the length in 32-bit multiples of the
         *               data to be retrieved.
         */
        if (x11.XGetWindowProperty(display, hWnd, xa_prop_name, long_offset, long_length, false,
                xa_prop_type, xa_ret_type_ref, ret_format_ref,
                ret_nitems_ref, ret_bytes_after_ref, ret_prop_ref) != X11.Success) {
            return new int[]{0, 0, 0, 0};
        }

        X11.Atom xa_ret_type = xa_ret_type_ref.getValue();
        Pointer ret_prop = ret_prop_ref.getValue();

        if (xa_ret_type == null) {
            return new int[]{0, 0, 0, 0};
        }

        if (xa_prop_type == null ||
                !xa_ret_type.toNative().equals(xa_prop_type.toNative())) {
            x11.XFree(ret_prop);
            return new int[]{0, 0, 0, 0};
        }

        int ret_nitems = ret_nitems_ref.getValue().intValue();

        long[] ret = ret_prop.getLongArray(0, ret_nitems);
        int[] intArray = Arrays.stream(ret).mapToInt(i -> (int) i).toArray();
        x11.XFree(ret_prop);
        return intArray;
    }

    private static Integer getIntProperty(X11.Window hWnd, X11.Atom xa_prop_type, X11.Atom xa_prop_name) {
        byte[] property = getProperty(hWnd, xa_prop_type, xa_prop_name);
        return bytesToInt(property, 0);
    }

    private static boolean visible(X11.Window hWnd) {
        X11.XWindowAttributes attr = new X11.XWindowAttributes();
        x11.XGetWindowAttributes(display, hWnd, attr);
        if (attr.map_state != x11.IsViewable) {
            return false;
        }
        X11.Window root = x11.XDefaultRootWindow(display);
        boolean visible = isWMState(hWnd, getAtom("_NET_WM_STATE_HIDDEN"));
        int winDesktop = getIntProperty(hWnd, X11.XA_CARDINAL, getAtom("_NET_WM_DESKTOP"));
        int currentDesktop = getIntProperty(root, X11.XA_CARDINAL, getAtom("_NET_CURRENT_DESKTOP"));
        boolean inWorkspace = winDesktop == currentDesktop;
        if (!visible || !inWorkspace) {
            return false;
        }

        return attr.y != attr.y + attr.height && attr.x != attr.x + attr.width;
    }

    private static boolean isWMState(X11.Window hWnd, X11.Atom wm_prop) {
        X11.Atom xa_prop_type = getAtom("ATOM");
        X11.Atom xa_prop_name = getAtom("_NET_WM_STATE");
        X11.AtomByReference xa_ret_type_ref = new X11.AtomByReference();
        IntByReference ret_format_ref = new IntByReference();
        NativeLongByReference ret_nitems_ref = new NativeLongByReference();
        NativeLongByReference ret_bytes_after_ref = new NativeLongByReference();
        PointerByReference ret_prop_ref = new PointerByReference();

        NativeLong long_offset = new NativeLong(0);
        NativeLong long_length = new NativeLong(MAX_PROPERTY_VALUE_LEN / 4);

        if (x11.XGetWindowProperty(display, hWnd, xa_prop_name, long_offset, long_length, false,
                xa_prop_type, xa_ret_type_ref, ret_format_ref,
                ret_nitems_ref, ret_bytes_after_ref, ret_prop_ref) != X11.Success) {
            return false;
        }

        X11.Atom xa_ret_type = xa_ret_type_ref.getValue();
        Pointer ret_prop = ret_prop_ref.getValue();

        if (xa_ret_type == null) {
            return false;
        }

        if (xa_prop_type == null ||
                !xa_ret_type.toNative().equals(xa_prop_type.toNative())) {
            x11.XFree(ret_prop);
            return false;
        }

        int ret_nitems = ret_nitems_ref.getValue().intValue();

        char[] ret = ret_prop.getCharArray(0, ret_nitems);

        x11.XFree(ret_prop);
        for (char c : ret) {
            if (((long) c) == wm_prop.longValue()) {
                return false;
            }
        }
        return true;
    }

    private void clientMsg(X11.Window hWnd, String msg, int data0, int data1, int data2, int data3, int data4) {
        X11.XClientMessageEvent event;
        NativeLong mask = new NativeLong(X11.SubstructureRedirectMask | X11.SubstructureNotifyMask);
        X11.Window root = x11.XDefaultRootWindow(display);
        event = new X11.XClientMessageEvent();
        event.type = X11.ClientMessage;
        event.serial = new NativeLong(0);
        event.send_event = 1;
        event.message_type = getAtom(msg);
        event.window = hWnd;
        event.format = 32;
        event.data.setType(NativeLong[].class);
        event.data.l[0] = new NativeLong(data0);
        event.data.l[1] = new NativeLong(data1);
        event.data.l[2] = new NativeLong(data2);
        event.data.l[3] = new NativeLong(data3);
        event.data.l[4] = new NativeLong(data4);

        X11.XEvent e = new X11.XEvent();
        e.setTypedValue(event);

        x11.XSendEvent(display, root, 0, mask, e);
    }

    public interface X11Ext extends X11 {
        X11Ext INSTANCE = Native.load("X11", X11Ext.class);

        void XMoveResizeWindow(Display display, Window w, int x, int y, int width, int height);
    }
}
