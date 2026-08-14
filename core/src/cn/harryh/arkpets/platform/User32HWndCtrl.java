/** Copyright (c) 2022-2026, Harry Huang, Litwak913
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.platform;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.COM.Unknown;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.ptr.PointerByReference;

import java.util.ArrayList;


public class User32HWndCtrl extends HWndCtrl {
    protected final HWND hWnd;

    public static final int WS_EX_TOPMOST       = 0x00000008;
    public static final int WS_EX_TRANSPARENT   = 0x00000020;
    public static final int WS_EX_TOOLWINDOW    = 0x00000080;
    public static final int WS_EX_APPWINDOW     = 0x00040000;
    public static final int WS_EX_LAYERED       = 0x00080000;

    public static final int WM_MOUSEMOVE    = 0x0200;
    public static final int WM_LBUTTONDOWN  = 0x0201;
    public static final int WM_LBUTTONUP    = 0x0202;
    public static final int WM_RBUTTONDOWN  = 0x0204;
    public static final int WM_RBUTTONUP    = 0x0205;
    public static final int WM_MBUTTONDOWN  = 0x0207;
    public static final int WM_MBUTTONUP    = 0x0208;

    private static final int MK_LBUTTON  = 0x0001;
    private static final int MK_RBUTTON  = 0x0002;
    private static final int MK_MBUTTON  = 0x0010;

    private static TaskbarList taskbarList = null;


    /** HWnd Controller instance.
     * @param hWnd The handle of the window.
     */
    protected User32HWndCtrl(HWND hWnd) {
        super(getWindowText(hWnd), getWindowRect(hWnd));
        this.hWnd = hWnd;
    }

    /** Finds a window.
     * @param className The class name of the window.
     * @param windowName The title of the window.
     */
    public static HWndCtrl find(String className, String windowName) {
        HWND hwnd = User32.INSTANCE.FindWindow(className, windowName);
        if (hwnd != null) {
            return new User32HWndCtrl(hwnd);
        }
        return null;
    }

    @Override
    public boolean isForeground() {
        return hWnd.equals(User32.INSTANCE.GetForegroundWindow());
    }

    @Override
    public boolean isVisible() {
        return isVisible(hWnd);
    }

    @Override
    public boolean close(int timeout) {
        return User32.INSTANCE.SendMessageTimeout(hWnd, 0x10, null, null, timeout, WinUser.SMTO_NORMAL, null).intValue() == 0;
    }

    @Override
    public HWndCtrl updated() {
        return new User32HWndCtrl(hWnd);
    }

    @Override
    public void setForeground() {
        User32.INSTANCE.SetForegroundWindow(hWnd);
    }

    @Override
    public void setWindowPosition(HWndCtrl insertAfter, int x, int y, int w, int h) {
        User32.INSTANCE.SetWindowPos(hWnd, insertAfter != null ? ((User32HWndCtrl) insertAfter).hWnd : null, x, y, w, h, WinUser.SWP_NOACTIVATE);
    }

    @Override
    public void setTaskbar(boolean enable) {
        // On Windows, this is implemented by toggle app-window ex-style and tool-window ex-style.
        // Furthermore, we introduced ITaskbarList operation to make the behavior more reliable.
        if (taskbarList == null) {
            taskbarList = TaskbarList.createAndInit();
        }
        if (enable) {
            setWindowExStyle((getWindowExStyle() & ~User32HWndCtrl.WS_EX_TOOLWINDOW) | User32HWndCtrl.WS_EX_APPWINDOW);
            taskbarList.AddTab(hWnd);
        } else {
            setWindowExStyle((getWindowExStyle() | User32HWndCtrl.WS_EX_TOOLWINDOW) & ~User32HWndCtrl.WS_EX_APPWINDOW);
            taskbarList.DeleteTab(hWnd);
        }
    }

    @Override
    public void setTopmost(boolean enable) {
        if (enable)
            setWindowExStyle(getWindowExStyle() | User32HWndCtrl.WS_EX_TOPMOST);
        else
            setWindowExStyle(getWindowExStyle() & ~User32HWndCtrl.WS_EX_TOPMOST);
    }

    @Override
    public void sendMouseEvent(MouseEvent msg, int x, int y) {
        int wmsg = switch (msg) {
            case MOUSEMOVE -> WM_MOUSEMOVE;
            case LBUTTONDOWN -> WM_LBUTTONDOWN;
            case LBUTTONUP -> WM_LBUTTONUP;
            case RBUTTONDOWN -> WM_RBUTTONDOWN;
            case RBUTTONUP -> WM_RBUTTONUP;
            case MBUTTONDOWN -> WM_MBUTTONDOWN;
            case MBUTTONUP -> WM_MBUTTONUP;
            default -> 0;
        };
        int wParam = switch (msg) {
            case LBUTTONDOWN -> MK_LBUTTON;
            case RBUTTONDOWN -> MK_RBUTTON;
            case MBUTTONDOWN -> MK_MBUTTON;
            default -> 0;
        };
        int lParam = (y << 16) | x;
        User32.INSTANCE.SendMessage(hWnd, wmsg, new WinDef.WPARAM(wParam), new WinDef.LPARAM(lParam));
    }

    /** Gets the current list of windows.
     * @param only_visible Whether exclude the invisible window.
     * @return An ArrayList consists of HWndCtrls.
     */
    public static ArrayList<User32HWndCtrl> getWindowList(boolean only_visible) {
        ArrayList<User32HWndCtrl> windowList = new ArrayList<>();
        User32.INSTANCE.EnumWindows((hWnd, arg1) -> {
            if (User32.INSTANCE.IsWindow(hWnd) && (!only_visible || isVisible(hWnd)))
                windowList.add(new User32HWndCtrl(hWnd));
            return true;
        }, null);
        return windowList;
    }

    /** Gets the current list of windows. (Advanced)
     * @param only_visible Whether exclude the invisible window.
     * @param exclude_ws_ex Exclude the specific window-style-extra.
     * @return An ArrayList consists of HWndCtrls.
     */
    public static ArrayList<User32HWndCtrl> getWindowList(boolean only_visible, long exclude_ws_ex) {
        ArrayList<User32HWndCtrl> windowList = new ArrayList<>();
        User32.INSTANCE.EnumWindows((hWnd, arg1) -> {
            if (User32.INSTANCE.IsWindow(hWnd) && (!only_visible || isVisible(hWnd))
                    && (User32.INSTANCE.GetWindowLong(hWnd, WinUser.GWL_EXSTYLE) & exclude_ws_ex) != exclude_ws_ex)
                windowList.add(new User32HWndCtrl(hWnd));
            return true;
        }, null);
        return windowList;
    }

    public static MousePoint getMousePos() {
        WinDef.POINT point = new WinDef.POINT();
        boolean result = User32.INSTANCE.GetCursorPos(point);
        if (!result) return new MousePoint(0, 0);
        return new MousePoint(point.x, point.y);
    }

    /** Gets the value of the window's extended styles.
     * @return EX_STYLE value.
     * @see WinUser
     */
    protected int getWindowExStyle() {
        return User32.INSTANCE.GetWindowLong(hWnd, WinUser.GWL_EXSTYLE);
    }

    /** Sets the window's extended styles.
     * @param newLong New EX_STYLE value.
     * @see WinUser
     */
    protected void setWindowExStyle(int newLong) {
        User32.INSTANCE.SetWindowLong(hWnd, WinUser.GWL_EXSTYLE, newLong);
        User32.INSTANCE.SetWindowPos(hWnd, null, 0, 0, 0, 0, WinUser.SWP_NOSIZE | WinUser.SWP_NOMOVE | WinUser.SWP_NOZORDER | WinUser.SWP_FRAMECHANGED);
    }

    /** Gets the topmost window.
     * @return The topmost window's HWndCtrl.
     */
    protected static User32HWndCtrl getTopmostWindow() {
        return new User32HWndCtrl(new HWND(Pointer.createConstant(-1)));
    }

    protected static String getWindowText(HWND hWnd) {
        char[] text = new char[1024];
        User32.INSTANCE.GetWindowText(hWnd, text, 1024);
        return Native.toString(text);
    }

    protected static WindowRect getWindowRect(HWND hWnd) {
        WinDef.RECT rect = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(hWnd, rect);
        return new WindowRect(rect.top, rect.bottom, rect.left, rect.right);
    }

    protected static boolean isVisible(HWND hWnd) {
        try {
            if (!User32.INSTANCE.IsWindowVisible(hWnd) || !User32.INSTANCE.IsWindowEnabled(hWnd))
                return false;
            WindowRect rect = getWindowRect(hWnd);
            if (rect.top() == rect.bottom() || rect.left() == rect.right())
                return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User32HWndCtrl hWndCtrl = (User32HWndCtrl) o;
        return hWnd.equals(hWndCtrl.hWnd);
    }

    @Override
    public int hashCode() {
        return hWnd.hashCode();
    }

    @Override
    public String toString() {
        return "‘" + windowText + "’ " + windowWidth + "*" + windowHeight +
                " ex-style=0x" + Integer.toHexString(getWindowExStyle());
    }


    /** The Windows taskbar controller that implements the ITaskbarList COM interface.
     * @see <a href="https://learn.microsoft.com/en-us/windows/win32/api/shobjidl_core/nn-shobjidl_core-itaskbarlist">ITaskbarList</a>
     */
    public static class TaskbarList extends Unknown {
        private static final Guid.GUID CLSID = new Guid.GUID("{56FDF344-FD6D-11d0-958A-006097C9A090}");
        private static final Guid.GUID IID = new Guid.GUID("{56FDF342-FD6D-11d0-958A-006097C9A090}");
        private boolean initialized = false;

        private TaskbarList(Pointer ptr) {
            super(ptr);
        }

        public static TaskbarList create() {
            Ole32.INSTANCE.CoInitialize(null);
            PointerByReference p = new PointerByReference();
            HRESULT hr = Ole32.INSTANCE.CoCreateInstance(CLSID, Pointer.NULL, WTypes.CLSCTX_INPROC_SERVER, IID, p);
            COMUtils.checkRC(hr);
            return new TaskbarList(p.getValue());
        }

        public static TaskbarList createAndInit() {
            TaskbarList taskbarList = create();
            taskbarList.HrInit();
            return taskbarList;
        }

        /** Initializes the taskbar list object.
         * This method must be called before any other ITaskbarList methods can be called.
         */
        public void HrInit() {
            int res = this._invokeNativeInt(3, new Object[]{this.getPointer()});
            COMUtils.checkRC(new HRESULT(res));
            initialized = true;
        }

        /** Adds an item to the taskbar.
         * @param hwnd The handle of the window to be added.
         */
        public void AddTab(HWND hwnd) {
            if (!initialized)
                throw new IllegalStateException("TaskbarList not initialized.");
            int res = this._invokeNativeInt(4, new Object[]{this.getPointer(), hwnd});
            COMUtils.checkRC(new HRESULT(res));
        }

        /** Deletes an item from the taskbar.
         * @param hwnd The handle of the window to be deleted.
         */
        public void DeleteTab(HWND hwnd) {
            if (!initialized)
                throw new IllegalStateException("TaskbarList not initialized.");
            int res = this._invokeNativeInt(5, new Object[]{this.getPointer(), hwnd});
            COMUtils.checkRC(new HRESULT(res));
        }

        @Override
        public String toString() {
            return "TaskbarList{" + "pointer=" + this.getPointer() + '}';
        }
    }
}
