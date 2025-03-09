package cn.harryh.arkpets.startup;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.utils.IOUtils;
import cn.harryh.arkpets.utils.Logger;

import java.io.File;
import java.io.FileNotFoundException;

import static cn.harryh.arkpets.Const.charsetDefault;


public class XDGStartupConfig extends StartupConfig {
    private boolean available;
    private File startupFile;
    private String desktopContent;

    private static final String startupTarget    = "ArkPets-" + Const.appVersion + ".AppImage";
    private static final String startupShortcut  = "ArkPetsStartup.desktop";

    public XDGStartupConfig() {
        try {
            File startupDir = new File(System.getProperty("user.home") + "/.config/autostart");
            if (!startupDir.isDirectory())
                throw new FileNotFoundException("Startup dir not found: " + startupDir.getAbsolutePath());
            if (!new File(startupTarget).exists())
                throw new FileNotFoundException("Executable not found.");
            this.desktopContent = new String(XDGStartupConfig.class.getResourceAsStream("/utils/xdgstartup.desktop").readAllBytes());
            this.startupFile = new File(startupDir.getAbsolutePath(), startupShortcut);
            this.available = true;
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
            String cd = System.getProperty("user.dir");
            String content = desktopContent
                    .replace("{{ARKPETS_WORKING_DIR}}",cd)
                    .replace("{{ARKPETS_EXECUTABLE}}",cd + "/" + startupTarget + " --direct-start");
            IOUtils.FileUtil.writeString(startupFile, charsetDefault, content, false);
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
