/** Copyright (c) 2022-2024, Harry Huang, Litwak913
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.startup;

import cn.harryh.arkpets.Const;
import com.sun.jna.Platform;


public abstract class StartupConfig {
    /** Gets the platform StartupConfig.
     * @return platform StartupConfig.
     */
    public static StartupConfig getInstance() {
        if (Const.isWindows) {
            return new WindowsStartupConfig();
        } else if (Const.isLinux) {
            return new XDGStartupConfig();
        } else if (Const.isMac) {
            return new LaunchdStartupConfig();
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
