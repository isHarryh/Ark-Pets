/** Copyright (c) 2022-2026, Harry Huang, Litwak913
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.platform;

public class NullStartupConfig extends StartupConfig {
    @Override
    public boolean addStartup() {
        return true;
    }

    @Override
    public void removeStartup() {
    }

    @Override
    public boolean isSetStartup() {
        return false;
    }

    @Override
    public boolean isStartupAvailable() {
        return false;
    }

    @Override
    public boolean isAutoUpdateAvailable() {
        return false;
    }
}
