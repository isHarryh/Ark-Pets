package cn.harryh.arkpets.platform;

import java.util.List;


public abstract class HWndCtrlFactory {
    /** Finds a window.
     * @param className The window's class name.
     * @param windowText The window's title.
     * @return The HWndCtrl, which may be null indicates not found.
     */
    public abstract HWndCtrl findWindow(String className, String windowText);

    /** Gets the list of current windows.
     * @param onlyVisible Whether exclude the invisible window.
     * @return An ArrayList consists of HWndCtrls.
     */
    public abstract List<? extends HWndCtrl> getWindowList(boolean onlyVisible);

    /** Gets the topmost window.
     * @return The topmost window's HWndCtrl.
     */
    public abstract HWndCtrl getTopmostWindow();

    /** Gets the mouse position.
     * @return The MousePoint record.
     */
    public abstract HWndCtrl.MousePoint getMousePos();

    /** Frees all the resources.
     */
    public abstract void free();

    /** Return current WindowSystem should enable resize.
     */
    public abstract boolean needResize();

    /** Return current WindowSystem should enable decoration.
     */
    public abstract boolean needDecorated();
}
