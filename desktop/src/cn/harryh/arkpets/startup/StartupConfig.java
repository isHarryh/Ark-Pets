/** Copyright (c) 2022-2024, Harry Huang, Litwak913
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.startup;

import com.sun.jna.Platform;


public abstract class StartupConfig {
    /** Gets the platform StartupConfig.
     * @return platform StartupConfig.
     */
    public static StartupConfig getInstance() {
        if (Platform.isWindows()) {
            return new WindowsStartupConfig();
        }
        return new NullStartupConfig();
    }

    /** Enables auto-startup.
     * @return true=success, false=failure.
     */
    public abstract boolean addStartup();

    /** Disables auto-startup.
     */
    public abstract void removeStartup();

    /** Returns true if auto-startup is enabled.
     */
    public abstract boolean isSetStartup();

    /** Returns true if auto-startup is available.
     */
    public abstract boolean isStartupAvailable();
}
