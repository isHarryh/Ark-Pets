/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import javafx.scene.shape.SVGPath;


abstract public class Handbook {
    public boolean hasShown = false;

    public Handbook() {
    }

    abstract public String getTitle();

    abstract public String getHeader();

    abstract public String getContent();

    public SVGPath getIcon() {
        return PopupUtils.IconUtil.getIcon(PopupUtils.IconUtil.ICON_HELP_ALT, PopupUtils.COLOR_INFO);
    }

    public boolean hasShown() {
        return hasShown;
    }

    public void setShown() {
        hasShown = true;
    }
}
