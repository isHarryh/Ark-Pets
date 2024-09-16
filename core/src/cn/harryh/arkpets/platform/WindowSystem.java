/** Copyright (c) 2022-2024, Harry Huang, Litwak913
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.platform;

import cn.harryh.arkpets.utils.Logger;
import com.sun.jna.Platform;

import java.util.ArrayList;
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

    public static WindowSystem detectWindowSystem() {
        if (Platform.isWindows()) {
            return WindowSystem.USER32;
        } else if (Platform.isMac()) {
            return WindowSystem.QUARTZ;
        } else if (Platform.isLinux()) {
            String desktop = System.getenv("XDG_CURRENT_DESKTOP");
            String type = System.getenv("XDG_SESSION_TYPE");
            if (desktop.equals("GNOME")) {
                return WindowSystem.MUTTER;
            } else if (desktop.equals("KDE")) {
                return WindowSystem.KWIN;
            } else if (type.equals("x11")) {
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
        if (PLATFORM == WindowSystem.AUTO){
            PLATFORM = detectWindowSystem();
        }
        Logger.info("System", "Using " + PLATFORM.toString() + " Window System");
        switch (PLATFORM) {
            case MUTTER -> {
                MutterHWndCtrl.init();
            }
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
        switch (PLATFORM) {
            case USER32 -> {
                return User32HWndCtrl.find(className, windowText);
            }
            case MUTTER -> {
                return MutterHWndCtrl.find(className, windowText);
            }
            default -> {
                return NullHWndCtrl.find(className, windowText);
            }
        }
    }

    /** Gets the list of current windows.
     * @param onlyVisible Whether exclude the invisible window.
     * @return An ArrayList consists of HWndCtrls.
     */
    public static List<? extends HWndCtrl> getWindowList(boolean onlyVisible) {
        switch (PLATFORM) {
            case USER32 -> {
                return User32HWndCtrl.getWindowList(onlyVisible);
            }
            case MUTTER -> {
                return MutterHWndCtrl.getWindowList(onlyVisible);
            }
            default -> {
                return new ArrayList<>();
            }
        }
    }

    /** Gets the topmost window.
     * @return The topmost window's HWndCtrl.
     */
    public static HWndCtrl getTopmostWindow() {
        switch (PLATFORM) {
            case USER32 -> {
                return User32HWndCtrl.getTopmostWindow();
            }
            case MUTTER -> {
                return MutterHWndCtrl.getTopmostWindow();
            }
            default -> {
                return new NullHWndCtrl();
            }
        }
    }

    /** Frees all the resources.
     */
    public static void free() {
        switch (PLATFORM) {
            case MUTTER -> {
                MutterHWndCtrl.free();
            }
        }
    }

    /** Return current WindowSystem should enable resize.
     */
    public static boolean needResize() {
        switch (PLATFORM) {
            case X11, MUTTER, KWIN -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}
