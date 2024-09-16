/** Copyright (c) 2022-2024, Harry Huang, Litwak913
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.platform;


import com.badlogic.gdx.Gdx;


public class NullHWndCtrl extends HWndCtrl {
    private static boolean startupFind;
    private int lastw, lasth;

    public NullHWndCtrl() {
        super("", new WindowRect());
    }

    public static NullHWndCtrl find(String className, String windowName){
        if(windowName.equals("ArkPets")){
            if(!NullHWndCtrl.startupFind){
                NullHWndCtrl.startupFind=true;
                return null;
            }
        }
        return new NullHWndCtrl();
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
    public void setWindowAlpha(float alpha) {
    }

    @Override
    public void setWindowPosition(HWndCtrl insertAfter, int x, int y, int w, int h) {
        if(lasth!=h||lastw!=w){
            lasth=h;
            lastw=w;
            Gdx.graphics.setWindowedMode(w,h);
        }
    }

    @Override
    public void setTaskbar(boolean enable) {
    }

    @Override
    public void setLayered(boolean enable) {
    }

    @Override
    public void setTopmost(boolean enable) {
    }

    @Override
    public void setTransparent(boolean enable) {
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
