package cn.harryh.arkpets.natives;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;


public class X11Helper {
    public static final int MAX_PROPERTY_VALUE_LEN = 4096;
    private static final X11Extension x11 = X11Extension.INSTANCE;


    private static final HashMap<String, X11Extension.Atom> atomsHash = new HashMap<>();

    public static X11.Atom getAtom(X11.Display disp, String name) {
        X11.Atom atom = atomsHash.get(name);
        if (atom == null) {
            atom = x11.XInternAtom(disp, name, false);
            atomsHash.put(name, atom);
        }
        return atom;
    }

    public static void clientMsg(X11.Display disp, X11.Window hWnd, String msg, int data0, int data1, int data2, int data3, int data4) {
        X11.XClientMessageEvent event;
        NativeLong mask = new NativeLong(X11.SubstructureRedirectMask | X11.SubstructureNotifyMask);
        X11.Window root = x11.XDefaultRootWindow(disp);
        event = new X11.XClientMessageEvent();
        event.type = X11.ClientMessage;
        event.serial = new NativeLong(0);
        event.send_event = 1;
        event.message_type = getAtom(disp,msg);
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

        x11.XSendEvent(disp, root, 0, mask, e);
    }

    public static int bytesToInt(byte[] prop, int offset) {
        return ((prop[3 + offset] & 0xff) << 24)
                | ((prop[2 + offset] & 0xff) << 16)
                | ((prop[1 + offset] & 0xff) << 8)
                | ((prop[offset] & 0xff));
    }

    public static byte[] getProperty(X11.Display disp, X11.Window win, X11.Atom xa_prop_type, X11.Atom xa_prop_name) {
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
        if (x11.XGetWindowProperty(disp, win, xa_prop_name, long_offset, long_length, false,
                xa_prop_type, xa_ret_type_ref, ret_format_ref,
                ret_nitems_ref, ret_bytes_after_ref, ret_prop_ref) != X11.Success) {
            return new byte[] {};
        }

        X11.Atom xa_ret_type = xa_ret_type_ref.getValue();
        Pointer ret_prop = ret_prop_ref.getValue();

        if (xa_ret_type == null) {
            //the specified property does not exist for the specified window
            return new byte[] {};
        }

        if (xa_prop_type == null ||
                !xa_ret_type.toNative().equals(xa_prop_type.toNative())) {
            x11.XFree(ret_prop);
            String prop_name = x11.XGetAtomName(disp, xa_prop_name);
            return new byte[] {};
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
            return new byte[] {};
        int length = Math.min((int) ret_nitems * nbytes, MAX_PROPERTY_VALUE_LEN);

        byte[] ret = ret_prop.getByteArray(0, length);

        x11.XFree(ret_prop);
        return ret;
    }

    public static String getUtf8Property(X11.Display disp, X11.Window win, X11.Atom xa_prop_type, X11.Atom xa_prop_name) {
        byte[] property = getNullReplacedStringProperty(disp,win, xa_prop_type, xa_prop_name);
        if (property == null) {
            return "";
        }
        return new String(property, StandardCharsets.UTF_8);
    }

    public static byte[] getNullReplacedStringProperty(X11.Display disp, X11.Window win, X11.Atom xa_prop_type, X11.Atom xa_prop_name) {
        byte[] bytes = getProperty(disp,win, xa_prop_type, xa_prop_name);

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

    public static Integer getIntProperty(X11.Display disp, X11.Window hWnd, X11.Atom xa_prop_type, X11.Atom xa_prop_name) {
        byte[] property = getProperty(disp,hWnd, xa_prop_type, xa_prop_name);
        return bytesToInt(property, 0);
    }

    public static boolean isWMState(X11.Display disp, X11.Window hWnd, X11.Atom wm_prop) {
        X11.Atom xa_prop_type = getAtom(disp,"ATOM");
        X11.Atom xa_prop_name = getAtom(disp,"_NET_WM_STATE");
        X11.AtomByReference xa_ret_type_ref = new X11.AtomByReference();
        IntByReference ret_format_ref = new IntByReference();
        NativeLongByReference ret_nitems_ref = new NativeLongByReference();
        NativeLongByReference ret_bytes_after_ref = new NativeLongByReference();
        PointerByReference ret_prop_ref = new PointerByReference();

        NativeLong long_offset = new NativeLong(0);
        NativeLong long_length = new NativeLong(MAX_PROPERTY_VALUE_LEN / 4);

        if (x11.XGetWindowProperty(disp, hWnd, xa_prop_name, long_offset, long_length, false,
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

    public static int[] getWMFrameBorder(X11.Display display, X11.Window hWnd, boolean gtkFrame) {
        X11.Atom xa_prop_type = getAtom(display,"CARDINAL");
        X11.Atom xa_prop_name;
        if (gtkFrame) {
            xa_prop_name = getAtom(display,"_GTK_FRAME_EXTENTS");
        } else {
            xa_prop_name = getAtom(display,"_NET_FRAME_EXTENTS");
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
}
