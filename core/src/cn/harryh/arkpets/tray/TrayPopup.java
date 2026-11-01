/** Copyright (c) 2022-2026, Harry Huang, Half Nothing
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.tray;

import cn.harryh.arkpets.Const;

import java.awt.*;
import java.awt.geom.AffineTransform;


/** Layout utility for the popup menus of the tray icons.
 */
final class TrayPopup {
    private TrayPopup() {
    }

    /** Adapts the coordinate of a tray icon mouse event to the logical screen space.
     * Note that the tray icon mouse event coordinate is measured in the device space on Windows,
     * while the AWT and libGDX share the logical screen space on the other platforms.
     * @param x The x-position of the mouse event (px).
     * @param y The y-position of the mouse event (px).
     * @param reference The component used to determine the system scaling.
     * @return The coordinate in the logical screen space (px).
     */
    static Point toLogicalPoint(int x, int y, Component reference) {
        /* Use `System.setProperty("sun.java2d.uiScale", "1")` can also avoid system scaling.
        Here we will adapt the coordinate for system scaling artificially. */
        if (Const.isWindows) {
            AffineTransform at = reference.getGraphicsConfiguration().getDefaultTransform();
            return new Point((int) (x / at.getScaleX()), (int) (y / at.getScaleY()));
        }
        return new Point(x, y);
    }

    /** Gets the top-left location of a popup menu of the given size, which is anchored near the given
     * point and kept inside the screen that contains the point.
     * @param x The anchor x-position in the logical screen space (px).
     * @param y The anchor y-position in the logical screen space (px).
     * @param menuSize The size of the popup menu (px).
     * @return The top-left location of the popup menu (px).
     */
    static Point getLocation(int x, int y, Dimension menuSize) {
        Rectangle screen = getScreenBounds(x, y);
        // Prefer expanding the menu upwards from the anchor, and flip it downwards
        // if there is not enough space above.
        int top = y - menuSize.height;
        if (top < screen.y)
            top = y;
        // Keep the menu horizontally inside the screen.
        int left = Math.max(screen.x, Math.min(x, screen.x + screen.width - menuSize.width));
        return new Point(left, top);
    }

    /** Gets the bounds of the screen that contains the given point.
     * @param x The x-position in the logical screen space (px).
     * @param y The y-position in the logical screen space (px).
     * @return The bounds of the screen (px).
     */
    private static Rectangle getScreenBounds(int x, int y) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice device : ge.getScreenDevices()) {
            Rectangle bounds = device.getDefaultConfiguration().getBounds();
            if (bounds.contains(x, y))
                return bounds;
        }
        return ge.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
    }
}
