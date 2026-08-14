/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import java.util.ArrayList;
import java.util.List;


public class Monitor {
    private final String name;
    private final int width;
    private final int height;
    private final int virtualX;
    private final int virtualY;
    private final int refreshRate;
    private final int bbp;

    private Monitor(Graphics.Monitor m, Graphics.DisplayMode dm) {
        this.name = m.name;
        this.width = dm.width;
        this.height = dm.height;
        this.virtualX = m.virtualX;
        this.virtualY = m.virtualY;
        this.refreshRate = dm.refreshRate;
        this.bbp = dm.bitsPerPixel;
    }

    public String getName() {
        return name;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getVirtualX() {
        return virtualX;
    }

    public int getVirtualY() {
        return virtualY;
    }

    /** Gets the information of all the existing monitors.
     * @return A list of Monitor objects.
     */
    public static List<Monitor> getMonitors() {
        synchronized (Monitor.class) {
            ArrayList<Monitor> list = new ArrayList<>();
            Graphics.Monitor[] monitors = Lwjgl3ApplicationConfiguration.getMonitors();
            for (Graphics.Monitor m : monitors) {
                Graphics.DisplayMode dm = Lwjgl3ApplicationConfiguration.getDisplayMode(m);
                Monitor monitor = new Monitor(m, dm);
                list.add(monitor);
            }
            return list;
        }
    }

    /** Gets the maximum refresh rate of all the existing monitors.
     * @return The maximum refresh rate (Hz), or -1 if failed.
     */
    public static int getMaxRefreshRate() {
        synchronized (Monitor.class) {
            int maxRate = -1;
            Graphics.Monitor[] monitors = Lwjgl3ApplicationConfiguration.getMonitors();
            for (Graphics.Monitor m : monitors) {
                Graphics.DisplayMode dm = Lwjgl3ApplicationConfiguration.getDisplayMode(m);
                if (dm.refreshRate > maxRate) {
                    maxRate = dm.refreshRate;
                }
            }
            return maxRate;
        }
    }

    @Override
    public String toString() {
        // Example: "Monitor 1: 1920x1080 @ (0,0), 60Hz, 32 bpp"
        return "%s: %dx%d @ (%d,%d), %dHz, %d bpp".formatted(name, width, height, virtualX, virtualY, refreshRate, bbp);
    }
}
