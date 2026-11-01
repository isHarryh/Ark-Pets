/** Copyright (c) 2022-2026, Harry Huang, Half Nothing
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.tray;

import java.awt.*;


/** Layout utility for the popup menus of the tray icons.
 */
final class TrayPopup {
    private TrayPopup() {
    }

    /** Gets the current mouse pointer position in the logical screen space.
     * Note that the pointer position is reported in the logical screen space shared by the AWT and libGDX,
     * so it can be used directly to place the AWT components, even if the system scaling is not 100%.
     * @param reference The component used to determine the fallback location if the pointer is unavailable.
     * @return The mouse pointer position in the logical screen space (px).
     */
    static Point getPointerLocation(Component reference) {
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        if (pointerInfo != null)
            return pointerInfo.getLocation();
        // The pointer info may be unavailable when the pointer is outside all the screens.
        return reference != null ? reference.getLocationOnScreen() : new Point(0, 0);
    }

    /** Gets the top-left location of a popup menu of the given size, which is anchored near the given
     * point and kept inside the work area of the screen that contains the point.
     * @param anchor The anchor point in the logical screen space (px).
     * @param menuSize The size of the popup menu (px).
     * @return The top-left location of the popup menu (px).
     */
    static Point getLocation(Point anchor, Dimension menuSize) {
        Rectangle workArea = getWorkArea(anchor);
        // Prefer expanding the menu upwards from the anchor, and flip it downwards
        // if there is not enough space above.
        int top = anchor.y - menuSize.height;
        if (top < workArea.y)
            top = anchor.y;
        // Keep the menu inside the work area, so it never hides behind the task bar, the Dock, etc.
        top = Math.max(workArea.y, Math.min(top, workArea.y + workArea.height - menuSize.height));
        int left = Math.max(workArea.x,
                Math.min(anchor.x, workArea.x + workArea.width - menuSize.width));
        return new Point(left, top);
    }

    /** Gets the work area of the screen that contains the given point.
     * Note that the work area excludes the task bar, the Dock and any other reserved screen edges.
     * @param point The point in the logical screen space (px).
     * @return The bounds of the work area (px).
     */
    private static Rectangle getWorkArea(Point point) {
        GraphicsConfiguration gc = getGraphicsConfiguration(point);
        Rectangle bounds = gc.getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        return new Rectangle(
                bounds.x + insets.left,
                bounds.y + insets.top,
                Math.max(0, bounds.width - insets.left - insets.right),
                Math.max(0, bounds.height - insets.top - insets.bottom)
        );
    }

    /** Gets the configuration of the screen that contains the given point.
     * @param point The point in the logical screen space (px).
     * @return The graphics configuration of the screen.
     */
    private static GraphicsConfiguration getGraphicsConfiguration(Point point) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice device : ge.getScreenDevices()) {
            GraphicsConfiguration gc = device.getDefaultConfiguration();
            if (gc.getBounds().contains(point))
                return gc;
        }
        return ge.getDefaultScreenDevice().getDefaultConfiguration();
    }
}
