/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.hwnd;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class HWndCtrl<T> {
    public final T hWnd; //Platform HWND
    public final String windowText;
    public final int posTop;
    public final int posBottom;
    public final int posLeft;
    public final int posRight;
    public final int windowWidth;
    public final int windowHeight;
    public HWndCtrl(){
        hWnd = null;
        windowText = "";
        posTop = 0;
        posBottom = 0;
        posLeft = 0;
        posRight = 0;
        windowWidth = 0;
        windowHeight = 0;
    }
    public HWndCtrl(T hWnd) {
        this.hWnd = hWnd;
        windowText = getWindowText(hWnd);
        WindowRect rect = getWindowRect(hWnd);
        posTop = rect.top;
        posBottom = rect.bottom;
        posLeft = rect.left;
        posRight = rect.right;
        windowWidth = posRight-posLeft;
        windowHeight = posBottom-posTop;
    }

    /** Returns window rect.
     */
    public abstract WindowRect getWindowRect(T hWnd);

    /** Returns window title.
     */
    public abstract String getWindowText(T hWnd);

    /** Returns true if the handle is empty.
     */
    public abstract boolean isEmpty();

    /** Returns true if the window is a foreground window now.
     */
    public abstract boolean isForeground();

    /** Returns true if the window is visible now.
     */
    public abstract boolean isVisible();

    /** Gets the center X position.
     * @return X.
     */
    public float getCenterX() {
        return posLeft + windowWidth / 2f;
    }

    /** Gets the center Y position.
     * @return Y.
     */
    public float getCenterY() {
        return posTop + windowHeight / 2f;
    }

    /** Requests to close the window.
     * @param timeout Timeout for waiting response (ms).
     * @return true=success, false=failure.
     */
    public abstract boolean close(int timeout);

    /** Sets the window as the foreground window.
     */
    public abstract void setForeground();

    /** Sets the window's transparency.
     * @param alpha Alpha value, from 0 to 1.
     */
    public abstract void setWindowAlpha(float alpha);

    /** Sets the window's position without activating the window.
     * @param insertAfter The window to precede the positioned window in the Z order.
     * @param x The new position of the left side of the window, in client coordinates.
     * @param y The new position of the top of the window, in client coordinates.
     * @param w The new width of the window, in pixels.
     * @param h The new height of the window, in pixels.
     */
    public abstract void setWindowPosition(HWndCtrl<T> insertAfter, int x, int y, int w, int h);

    /** Sets the window's ability to be passed through.
     * @param transparent Whether the window can be passed through.
     */
    public abstract void setWindowTransparent(boolean transparent);

    /**
     * Sets the window is a tool window.
     * @param enable Whether the window is a tool window.
     */
    public abstract void setToolWindow(boolean enable);

    /**
     * Sets the window is layered.
     * @param enable Whether the window is layered.
     */
    public abstract void setLayered(boolean enable);

    /**
     * Sets the window is topmost.
     * @param enable Whether the window is topmost.
     */
    public abstract void setTopMost(boolean enable);

    /** Sends a mouse event message to the window.
     * @param msg The window message value.
     * @param x The X-axis coordinate, related to the left border of the window.
     * @param y The Y-axis coordinate, related to the top border of the window.
     */
    public abstract void sendMouseEvent(MouseEvent msg, int x, int y);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HWndCtrl hWndCtrl = (HWndCtrl)o;
        return hWnd.equals(hWndCtrl.hWnd);
    }

    @Override
    public int hashCode() {
        return hWnd.hashCode();
    }

    @Override
    public String toString() {
        return "‘" + windowText + "’ " + windowWidth + "*" + windowHeight;
    }


    public static class NumberedTitleManager {
        private final String zeroNameFormat;
        private final String numberedNameFormat;
        private final Pattern zeroNamePattern;
        private final Pattern numberedNamePattern;

        public NumberedTitleManager(String coreName) {
            zeroNameFormat = coreName;
            numberedNameFormat = coreName + " (%d)";
            zeroNamePattern = Pattern.compile("^" + coreName + "$");
            numberedNamePattern = Pattern.compile("^" + coreName + " \\(([0-9]+)\\)");
        }

        public int getNumber(HWndCtrl<?> hWndCtrl) {
            if (hWndCtrl.isEmpty()) return -1;
            return getNumber(hWndCtrl.windowText);
        }

        public int getNumber(String windowText) {
            if (windowText.isEmpty()) return -1;
            if (zeroNamePattern.matcher(windowText).find()) return 0;
            try {
                Matcher matcher = numberedNamePattern.matcher(windowText);
                return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
            } catch (NumberFormatException ignored) {
                return -1;
            }
        }

        public String getIdleTitle() {
            String title = String.format(zeroNameFormat);
            if (HWndCtrlFactory.find(null, title) == null) {
                return title;
            } else {
                for (int cur = 2; cur <= 1024 ; cur ++) {
                    title = String.format(numberedNameFormat, cur);
                    if (HWndCtrlFactory.find(null, title) == null)
                        return title;
                }
                throw new IllegalStateException("Failed to get idle title.");
            }
        }
    }

    public static class WindowRect{
        public int top;
        public int bottom;
        public int right;
        public int left;
        public WindowRect(){}
        public WindowRect(int x,int y,int h,int w){
            this.top=y;
            this.left=x;
            this.bottom=y+h;
            this.right=x+w;
        }
    }

    public enum MouseEvent{
        EMPTY,
        MOUSEMOVE,
        LBUTTONDOWN,
        LBUTTONUP,
        RBUTTONDOWN,
        RBUTTONUP,
        MBUTTONDOWN,
        MBUTTONUP,
    }
}
