/** Copyright (c) 2022-2026, Harry Huang, Litwak913
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.platform;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.utils.Logger;

import java.util.List;


public enum WindowSystem {
    AUTO,
    USER32,
    X11,
    MUTTER,
    KWIN,
    QUARTZ,
    NULL;

    private static WindowSystem PLATFORM = null;
    private static HWndCtrlFactory factory;

    public static WindowSystem detectWindowSystem() {
        if (Const.isWindows) {
            return WindowSystem.USER32;
        } else if (Const.isMac) {
            return WindowSystem.QUARTZ;
        } else if (Const.isLinux) {
            String desktop = System.getenv("XDG_CURRENT_DESKTOP");
            String type = System.getenv("XDG_SESSION_TYPE");
            if (desktop != null && type != null) {
                if (type.equals("x11")) {
                    return WindowSystem.X11;
                } else if (type.equals("wayland")) {
                    for (String desktopName : desktop.split(":")) {
                        if (desktopName.equals("GNOME")) return WindowSystem.MUTTER;
                        if (desktopName.equals("KDE")) return WindowSystem.KWIN;
                    }
                }
            } else {
                return WindowSystem.X11;
            }
        }
        return WindowSystem.NULL;
    }

    /** Initializes the platform window system.
     * @param platform WindowSystem to initialize.
     */
    public static void init(WindowSystem platform) {
        PLATFORM = platform;
        if (PLATFORM == WindowSystem.AUTO) {
            PLATFORM = detectWindowSystem();
        }
        Logger.info("System", "Using " + PLATFORM.toString() + " Window System");
        switch (PLATFORM) {
            case USER32 -> factory = new User32HWndCtrlFactory();
            case MUTTER -> factory = new MutterHWndCtrlFactory();
            case KWIN -> factory = new KWinHWndCtrlFactory();
            case X11 -> factory = new X11HWndCtrlFactory();
            case QUARTZ -> factory = new QuartzHWndCtrlFactory();
            case NULL -> factory = new NullHWndCtrlFactory();
        }
    }

    /** Get current WindowSystem.
     * @return The current WindowSystem.
     */
    public static WindowSystem getWindowSystem() {
        return PLATFORM;
    }

    /** Finds a window.
     * @param className The window's class name.
     * @param windowText The window's title.
     * @return The HWndCtrl, which may be null indicates not found.
     */
    public static HWndCtrl findWindow(String className, String windowText) {
        return factory.findWindow(className, windowText);
    }

    /** Gets the list of current windows.
     * @param onlyVisible Whether exclude the invisible window.
     * @return An ArrayList consists of HWndCtrls.
     */
    public static List<? extends HWndCtrl> getWindowList(boolean onlyVisible) {
        return factory.getWindowList(onlyVisible);
    }

    /** Gets the topmost window.
     * @return The topmost window's HWndCtrl.
     */
    public static HWndCtrl getTopmostWindow() {
        return factory.getTopmostWindow();
    }

    /** Gets the mouse position.
     * @return The MousePoint record.
     */
    public static HWndCtrl.MousePoint getMousePos() {
        return factory.getMousePos();
    }

    /** Frees all the resources.
     */
    public static void free() {
        if (factory == null) return;
        factory.free();
        factory = null;
    }

    /** Return current WindowSystem should enable resize.
     */
    public static boolean needResize() {
        return factory.needResize();
    }

    /** Return current WindowSystem should enable decoration.
     */
    public static boolean needDecorated() {
        return factory.needDecorated();
    }
}
