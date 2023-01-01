/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets;

import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;


public class MonitorConfig {
    private MonitorConfig() {
    }

    public static int[] getDefaultMonitorInfo() {
        DisplayMode displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
        return new int[] {displayMode.width, displayMode.height, displayMode.refreshRate, displayMode.bitsPerPixel};
    }
}
