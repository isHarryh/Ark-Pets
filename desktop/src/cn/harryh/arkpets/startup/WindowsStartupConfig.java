/** Copyright (c) 2022-2024, Harry Huang, Litwak913
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.startup;

import cn.harryh.arkpets.naitves.IPersistFile;
import cn.harryh.arkpets.naitves.IShellLink;
import cn.harryh.arkpets.utils.IOUtils;
import cn.harryh.arkpets.utils.Logger;

import java.io.File;
import java.io.FileNotFoundException;


public class WindowsStartupConfig extends StartupConfig {
    private boolean available;
    private File startupFile;

    private static final String startupTarget    = "ArkPets.exe";
    private static final String startupShortcut  = "ArkPetsStartup.lnk";
    private static final String oldStartupScript = "ArkPetsStartupService.vbs";

    public WindowsStartupConfig() {
        try {
            File startupDir = new File(System.getProperty("user.home") + "/AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Startup");
            if (!startupDir.isDirectory())
                throw new FileNotFoundException("Startup dir not found: " + startupDir.getAbsolutePath());
            if (!new File(startupTarget).exists())
                throw new FileNotFoundException("Executable not found.");

            this.startupFile = new File(startupDir.getAbsolutePath(), startupShortcut);
            this.available = true;

            File oldStartup = new File(startupDir.getAbsolutePath(), oldStartupScript);
            try {
                if (oldStartup.exists()) {
                    Logger.info("Config", "Found old version startup, migrate to new approach.");
                    if (oldStartup.delete())
                        addStartup();
                }
            } catch (Exception e) {
                Logger.error("Config", "Cannot migrate startup, details see below.", e);
            }
        } catch (Exception e) {
            this.startupFile = null;
            this.available = false;
            Logger.debug("Config", "Auto-startup is unavailable.");
        }
    }

    @Override
    public boolean addStartup() {
        if (!this.available) return false;
        try {
            IShellLink lnk = IShellLink.create();
            IPersistFile pf = lnk.getPF();
            String cd = System.getProperty("user.dir");
            cd = cd.replaceAll("\"", "\"\"");
            lnk.SetPath(cd + "\\" + startupTarget);
            lnk.SetArguments("--direct-start");
            lnk.SetWorkingDirectory(cd);
            pf.Save(startupFile.getAbsolutePath().replaceAll("\"", "\"\""));
            pf.Release();
            lnk.Release();
            Logger.info("Config", "Auto-startup added.");
            return true;
        } catch (Exception e) {
            Logger.error("Config", "Auto-startup adding failed, details see below.", e);
            return false;
        }
    }

    @Override
    public void removeStartup() {
        try {
            IOUtils.FileUtil.delete(startupFile.toPath(), false);
            Logger.info("Config", "Auto-startup removed.");
        } catch (Exception e) {
            Logger.error("Config", "Auto-startup removing failed, details see below.", e);
        }
    }

    @Override
    public boolean isSetStartup() {
        return this.available && startupFile.exists();
    }

    @Override
    public boolean isStartupAvailable() {
        return this.available;
    }
}
