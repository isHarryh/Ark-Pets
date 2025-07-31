package cn.harryh.arkpets.platform;

import cn.harryh.arkpets.rpc.MutterInterface;
import cn.harryh.arkpets.utils.HdpiUtils;
import org.freedesktop.dbus.types.UInt32;

import static cn.harryh.arkpets.platform.MutterHWndCtrlFactory.dBusInterface;
import static cn.harryh.arkpets.utils.HdpiUtils.toBackBufferX;
import static cn.harryh.arkpets.utils.HdpiUtils.toBackBufferY;


public class MutterHWndCtrl extends WaylandHWndCtrl {
    protected final UInt32 hWnd;
    protected MutterInterface.DetailsStruct details;

    protected MutterHWndCtrl(MutterInterface.DetailsStruct details) {
        super(details.title, new WindowRect(
                toBackBufferY(details.y),
                toBackBufferY(details.y + details.h.intValue()),
                toBackBufferX(details.x),
                toBackBufferX(details.x + details.w.intValue())));
        this.hWnd = details.id;
        this.details = details;
    }

    @Override
    public boolean isForeground() {
        return dBusInterface.IsActive(hWnd);
    }

    @Override
    public boolean isVisible() {
        return details.visible;
    }

    @Override
    public boolean close(int timeout) {
        return false;
    }

    @Override
    public HWndCtrl updated() {
        return new MutterHWndCtrl(dBusInterface.Details(hWnd));
    }

    @Override
    public void setForeground() {
        dBusInterface.Activate(hWnd);
    }

    @Override
    public void setWindowPosition(HWndCtrl insertAfter, int x, int y, int w, int h) {
        dBusInterface.MoveResize(hWnd, HdpiUtils.toLogicalX(x), HdpiUtils.toLogicalY(y),
                new UInt32(HdpiUtils.toLogicalX(w)), new UInt32(HdpiUtils.toLogicalY(h)));
    }

    @Override
    public void setTaskbar(boolean enable) {
        dBusInterface.Stick(hWnd, !enable);
    }

    @Override
    public void setTopmost(boolean enable) {
        dBusInterface.Above(hWnd, enable);
    }

    @Override
    public void sendMouseEvent(MouseEvent msg, int x, int y) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MutterHWndCtrl hWndCtrl = (MutterHWndCtrl) o;
        return hWnd.equals(hWndCtrl.hWnd);
    }

    @Override
    public int hashCode() {
        return hWnd.hashCode();
    }

}
