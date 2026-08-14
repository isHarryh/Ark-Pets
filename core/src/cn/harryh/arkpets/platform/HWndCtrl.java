/** Copyright (c) 2022-2026, Harry Huang, Litwak913
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.platform;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import org.lwjgl.glfw.GLFW;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class HWndCtrl {
    public final String windowText;
    public final int posTop;
    public final int posBottom;
    public final int posLeft;
    public final int posRight;
    public final int windowWidth;
    public final int windowHeight;
    protected long glfwHandle;

    public HWndCtrl(String windowText, WindowRect windowRect) {
        this.windowText = windowText;
        posTop = windowRect.top;
        posBottom = windowRect.bottom;
        posLeft = windowRect.left;
        posRight = windowRect.right;
        windowWidth = posRight - posLeft;
        windowHeight = posBottom - posTop;
    }

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

    /** Gets a new HWndCtrl which contains the updated information of this window.
     * @return The up-to-dated HWndCtrl.
     */
    public abstract HWndCtrl updated();

    /** Sets the window as the foreground window.
     */
    public abstract void setForeground();

    /** Sets the window's position without activating the window.
     * @param insertAfter The window to precede the positioned window in the Z order.
     * @param x The new position of the left side of the window, in client coordinates.
     * @param y The new position of the top of the window, in client coordinates.
     * @param w The new width of the window, in pixels.
     * @param h The new height of the window, in pixels.
     */
    public abstract void setWindowPosition(HWndCtrl insertAfter, int x, int y, int w, int h);

    /** Sets the window's taskbar visibility.
     * @param enable Whether to let the window's entry visible on the taskbar.
     */
    public abstract void setTaskbar(boolean enable);

    /** Sets the window's topmost style.
     * @param enable Whether to enable the topmost style.
     */
    public abstract void setTopmost(boolean enable);

    /** Sets the window's ability to be passed through.
     * @param enable Whether the window can be passed through.
     */
    public void setTransparent(boolean enable) {
        if (glfwHandle == 0) return;
        GLFW.glfwSetWindowAttrib(glfwHandle, GLFW.GLFW_MOUSE_PASSTHROUGH, enable ? 1 : 0);
    }

    /** Sends a mouse event message to the window.
     * @param msg The window message value.
     * @param x The X-axis coordinate, related to the left border of the window.
     * @param y The Y-axis coordinate, related to the top border of the window.
     */
    public abstract void sendMouseEvent(MouseEvent msg, int x, int y);

    /** Attach a GLFW window handle to the window.
     * @param graphics The Lwjgl3Graphics instance.
     */
    public void attachGLFWWindow(Lwjgl3Graphics graphics) {
        this.glfwHandle = graphics.getWindow().getWindowHandle();
    }

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();

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

        public int getNumber(HWndCtrl hWndCtrl) {
            if (hWndCtrl == null) return -1;
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
            if (WindowSystem.findWindow(null, title) == null) {
                return title;
            } else {
                for (int cur = 2; cur <= 1024; cur++) {
                    title = String.format(numberedNameFormat, cur);
                    if (WindowSystem.findWindow(null, title) == null)
                        return title;
                }
                throw new IllegalStateException("Failed to get idle title.");
            }
        }
    }

    public record WindowRect(int top, int bottom, int left, int right) {
        public WindowRect() {
            this(0, 0, 0, 0);
        }

        public int width() { return right - left; }

        public int height() { return bottom - top; }
    }

    public enum MouseEvent {
        EMPTY,
        MOUSEMOVE,
        LBUTTONDOWN,
        LBUTTONUP,
        RBUTTONDOWN,
        RBUTTONUP,
        MBUTTONDOWN,
        MBUTTONUP,
    }

    public record MousePoint(int x, int y) {
    }
}
