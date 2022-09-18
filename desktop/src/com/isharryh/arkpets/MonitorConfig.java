/** Copyright (c) 2022, Harry Huang
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
        return new int[] {(int)displayMode.width, (int)displayMode.height, (int)displayMode.refreshRate, (int)displayMode.bitsPerPixel};
    }
}
