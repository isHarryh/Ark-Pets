package cn.harryh.arkpets.platform;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import java.util.ArrayList;
import java.util.List;

import static cn.harryh.arkpets.platform.User32HWndCtrl.isVisible;


public class User32HWndCtrlFactory extends HWndCtrlFactory{
    @Override
    public HWndCtrl findWindow(String className, String windowText) {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(className, windowText);
        if (hwnd != null) {
            return new User32HWndCtrl(hwnd);
        }
        return null;
    }

    @Override
    public List<? extends HWndCtrl> getWindowList(boolean onlyVisible) {
        ArrayList<User32HWndCtrl> windowList = new ArrayList<>();
        User32.INSTANCE.EnumWindows((hWnd, arg1) -> {
            if (User32.INSTANCE.IsWindow(hWnd) && (!onlyVisible || isVisible(hWnd)))
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
    public ArrayList<User32HWndCtrl> getWindowList(boolean only_visible, long exclude_ws_ex) {
        ArrayList<User32HWndCtrl> windowList = new ArrayList<>();
        User32.INSTANCE.EnumWindows((hWnd, arg1) -> {
            if (User32.INSTANCE.IsWindow(hWnd) && (!only_visible || isVisible(hWnd))
                    && (User32.INSTANCE.GetWindowLong(hWnd, WinUser.GWL_EXSTYLE) & exclude_ws_ex) != exclude_ws_ex)
                windowList.add(new User32HWndCtrl(hWnd));
            return true;
        }, null);
        return windowList;
    }

    @Override
    public HWndCtrl getTopmostWindow() {
        return new User32HWndCtrl(new WinDef.HWND(Pointer.createConstant(-1)));
    }

    @Override
    public HWndCtrl.MousePoint getMousePos() {
        WinDef.POINT point = new WinDef.POINT();
        boolean result = User32.INSTANCE.GetCursorPos(point);
        if (!result) return new HWndCtrl.MousePoint(0, 0);
        return new HWndCtrl.MousePoint(point.x, point.y);
    }

    @Override
    public void free() {}

    @Override
    public boolean needResize() {
        return false;
    }

    @Override
    public boolean needDecorated() {
        return false;
    }
}
