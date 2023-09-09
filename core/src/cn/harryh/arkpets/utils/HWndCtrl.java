/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.W32APIOptions;

import java.util.ArrayList;


public class HWndCtrl {
    public final HWND hWnd;
    public final String windowText;
    public final Pointer windowPointer;
    public final int posTop;
    public final int posBottom;
    public final int posLeft;
    public final int posRight;
    public final int windowWidth;
    public final int windowHeight;
    public static final HWndCtrl EMPTY = new HWndCtrl();

    /** HWnd Controller instance.
     * @param $hWnd The handle of the window.
     */
    public HWndCtrl(HWND $hWnd) {
        hWnd = $hWnd;
        windowText = getWindowText(hWnd);
        windowPointer = getWindowIdx($hWnd);
        RECT rect = getWindowRect(hWnd);
        posTop = rect.top;
        posBottom = rect.bottom;
        posLeft = rect.left;
        posRight = rect.right;
        windowWidth = posRight-posLeft;
        windowHeight = posBottom-posTop;
    }

    /** Empty HWnd Controller instance.
     */
    public HWndCtrl() {
        hWnd = null;
        windowText = null;
        windowPointer = null;
        posTop = 0;
        posBottom = 0;
        posLeft = 0;
        posRight = 0;
        windowWidth = 0;
        windowHeight = 0;
    }

    /** Judge whether the window is visible.
     * @return true=visible, false=invisible.
     */
    public boolean isVisible() {
        return isVisible(hWnd);
    }

    /** Get the center X position.
     * @return X.
     */
    public float getCenterX() {
        return posLeft + windowWidth / 2f;
    }

    /** Get the center Y position.
     * @return Y.
     */
    public float getCenterY() {
        return posTop + windowHeight / 2f;
    }

    /** Get the RGB color value at the specified position of the window.
     * @param $x The X-axis coordinate, related to the left border of the window.
     * @param $y The Y-axis coordinate, related to the top border of the window.
     * @return The color array (R,G,B).
     */
    public int[] getPixel(int $x, int $y) {
        WinDef.HDC hdc = User32.INSTANCE.GetDC(hWnd);
        int color = GDI32Extended.INSTANCE.GetPixel(hdc, $x, $y);
        int r = (color) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color >> 16) & 0xFF;
        User32.INSTANCE.ReleaseDC(hWnd, hdc);
        return new int[] {r, g, b};
    }

    /** Request to close the window.
     * @param $timeout Timeout for waiting response (ms).
     * @return true=success, false=failure.
     */
    public boolean close(int $timeout) {
        return User32.INSTANCE.SendMessageTimeout(hWnd, 0x10, null, null, $timeout, WinUser.SMTO_NORMAL, null).intValue() == 0;
    }

    /** Get the current list of windows.
     * @param $only_visible Whether exclude the invisible window.
     * @return An ArrayList consists of HWndCtrls.
     */
    public static ArrayList<HWndCtrl> getWindowList(boolean $only_visible) {
        ArrayList<HWndCtrl> windowList = new ArrayList<>();
        User32.INSTANCE.EnumWindows((hWnd, arg1) -> {
            if (User32.INSTANCE.IsWindow(hWnd) && (!$only_visible || isVisible(hWnd)))
                windowList.add(new HWndCtrl(hWnd));
            return true;
        }, null);
        return windowList;
    }

    /** Get the current list of windows. (Advanced)
     * @param $only_visible Whether exclude the invisible window.
     * @param $exclude_ws_ex Exclude the specific window-style-extra.
     * @return An ArrayList consists of HWndCtrls.
     */
    public static ArrayList<HWndCtrl> getWindowList(boolean $only_visible, long $exclude_ws_ex) {
        ArrayList<HWndCtrl> windowList = new ArrayList<>();
        User32.INSTANCE.EnumWindows((hWnd, arg1) -> {
            if (User32.INSTANCE.IsWindow(hWnd) && (!$only_visible || isVisible(hWnd))
                    && (User32.INSTANCE.GetWindowLong(hWnd, WinUser.GWL_EXSTYLE) & $exclude_ws_ex) != $exclude_ws_ex)
                windowList.add(new HWndCtrl(hWnd));
            return true;
        }, null);
        return windowList;
    }

    private static boolean isVisible(HWND $hWnd) {
        try {
            if (!User32.INSTANCE.IsWindowVisible($hWnd) || !User32.INSTANCE.IsWindowEnabled($hWnd))
                return false;
            RECT rect = getWindowRect($hWnd);
            if (rect.top == rect.bottom || rect.left == rect.right)
                return false;
        } catch(Exception e) {
            return false;
        }
        return true;
    }

    private static Pointer getWindowIdx(HWND $hWnd) {
        return $hWnd.getPointer();
    }

    private static String getWindowText(HWND $hWnd) {
        char[] text = new char[1024];
        User32.INSTANCE.GetWindowText($hWnd, text, 512);
        return Native.toString(text);
    }

    private static  RECT getWindowRect(HWND $hWnd) {
        RECT rect = new RECT();
        User32.INSTANCE.GetWindowRect($hWnd, rect);
        return rect;
    }

    @Override
    public String toString() {
        return "‘" + windowText + "’ " + windowWidth + "*" + windowHeight;
    }


    private interface GDI32Extended extends com.sun.jna.platform.win32.GDI32 {
        GDI32Extended INSTANCE = Native.load("gdi32", GDI32Extended.class, W32APIOptions.DEFAULT_OPTIONS);

        int GetPixel(WinDef.HDC hdc, int x, int y);
    }
}
