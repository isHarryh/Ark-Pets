/** Copyright (c) 2022-2024, Harry Huang, Litwak913
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.startup;

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
}
