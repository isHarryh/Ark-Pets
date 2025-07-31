package cn.harryh.arkpets.platform;

import java.util.List;


public class NullHWndCtrlFactory extends HWndCtrlFactory{
    private boolean startupFind;

    @Override
    public HWndCtrl findWindow(String className, String windowText) {
        if (windowText.equals("ArkPets")) {
            if (!startupFind) {
                startupFind = true;
                return null;
            }
        }
        return new NullHWndCtrl();
    }

    @Override
    public List<? extends HWndCtrl> getWindowList(boolean onlyVisible) {
        return List.of();
    }

    @Override
    public HWndCtrl getTopmostWindow() {
        return new NullHWndCtrl();
    }

    @Override
    public HWndCtrl.MousePoint getMousePos() {
        return new HWndCtrl.MousePoint(0, 0);
    }

    @Override
    public void free() {

    }

    @Override
    public boolean needResize() {
        return true;
    }

    @Override
    public boolean needDecorated() {
        return true;
    }
}
