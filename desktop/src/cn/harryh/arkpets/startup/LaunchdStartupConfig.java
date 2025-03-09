package cn.harryh.arkpets.startup;

import cn.harryh.arkpets.utils.IOUtils;
import cn.harryh.arkpets.utils.Logger;

import java.io.File;
import java.io.FileNotFoundException;

import static cn.harryh.arkpets.Const.charsetDefault;


public class LaunchdStartupConfig extends StartupConfig{
    private boolean available;
    private File startupFile;
    private String plistContent;

    private static final String startupTarget = "/Applications/ArkPets.app";
    private static final String startupPlist  = "cn.harryh.arkpets.agent.plist";

    public LaunchdStartupConfig() {
        try {
            File startupDir = new File(System.getProperty("user.home") + "/Library/LaunchAgents");
            if (!startupDir.isDirectory())
                if (!startupDir.mkdir()) // Some system no LaunchAgents folder in user home.
                    throw new RuntimeException("Failed to create startup folder: " + startupDir.getAbsolutePath());
            if (!new File(startupTarget).exists())
                throw new FileNotFoundException("Executable not found.");
            this.plistContent=new String(LaunchdStartupConfig.class.getResourceAsStream("/utils/launchdagent.plist").readAllBytes());
            this.startupFile = new File(startupDir.getAbsolutePath(), startupPlist);
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
            String content = plistContent.replace("{{ARKPETS_WORKING_DIR}}",System.getProperty("user.dir"));
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
