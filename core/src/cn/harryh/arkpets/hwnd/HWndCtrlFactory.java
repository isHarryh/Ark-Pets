package cn.harryh.arkpets.hwnd;

import cn.harryh.arkpets.utils.Logger;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.WinDef;

import java.util.ArrayList;
import java.util.List;


public class HWndCtrlFactory {
    private static WindowSystem platform;
    public static HWndCtrl<?> EMPTY;
    public enum WindowSystem {
        AUTO,
        USER32,
        X11,
        MUTTER,
        KWIN,
        QUARTZ,
        NULL
    }
    public static WindowSystem detectWindowSystem(){
        if(Platform.isWindows()){
            return WindowSystem.USER32;
        } else if (Platform.isMac()) {
            return WindowSystem.QUARTZ;
        } else if (Platform.isLinux()) {
            String desktop=System.getenv("XDG_CURRENT_DESKTOP");
            String type=System.getenv("XDG_SESSION_TYPE");
            if (desktop.equals("GNOME")){
                return WindowSystem.MUTTER;
            } else if (desktop.equals("KDE")) {
                return WindowSystem.KWIN;
            } else if (type.equals("x11")) {
                return WindowSystem.X11;
            }
        }
        return WindowSystem.NULL;
    }

    /**
     * Init window system.
     */
    public static void init(){
        platform=detectWindowSystem();
        Logger.info("System","Using "+platform.toString()+" Window System");
        //establish connection
        switch (platform){
            case MUTTER -> {
                //todo
            }
        }
        EMPTY=create();
    }
    /**
     * Find a window.
     * @param className window's class name.
     * @param windowName window's title.
     * @return HWndCtrl
     */
    public static HWndCtrl<?> find(String className, String windowName){
        switch (platform){
            case USER32 -> {
                return User32HWndCtrl.find(className,windowName);
            }
            default -> {
                return new NullHWndCtrl();
            }
        }
    }

    /**
     * Create empty HWndCtrl.
     * @return empty HWndCtrl
     */
    public static HWndCtrl<?> create(){
        switch (platform){
            case USER32 -> {
                return new User32HWndCtrl();
            }
            default -> {
                return new NullHWndCtrl();
            }
        }
    }
    /** Gets the current list of windows.
     * @param only_visible Whether exclude the invisible window.
     * @return An ArrayList consists of HWndCtrls.
     */
    public static List<? extends HWndCtrl<?>> getWindowList(boolean only_visible){
        switch (platform){
            case USER32 -> {
                return User32HWndCtrl.getWindowList(only_visible);
            }
            default-> {
                return new ArrayList<>();
            }
        }
    }

    /**
     * Gets the topmost window.
     * @return The topmost window's HWndCtrl.
     */
    public static HWndCtrl<?> getTopMost(){
        switch (platform){
            case USER32 -> {
                return User32HWndCtrl.getTopMost();
            }
            default-> {
                return new NullHWndCtrl();
            }
        }
    }

    /** Gets a new HWndCtrl which contains the updated information of this window.
     * @return The up-to-dated HWndCtrl.
     */
    public static HWndCtrl<?> update(HWndCtrl<?> old){
        switch (platform){
            case USER32 -> {
                return new User32HWndCtrl((WinDef.HWND) old.hWnd);
            }
            default-> {
                return new NullHWndCtrl();
            }
        }
    }

    /** Free all resources.
     */
    public static void free(){
        switch (platform){
            case X11 -> {
                //todo
            }
            default-> {
                return;
            }
        }
    }
}
