/** Copyright (c) 2022-2026, Harry Huang, Litwak913
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.platform;


public class NullHWndCtrl extends HWndCtrl {
    public NullHWndCtrl() {
        super("", new WindowRect());
    }

    @Override
    public boolean isForeground() {
        return false;
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public boolean close(int timeout) {
        return false;
    }

    @Override
    public HWndCtrl updated() {
        return null;
    }

    @Override
    public void setForeground() {
    }

    @Override
    public void setWindowPosition(HWndCtrl insertAfter, int x, int y, int w, int h) {
    }

    @Override
    public void setTaskbar(boolean enable) {
    }

    @Override
    public void setTopmost(boolean enable) {
    }

    @Override
    public void sendMouseEvent(MouseEvent msg, int x, int y) {
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
